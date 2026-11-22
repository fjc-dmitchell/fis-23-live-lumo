package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.*;
import io.jmix.core.FetchPlan;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides data access operations used to validate and process documents
 * imported from JIFMS.
 *
 * <p>This service retrieves the funds, appropriations, divisions, activities,
 * object classes, obligations, projections, allocations, and vendors required
 * by the JIFMS processing pipeline. It also creates and persists records
 * produced during processing, including obligations, fund control notices,
 * activity projections, division allocations, and vendors.
 *
 * <p>Queries are executed through the {@link UnconstrainedDataManager} because
 * document processing is a system-level operation that must not depend on the
 * resource and row-level roles assigned to the current user.
 *
 * @author Doug Mitchell
 */
@Component("fis_JifmsQueryService")
public class JifmsQueryService {
    private final UnconstrainedDataManager unconstrainedDataManager;

    /**
     * Creates a JIFMS query service.
     *
     * @param unconstrainedDataManager the data manager used to query and
     *                                 persist entities without applying user
     *                                 access constraints
     */
    public JifmsQueryService(UnconstrainedDataManager unconstrainedDataManager) {
        this.unconstrainedDataManager = unconstrainedDataManager;
    }

    /**
     * Retrieves all funds indexed by fund code.
     *
     * @return a map whose keys are fund codes and whose values are the
     * corresponding funds
     */
    Map<String, Fund> fetchFundMap() {
        return unconstrainedDataManager.load(Fund.class)
                .all()
                .list()
                .stream()
                .collect(Collectors.toMap(Fund::getFundCode, Function.identity()));
    }

    /**
     * Retrieves the fund identified by the configured two-year fund code.
     *
     * @param twoYearFundCode the fund code assigned to the two-year fund
     * @return the matching fund, or an empty optional if no match exists
     */
    Optional<Fund> fetchTwoYearFund(String twoYearFundCode) {
        return unconstrainedDataManager.load(Fund.class)
                .query("SELECT f FROM fis_Fund f WHERE f.fundCode = :twoYearFundCode")
                .parameter("twoYearFundCode", twoYearFundCode)
                .optional();
    }

