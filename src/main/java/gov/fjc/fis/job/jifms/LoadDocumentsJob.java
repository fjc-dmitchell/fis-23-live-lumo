package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.Document;
import gov.fjc.fis.service.AdministrationService;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.Authenticated;
import org.quartz.*;
import org.springframework.jdbc.core.JdbcTemplate;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static gov.fjc.fis.FisUtilities.cleanText;

/**
 * Job to load Document files (purchase and travel) into FIS and trigger processing job.
 * Both the file directory and archive directory must exist with proper permissions and
 * have property keys defined. If email addresses properties are not configured,
 * job will attempt to send email to all administrators.
 * Dependencies: Quartz and Email libraries must be installed and configured (e.g., smtp server).
 *
 * @author Doug Mitchell
 * @version 2.4
 * @since 2.1
 */
@DisallowConcurrentExecution
public class LoadDocumentsJob implements Job {
    @Autowired
    private AdministrationService administrationService;
    @Autowired
    UnconstrainedDataManager unconstrainedDataManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    Scheduler scheduler;

    @Value("${jifms.feed.directory}")
    private String feedDirectory;
    @Value("${jifms.archive.directory}")
    private String archiveDirectory;
    @Value("${jifms.travel.file}")
    private String travelFile;
    @Value("${jifms.purchase.file}")
    private String purchaseFile;
    @Value("${jifms.email.job-status.addresses}")
    private String jobStatusEmailAddresses;

    private static final int BATCH_SIZE = 500;
    private static final String JITF_FUND = "51140X";
    private static final String JITF_BUDGET_ORG = "JXXXXXF";

    private static final JobKey PROCESS_DOCUMENTS_JOB_KEY = new JobKey("processDocuments", "JIFMS");
    private static final String ABEND_SUBJECT = "JIFMS feed processing ABENDED";
    private static final Logger log = LoggerFactory.getLogger(LoadDocumentsJob.class);

    // ToDo: request to AO to limit feeds to five years, limit loading to open fiscal years
    private static final String STARTING_YEAR = "2022";

    @Authenticated
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("LoadDocuments has been executed.");
        Path purchasePath = Path.of(feedDirectory).resolve(purchaseFile);
        Path travelPath = Path.of(feedDirectory).resolve(travelFile);
        boolean purchaseExists = Files.exists(purchasePath);
        boolean travelExists = Files.exists(travelPath);

        if (!purchaseExists || !travelExists) {
            handleMissingFiles(purchaseExists, travelExists, purchasePath, travelPath);
            throw new JobExecutionException("Missing purchase and travel purchase files.");
        }

