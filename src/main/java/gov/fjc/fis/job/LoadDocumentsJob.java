package gov.fjc.fis.job;

import gov.fjc.fis.entity.Document;
import gov.fjc.fis.service.AdministrationService;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.Authenticated;
import org.quartz.*;
import org.springframework.jdbc.core.JdbcTemplate;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import gov.fjc.fis.entity.dto.PurchaseOrderDto;
import gov.fjc.fis.entity.dto.TravelAuthorizationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static gov.fjc.fis.FisUtilities.cleanText;

/**
 * Job to load Document files (purchase and travel) into FIS and trigger processing job.
 * Both the file directory and archive directory must exist with proper permissions and
 * have property keys defined. If email addresses properties are not configured,
 * job will attempt to send email to all administrators.
 * Dependencies: Quartz and Email libraries must be installed and configured (e.g., smtp server).
 *
 * @author Doug Mitchell
 * @version 2.3
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
    private static final JobKey PROCESS_DOCUMENTS_JOB_KEY = new JobKey("processDocuments", "JIFMS");
    private static final String ABEND_SUBJECT = "JIFMS feed processing ABENDED";
    private static final Logger log = LoggerFactory.getLogger(LoadDocumentsJob.class);

    // ToDo: request to AO to limit feeds to five years
    private final String startingYear = "2021";

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
            processDocuments(purchasePath, travelPath);
//            scheduler.triggerJob(PROCESS_DOCUMENTS_JOB_KEY);
        } catch (Exception ex) {
            handleProcessingFailure(ex);
            throw new JobExecutionException("LoadDocuments failed", ex);
        }
        log.info("LoadDocuments has been ended.");
    }

    private void processDocuments(Path purchasePath, Path travelPath) {
        jdbcTemplate.execute("TRUNCATE TABLE FIS_DOCUMENT");

        loadPurchaseOrders(purchasePath);
        loadTravelAuthorizations(travelPath);

        log.info("Load Documents executed with purchasePath={} travelPath={}",
                purchasePath, travelPath);

        administrationService.archiveFile(jobStatusEmailAddresses, ABEND_SUBJECT, feedDirectory, archiveDirectory, purchaseFile);
        administrationService.archiveFile(jobStatusEmailAddresses, ABEND_SUBJECT, feedDirectory, archiveDirectory, travelFile);
    }

    private void loadPurchaseOrders(Path purchaseFilePath) {
        log.info("LoadDocuments: loading Purchase Orders");
        try (Reader reader = Files.newBufferedReader(purchaseFilePath)) {

            CsvToBean<PurchaseOrderDto> csvToBean = new CsvToBeanBuilder<PurchaseOrderDto>(reader)
                    .withType(PurchaseOrderDto.class)
                    .withSkipLines(1)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            SaveContext saveContext = new SaveContext();
            int batchCount = 0;

            for (PurchaseOrderDto dto : csvToBean) {

                // business rules
                if (dto.getFundCode().equals("51140X") && !dto.getBudgetOrg().equals("JXXXXXF")) {
                    continue;
                }
                if (dto.getBbfy().compareTo(startingYear) >= 0) {
                    Document doc = createPurchaseDocument(dto);
                    saveContext.saving(doc);
                    batchCount++;
                }

                // flush batched records
                if (batchCount == BATCH_SIZE) {
                    unconstrainedDataManager.save(saveContext);
                    saveContext = new SaveContext(); // reset for next batch
                    batchCount = 0;
                }
            }

            // flush any remaining records
            if (batchCount > 0) {
                unconstrainedDataManager.save(saveContext);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadTravelAuthorizations(Path travelFilePath) {
        log.info("LoadDocuments: loading Travel Authorizations");
        try (Reader reader = Files.newBufferedReader(travelFilePath)) {

            CsvToBean<TravelAuthorizationDto> csvToBean =
                    new CsvToBeanBuilder<TravelAuthorizationDto>(reader)
                            .withType(TravelAuthorizationDto.class)
                            .withSkipLines(1)
                            .withIgnoreLeadingWhiteSpace(true)
                            .build();

            SaveContext saveContext = new SaveContext();
            int batchCount = 0;

            for (TravelAuthorizationDto dto : csvToBean) {

                // business rules
                if (dto.getFundCode().equals("51140X") && !dto.getBudgetOrg().equals("JXXXXXF")) {
                    continue;
                }
                if (dto.getBbfy().compareTo(startingYear) >= 0) {
                    Document doc = createTravelDocument(dto);
                    saveContext.saving(doc);
                    batchCount++;
                }

                // flush batched records
                if (batchCount == BATCH_SIZE) {
                    unconstrainedDataManager.save(saveContext);
                    saveContext = new SaveContext();   // reset for next batch
                    batchCount = 0;
                }
            }

            // flush remaining items
            if (batchCount > 0) {
                unconstrainedDataManager.save(saveContext);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
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
     * Creates Document entity from Purchase Order DTO. Fields from Purchase Order
     * and Travel Authorization data transfer objects should NOT be consolidated to
     * a base class due to OpenCSV annotations!
     *
     * @param dto Purchase Order
     */
    Document createPurchaseDocument(PurchaseOrderDto dto) {
        Document purchaseDocument = unconstrainedDataManager.create(Document.class);

        purchaseDocument.setFundCode(dto.getFundCode());
        purchaseDocument.setBbfy(dto.getBbfy());
        purchaseDocument.setEbfy(dto.getEbfy());
        purchaseDocument.setBudgetOrg(dto.getBudgetOrg());
        purchaseDocument.setCostOrg(dto.getCostOrg());
        purchaseDocument.setDocumentType(dto.getDocumentType());
        purchaseDocument.setDocumentNumber(dto.getDocumentNumber());
        purchaseDocument.setDocumentDate(dto.getDocumentDate());
        purchaseDocument.setDocumentCreationDate(dto.getDocumentCreationDate());
        purchaseDocument.setTitle(cleanText(dto.getTitle()));
        purchaseDocument.setBudgetObjectClass(dto.getBudgetObjectClass());
        purchaseDocument.setMajorObjectClass(dto.getMajorObjectClass());
        purchaseDocument.setProject(dto.getProject());
        purchaseDocument.setAmount(dto.getAmount());
        purchaseDocument.setLineNumber(dto.getLineNumber());
        purchaseDocument.setTaxId(dto.getTaxId());
        purchaseDocument.setTaxIdType(dto.getTaxIdType());
        purchaseDocument.setAddressCode(dto.getAddressCode());
        purchaseDocument.setVendorCode(dto.getVendorCode());
        purchaseDocument.setVendorName(cleanText(dto.getVendorName()));
        purchaseDocument.setExpendedAmount(dto.getExpendedAmount());
        purchaseDocument.setClosedAmount(dto.getClosedAmount());
        purchaseDocument.setClosedDate(dto.getClosedDate());
        purchaseDocument.setLastModifiedBy(dto.getLastModifiedBy());
        purchaseDocument.setFjc(dto.getFjc());
        purchaseDocument.setOrderedAmount(dto.getOrderedAmount());
        purchaseDocument.setOutstandingAmount(dto.getOutstandingAmount());
        purchaseDocument.setPrepaidAmount(dto.getPrepaidAmount());
        purchaseDocument.setRefundedAmount(dto.getRefundedAmount());

        return purchaseDocument;
    }

    /**
     * Creates Document entity from Travel Authorization DTO. Fields from Purchase Order
     * and Travel Authorization data transfer objects should NOT be consolidated to
     * a base class due to OpenCSV annotations!
     *
     * @param dto Travel Authorization
     */
    Document createTravelDocument(TravelAuthorizationDto dto) {
        Document travelDocument = unconstrainedDataManager.create(Document.class);

        travelDocument.setFundCode(dto.getFundCode());
        travelDocument.setBbfy(dto.getBbfy());
        travelDocument.setEbfy(dto.getEbfy());
        travelDocument.setBudgetOrg(dto.getBudgetOrg());
        travelDocument.setCostOrg(dto.getCostOrg());
        travelDocument.setDocumentType(dto.getDocumentType());
        travelDocument.setDocumentNumber(dto.getDocumentNumber());
        travelDocument.setDocumentDate(dto.getDocumentDate());
        travelDocument.setDocumentCreationDate(dto.getDocumentCreationDate());
        travelDocument.setTitle(cleanText(dto.getTitle()));
        travelDocument.setBudgetObjectClass(dto.getBudgetObjectClass());
        travelDocument.setMajorObjectClass(dto.getMajorObjectClass());
        travelDocument.setProject(dto.getProject());
        travelDocument.setAmount(dto.getAmount());
        travelDocument.setLineNumber(dto.getLineNumber());
        travelDocument.setVendorCode(dto.getVendorCode());
        travelDocument.setVendorName(cleanText(dto.getVendorName()));
        travelDocument.setTravelStartDate(dto.getTravelStartDate());
        travelDocument.setTravelEndDate(dto.getTravelEndDate());
        travelDocument.setExpendedAmount(dto.getExpendedAmount());
        travelDocument.setClosedAmount(dto.getClosedAmount());
        travelDocument.setClosedDate(dto.getClosedDate());
        travelDocument.setLastModifiedBy(dto.getLastModifiedBy());
        travelDocument.setFjc(dto.getFjc());
        travelDocument.setOrderedAmount(dto.getOrderedAmount());
        travelDocument.setOutstandingAmount(dto.getOutstandingAmount());
        travelDocument.setPrepaidAmount(dto.getPrepaidAmount());
        travelDocument.setRefundedAmount(dto.getRefundedAmount());

        return travelDocument;
    }
}