package gov.fjc.fis.job.jifms;

import java.util.List;

@FunctionalInterface
public interface Validator {

    ValidationResult apply(ResolvedContext ctx);

    /**
     * Compose two validators; short-circuits on first Fail.
     */
    default Validator and(Validator next) {
        return ctx -> {
            ValidationResult r = this.apply(ctx);

            if (r instanceof ValidationResult.Ok) {
                return next.apply(ctx);
            }

            return r;
        };
    }

    /**
     * Build a pipeline from many validators; short-circuits on first Fail.
     */
    static Validator chain(List<Validator> steps) {
        return steps.stream()
                .reduce(
                        ctx -> ValidationResult.Ok.INSTANCE,
                        Validator::and
                );
    }
}