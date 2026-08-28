package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.*;

import java.util.List;
import java.util.Map;

final class Validators {

    static Validator fundIsKnown(Map<String, Fund> fundMap) {
        return ctx -> {
            var fund = fundMap.get(ctx.getDocument().getFundCode());
            if (fund == null)
                return new ValidationResult.Fail("Invalid fund: " + ctx.getDocument().getFundCode());

            ctx.setFund(fund);
            return ValidationResult.Ok.INSTANCE;
        };
    }

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
}