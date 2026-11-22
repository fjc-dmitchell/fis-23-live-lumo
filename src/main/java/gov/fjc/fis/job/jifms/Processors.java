package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provides processing steps used to create or update FIS records from
 * documents imported from JIFMS.
 *
 * <p>Each processor returned by this class performs one stage of the document
 * processing pipeline. These stages create or update obligations, activity
 * projections, division allocations, fund control notices, and vendors.
 * Processors may also enrich the {@link ResolvedContext} and populate the
 * associated {@link AuditRecord} with details of the changes made.
 *
 * <p>This class is not intended to be instantiated.
 *
 * @author Doug Mitchell
 */
public final class Processors {

    /**
     * Prevents instantiation of this utility class.
     */
    private Processors() {
    } // Same pattern as Validators

    /**
     * Creates a processor that inserts or updates the {@link Obligation}
     * represented by a JIFMS document.
     *
     * <p>If no matching obligation exists, the processor creates and saves a
     * new obligation, records the document amount as the obligation amount
     * difference, and returns {@link ProcessingResult.Inserted}. If an
     * obligation exists, the processor captures its original audit values,
     * calculates the change in amount, and updates fields whose incoming
     * values differ from the existing values.
     *
     * <p>The processor records the previous activity or object class in the
     * context when either value changes. It also indicates whether a financial
     * change notice is required when the obligation amount changes.
     *
     * @param q     the query service used to retrieve, create, and save obligations
     * @param audit the audit record in which processing changes are recorded
     * @return a processor that inserts, updates, or ignores the obligation
     */
    public static Processor processObligation(JifmsQueryService q, AuditRecord audit) {
        return ctx -> {
            var doc = ctx.getDocument();
            var obligation = q.fetchObligation(ctx.getDivision(), doc.getDocumentNumber(), doc.getLineNumber());
            if (obligation == null) {
                obligation = q.createObligationFrom(ctx);
                ctx.setObligation(obligation);
                ctx.setObligationAmountDifference(ctx.getDocument().getAmount());
                audit.setLoggedChanges(String.format("NEW Obligation created: %s", doc.getDocumentNumber()));
                q.saveObligation(obligation);
                return ProcessingResult.Inserted.INSTANCE;
            } else {
                // on update, save original obligation fields
                audit.setObligationAuditFields(ObligationAuditFields.from(obligation));

                var obligationAmountDifference = Objects.requireNonNullElse(ctx.getDocument().getAmount(), BigDecimal.ZERO)
                        .subtract(Objects.requireNonNullElse(obligation.getAmount(), BigDecimal.ZERO));

                ctx.setObligationAmountDifference(obligationAmountDifference);

                ctx.setFcnRequired(BigDecimal.ZERO.compareTo(obligationAmountDifference) != 0);

                if (!Objects.equals(obligation.getActivity().getId(), ctx.getActivity().getId())) {
                    ctx.setPreviousActivity(obligation.getActivity());
                }
                if (!Objects.equals(obligation.getObjectClass().getId(), ctx.getObjectClass().getId())) {
                    ctx.setPreviousObjectClass(obligation.getObjectClass());
                }

                ctx.setObligation(obligation);
                var updated = updateObligation(ctx, audit);
                if (updated) {
                    q.saveObligation(obligation);
                    return ProcessingResult.Updated.INSTANCE;
                }
                return ProcessingResult.Ignored.INSTANCE;
            }
        };
    }