    /**
     * Retrieves all open appropriations in ascending budget fiscal year order.
     *
     * @return the open appropriations
     */
    List<Appropriation> fetchOpenAppropriations() {
        return unconstrainedDataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a"
                        + " WHERE a.status = TRUE"
                        + " ORDER BY a.budgetFiscalYear ASC")
                .list();
    }

    /**
     * Retrieves the divisions in an appropriation that have a nonblank budget
     * organization.
     *
     * <p>The appropriation and fund associations required during validation
     * are included in the fetch plan. Results are ordered by division code.
     *
     * @param appropriation the appropriation whose divisions are retrieved
     * @return the matching divisions, or an empty list if
     * {@code appropriation} is {@code null}
     */
    List<Division> fetchDivisionsWithBudgetOrgs(Appropriation appropriation) {
        if (appropriation == null) return List.of();
        return unconstrainedDataManager.load(Division.class)
                .query("SELECT d FROM fis_Division d"
                        + " WHERE d.appropriation = :appropriation"
                        + " AND d.budgetOrg IS NOT NULL AND TRIM(d.budgetOrg) <> ''"
                        + " ORDER BY d.divisionCode")
                .parameter("appropriation", appropriation)
                .fetchPlan(fp -> {
                    fp.addFetchPlan(FetchPlan.BASE);
                    fp.add("appropriation", fpb -> fpb.addFetchPlan(FetchPlan.BASE));
                    fp.add("fund", fpb -> fpb.addFetchPlan(FetchPlan.BASE));
                })
                .list();
    }

    /**
     * Retrieves the education division for an appropriation.
     *
     * @param appropriation         the appropriation containing the division
     * @param educationDivisionCode the code assigned to the education division
     * @return the matching division, or an empty optional if no match exists
     */
    Optional<Division> fetchEducationDivision(Appropriation appropriation, String educationDivisionCode) {
        return unconstrainedDataManager.load(Division.class)
                .query("SELECT d FROM fis_Division d"
                        + " WHERE d.appropriation = :appropriation"
                        + " AND d.divisionCode = :educationDivisionCode")
                .parameter("appropriation", appropriation)
                .parameter("educationDivisionCode", educationDivisionCode)
                .fetchPlan(fp -> {
                    fp.addFetchPlan(FetchPlan.BASE);
                    fp.add("fund");
                })
                .optional();
    }

    /**
     * Retrieves the object classes for an appropriation, indexed by budget
     * object class code.
     *
     * <p>Generic object classes are those whose budget object class codes end
     * in {@code 00}. They may be included for processing generic activity
     * projections or excluded when only document-level object classes are
     * required.
     *
     * @param appropriation   the appropriation whose object classes are retrieved
     * @param includeGenerics whether generic object classes are included
     * @return the matching object classes indexed by budget object class code,
     * or an empty map if {@code appropriation} is {@code null}
     */
    Map<String, ObjectClass> fetchObjectClassMap(Appropriation appropriation, boolean includeGenerics) {
        // can we eliminate the generics option here? is it ever used without generics?
        if (appropriation == null) return Map.of();
        return unconstrainedDataManager.load(ObjectClass.class)
                .query("SELECT o FROM fis_ObjectClass o"
                        + " WHERE o.objectCategory.appropriation = :appropriation"
                        + " AND (:includeGenerics = true OR o.budgetObjectClass NOT LIKE '%00')")
                .parameter("appropriation", appropriation)
                .parameter("includeGenerics", includeGenerics)
                .fetchPlan(fp -> {
                    fp.add("objectCategory");
                    fp.add("budgetObjectClass");
                })
                .list()
                .stream()
                .collect(Collectors.toMap(ObjectClass::getBudgetObjectClass, Function.identity()));
    }

    /**
     * Retrieves a batch of documents in the order required to detect duplicate
     * obligation budget object classes.
     *
     * <p>JIFMS permits duplicate budget object classes within a document, but
     * FIS requires each document line to represent a distinct budget object
     * class. Documents matching a {@link DocumentException} are excluded from
     * processing.
     *
     * @param bbfy        the beginning budget fiscal year
     * @param firstResult the zero-based index of the first document to retrieve
     * @param maxResults  the maximum number of documents to retrieve
     * @return the requested batch of documents
     */
    List<Document> fetchDocuments(String bbfy, int firstResult, int maxResults) {
        return unconstrainedDataManager.load(Document.class)
                .query("SELECT d FROM fis_Document d"
                        + " WHERE d.bbfy = :bbfy"
                        + " AND NOT EXISTS ("
                        + "     SELECT e FROM fis_DocumentException e"
                        + "     WHERE e.bbfy = d.bbfy"
                        + "     AND e.fundCode = d.fundCode"
                        + "     AND e.budgetOrg = d.budgetOrg"
                        + "     AND e.budgetObjectClass = d.budgetObjectClass"
                        + "     AND e.documentNumber = d.documentNumber"
                        + " )"
                        + " ORDER BY d.bbfy, d.budgetOrg, d.documentNumber, d.budgetObjectClass")
                .parameter("bbfy", bbfy)
                .firstResult(firstResult)
                .maxResults(maxResults)
                .list();
    }

    /**
     * Retrieves an activity by division and activity number.
     *
     * <p>The activity's fund and group associations required during validation
     * and generic activity resolution are included in the fetch plan.
     *
     * @param division       the division containing the activity
     * @param activityNumber the activity number
     * @return the matching activity, or {@code null} if no match exists
     */
    Activity fetchActivity(Division division, String activityNumber) {
        return unconstrainedDataManager.load(Activity.class)
                .query("SELECT a FROM fis_Activity a"
                        + " WHERE a.division = :division AND a.activityNumber = :activityNumber")
                .parameter("division", division)
                .parameter("activityNumber", activityNumber)
                .fetchPlan(fp -> {
                    fp.addFetchPlan(FetchPlan.BASE);
                    fp.add("fund", fpb -> fpb.addFetchPlan(FetchPlan.BASE));
                    fp.add("group", fpb -> fpb.addFetchPlan(FetchPlan.BASE));
                })
                .optional().orElse(null);
    }

    /**
     * Retrieves the generic activity associated with an activity.
     *
     * <p>The generic activity number is derived by appending {@code 00} to the
     * activity's group code.
     *
     * @param activity the activity whose generic activity is retrieved
     * @return the corresponding generic activity, or {@code null} if the
     * activity has no group or no matching generic activity exists
     */
    Activity fetchGenericActivity(Activity activity) {
        return activity.getGroup() == null ? null : fetchActivity(activity.getDivision(),
                activity.getGroup().getGroupCode().concat("00"));
    }

    /**
     * Determines whether a matching rejection audit already exists for a
     * zero-amount document.
     *
     * <p>Rejection matching is not performed for documents with a nonzero
     * amount.
     *
     * @param document the document to check
     * @return {@code true} if the document has a zero amount and a matching
     * rejected {@link DocumentAudit} exists; otherwise {@code false}
     */
    boolean doesRejectionExist(Document document) {
        // Rejection matching only applies to zero-amount documents;
        if (document.getAmount().signum() != 0) {
            return false;
        }
        return unconstrainedDataManager.load(DocumentAudit.class)
                .query("SELECT e FROM fis_DocumentAudit e"
                        + " WHERE e.processStatus = 'R'"
                        + " AND e.documentFundCode = :fundCode"
                        + " AND e.documentBbfy = :bbfy"
                        + " AND e.documentBudgetOrg = :budgetOrg"
                        + " AND e.documentDocumentType = :documentType"
                        + " AND e.documentDocumentNumber = :documentNumber"
                        + " AND e.documentLineNumber = :lineNumber"
                        + " AND e.documentBudgetObjectClass = :budgetObjectClass"
                        + " AND e.documentAmount = 0")
                .parameter("fundCode", document.getFundCode())
                .parameter("bbfy", document.getBbfy())
                .parameter("budgetOrg", document.getBudgetOrg())
                .parameter("documentType", document.getDocumentType())
                .parameter("documentNumber", document.getDocumentNumber())
                .parameter("lineNumber", document.getLineNumber())
                .parameter("budgetObjectClass", document.getBudgetObjectClass())
                .maxResults(1)
                .optional()
                .isPresent();
    }

    /**
     * Determines whether another line of an obligation uses the same budget
     * object class.
     *
     * @param documentNumber    the obligation document number
     * @param budgetObjectClass the budget object class code
     * @param lineNumber        the line number to exclude from the search
     * @return {@code true} if another line with the same document number and
     * budget object class exists; otherwise {@code false}
     */
    Boolean duplicateBocExists(String documentNumber, String budgetObjectClass, Integer lineNumber) {
        return unconstrainedDataManager.loadValue(
                        "SELECT e.id FROM fis_Obligation e"
                                + " WHERE e.documentNumber = :documentNumber"
                                + " AND e.objectClass.budgetObjectClass = :budgetObjectClass"
                                + " AND e.lineNumber <> :lineNumber", UUID.class)
                .parameter("documentNumber", documentNumber)
                .parameter("budgetObjectClass", budgetObjectClass)
                .parameter("lineNumber", lineNumber)
                .optional()
                .isPresent();
    }

    /**
     * Retrieves an obligation by division, document number, and line number.
     *
     * @param division       the division associated with the obligation's activity
     * @param documentNumber the obligation document number
     * @param lineNumber     the obligation line number
     * @return the matching obligation, or {@code null} if no match exists
     */
    Obligation fetchObligation(Division division, String documentNumber, Integer lineNumber) {
        return unconstrainedDataManager.load(Obligation.class)
                .query("SELECT o FROM fis_Obligation o "
                        + " WHERE o.activity.division = :division"
                        + " AND o.documentNumber = :documentNumber"
                        + " AND o.lineNumber = :lineNumber")
                .parameter("division", division)
                .parameter("documentNumber", documentNumber)
                .parameter("lineNumber", lineNumber)
                .fetchPlan(fpb -> fpb.addFetchPlan(FetchPlan.BASE)
                        .add("activity", ab -> ab.addFetchPlan(FetchPlan.BASE)))
                .optional()
                .orElse(null);
    }

    /**
     * Creates an unsaved obligation from a resolved JIFMS document.
     *
     * <p>The obligation is initialized with the resolved activity and object
     * class and with the applicable document, vendor, travel, cost
     * organization, and synchronization values. Its document type is derived
     * from the imported document type, and its status is open when the JIFMS
     * document has no closed date.
     *
     * @param ctx the resolved context containing the document and related FIS
     *            entities
     * @return the newly created, unsaved obligation
     */
    Obligation createObligationFrom(ResolvedContext ctx) {

        var newObligation = unconstrainedDataManager.create(Obligation.class);
        newObligation.setActivity(ctx.getActivity());
        newObligation.setObjectClass(ctx.getObjectClass());

        var document = ctx.getDocument();
        newObligation.setDocumentNumber(document.getDocumentNumber());
        newObligation.setLineNumber(document.getLineNumber());
        newObligation.setAmount(document.getAmount());
        newObligation.setDocumentDate(document.getDocumentDate());
        newObligation.setProcessDate(document.getDocumentCreationDate().toLocalDate());
        if (document.getFjc() != null) {
            newObligation.setBlanketPurchaseOrder(document.getFjc().equalsIgnoreCase("bpo"));
        }
        if (document.getDocumentType().toLowerCase().startsWith("mo")) {
            newObligation.setDocumentType(DocumentType.MISCELLANEOUS_OBLIGATION);
        } else {
            newObligation.setDocumentType(DocumentType.TRAVEL_AUTHORIZATION);
            newObligation.setTravelStartDate(document.getTravelStartDate());
            newObligation.setTravelEndDate(document.getTravelEndDate());
        }
        newObligation.setEin(document.getTaxId());
        newObligation.setVendor(document.getTitle());
        newObligation.setVendorCode(document.getVendorCode());
        newObligation.setAddressCode(document.getAddressCode());

        newObligation.setBudgetOrg(document.getBudgetOrg());
        newObligation.setCostOrg(ctx.getProjectionActivity().getCostOrg());

        newObligation.setAoSend(false);
        newObligation.setAoSyncDate(LocalDate.now());
        newObligation.setStatus(document.getClosedDate() == null);

        return newObligation;
    }

    /**
     * Saves an obligation without reloading it from the data store.
     *
     * @param obligation the obligation to save
     */
    void saveObligation(Obligation obligation) {
        unconstrainedDataManager.saveWithoutReload(obligation);
    }

    /**
     * Creates and saves a fund control notice for the obligation in the
     * resolved context.
     *
     * <p>The fund control notice records the obligation amount difference
     * and copies the applicable budget organization and EIN from the
     * obligation. Its fund control notice date and synchronization date
     * are set to the current date.
     *
     * @param ctx the resolved context containing the obligation and amount
     *            difference
     */
    void createFcnFrom(ResolvedContext ctx) {
        var obligation = ctx.getObligation();
        var today = LocalDate.now();

        var newFcn = unconstrainedDataManager.create(FundControlNotice.class);
        newFcn.setAmount(ctx.getObligationAmountDifference());
        newFcn.setObligation(obligation);
        newFcn.setBudgetOrg(obligation.getBudgetOrg());
        newFcn.setAoSyncDate(today);
        newFcn.setEin(obligation.getEin());
        newFcn.setFcnDate(today);
        // should we track date changes?
//        newFcn.setTravelStartDate(obligation.getTravelStartDate());
//        newFcn.setTravelEndDate(obligation.getTravelEndDate());
        unconstrainedDataManager.saveWithoutReload(newFcn);
    }

    /**
     * Retrieves the projection for an activity and object class.
     *
     * @param activity    the activity associated with the projection
     * @param objectClass the object class associated with the projection
     * @return the matching projection, or {@code null} if no match exists
     */
    ActivityProjection fetchActivityProjection(Activity activity, ObjectClass objectClass) {
        return unconstrainedDataManager.load(ActivityProjection.class)
                .query("SELECT p FROM fis_ActivityProjection p"
                        + " WHERE p.activity = :activity and p.objectClass = :objectClass")
                .parameter("activity", activity)
                .parameter("objectClass", objectClass)
                .optional().orElse(null);
    }

    /**
     * Saves an activity projection without reloading it from the data store.
     *
     * @param projection the activity projection to save
     */
    void saveActivityProjection(ActivityProjection projection) {
        unconstrainedDataManager.saveWithoutReload(projection);
    }

    /**
     * Creates and saves an activity projection.
     *
     * @param activity    the activity associated with the projection
     * @param objectClass the object class associated with the projection
     * @param amount      the initial projection amount
     */
    void createActivityProjection(Activity activity, ObjectClass objectClass, BigDecimal amount) {
        var newProjection = unconstrainedDataManager.create(ActivityProjection.class);
        newProjection.setActivity(activity);
        newProjection.setObjectClass(objectClass);
        newProjection.setAmount(amount);
        unconstrainedDataManager.saveWithoutReload(newProjection);
    }

    /**
     * Retrieves the allocation for a division and object category.
     *
     * @param division       the division associated with the allocation
     * @param objectCategory the object category associated with the allocation
     * @return the matching allocation, or an empty optional if no match exists
     */
    Optional<DivisionAllocation> fetchAllocation(Division division, ObjectCategory objectCategory) {
        return unconstrainedDataManager.load(DivisionAllocation.class)
                .query("SELECT a FROM fis_DivisionAllocation a"
                        + " WHERE a.division = :division"
                        + " AND a.objectCategory = :objectCategory")
                .parameter("division", division)
                .parameter("objectCategory", objectCategory)
                .optional();
    }

    /**
     * Creates and saves a division allocation with zero one-year and two-year
     * amounts.
     *
     * @param division       the division associated with the allocation
     * @param objectCategory the object category associated with the allocation
     */
    void createDivisionAllocation(Division division, ObjectCategory objectCategory) {
        var newAllocation = unconstrainedDataManager.create(DivisionAllocation.class);
        newAllocation.setDivision(division);
        newAllocation.setObjectCategory(objectCategory);
        newAllocation.setOneYearAmount(BigDecimal.ZERO);
        newAllocation.setTwoYearAmount(BigDecimal.ZERO);
        unconstrainedDataManager.saveWithoutReload(newAllocation);
    }

    /**
     * Retrieves a vendor by vendor code and address code.
     *
     * @param vendorCode  the vendor code
     * @param addressCode the vendor address code
     * @return the matching vendor, or an empty optional if no match exists
     */
    Optional<Vendor> fetchVendor(String vendorCode, String addressCode) {
        return unconstrainedDataManager.load(Vendor.class)
                .query("SELECT v FROM fis_Vendor v"
                        + " WHERE v.vendorCode = :vendorCode"
                        + " AND v.addressCode = :addressCode")
                .parameter("vendorCode", vendorCode)
                .parameter("addressCode", addressCode)
                .optional();
    }

    /**
     * Creates and saves an active vendor from an imported JIFMS document.
     *
     * <p>The document's tax ID is assigned as the vendor's EIN unless the tax
     * ID is {@code null} or consists entirely of uppercase {@code X}
     * characters.
     *
     * @param document the document containing the vendor information
     */
    void createVendor(Document document) {
        var newVendor = unconstrainedDataManager.create(Vendor.class);
        newVendor.setName(document.getVendorName());
        newVendor.setVendorCode(document.getVendorCode());
        newVendor.setAddressCode(document.getAddressCode());
        String taxId = document.getTaxId();
        if (taxId != null && !taxId.matches("X+")) {
            newVendor.setEin(taxId);
        }
        newVendor.setActive(true);
        unconstrainedDataManager.saveWithoutReload(newVendor);
    }
}