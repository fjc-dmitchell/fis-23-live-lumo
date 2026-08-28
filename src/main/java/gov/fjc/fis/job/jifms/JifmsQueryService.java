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
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("fis_JifmsQueryService")
public class JifmsQueryService {
    private final UnconstrainedDataManager unconstrainedDataManager;

    public JifmsQueryService(UnconstrainedDataManager unconstrainedDataManager) {
        this.unconstrainedDataManager = unconstrainedDataManager;
    }

    Map<String, Fund> fetchFundMap() {
        return unconstrainedDataManager.load(Fund.class)
                .all()
                .list()
                .stream()
                .collect(Collectors.toMap(Fund::getFundCode, Function.identity()));
    }

    Optional<Fund> fetchTwoYearFund(String twoYearFundCode) {
        return unconstrainedDataManager.load(Fund.class)
                .query("SELECT f FROM fis_Fund f WHERE f.fundCode = :twoYearFundCode")
                .parameter("twoYearFundCode", twoYearFundCode)
                .optional();
    }

    List<Appropriation> fetchOpenAppropriations() {
        return unconstrainedDataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a"
                        + " WHERE a.status = TRUE"
                        + " ORDER BY a.budgetFiscalYear ASC")
                .list();
    }

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

    // can we eliminate the generics option here? is it ever used without generics?
    Map<String, ObjectClass> fetchObjectClassMap(Appropriation appropriation, boolean includeGenerics) {
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
     * Retrieves a batch of Documents ordered to allow detection of duplicate obligation BOCs.
     * JIFMS permits duplicate BOCs per document; FIS does not — each line represents a distinct BOC.
     * Documents with a matching {@link DocumentException} are excluded.
     *
     * @param bbfy        beginning budget fiscal year
     * @param firstResult starting index for batch retrieval
     * @param maxResults  maximum number of Documents to retrieve
     * @return batched list of Documents for the given {@code bbfy}
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

    Activity fetchGenericActivity(Activity activity) {
        return activity.getGroup() == null ? null : fetchActivity(activity.getDivision(),
                activity.getGroup().getGroupCode().concat("00"));
    }

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

    public Obligation createObligationFrom(ResolvedContext ctx) {

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

    void saveObligation(Obligation obligation) {
        unconstrainedDataManager.saveWithoutReload(obligation);
    }

    public void createFcnFrom(ResolvedContext ctx) {
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

    ActivityProjection fetchActivityProjection(Activity activity, ObjectClass objectClass) {
        return unconstrainedDataManager.load(ActivityProjection.class)
                .query("SELECT p FROM fis_ActivityProjection p"
                        + " WHERE p.activity = :activity and p.objectClass = :objectClass")
                .parameter("activity", activity)
                .parameter("objectClass", objectClass)
                .optional().orElse(null);
    }

    void saveActivityProjection(ActivityProjection projection) {
        unconstrainedDataManager.saveWithoutReload(projection);
    }

    void createActivityProjection(Activity activity, ObjectClass objectClass, BigDecimal amount) {
        var newProjection = unconstrainedDataManager.create(ActivityProjection.class);
        newProjection.setActivity(activity);
        newProjection.setObjectClass(objectClass);
        newProjection.setAmount(amount);
        unconstrainedDataManager.saveWithoutReload(newProjection);
    }

    Optional<DivisionAllocation> fetchAllocation(Division division, ObjectCategory objectCategory) {
        return unconstrainedDataManager.load(DivisionAllocation.class)
                .query("SELECT a FROM fis_DivisionAllocation a"
                        + " WHERE a.division = :division"
                        + " AND a.objectCategory = :objectCategory")
                .parameter("division", division)
                .parameter("objectCategory", objectCategory)
                .optional();
    }

    void createDivisionAllocation(Division division, ObjectCategory objectCategory) {
        var newAllocation = unconstrainedDataManager.create(DivisionAllocation.class);
        newAllocation.setDivision(division);
        newAllocation.setObjectCategory(objectCategory);
        newAllocation.setOneYearAmount(BigDecimal.ZERO);
        newAllocation.setTwoYearAmount(BigDecimal.ZERO);
        unconstrainedDataManager.saveWithoutReload(newAllocation);
    }

    Optional<Vendor> fetchVendor(String vendorCode, String addressCode) {
        return unconstrainedDataManager.load(Vendor.class)
                .query("SELECT v FROM fis_Vendor v"
                        + " WHERE v.vendorCode = :vendorCode"
                        + " AND v.addressCode = :addressCode")
                .parameter("vendorCode", vendorCode)
                .parameter("addressCode", addressCode)
                .optional();
    }

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