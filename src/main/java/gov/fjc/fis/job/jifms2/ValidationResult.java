package gov.fjc.fis.job.jifms;

import java.util.List;

/** Result of one validation step. */
sealed interface ValidationResult permits ValidationResult.Ok, ValidationResult.Fail {
    record Ok(ResolvedContext ctx) implements ValidationResult {}
    record Fail(String error) implements ValidationResult {}
}

@FunctionalInterface
interface Validator {
    ValidationResult apply(ResolvedContext ctx);

    /** Compose two validators; short-circuits on the first Fail. */
    default Validator and(Validator next) {
        return ctx -> {
            var r = this.apply(ctx);
            return (r instanceof ValidationResult.Ok(ResolvedContext ctx1)) ? next.apply(ctx1) : r;
        };
    }

    /** Build a pipeline from many validators; short-circuits on first Fail. */
    static Validator chain(List<Validator> steps) {
        return steps.stream().reduce(ValidationResult.Ok::new, Validator::and);
    }
}