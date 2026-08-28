package gov.fjc.fis.job.jifms;

import java.util.List;

@FunctionalInterface
public interface Processor {

    ProcessingResult apply(ResolvedContext ctx);

    /**
     * Compose three obligation processor results. IGNORE indicates no changes
     * so short-circuit and skip remaining steps. INSERTED and UPDATED preserve
     * the result and continue steps. CONTINUE is a pass-through result
     * for remaining processors and is not preserved.
     */
    default Processor and(Processor next) {
        return ctx -> {
            ProcessingResult result = this.apply(ctx);

            // If obligation ignored, skip the rest
            if (result instanceof ProcessingResult.Ignored) {
                return ProcessingResult.Ignored.INSTANCE;
            }

            // If obligation inserted, preserve it
            if (result instanceof ProcessingResult.Inserted) {
                next.apply(ctx);  // run next step for side-effects only
                return ProcessingResult.Inserted.INSTANCE;
            }

            // If obligation updated, preserve it
            if (result instanceof ProcessingResult.Updated) {
                next.apply(ctx);
                return ProcessingResult.Updated.INSTANCE;
            }

            // If Continue, just pass result from next
            // nextResult will also be Continue, because non-obligation processors return Continue
            return next.apply(ctx);
        };
    }

    /**
     * Builds a processing pipeline, just like Validator.chain().
     */
    static Processor chain(List<Processor> steps) {
        return steps.stream()
                .reduce(
                        ctx -> ProcessingResult.Continue.INSTANCE,  // neutral identity
                        Processor::and
                );
    }
}