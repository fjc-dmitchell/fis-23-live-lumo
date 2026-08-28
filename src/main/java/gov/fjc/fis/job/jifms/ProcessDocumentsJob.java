package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.*;
import io.jmix.core.security.Authenticated;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DisallowConcurrentExecution
public class ProcessDocumentsJob implements Job {
    @Autowired
    private JifmsQueryService jifmsQueryService;
    @Autowired
    private DocumentAuditService documentAuditService;

    private static final Logger log = LoggerFactory.getLogger(gov.fjc.fis.job.jifms.ProcessDocumentsJob.class);

    private static final String TWO_YEAR_FUND_CODE = "09280M";
    private static final String EDUCATION_DIVISION_CODE = "2";
    private static final String OBBBA_BUDGET_ORG = "JXXMAPP";
    private static final List<String> TRAVEL_DOCUMENT_TYPES = List.of("TA", "TAJ", "JTA");
    private static final List<String> PURCHASE_DOCUMENT_TYPES = List.of("MO", "MOJ");

    @Authenticated
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Map<String, Fund> fundMap = jifmsQueryService.fetchFundMap();
        Fund twoYearFund = jifmsQueryService.fetchTwoYearFund(TWO_YEAR_FUND_CODE)
                .orElseThrow(() -> new JobExecutionException(
                        "Two year fund code not found: " + TWO_YEAR_FUND_CODE));
        List<Appropriation> openAppropriations = jifmsQueryService.fetchOpenAppropriations();
        if (openAppropriations.isEmpty()) {
            throw new JobExecutionException("No open appropriations found. No documents will be processed.");
        }
        List<Document> documents;

        for (var appropriation : openAppropriations) {

            List<Division> divisionList = jifmsQueryService.fetchDivisionsWithBudgetOrgs(appropriation);
            Division educationDivision = jifmsQueryService.fetchEducationDivision(appropriation, EDUCATION_DIVISION_CODE)
                    .orElseThrow(() -> new JobExecutionException(
                            "Education division not found for: " + appropriation.getBudgetFiscalYear()));
            Map<String, ObjectClass> objectClassMap = jifmsQueryService.fetchObjectClassMap(appropriation, true);
            var bbfy = appropriation.getBudgetFiscalYear();

            // fetch document entities in small batches to reduce memory overhead
            int offset = 0;
            int max = 100; // process documents in small batches
            while (!(documents = jifmsQueryService.fetchDocuments(bbfy, offset, max)).isEmpty()) {
//                System.out.println("bbfy: " + bbfy + " offset: " + offset + " max: " + max + " size: " + documents.size());

                for (var document : documents) {

                    ResolvedContext resolvedContext = new ResolvedContext(document, twoYearFund, educationDivision);

                    // create pipeline to validate critical fields of the Document
                    Validator validationPipeline = Validator.chain(List.of(
                            Validators.fundIsKnown(fundMap),
                            Validators.divisionMatchesBudgetOrg(divisionList),
                            Validators.activityExists(jifmsQueryService),
                            Validators.objectClassKnown(objectClassMap),
                            Validators.genericObjectClassWhenGeneric(objectClassMap),
                            Validators.documentNumberRule(
                                    TRAVEL_DOCUMENT_TYPES,
                                    PURCHASE_DOCUMENT_TYPES,
                                    OBBBA_BUDGET_ORG)
                    ));

                    // perform operations in validation pipeline
                    ValidationResult validationResult = validationPipeline.apply(resolvedContext);

                    // if any validator failed, log the error and move on to next document
                    if (validationResult instanceof ValidationResult.Fail fail) {
                        documentAuditService.recordFailure(resolvedContext, fail.getError());
                        log.warn(fail.getError());
                        continue;
                    }

                    AuditRecord audit = new AuditRecord();
                    Processor processingPipeline = Processor.chain(List.of(
                            Processors.processObligation(jifmsQueryService, audit),
                            Processors.projection(jifmsQueryService, objectClassMap, audit),
                            Processors.allocation(jifmsQueryService),
                            Processors.fcn(jifmsQueryService),
                            Processors.vendor(jifmsQueryService)
                    ));

                    ProcessingResult processingResult = processingPipeline.apply(resolvedContext);
                    if (!(processingResult instanceof ProcessingResult.Ignored)) {
                        if ((processingResult instanceof ProcessingResult.Inserted)) {
                            documentAuditService.recordInsert(resolvedContext, audit);
                            log.info("Inserted documents for document: {}", document.getDocumentNumber());
                        }
                        if ((processingResult instanceof ProcessingResult.Updated)) {
                            documentAuditService.recordUpdate(resolvedContext, audit);
                            log.info("Updated documents for document: {}", document.getDocumentNumber());
                        }
                    }
                }

                offset += documents.size();
            }
        }
        log.info("Processing completed");
    }
}
