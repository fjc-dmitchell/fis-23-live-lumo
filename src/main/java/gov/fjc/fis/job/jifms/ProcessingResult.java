package gov.fjc.fis.job.jifms;

/**
 * Represents the outcome of a processing step in the JIFMS document
 * processing pipeline.
 * <p>
 * Processors return a {@code ProcessingResult} to indicate whether a
 * document resulted in an obligation insertion, obligation update,
 * no action, or whether processing should continue to the next pipeline
 * step. These results are used by the pipeline to determine control flow
 * and audit behavior.
 * </p>
 * <ul>
 *   <li>{@link Inserted} indicates that a new obligation was created.</li>
 *   <li>{@link Updated} indicates that an existing obligation was modified.</li>
 *   <li>{@link Ignored} indicates that processing should stop without
 *   recording changes.</li>
 *   <li>{@link Continue} indicates that no final outcome has been reached
 *   and subsequent processors should be evaluated.</li>
 * </ul>
 * <p>
 * Each result type is implemented as a singleton to minimize object
 * allocation during batch document processing.
 * </p>
 *
 * @author Doug Mitchell
 */
public sealed interface ProcessingResult
        permits ProcessingResult.Inserted,
        ProcessingResult.Updated,
        ProcessingResult.Ignored,
        ProcessingResult.Continue {

    /**
     * Indicates that processing created a new obligation.
     */
    final class Inserted implements ProcessingResult {
        public static final Inserted INSTANCE = new Inserted();

        private Inserted() {
        }
    }

    /**
     * Indicates that processing modified an existing obligation.
     */
    final class Updated implements ProcessingResult {
        public static final Updated INSTANCE = new Updated();

        private Updated() {
        }
    }

    /**
     * Indicates that processing should stop and no further actions should
     * be performed for the current document.
     */
    final class Ignored implements ProcessingResult {
        public static final Ignored INSTANCE = new Ignored();

        private Ignored() {
        }
    }

    /**
     * Indicates that processing should continue to the next processor because
     * no final outcome has been determined.
     */
    final class Continue implements ProcessingResult {
        public static final Continue INSTANCE = new Continue();

        private Continue() {
        }
    }
}