    /**
     * Creates a processor that adjusts the {@link ActivityProjection}
     * associated with an obligation.
     *
     * <p>When an obligation's activity or object class changes, the processor
     * first restores the amount previously deducted from the former
     * projection, provided that projection exists. It then applies the
     * obligation amount difference to the current projection. For activities
     * that use generic projections, the corresponding generic object class is
     * used.
     *
     * <p>If the current projection does not exist, the processor creates it.
     * The projection amounts and identifying values before and after processing
     * are stored in the supplied audit record.
     *
     * @param q              the query service used to retrieve, create, and save activity
     *                       projections
     * @param objectClassMap the object classes indexed by budget object class
     *                       code
     * @param audit          the audit record in which projection changes are recorded
     * @return a processor that updates the applicable activity projections and
     * returns {@link ProcessingResult.Continue}
     */
    public static Processor projection(JifmsQueryService q, Map<String, ObjectClass> objectClassMap, AuditRecord audit) {
        return ctx -> {
            var projectionAudit = new ProjectionAuditFields();

            projectionAudit.setCurrentActivityNumber(ctx.getProjectionActivity().getActivityNumber());
            projectionAudit.setCurrentProjectionBoc(ctx.getProjectionObjectClass().getBudgetObjectClass());

            var previousActivity = ctx.getPreviousActivity();
            var previousObjectClass = ctx.getPreviousObjectClass();


            BigDecimal restoreAmount = BigDecimal.ZERO;

            // on activity or BOC change, restore the previous projection only if record exists
            if (previousActivity != null || previousObjectClass != null) {
                projectionAudit.setPreviousActivityNumber(previousActivity == null ? null : previousActivity.getActivityNumber());
                projectionAudit.setPreviousProjectionBoc(previousObjectClass == null ? null : previousObjectClass.getBudgetObjectClass());

                Activity restoreActivity =
                        previousActivity != null ? previousActivity : ctx.getActivity();

                ObjectClass restoreObjectClass =
                        previousObjectClass != null ? previousObjectClass : ctx.getObjectClass();

                if (restoreActivity.getGenericProjection()) {
                    String ocPrefix = restoreObjectClass.getBudgetObjectClass().substring(0, 2);
                    restoreObjectClass = objectClassMap.get(ocPrefix + "00");
                }

                var restoreProjection = q.fetchActivityProjection(restoreActivity, restoreObjectClass);

                if (restoreProjection != null) {
                    projectionAudit.setPreviousProjectionAmountBefore(restoreProjection.getAmount());

                    restoreAmount = ctx.getObligation().getAmount().subtract(ctx.getObligationAmountDifference());

                    restoreProjection.setAmount(
                            calculateNewProjection(
                                    ctx.getDocument().getBbfy(),
                                    restoreProjection.getAmount(),
                                    restoreAmount.negate(),
                                    restoreActivity.getTrainingProject()
                            )
                    );
                    projectionAudit.setPreviousProjectionAmountAfter(restoreProjection.getAmount());

                    q.saveActivityProjection(restoreProjection);
                }
            }


            var activity = ctx.getProjectionActivity();
            var objectClass = ctx.getProjectionObjectClass();
            ActivityProjection activityProjection = q.fetchActivityProjection(activity, objectClass);
            BigDecimal currentProjectionAmountBefore;
            BigDecimal currentProjectionAmountAfter;
            if (activityProjection != null) {
                currentProjectionAmountBefore = activityProjection.getAmount();
                currentProjectionAmountAfter = calculateNewProjection(ctx.getDocument().getBbfy(),
                        currentProjectionAmountBefore, ctx.getObligationAmountDifference().add(restoreAmount), ctx.getActivity().getTrainingProject());

                activityProjection.setAmount(currentProjectionAmountAfter);
                q.saveActivityProjection(activityProjection);
            } else {

                currentProjectionAmountBefore = BigDecimal.ZERO;
                currentProjectionAmountAfter = calculateNewProjection(ctx.getDocument().getBbfy(),
                        currentProjectionAmountBefore, ctx.getObligationAmountDifference(), ctx.getActivity().getTrainingProject());
                q.createActivityProjection(activity, objectClass, currentProjectionAmountAfter);
            }

            projectionAudit.setCurrentProjectionAmountBefore(currentProjectionAmountBefore);
            projectionAudit.setCurrentProjectionAmountAfter(currentProjectionAmountAfter);

            audit.setProjectionAuditFields(projectionAudit);
            return ProcessingResult.Continue.INSTANCE;       // never overrides INSERTED
        };
    }

