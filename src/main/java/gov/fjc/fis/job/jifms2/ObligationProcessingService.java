package gov.fjc.fis.job.jifms;

//import gov.fjc.fis.job.DocumentAuditContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component("fis_ObligationProcessingService")
public class ObligationProcessingService {
    //    private final UncondstrainedQueries q;
    private final DocumentProcessingService processingService;

    ObligationProcessingService(DocumentProcessingService documentProcessingService) {
//        this.q = unconstrainedQueries;
        this.processingService = documentProcessingService;
    }


    /**
     * Returns Inserted/Updated/Ignored (no Rejected here; that's handled in validation).
     */
    ProcessingOutcome apply(ResolvedContext ctx) {
        var doc = ctx.document();
        var obligation = processingService.fetchObligation(ctx.division(), doc.getDocumentNumber(), doc.getLineNumber());
        var changes = new FieldChanges();

        if (obligation.isEmpty()) {
            var newOb = processingService.createObligationFrom(ctx);  // pure mapping from ResolvedContext+Document
            // capture initial fields for audit
            changes.add("amount", null, newOb.getAmount());
            changes.add("activity", null, newOb.getActivity().getActivityNumber());
            changes.add("objectClass", null, newOb.getObjectClass().getBudgetObjectClass());

            // projection change (use your existing rule—inject Clock if needed)
//            var delta = computeProjectionDeltaOnInsert(newOb, ctx);
//            q.updateActivityProjection(ctx, delta);

            processingService.saveObligation(newOb);
            return new ProcessingOutcome.Inserted(doc.getDocumentNumber(), changes);
        } else {
//            collectObligationChanges(doc, ctx, existing, changes); // compare before/after (pure)
//            if (changes.isEmpty()) {
            return new ProcessingOutcome.Ignored("No changes");
//            }
//            q.applyObligationChanges(existing, changes);
//            var delta = computeProjectionDeltaOnUpdate(existing, ctx);
//            q.updateActivityProjection(ctx, delta);
//            q.saveObligation(existing);
//            return new ProcessingOutcome.Updated(doc.getDocumentNumber(), changes);
        }
    }

    // TODO: implement computeProjectionDeltaOnInsert/Update using your rule
    // and your current calculateProjectionChangeAmount logic.


    /**
     * Projection rule created from 10/3/19 meeting with Nanticha and Mary. Doug suggested
     * to make rule based on training. Nanticha rule: do not increase projections for training de-obs.
     * Mary rule: do not increase projections after fiscal yearend. Otherwise, adjust the projection.
     *
     * @param context The DocumentAuditContext containing state variables
     * @return the amount the projection should be adjusted by based on business rules
     */
//    BigDecimal calculateProjectionChangeAmount(DocumentAuditContext context) {
//        var trainingProject = context.getActivity().getTrainingProject();
//        var obligationChange = context.getObligationDifference();
//        var bbfy = context.getDocumentBbfy();
//
//        // obligation increased, the projection will be decreased
//        if (obligationChange.compareTo(BigDecimal.ZERO) >= 0) {
//            return obligationChange;
//        }
//
//        // obligation decreased, projection will not be increased if
//        // training project OR today is on or after 10/1/bbfy
//        if (trainingProject || isOnOrAfterOctoberFirst(bbfy)) {
//            return BigDecimal.ZERO;
//        }
//
//        // obligation decreased, the projection will be increased
//        return obligationChange;
//    }

//    boolean isOnOrAfterOctoberFirst(String yearString) {
//        // Validate and parse the year
//        if (yearString == null || !yearString.matches("\\d{4}")) {
//            throw new IllegalArgumentException("yearStr must be a 4-digit year (e.g., \"2026\").");
//        }
//
//        // Parse the input year
//        int year = Integer.parseInt(yearString);
//
//        // Today's date
//        LocalDate today = LocalDate.now();
//
//        // October 1 of the given year
//        LocalDate comparisonDate = LocalDate.of(year, 10, 1);
//
//        // Return true if today is on or after October 1 of that year
//        return !today.isBefore(comparisonDate);
//    }
//
//        private void collectObligationChanges(Document doc, ResolvedContext ctx, Obligation ob, FieldChanges changes) {
//        if (!java.util.Objects.equals(doc.getAmount(), ob.getAmount()))
//            changes.add("amount", ob.getAmount(), doc.getAmount());
//        if (!java.util.Objects.equals(ctx.activity(), ob.getActivity()))
//            changes.add("activity", ob.getActivity().getActivityNumber(), ctx.activity().getActivityNumber());
//        if (!java.util.Objects.equals(ctx.objectClass(), ob.getObjectClass()))
//            changes.add("objectClass", ob.getObjectClass().getBudgetObjectClass(), ctx.objectClass().getBudgetObjectClass());
//        if (!java.util.Objects.equals(doc.getTitle(), ob.getVendor()))
//            changes.add("vendor", ob.getVendor(), doc.getTitle());
//        // etc. (travel dates, vendorCode, taxId…)
//    }
}