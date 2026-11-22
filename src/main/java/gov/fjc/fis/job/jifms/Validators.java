package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.*;

import java.util.List;
import java.util.Map;

/**
 * Provides validation steps used when resolving and processing documents
 * imported from JIFMS.
 *
 * <p>Each factory method returns a {@link Validator} that verifies one aspect
 * of a document or its resolved entities. A successful validator may enrich
 * the {@link ResolvedContext} with entities required by subsequent validation
 * or processing steps. A failed validator returns a
 * {@link ValidationResult.Fail} describing the validation error.
 *
 * <p>The validators are intended to be composed into a validation pipeline and
 * executed in dependency order. For example, the fund must be resolved before
 * validating that a division belongs to that fund.
 *
 * @author Doug Mitchell
 */
final class Validators {

    /**
     * Prevents instantiation of this utility class.
     */
    private Validators() {
    }

    /**
     * Creates a validator that resolves the document's fund code to a known
     * {@link Fund}.
     *
     * <p>When validation succeeds, the resolved fund is stored in the
     * validation context.
     *
     * @param fundMap the funds indexed by fund code
     * @return a validator that fails when the document's fund code is not
     * present in {@code fundMap}
     */
    static Validator fundIsKnown(Map<String, Fund> fundMap) {
        return ctx -> {
            var fund = fundMap.get(ctx.getDocument().getFundCode());
            if (fund == null)
                return new ValidationResult.Fail("Invalid fund: " + ctx.getDocument().getFundCode());

            ctx.setFund(fund);
            return ValidationResult.Ok.INSTANCE;
        };
    }

    /**
     * Creates a validator that resolves the document's budget organization to
     * exactly one {@link Division}.
     *
     * <p>A division normally matches when its budget organization and fund
     * match those of the document. The education division is also accepted for
     * the two-year fund. When validation succeeds, the matching division is
     * stored in the validation context.
     *
     * @param divisions the divisions available for matching
     * @return a validator that fails when no division or more than one division
     * matches the document's budget organization
     */
    static Validator divisionMatchesBudgetOrg(List<Division> divisions) {
        return ctx -> {
            var budgetOrg = ctx.getDocument().getBudgetOrg();

            var matches = divisions.stream()
                    .filter(d ->
                            d.getBudgetOrg().equals(budgetOrg)
                                    && (d.getFund().equals(ctx.getFund())
                                    || (ctx.getFund().equals(ctx.getTwoYearFund())
                                    && d.equals(ctx.getEducationDivision()))))
                    .toList();

            if (matches.size() != 1) {
                var msg = matches.isEmpty()
                        ? "Invalid budgetOrg: " + budgetOrg
                        : "Multiple divisions matching budgetOrg: " + budgetOrg;
                return new ValidationResult.Fail(msg);
            }

            ctx.setDivision(matches.getFirst());
            return ValidationResult.Ok.INSTANCE;
        };
    }

    /**
     * Creates a validator that resolves the document's project code to an
     * existing {@link Activity} and verifies that the activity belongs to the
     * resolved fund.
     *
     * <p>When validation succeeds, the resolved activity is stored in the
     * validation context. The corresponding generic activity is stored as the
     * projection activity when one exists; otherwise, the resolved activity is
     * used as the projection activity.
     *
     * @param q the query service used to resolve activities
     * @return a validator that fails when the activity does not exist or does
     * not belong to the resolved fund
     */
    static Validator activityExists(JifmsQueryService q) {
        return ctx -> {
            var activity = q.fetchActivity(ctx.getDivision(), ctx.getDocument().getProject());

            if (activity == null)
                return new ValidationResult.Fail("Invalid activity: " + ctx.getDocument().getProject());

            if (!activity.getFund().equals(ctx.getFund()))
                return new ValidationResult.Fail("Invalid activity fund: " + activity.getFund().getFundCode());

            var genericActivity = q.fetchGenericActivity(activity);

            ctx.setActivity(activity);
            ctx.setProjectionActivity(genericActivity == null ? activity : genericActivity);

            return ValidationResult.Ok.INSTANCE;
        };
    }

    /**
     * Creates a validator that resolves the document's budget object class code
     * to a known {@link ObjectClass}.
     *
     * <p>When validation succeeds, the resolved object class is stored in the
     * validation context.
     *
     * @param objectClassMap the object classes indexed by budget object class
     *                       code
     * @return a validator that fails when the document's budget object class
     * code is not present in {@code objectClassMap}
     */
    static Validator objectClassKnown(Map<String, ObjectClass> objectClassMap) {
        return ctx -> {
            var documentBoc = ctx.getDocument().getBudgetObjectClass();
            var fisObjectClass = objectClassMap.get(documentBoc);

            if (fisObjectClass == null)
                return new ValidationResult.Fail("Invalid objectClass: " + documentBoc);

            ctx.setObjectClass(fisObjectClass);
            return ValidationResult.Ok.INSTANCE;
        };
    }