    /**
     * Creates a processor that ensures a {@link DivisionAllocation} exists for
     * the resolved division and object category.
     *
     * <p>A new allocation is created only when the context contains both a
     * division and an object class and no matching allocation already exists.
     *
     * @param q the query service used to retrieve and create division
     *          allocations
     * @return a processor that ensures the required division allocation exists
     * and returns {@link ProcessingResult.Continue}
     */
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

    /**
     * Creates a processor that generates a fund control notice when one is
     * required by the resolved context.
     *
     * @param q the query service used to create the fund control notice
     * @return a processor that conditionally creates a fund control notice
     * and returns {@link ProcessingResult.Continue}
     */
    public static Processor fcn(JifmsQueryService q) {
        return ctx -> {
            if (ctx.isFcnRequired()) {
                q.createFcnFrom(ctx);
            }
            return ProcessingResult.Continue.INSTANCE;
        };
    }

    /**
     * Creates a processor that ensures the document's vendor exists.
     *
     * <p>A vendor is created when no vendor matches the document's vendor and
     * address codes and both codes are non-null.
     *
     * @param q the query service used to retrieve and create vendors
     * @return a processor that conditionally creates a vendor and returns
     * {@link ProcessingResult.Continue}
     */
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

    /**
     * Updates an existing obligation with values from the resolved document
     * and records the names of changed fields.
     *
     * <p>Values required by later processing steps are captured in the context
     * before the corresponding obligation fields are overwritten. When at
     * least one audited field changes, a description of the changes is added
     * to the audit record.
     *
     * @param context the resolved processing context containing the document
     *                and obligation
     * @param audit   the audit record in which obligation changes are recorded
     * @return {@code true} if at least one audited field changed; otherwise
     * {@code false}
     */
    static boolean updateObligation(ResolvedContext context, AuditRecord audit) {
        var document = context.getDocument();
        var obligation = context.getObligation();
        var changes = new StringBuilder();

        syncStatus(document, obligation, changes);
        syncBlanketPurchaseOrder(document, obligation, changes);

        syncTracked("Amount", document.getAmount(), obligation.getAmount(), obligation::setAmount, context::setPreviousObligationAmount, changes);
        syncTracked("Activity", context.getActivity(), obligation.getActivity(), obligation::setActivity, context::setPreviousActivity, changes);
        syncTracked("BOC", context.getObjectClass(), obligation.getObjectClass(), obligation::setObjectClass, context::setPreviousObjectClass, changes);

        sync("Vendor", document.getTitle(), obligation.getVendor(), obligation::setVendor, changes);
        sync("Travel Start Date", document.getTravelStartDate(), obligation.getTravelStartDate(), obligation::setTravelStartDate, changes);
        sync("Travel End Date", document.getTravelEndDate(), obligation.getTravelEndDate(), obligation::setTravelEndDate, changes);
        sync("Addr. Code", document.getAddressCode(), obligation.getAddressCode(), obligation::setAddressCode, changes);
        sync("Vendor Code", document.getVendorCode(), obligation.getVendorCode(), obligation::setVendorCode, changes);
        sync("TaxId", document.getTaxId(), obligation.getEin(), obligation::setEin, changes);

        boolean changed = !changes.isEmpty();
        if (changed) {
            audit.setLoggedChanges(String.format("UPDATED %s: %s", document.getDocumentNumber(), changes));
        }
        return changed;
    }


    /**
     * Replaces a field value when the incoming and current values differ and
     * appends the field name to the change description.
     *
     * @param name     the field name to include in the change description
     * @param incoming the incoming field value
     * @param current  the current field value
     * @param setter   the operation that replaces the current value
     * @param changes  the accumulated description of changed fields
     * @param <T>      the field value type
     */
    private static <T> void sync(String name, T incoming, T current,
                                 Consumer<T> setter, StringBuilder changes) {
        if (!Objects.equals(incoming, current)) {
            setter.accept(incoming);
            changes.append(" -").append(name);
        }
    }


