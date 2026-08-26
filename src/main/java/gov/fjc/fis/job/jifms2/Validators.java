package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.*;

import java.util.List;
import java.util.Map;

final class Validators {

    static Validator fundIsKnown(Map<String, Fund> fundMap) {
        return ctx -> {
            var fund = fundMap.get(ctx.document().getFundCode());
            if (fund == null) return new ValidationResult.Fail("Invalid fund: " + ctx.document().getFundCode());
            return new ValidationResult.Ok(new ResolvedContext(
                    ctx.document(), ctx.twoYearFund(), ctx.educationDivision(),
                    fund, ctx.division(), ctx.activity(), ctx.projectionActivity(),
                    ctx.objectClass(), ctx.projectionObjectClass()));
        };
    }

    static Validator divisionMatches(List<Division> divisions) {
        return ctx -> {
            var budgetOrg = ctx.document().getBudgetOrg();
            var matches = divisions.stream()
                    .filter(d ->
                            d.getBudgetOrg().equals(budgetOrg) &&
                                    (d.getFund().equals(ctx.fund()) ||
                                            (ctx.fund().equals(ctx.twoYearFund()) && d.equals(ctx.educationDivision()))))
                    .toList();

            if (matches.size() != 1) {
                var msg = matches.isEmpty()
                        ? "Invalid budgetOrg: " + budgetOrg
                        : "Multiple divisions matching budgetOrg: " + budgetOrg;
                return new ValidationResult.Fail(msg);
            }
            var div = matches.getFirst();
            return new ValidationResult.Ok(new ResolvedContext(
                    ctx.document(), ctx.twoYearFund(), ctx.educationDivision(),
                    ctx.fund(), div, ctx.activity(), ctx.projectionActivity(),
                    ctx.objectClass(), ctx.projectionObjectClass()));
        };
    }

    static Validator activityExists(DocumentProcessingService p) {
        return ctx -> {
            var act = p.fetchActivity(ctx.division(), ctx.document().getProject());
            if (act == null) return new ValidationResult.Fail("Invalid activity: " + ctx.document().getProject());
            if (!act.getFund().equals(ctx.fund()))
                return new ValidationResult.Fail("Invalid activity fund: " + act.getFund().getFundCode());
            var genAct = p.fetchGenericActivity(act);
            return new ValidationResult.Ok(new ResolvedContext(
                    ctx.document(), ctx.twoYearFund(), ctx.educationDivision(),
                    ctx.fund(), ctx.division(), act, genAct == null ? act : genAct,
                    ctx.objectClass(), ctx.projectionObjectClass()));
        };
    }

    static Validator objectClassKnown(Map<String, ObjectClass> ocMap) {
        return ctx -> {
            var boc = ctx.document().getBudgetObjectClass();
            var oc = ocMap.get(boc);
            if (oc == null) return new ValidationResult.Fail("Invalid objectClass: " + boc);
            return new ValidationResult.Ok(new ResolvedContext(
                    ctx.document(), ctx.twoYearFund(), ctx.educationDivision(),
                    ctx.fund(), ctx.division(), ctx.activity(), ctx.projectionActivity(),
                    oc, ctx.projectionObjectClass()));
        };
    }

    static Validator genericObjectClassWhenGeneric(Map<String, ObjectClass> ocMap) {
        return ctx -> {
            var activity = ctx.projectionActivity();
            if (activity != null && activity.getGenericProjection()) {
                var boc = ctx.document().getBudgetObjectClass();
                if (boc == null || boc.length() < 2)
                    return new ValidationResult.Fail("Generic BOC derivation failed: invalid BOC: " + boc);

                var genericBoc = boc.substring(0, 2) + "00";
                var genOc = ocMap.get(genericBoc);
                if (genOc == null)
                    return new ValidationResult.Fail("Generic BOC required but not found: " + genericBoc);

                return new ValidationResult.Ok(new ResolvedContext(
                        ctx.document(), ctx.twoYearFund(), ctx.educationDivision(),
                        ctx.fund(), ctx.division(), ctx.activity(), ctx.projectionActivity(),
                        ctx.objectClass(), genOc));
            }
            // Use specific OC as projection OC
            return new ValidationResult.Ok(new ResolvedContext(
                    ctx.document(), ctx.twoYearFund(), ctx.educationDivision(),
                    ctx.fund(), ctx.division(), ctx.activity(), ctx.projectionActivity(),
                    ctx.objectClass(), ctx.objectClass()));
        };
    }

    static Validator documentNumberRule(
            List<String> travelTypes, List<String> purchaseTypes, String obbbaBudgetOrg) {

        return ctx -> {
            var doc = ctx.document();
            var dn = doc.getDocumentNumber();
            if (dn == null) return new ValidationResult.Fail("Invalid document number: null");
            if (dn.length() != 11) return new ValidationResult.Fail("Invalid document number length: " + dn);
            if (!dn.toUpperCase().startsWith("FJC"))
                return new ValidationResult.Fail("Invalid document number prefix: " + dn);
            if (!dn.substring(3, 4).equals(doc.getBbfy().substring(2, 3)))
                return new ValidationResult.Fail("Invalid document number BFY segment: " + dn);
            if (dn.charAt(5) != '-') return new ValidationResult.Fail("Missing hyphen in document number: " + dn);

            var divisionCode = ctx.division().getDivisionCode();
            if (!doc.getBudgetOrg().equals(obbbaBudgetOrg) && !dn.substring(7, 8).equals(divisionCode))
                return new ValidationResult.Fail("Division code mismatch in document number: " + dn);

            var dt = doc.getDocumentType();
            var isTravel = dn.charAt(6) == '7' && travelTypes.contains(dt);
            var isPO = dn.charAt(6) == '8' && purchaseTypes.contains(dt);
            if (!(isTravel || isPO))
                return new ValidationResult.Fail("Document type code mismatch in document number: " + dn);

            return new ValidationResult.Ok(ctx);
        };
    }
}
