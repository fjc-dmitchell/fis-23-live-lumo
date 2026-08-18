package gov.fjc.fis.job.jifms;

import org.springframework.stereotype.Component;

@Component("fis_ObligationProcessingService")
public class ObligationProcessingService {
//    private final UncondstrainedQueries q;
    private final DocumentProcessingService processingService;

    ObligationProcessingService(DocumentProcessingService documentProcessingService) {
//        this.q = unconstrainedQueries;
        this.processingService = documentProcessingService;
    }


    /** Returns Inserted/Updated/Ignored (no Rejected here; that's handled in validation). */
    ProcessingOutcome apply(ResolvedContext ctx) {
        var doc = ctx.document();
//        var existing = q.getObligation(ctx.division(), doc.getDocumentNumber(), doc.getLineNumber());
        var obligation = processingService.fetchObligation(ctx.division(), doc.getDocumentNumber(), doc.getLineNumber());
        var changes = new FieldChanges();

        if(obligation.isEmpty()) {
//            var newOb = q.createObligationFrom(ctx);  // pure mapping from ResolvedContext+Document
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
}