package gov.fjc.fis.job.jifms;

import java.util.List;

/**
 * A validation step applied to a resolved JIFMS document context.
 * <p>
 * Validators may be composed into a pipeline using {@link #and(Validator)}
 * or {@link #chain(List)}. The pipeline evaluates validators in order and
 * stops when a validator returns a {@link ValidationResult.Fail}. If all
 * validators succeed, the pipeline returns {@link ValidationResult.Ok}.
 * </p>
 *
 * @author Doug Mitchell
 */
@FunctionalInterface
public interface Validator {

    /**
     * Validates the supplied resolved document context.
     *
     * @param ctx the resolved context to validate
     * @return the result of the validation
     */
    ValidationResult apply(ResolvedContext ctx);

    /**
     * Composes this validator with another validator.
     * <p>
     * The next validator is evaluated only if this validator returns
     * {@link ValidationResult.Ok}. A failure is returned immediately without
     * evaluating the next validator.
     * </p>
     *
     * @param next the validator to evaluate after this validator succeeds
     * @return a validator that applies both validators in sequence
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
     * Creates a validation pipeline from the supplied validators.
     * <p>
     * Validators are evaluated in list order. Evaluation stops at the first
     * {@link ValidationResult.Fail}. An empty list produces a validator that
     * always returns {@link ValidationResult.Ok}.
     * </p>
     *
     * @param steps the validators to include in the pipeline
     * @return a validator that applies the supplied validators in sequence
     */
    static Validator chain(List<Validator> steps) {
        return steps.stream()
                .reduce(
                        ctx -> ValidationResult.Ok.INSTANCE,
                        Validator::and
                );
    }
}