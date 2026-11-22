package gov.fjc.fis.job.jifms;

import java.util.List;

/**
 * A processing step applied to a validated and resolved JIFMS document context.
 * <p>
 * Processors may be composed into a pipeline using {@link #and(Processor)}
 * or {@link #chain(List)}. The pipeline executes processors in order and uses
 * {@link ProcessingResult} to indicate whether document processing inserted,
 * updated, ignored, or continued without changing an obligation.
 * </p>
 * <p>
 * An {@link ProcessingResult.Ignored} result stops the pipeline. An
 * {@link ProcessingResult.Inserted} or {@link ProcessingResult.Updated} result
 * is preserved while the remaining processors execute for their side effects.
 * A {@link ProcessingResult.Continue} result allows the next processor's result
 * to become the pipeline result.
 * </p>
 *
 * @author Doug Mitchell
 */
@FunctionalInterface
public interface Processor {

    /**
     * Processes the supplied resolved document context.
     *
     * @param ctx the resolved context to process
     * @return the result of the processing step
     */
    ProcessingResult apply(ResolvedContext ctx);


    /**
     * Composes this processor with another processor.
     * <p>
     * If this processor returns {@link ProcessingResult.Ignored}, the next
     * processor is not executed. If it returns
     * {@link ProcessingResult.Inserted} or {@link ProcessingResult.Updated},
     * the next processor executes for its side effects while the original
     * result is preserved. If it returns {@link ProcessingResult.Continue},
     * the result of the next processor is returned.
     * </p>
     *
     * @param next the processor to execute after this processor
     * @return a processor that applies both processors in sequence
     */
    default Processor and(Processor next) {
        return ctx -> {
            ProcessingResult result = this.apply(ctx);

            if (result instanceof ProcessingResult.Ignored) {
                return ProcessingResult.Ignored.INSTANCE;
            }

            if (result instanceof ProcessingResult.Inserted) {
                next.apply(ctx);
                return ProcessingResult.Inserted.INSTANCE;
            }

            if (result instanceof ProcessingResult.Updated) {
                next.apply(ctx);
                return ProcessingResult.Updated.INSTANCE;
            }

            return next.apply(ctx);
        };
    }

    /**
     * Creates a processing pipeline from the supplied processors.
     * <p>
     * Processors are composed and executed in list order. An empty list
     * produces a processor that returns {@link ProcessingResult.Continue}.
     * </p>
     *
     * @param steps the processors to include in the pipeline
     * @return a processor that applies the supplied processors in sequence
     */
    static Processor chain(List<Processor> steps) {
        return steps.stream()
                .reduce(
                        ctx -> ProcessingResult.Continue.INSTANCE,  // neutral identity
                        Processor::and
                );
    }
}