    /**
     * Captures and replaces a field value when the incoming and current values
     * differ, then appends the field name to the change description.
     *
     * @param name            the field name to include in the change description
     * @param incoming        the incoming field value
     * @param current         the current field value
     * @param setter          the operation that replaces the current value
     * @param previousCapture the operation that captures the current value
     *                        before it is replaced
     * @param changes         the accumulated description of changed fields
     * @param <T>             the field value type
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
     * Synchronizes the obligation's open status with the JIFMS document.
     *
     * <p>This operation closes an open obligation when the corresponding
     * document has a closed date. It does not reopen an obligation when the
     * JIFMS document remains open because users may close an obligation in FIS
     * before the document is closed in JIFMS.
     *
     * @param document   the incoming JIFMS document
     * @param obligation the obligation whose status may be updated
     * @param changes    the accumulated description of changed fields
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

    /**
     * Synchronizes the obligation's blanket purchase order flag with the
     * document's FJC value.
     *
     * <p>When the FJC value is present, the blanket purchase order flag is set
     * according to whether that value equals {@code bpo}, ignoring case, and
     * any change is recorded. When the FJC value is absent, an existing flag
     * is cleared without an audit entry for budget fiscal year 2026 and later.
     *
     * @param document   the incoming JIFMS document
     * @param obligation the obligation whose blanket purchase order flag may
     *                   be updated
     * @param changes    the accumulated description of audited field changes
     */
    private static void syncBlanketPurchaseOrder(Document document, Obligation obligation, StringBuilder changes) {
        if (document.getFjc() == null) {
            if (obligation.getBlanketPurchaseOrder()
                    && document.getBbfy().compareTo(BPO_SYNC_START_BBFY) >= 0) {
//                System.out.println("document bpo: " + document.getFjc() + " obligation bpo: " + obligation.getBpoString());
                obligation.setBlanketPurchaseOrder(false); // silent sync, no audit entry
            }
        } else {
            boolean fjcIsBpo = "bpo".equalsIgnoreCase(document.getFjc());
            if (obligation.getBlanketPurchaseOrder() != fjcIsBpo) {
//                System.out.println("---------------- BFY: " + document.getBbfy() + " Document bpo: " + document.getFjc() + " obligation bpo: " + obligation.getBpoString());
                obligation.setBlanketPurchaseOrder(fjcIsBpo);
                changes.append(" -BPO");
            }
        }
    }

    /**
     * Calculates an updated projection amount from a change in an obligation.
     *
     * <p>An increase in the obligation decreases the projection, but never
     * below zero. A decrease in the obligation normally increases the
     * projection. The projection is not increased for a training project or
     * on or after October 1 of the specified budget fiscal year.
     *
     * @param bbfy               the four-digit budget fiscal year
     * @param originalProjection the projection amount before the obligation
     *                           change
     * @param obligationChange   the change in the obligation amount; a positive
     *                           value represents an increase and a negative value
     *                           represents a decrease
     * @param trainingProject    whether the obligation belongs to a training
     *                           project
     * @return the adjusted projection amount
     */
    static BigDecimal calculateNewProjection(String bbfy, BigDecimal originalProjection, BigDecimal obligationChange, boolean trainingProject) {

        // obligation increased, decrease projection
        if (obligationChange.compareTo(BigDecimal.ZERO) > 0) {
            return originalProjection.subtract(obligationChange).max(BigDecimal.ZERO);
        }

        // obligation decreased, don't increase projection if
        // training project OR today is on or after 10/1/bbfy
        if (trainingProject || isOnOrAfterOctoberFirst(bbfy)) {
            return originalProjection;
        }

        // obligation decreased, the projection will be increased
        return originalProjection.subtract(obligationChange);
    }

    /**
     * Determines whether the current date is on or after October 1 of the
     * specified year.
     *
     * @param yearString the year as a four-digit string
     * @return {@code true} if the current date is October 1 of the specified
     * year or later; otherwise {@code false}
     * @throws IllegalArgumentException if {@code yearString} is null or is not
     *                                  a four-digit year
     */
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