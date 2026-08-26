package gov.fjc.fis.job.jifms;

/**
 * Optimized validation result:
 * - Ok is a singleton (no allocations)
 * - Fail allocates only a small error object
 */
public sealed interface ValidationResult
        permits ValidationResult.Ok, ValidationResult.Fail {

    final class Ok implements ValidationResult {
        public static final Ok INSTANCE = new Ok();

        private Ok() {
        }
    }

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