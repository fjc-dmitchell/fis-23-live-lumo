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
    private DocumentProcessingService processingService;
    @Autowired
    private ObligationProcessingService obligationProcessingService;
    @Autowired
    private AuditService auditService;

    private static final Logger log = LoggerFactory.getLogger(ProcessDocumentsJob.class);

    private static final String TWO_YEAR_FUND_CODE = "09280M";
    private static final String EDUCATION_DIVISION_CODE = "2";
    private static final String OBBBA_BUDGET_ORG = "JXXMAPP";
    private static final List<String> TRAVEL_DOCUMENT_TYPES = List.of("TA", "TAJ", "JTA");
    private static final List<String> PURCHASE_DOCUMENT_TYPES = List.of("MO", "MOJ");

    @Authenticated
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Map<String, Fund> fundMap = processingService.fetchFundMap();
        Fund twoYearFund = processingService.fetchTwoYearFund(TWO_YEAR_FUND_CODE)
                .orElseThrow(() -> new JobExecutionException(
                        "Two year fund code not found: " + TWO_YEAR_FUND_CODE));
        List<Appropriation> openAppropriations = processingService.fetchOpenAppropriations();
        if (openAppropriations.isEmpty()) {
            throw new JobExecutionException("No open appropriations found. No documents will be processed.");
        }
        List<Document> documents;

        for (var appropriation : openAppropriations) {

            List<Division> divisionList = processingService.fetchDivisionsWithBudgetOrgs(appropriation);
            Division educationDivision = processingService.fetchEducationDivision(appropriation, EDUCATION_DIVISION_CODE)
                    .orElseThrow(() -> new JobExecutionException(
                            "Education division not found for: " + appropriation.getBudgetFiscalYear()));
            Map<String, ObjectClass> objectClassMap = processingService.fetchObjectClassMap(appropriation, true);
            var bbfy = appropriation.getBudgetFiscalYear();

            // fetch document entities in small batches to reduce memory overhead
            int offset = 0;
            int max = 100; // process documents in small batches
            while (!(documents = processingService.fetchDocuments(bbfy, offset, max)).isEmpty()) {
//                System.out.println("bbfy: " + bbfy + " offset: " + offset + " max: " + max + " size: " + documents.size());

                for (var document : documents) {

                    ResolvedContext initialCtx = new ResolvedContext(document, twoYearFund, educationDivision);

                    Validator pipeline = Validator.chain(List.of(
                            Validators.fundIsKnown(fundMap),
                            Validators.divisionMatches(divisionList),
                            Validators.activityExists(processingService),
                            Validators.objectClassKnown(objectClassMap),
                            Validators.genericObjectClassWhenGeneric(objectClassMap),
                            Validators.documentNumberRule(
                                    TRAVEL_DOCUMENT_TYPES,
                                    PURCHASE_DOCUMENT_TYPES,
                                    OBBBA_BUDGET_ORG)
                    ));

                    ValidationResult result = pipeline.apply(initialCtx);

                    // if rejected, write audit if it doesn't already exists and continue to next document
                    if (result instanceof ValidationResult.Fail(String error)) {
                        if(!processingService.doesRejectionExist(document)) {
                            auditService.record(document, new ProcessingOutcome.Rejected(List.of(error)));
                        }
                        log.warn(error);
                        continue;
                    }

                    // Success path: create/update obligation + projection + audit
                    ResolvedContext ctx = ((ValidationResult.Ok) result).ctx();

                    

                    ProcessingOutcome outcome = obligationProcessingService.apply(ctx);

                    if (!(outcome instanceof ProcessingOutcome.Ignored)) {
                        log.info(outcome.summary());
                        auditService.record(document, outcome);
                    }

                    // create Vendor
                    // create FCN

                    // Audit every non-IGNORED outcome (or include IGNORE if you prefer full visibility)
//                    if (!(outcome instanceof ProcessingOutcome.Ignored)) {
////                        auditService.record(document, outcome);
//                        System.out.println(outcome);
//                    }

//                    if(result instanceof ValidationResult.Ok(ResolvedContext ctx)) {
//                        System.out.println(ctx.toString());
//                    }
//
//                    if (result instanceof ValidationResult.Fail(String error)) {
//                        // Immediately write a rejection audit and continue to the next document
//                        auditService.record(document, new ProcessingOutcome.Rejected(List.of(error)));
//                        continue;
//                    }
//
//// Success path: create/update obligation + projection + audit
//                    var ctx = ((ValidationResult.Ok) result).ctx();
//                    var outcome = obligationService.apply(ctx);
//
//// Audit every non-IGNORED outcome (or include IGNORE if you prefer full visibility)
//                    if (!(outcome instanceof ProcessingOutcome.Ignored)) {
//                        auditService.record(document, outcome);
//                    }


                    // maintains state of document being processed
//                    var auditContext = new DocumentAuditContext(
//                            twoYearFund,
//                            educationDivision,
//                            document
//                    );
//
//                    documentValidator.validateFund(auditContext, fundMap);
//                    documentValidator.validateDivision(auditContext, divisionList);
//                    documentValidator.validateActivity(auditContext);
//                    documentValidator.validateObjectClass(auditContext, objectClassMap);
//                    documentValidator.validateGenericObjectClass(auditContext, objectClassMap);
//                    documentValidator.validateDocumentNumber(auditContext, obbbaBudgetOrg);
//                    documentValidator.validateObligation(auditContext);
//
//                    if(auditContext.isInserted()) {
//                        // create new obligation
//                        // update activity projection
//                    }
//
//                    if(auditContext.isUpdated()) {
//                        // update existing obligation
//                        // update activity projection
//                        // create Fund Control Notice
//                    }
//
//                    if(!auditContext.isIgnored()) {
//                        unconstrainedQueries.createAndSaveAuditRecord(auditContext);
//                    }


                }

                offset += documents.size();
            }
        }
        log.info("Processing completed");
    }
}
