package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class Processors {

    private Processors() {
    } // Same pattern as Validators

    public static Processor processObligation(JifmsQueryService q, AuditRecord audit) {
        return ctx -> {
            var doc = ctx.getDocument();
            var obligation = q.fetchObligation(ctx.getDivision(), doc.getDocumentNumber(), doc.getLineNumber());
            if (obligation == null) {
                // calculate delta
                obligation = q.createObligationFrom(ctx);
                ctx.withObligation(obligation);
                ctx.setObligationAmountDifference(BigDecimal.ZERO);
                audit.setLoggedChanges(String.format("NEW Obligation: %s", doc.getDocumentNumber()));
                q.saveObligation(obligation);
                return ProcessingResult.Inserted.INSTANCE;
            } else {
                // on update, save original obligation fields
                audit.setObligationAuditFields(ObligationAuditFields.from(obligation));

//                ctx.setObligationAmountDifference(
//                        Objects.requireNonNullElse(obligation.getAmount(), BigDecimal.ZERO)
//                                .subtract(Objects.requireNonNullElse(ctx.getDocument().getAmount(), BigDecimal.ZERO)));
                ctx.setObligationAmountDifference(
                        Objects.requireNonNullElse(ctx.getDocument().getAmount(), BigDecimal.ZERO)
                                .subtract(Objects.requireNonNullElse(obligation.getAmount(), BigDecimal.ZERO))
                );
                ctx.withObligation(obligation);
                var updated = updateObligation(ctx, audit);
                if (updated) {
                    ctx.withObligation(obligation);
                    q.saveObligation(obligation);
                    return ProcessingResult.Updated.INSTANCE;
                }
                return ProcessingResult.Ignored.INSTANCE;
            }
        };
    }

    public static Processor projection(JifmsQueryService q, AuditRecord audit) {
        return ctx -> {
//            var objectClass = ctx.getPreviousObjectClass();
//            var objectClass = ctx.getObjectClass();
            var activity = ctx.getProjectionActivity();
            var objectClass = ctx.getProjectionObjectClass();
            Optional<ActivityProjection> projection = q.fetchActivityProjection(activity, objectClass);
            if (projection.isPresent()) {
                var activityProjection = projection.get();
                activityProjection.setAmount(activityProjection.getAmount().subtract(calculateProjectionChangeAmount(ctx)));
//                projection.get().setAmount(calculateProjectionChangeAmount(ctx));
                q.saveActivityProjection(projection.get());
            } else {
                q.createActivityProjection(activity, objectClass, BigDecimal.ZERO);
            }
            return ProcessingResult.Continue.INSTANCE;       // never overrides INSERTED
        };
    }

    public static Processor allocation(JifmsQueryService q) {
        return ctx -> {
            var division = ctx.getDivision();
            var objectClass = ctx.getObjectClass();
            if (division != null && objectClass != null) {
                var objectCategory = objectClass.getObjectCategory();
                Optional<DivisionAllocation> allocation = q.fetchAllocation(division, objectCategory);
                if (allocation.isEmpty()) {
                    q.createDivisionAllocation(division, objectCategory);
                }
            }
            return ProcessingResult.Continue.INSTANCE;
        };
    }

    public static Processor fcn(JifmsQueryService q) {
        return ctx -> {
//            if (ctx.getObligationAmountDifference().compareTo(BigDecimal.ZERO) != 0) {
//                q.createFcnFrom(ctx);
//            }
            if (ctx.createFcn()) {
                q.createFcnFrom(ctx);
            }
            return ProcessingResult.Continue.INSTANCE;
        };
    }

    public static Processor vendor(JifmsQueryService q) {
        return ctx -> {
            String vendorCode = ctx.getDocument().getVendorCode();
            String addressCode = ctx.getDocument().getAddressCode();
            Optional<Vendor> vendor = q.fetchVendor(vendorCode, addressCode);
            if (vendor.isEmpty() && vendorCode != null && addressCode != null) {
                q.createVendor(ctx.getDocument());
            }
            return ProcessingResult.Continue.INSTANCE;
        };
    }

    static boolean updateObligation(ResolvedContext context, AuditRecord audit) {
        var document = context.getDocument();
        var obligation = context.getObligation();
        var changes = new StringBuilder();

        syncStatus(document, obligation, changes);
        syncBlanketPurchaseOrder(document, obligation, changes);

        syncTracked("amount", document.getAmount(), obligation.getAmount(), obligation::setAmount, context::setPreviousObligationAmount, changes);
        syncTracked("activity", context.getActivity(), obligation.getActivity(), obligation::setActivity, context::setPreviousActivity, changes);
        syncTracked("objectClass", context.getObjectClass(), obligation.getObjectClass(), obligation::setObjectClass, context::setPreviousObjectClass, changes);

        sync("vendor", document.getTitle(), obligation.getVendor(), obligation::setVendor, changes);
        sync("travelStartDate", document.getTravelStartDate(), obligation.getTravelStartDate(), obligation::setTravelStartDate, changes);
        sync("travelEndDate", document.getTravelEndDate(), obligation.getTravelEndDate(), obligation::setTravelEndDate, changes);
        sync("vendorCode", document.getVendorCode(), obligation.getVendorCode(), obligation::setVendorCode, changes);
        sync("taxId", document.getTaxId(), obligation.getEin(), obligation::setEin, changes);

        boolean changed = !changes.isEmpty();
        if (changed) {
            audit.setLoggedChanges(String.format("UPDATED %s: %s", document.getDocumentNumber(), changes));
        }
        return changed;
    }

    /**
     * Syncs a field that requires no prior-value tracking.
     */
    private static <T> void sync(String name, T incoming, T current,
                                 Consumer<T> setter, StringBuilder changes) {
        if (!Objects.equals(incoming, current)) {
            setter.accept(incoming);
            changes.append(" -").append(name);
        }
    }

    /**
     * Syncs a field and captures the old value before overwriting.
     */
    private static <T> void syncTracked(String name, T incoming, T current,
                                        Consumer<T> setter, Consumer<T> previousCapture,
                                        StringBuilder changes) {
        if (!Objects.equals(incoming, current)) {
            previousCapture.accept(current);
            setter.accept(incoming);
            changes.append(" -").append(name);
        }
    }


    /**
     * Business rule (2019): only close an obligation when JIFMS closes it;
     * never re-open FIS obligation through this sync path. User may close
     * obligation in FIS before Document is closed in JIFMS.
     */
    private static void syncStatus(Document document, Obligation obligation, StringBuilder changes) {
        boolean jifmsOpen = document.getClosedDate() == null;
        if (obligation.getStatus() && !jifmsOpen) {
            obligation.setStatus(false);
            changes.append(" -status");
        }
    }

    /**
     * Syncs blanket purchase order flag.
     * If FJC is null, silently clears BPO starting in BBFY 2026 (no audit entry).
     * If FJC is present, derives BPO from whether FJC equals "bpo" and audits the change.
     * <p>
     * Note: if Court-Activity is ever used for values other than "bpo",
     * this logic will need to parse accordingly.
     */
    private static final String BPO_SYNC_START_BBFY = "2026";

    private static void syncBlanketPurchaseOrder(Document document, Obligation obligation, StringBuilder changes) {
        if (document.getFjc() == null) {
            if (obligation.getBlanketPurchaseOrder()
                    && document.getBbfy().compareTo(BPO_SYNC_START_BBFY) >= 0) {
                System.out.println("document bpo: " + document.getFjc() + " obligation bpo: " + obligation.getBpoString());
                obligation.setBlanketPurchaseOrder(false); // silent sync, no audit entry
            }
        } else {
            boolean fjcIsBpo = "bpo".equalsIgnoreCase(document.getFjc());
            if (obligation.getBlanketPurchaseOrder() != fjcIsBpo) {
                System.out.println("---------------- BFY: " + document.getBbfy() + " Document bpo: " + document.getFjc() + " obligation bpo: " + obligation.getBpoString());
                obligation.setBlanketPurchaseOrder(fjcIsBpo);
                changes.append(" -BPO");
            }
        }
    }

    /**
     * Projection rule created from 10/3/19 meeting with Nanticha and Mary. Doug suggested
     * to make rule based on training. Nanticha rule: do not increase projections for training de-obs.
     * Mary rule: do not increase projections after fiscal yearend. Otherwise, adjust the projection.
     *
     * @param context The DocumentAuditContext containing state variables
     * @return the amount the projection should be adjusted by based on business rules
     */
    static BigDecimal calculateProjectionChangeAmount(ResolvedContext context) {
        var trainingProject = context.getActivity().getTrainingProject();
        var obligationChange = context.getObligationAmountDifference();
        var bbfy = context.getDocument().getBbfy();

        // obligation increased, the projection will be decreased
        if (obligationChange.compareTo(BigDecimal.ZERO) >= 0) {
            return obligationChange;
        }

        // obligation decreased, projection will not be increased if
        // training project OR today is on or after 10/1/bbfy
        if (trainingProject || isOnOrAfterOctoberFirst(bbfy)) {
            return BigDecimal.ZERO;
        }

        // obligation decreased, the projection will be increased
        return obligationChange;
    }

    static boolean isOnOrAfterOctoberFirst(String yearString) {
        // Validate and parse the year
        if (yearString == null || !yearString.matches("\\d{4}")) {
            throw new IllegalArgumentException("yearStr must be a 4-digit year (e.g., \"2026\").");
        }

        // Parse the input year
        int year = Integer.parseInt(yearString);

        // Today's date
        LocalDate today = LocalDate.now();

        // October 1 of the given year
        LocalDate comparisonDate = LocalDate.of(year, 10, 1);

        // Return true if today is on or after October 1 of that year
        return !today.isBefore(comparisonDate);
    }
}