        try {
            loadDocuments(purchasePath, travelPath);
            scheduler.triggerJob(PROCESS_DOCUMENTS_JOB_KEY);
        } catch (Exception ex) {
            handleProcessingFailure(ex);
            throw new JobExecutionException("LoadDocuments failed", ex);
        }
        log.info("LoadDocuments has ended.");
    }

    private void loadDocuments(Path purchasePath, Path travelPath) {
        jdbcTemplate.execute("TRUNCATE TABLE FIS_DOCUMENT");

        loadPurchaseOrders(purchasePath);
        loadTravelAuthorizations(travelPath);

        log.info("Load Documents executed with purchasePath={} travelPath={}",
                purchasePath, travelPath);

        administrationService.archiveFile(jobStatusEmailAddresses, ABEND_SUBJECT,
                feedDirectory, archiveDirectory, purchaseFile);
        administrationService.archiveFile(jobStatusEmailAddresses, ABEND_SUBJECT,
                feedDirectory, archiveDirectory, travelFile);
    }

    private void loadPurchaseOrders(Path purchaseFilePath) {
        log.info("LoadDocuments: loading Purchase Orders");

        try (Reader reader = Files.newBufferedReader(purchaseFilePath)) {

            CsvToBean<PurchaseOrderImportRow> csvToBean = new CsvToBeanBuilder<PurchaseOrderImportRow>(reader)
                    .withType(PurchaseOrderImportRow.class)
                    .withSkipLines(1)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            Iterator<PurchaseOrderImportRow> iterator = csvToBean.iterator();

            BatchProcessor<PurchaseOrderImportRow> processor = new BatchProcessor<>(
                    BATCH_SIZE,
                    batch -> {
                        SaveContext ctx = new SaveContext();
                        for (PurchaseOrderImportRow dto : batch) {
                            Document doc = createPurchaseDocument(dto);
                            ctx.saving(doc);
                        }
                        unconstrainedDataManager.save(ctx);
                    }
            );

            // Business rules filter
            Predicate<PurchaseOrderImportRow> filter = row -> {
                if (row.getFundCode().equals(JITF_FUND) &&
                        !row.getBudgetOrg().equals(JITF_BUDGET_ORG)) return false;

                return row.getBbfy().compareTo(STARTING_YEAR) >= 0;
            };

            processor.process(iterator, filter);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadTravelAuthorizations(Path travelFilePath) {
        log.info("LoadDocuments: loading Travel Authorizations");

        try (Reader reader = Files.newBufferedReader(travelFilePath)) {

            CsvToBean<TravelAuthorizationImportRow> csvToBean =
                    new CsvToBeanBuilder<TravelAuthorizationImportRow>(reader)
                            .withType(TravelAuthorizationImportRow.class)
                            .withSkipLines(1)
                            .withIgnoreLeadingWhiteSpace(true)
                            .build();

            Iterator<TravelAuthorizationImportRow> iterator = csvToBean.iterator();

            BatchProcessor<TravelAuthorizationImportRow> processor = new BatchProcessor<>(
                    BATCH_SIZE,
                    batch -> {
                        SaveContext ctx = new SaveContext();
                        for (TravelAuthorizationImportRow dto : batch) {
                            Document doc = createTravelDocument(dto);
                            ctx.saving(doc);
                        }
                        unconstrainedDataManager.save(ctx);
                    }
            );

            // Business rules filter
            Predicate<TravelAuthorizationImportRow> filter = row -> {
                if (row.getFundCode().equals(JITF_FUND) &&
                        !row.getBudgetOrg().equals(JITF_BUDGET_ORG)) return false;

                return row.getBbfy().compareTo(STARTING_YEAR) >= 0;
            };

            processor.process(iterator, filter);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The batch loader should be refactored to its own file. It must not be static!
     * @param <T>
     */
    public class BatchProcessor<T> {

        private final int batchSize;
        private final Consumer<List<T>> batchConsumer;

        public BatchProcessor(int batchSize, Consumer<List<T>> batchConsumer) {
            this.batchSize = batchSize;
            this.batchConsumer = batchConsumer;
        }

        public void process(Iterator<T> iterator, Predicate<T> filter) {
            List<T> batch = new ArrayList<>(batchSize);

            while (iterator.hasNext()) {
                T item = iterator.next();

                if (filter != null && !filter.test(item)) {
                    continue;
                }

                batch.add(item);

                if (batch.size() == batchSize) {
                    batchConsumer.accept(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                batchConsumer.accept(batch);
            }
        }
    }


    private void handleProcessingFailure(Exception ex) {
        administrationService.sendEmail(
                jobStatusEmailAddresses,
                ABEND_SUBJECT,
                "<strong>LoadDocuments</strong> failed: " + ex.getMessage()
        );
    }

    private void handleMissingFiles(boolean purchaseExists,
                                    boolean travelExists,
                                    Path purchasePath,
                                    Path travelPath) {

        StringBuilder body = new StringBuilder();

        if (!purchaseExists && !travelExists) {
            body.append("<strong>Neither purchase.CSV nor travel.CSV files were found:</strong><br/><br/>")
                    .append(purchasePath).append("<br/>")
                    .append(travelPath);
            log.info("{} and {} were not found. Processing aborted.", purchasePath.toString(), travelPath.toString());

        } else if (!purchaseExists) {
            body.append("<strong>purchase.CSV file was not found:</strong><br/><br/>")
                    .append(purchasePath);
            log.info("{} was not found. Processing aborted.", purchasePath.toString());

        } else {
            body.append("<strong>travel.CSV file was not found:</strong><br/><br/>")
                    .append(travelPath);
            log.info("{} was not found. Processing aborted.", travelPath.toString());
        }

        body.append("<br/><br/><strong>Documents are untouched. Processing has been aborted.</strong>");

        administrationService.sendEmail(
                jobStatusEmailAddresses,
                ABEND_SUBJECT,
                body.toString()
        );
    }

    /**
     * Creates Document entity from Purchase Order bean. Fields from Purchase Order
     * and Travel Authorization data transfer objects should NOT be consolidated to
     * a base class due to OpenCSV annotations!
     *
     * @param row Purchase Order
     */
    Document createPurchaseDocument(PurchaseOrderImportRow row) {
        Document purchaseDocument = unconstrainedDataManager.create(Document.class);

        purchaseDocument.setFundCode(row.getFundCode());
        purchaseDocument.setBbfy(row.getBbfy());
        purchaseDocument.setEbfy(row.getEbfy());
        purchaseDocument.setBudgetOrg(row.getBudgetOrg());
        purchaseDocument.setCostOrg(row.getCostOrg());
        purchaseDocument.setDocumentType(row.getDocumentType());
        purchaseDocument.setDocumentNumber(row.getDocumentNumber());
        purchaseDocument.setDocumentDate(row.getDocumentDate());
        purchaseDocument.setDocumentCreationDate(row.getDocumentCreationDate());
        purchaseDocument.setTitle(cleanText(row.getTitle()));
        purchaseDocument.setBudgetObjectClass(row.getBudgetObjectClass());
        purchaseDocument.setMajorObjectClass(row.getMajorObjectClass());
        purchaseDocument.setProject(row.getProject());
        purchaseDocument.setAmount(row.getAmount());
        purchaseDocument.setLineNumber(row.getLineNumber());
        purchaseDocument.setTaxId(row.getTaxId());
        purchaseDocument.setTaxIdType(row.getTaxIdType());
        purchaseDocument.setAddressCode(row.getAddressCode());
        purchaseDocument.setVendorCode(row.getVendorCode());
        purchaseDocument.setVendorName(cleanText(row.getVendorName()));
        purchaseDocument.setExpendedAmount(row.getExpendedAmount());
        purchaseDocument.setClosedAmount(row.getClosedAmount());
        purchaseDocument.setClosedDate(row.getClosedDate());
        purchaseDocument.setLastModifiedBy(row.getLastModifiedBy());
        purchaseDocument.setFjc(row.getFjc());
        purchaseDocument.setOrderedAmount(row.getOrderedAmount());
        purchaseDocument.setOutstandingAmount(row.getOutstandingAmount());
        purchaseDocument.setPrepaidAmount(row.getPrepaidAmount());
        purchaseDocument.setRefundedAmount(row.getRefundedAmount());

        return purchaseDocument;
    }

    /**
     * Creates Document entity from Travel Authorization bean. Fields from Purchase Order
     * and Travel Authorization data transfer objects should NOT be consolidated to
     * a base class due to OpenCSV annotations!
     *
     * @param row Travel Authorization
     */
    Document createTravelDocument(TravelAuthorizationImportRow row) {
        Document travelDocument = unconstrainedDataManager.create(Document.class);

        travelDocument.setFundCode(row.getFundCode());
        travelDocument.setBbfy(row.getBbfy());
        travelDocument.setEbfy(row.getEbfy());
        travelDocument.setBudgetOrg(row.getBudgetOrg());
        travelDocument.setCostOrg(row.getCostOrg());
        travelDocument.setDocumentType(row.getDocumentType());
        travelDocument.setDocumentNumber(row.getDocumentNumber());
        travelDocument.setDocumentDate(row.getDocumentDate());
        travelDocument.setDocumentCreationDate(row.getDocumentCreationDate());
        travelDocument.setTitle(cleanText(row.getTitle()));
        travelDocument.setBudgetObjectClass(row.getBudgetObjectClass());
        travelDocument.setMajorObjectClass(row.getMajorObjectClass());
        travelDocument.setProject(row.getProject());
        travelDocument.setAmount(row.getAmount());
        travelDocument.setLineNumber(row.getLineNumber());
        travelDocument.setVendorCode(row.getVendorCode());
        travelDocument.setVendorName(cleanText(row.getVendorName()));
        travelDocument.setTravelStartDate(row.getTravelStartDate());
        travelDocument.setTravelEndDate(row.getTravelEndDate());
        travelDocument.setExpendedAmount(row.getExpendedAmount());
        travelDocument.setClosedAmount(row.getClosedAmount());
        travelDocument.setClosedDate(row.getClosedDate());
        travelDocument.setLastModifiedBy(row.getLastModifiedBy());
        travelDocument.setFjc(row.getFjc());
        travelDocument.setOrderedAmount(row.getOrderedAmount());
        travelDocument.setOutstandingAmount(row.getOutstandingAmount());
        travelDocument.setPrepaidAmount(row.getPrepaidAmount());
        travelDocument.setRefundedAmount(row.getRefundedAmount());

        return travelDocument;
    }
}