package gov.fjc.fis.job.jifms;

/**
 * Represents the outcome of a validation step in the JIFMS document
 * validation pipeline.
 * <p>
 * Validators return a {@code ValidationResult} to indicate whether
 * processing may continue or the document should be rejected. Validation
 * pipelines stop at the first {@link Fail} result and return the associated
 * error message. If all validation steps succeed, the pipeline returns
 * {@link Ok}.
 * </p>
 * <p>
 * The implementation is optimized for high-volume document processing.
 * Successful validations share a singleton {@link Ok} instance, while
 * failed validations allocate a {@link Fail} instance containing the
 * rejection reason.
 * </p>
 * @author Doug Mitchell
 */
public sealed interface ValidationResult
        permits ValidationResult.Ok, ValidationResult.Fail {

    /**
     * Indicates that validation completed successfully.
     */
    final class Ok implements ValidationResult {
        public static final Ok INSTANCE = new Ok();

        private Ok() {
        }
    }

    /**
     * Indicates that validation failed and document processing should stop.
     * The error message describes the reason for rejection.
     */
    final class Fail implements ValidationResult {
        private final String error;

        public Fail(String error) {
            this.error = String.format("REJECTED: %s", error);
        }

        public String getError() {
            return error;
        }
    }
}