    /**
     * Creates a validator that resolves the object class used for projections.
     *
     * <p>For a generic projection activity, the validator derives a generic
     * budget object class by retaining the first two characters of the
     * document's budget object class code and replacing the remaining
     * characters with {@code 00}. For a non-generic projection activity, the
     * document's resolved object class is used unchanged.
     *
     * <p>When validation succeeds, the resulting object class is stored as the
     * projection object class in the validation context.
     *
     * @param objectClassMap the object classes indexed by budget object class
     *                       code
     * @return a validator that fails when a generic object class is required
     * but cannot be derived or resolved
     */
    static Validator genericObjectClassWhenGeneric(Map<String, ObjectClass> objectClassMap) {
        return ctx -> {
            var activity = ctx.getProjectionActivity();

            if (activity != null && activity.getGenericProjection()) {
                var documentBoc = ctx.getDocument().getBudgetObjectClass();

                if (documentBoc == null || documentBoc.length() < 2)
                    return new ValidationResult.Fail("Generic BOC derivation failed: invalid BOC: " + documentBoc);

                var genericBoc = documentBoc.substring(0, 2) + "00";
                var fisObjectClass = objectClassMap.get(genericBoc);

                if (fisObjectClass == null)
                    return new ValidationResult.Fail("Generic BOC required but not found: " + genericBoc);

                ctx.setProjectionObjectClass(fisObjectClass);
                return ValidationResult.Ok.INSTANCE;
            }

            ctx.setProjectionObjectClass(ctx.getObjectClass());
            return ValidationResult.Ok.INSTANCE;
        };
    }

    /**
     * Creates a validator that verifies the structure and encoded values of a
     * JIFMS document number.
     *
     * <p>The document number must:
     * <ul>
     *     <li>contain exactly 11 characters;</li>
     *     <li>begin with {@code FJC}, ignoring letter case;</li>
     *     <li>contain the expected budget fiscal year segment;</li>
     *     <li>contain a hyphen at the expected position;</li>
     *     <li>contain the resolved division code, except for documents
     *         belonging to the specified OBBBA budget organization; and</li>
     *     <li>contain the code corresponding to either a recognized travel or
     *         purchase document type.</li>
     * </ul>
     *
     * @param travelTypes    the document types recognized as travel documents
     * @param purchaseTypes  the document types recognized as purchase documents
     * @param obbbaBudgetOrg the budget organization exempt from the division
     *                       code comparison
     * @return a validator that fails when the document number is missing,
     * malformed, or inconsistent with the document
     */
    static Validator documentNumberRule(
            List<String> travelTypes, List<String> purchaseTypes, String obbbaBudgetOrg) {

        return ctx -> {
            var doc = ctx.getDocument();
            var dn = doc.getDocumentNumber();

            if (dn == null)
                return new ValidationResult.Fail("Invalid document number: null");
            if (dn.length() != 11)
                return new ValidationResult.Fail("Invalid document number length: " + dn);
            if (!dn.toUpperCase().startsWith("FJC"))
                return new ValidationResult.Fail("Invalid document number prefix: " + dn);
            if (!dn.substring(3, 4).equals(doc.getBbfy().substring(2, 3)))
                return new ValidationResult.Fail("Invalid document number BFY segment: " + dn);
            if (dn.charAt(5) != '-')
                return new ValidationResult.Fail("Missing hyphen in document number: " + dn);

            var divisionCode = ctx.getDivision().getDivisionCode();

            if (!doc.getBudgetOrg().equals(obbbaBudgetOrg)
                    && !dn.substring(7, 8).equals(divisionCode))
                return new ValidationResult.Fail("Division code mismatch in document number: " + dn);

            var dt = doc.getDocumentType();
            var isTravel = dn.charAt(6) == '7' && travelTypes.contains(dt);
            var isPO = dn.charAt(6) == '8' && purchaseTypes.contains(dt);

            if (!(isTravel || isPO))
                return new ValidationResult.Fail("Document type code mismatch in document number: " + dn);

            return ValidationResult.Ok.INSTANCE;
        };
    }

    /**
     * Creates a validator that checks whether another document line already
     * uses the same budget object class.
     *
     * @param q the query service used to search for an existing budget object
     *          class assignment
     * @return a validator that fails when a duplicate budget object class
     * exists for the document number and line number
     */
    static Validator duplicateBocExists(JifmsQueryService q) {
        return ctx -> {
            var doc = ctx.getDocument();
            if (q.duplicateBocExists(doc.getDocumentNumber(), doc.getBudgetObjectClass(), doc.getLineNumber())) {
                return new ValidationResult.Fail(String.format("Duplicate boc %s exists for %s",
                        doc.getBudgetObjectClass(), doc.getDocumentNumber()));
            }
            return ValidationResult.Ok.INSTANCE;
        };
    }
}