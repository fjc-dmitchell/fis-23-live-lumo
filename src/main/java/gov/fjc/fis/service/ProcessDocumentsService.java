package gov.fjc.fis.service;

import gov.fjc.fis.entity.*;
import io.jmix.core.FetchPlans;
import io.jmix.core.Metadata;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to process JIFMS Documents in FIS 2.1. This will be triggered by cron job.
 * This self-contained service uses EntityManager, not DataManager. It does not rely on
 * other services, so certain codes (e.g., Education division, Two year fund) are hardcoded.
 * <p>
 * The business rules were specified by Mary Greiner and Nanticha Sansung
 * when JIFMS feeds went into production in September 2019.
 * <p>
 * This first draft is ugly.
 *
 * @author Doug Mitchell
 * @version 2.1
 * @since 2.1
 */
@Component("fis_ProcessDocumentsService")
public class ProcessDocumentsService {
    private static final Logger log = LoggerFactory.getLogger(ProcessDocumentsService.class);
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private Metadata metadata;
    @Autowired
    private FetchPlans fetchPlans;

    private final String educationDivisionCode = "2";
    private final String twoYearFundCode = "09280M";
    private final String obbbaBudgetOrg = "JXXMAPP";
    private final List<String> travelDocumentTypes = List.of("TA", "TAJ", "JTA");
    private final List<String> purchaseDocumentTypes = List.of("MO", "MOJ");
    private final ZoneId timeZoneId = ZoneId.of("America/New_York");
//    private final Date today = new Date(); // this is an issue for service bean - will never change!
    private final LocalDate today = LocalDate.now(); // this is an issue for service bean - will never change!
    private final String processingUser = "JIFMS-FIS processing";
    private Audit audit; // cannot do this in service bean!


    enum auditState {
        REJECT, UPDATE, INSERT, IGNORE
    }

    /**
     * Process JIFMS Documents that have already been loaded and scrubbed (i.e., documents since FY2020,
     * JITF fund documents belonging to FJC budget org JXXXXXF, etc.)
     * <p>
     * This method will be triggered by Cron or Quartz job schedule.
     */
    @Transactional
    public void processDocuments() {

        // retain map of funds and list of open appropriations for duration of job
        var fundMap = getFundMap();
        var twoYearFund = getTwoYearFund();
        var appropriations = getOpenAppropriations();

//        Fund fund;
//        Division division;
//        Activity activity;
//        Activity genericActivity;
//        ActivityProjection activityProjection;
//        Category category;
//        ObjectClass objectClass;
        Obligation obligation;
        List<Document> documents;

        for (var appropriation : appropriations) {
            List<Division> divisionList = getAllDivisionsWithBudgetOrgs(appropriation);
            Division education = getEducationDivision(appropriation);
            Map<String, ObjectClass> bocMap = getObjectClassMap(appropriation, true);

            var bbfy = appropriation.getBudgetFiscalYear();

            // fetch document entities in small batches to reduce memory overhead
            int offset = 0;
            int max = 100; // process documents in small batches
            while ((documents = getDocuments(bbfy, offset, max)).size() > 0) {

                for (var document : documents) {

//                    division = null;
//                    objectClass = null;
//                    activity = null;
//                    genericActivity = null;

                    audit = new Audit(twoYearFund, education);

                    audit.validateFund(fundMap, document.getFundCode());
                    audit.validateDivision(divisionList, document.getBudgetOrg());
                    audit.validateObjectClass(bocMap, document.getBudgetObjectClass());
                    audit.validateActivity(document.getProject());
                    obligation = audit.validateObligation(document);

                    // update/insert projection
                    // insert zero allocation if necessary
                    // create fcn

                    // create FCN if obligation amount updated
                    // validate projection & insert if necessary

                    // handle condition of duplication obligation BOC - create special audit message

                    if (!audit.auditState.equals(auditState.IGNORE)) {
                        DocumentAudit documentAudit = metadata.create(DocumentAudit.class);
                        documentAudit.setProcessDate(today);
                        setDocumentFields(documentAudit, document);
                        documentAudit.setLoggedChanges(audit.loggedChanges.toString());
                        switch (audit.auditState) {
                            case auditState.REJECT:
                                documentAudit.setProcessStatus("R");
                                break;
                            case auditState.INSERT:
                                documentAudit.setProcessStatus("I");
                                break;
                            case auditState.UPDATE:
                                documentAudit.setProcessStatus("U");
                                setObligationFields(documentAudit, obligation);
                                break;
                        }
                        entityManager.persist(documentAudit);
//                        System.out.println("Audit State: " + audit.auditState);
                    }

                }
//                entityManager.flush();
//                entityManager.clear();
                offset += documents.size();
            }
//            entityManager.flush();
//            entityManager.clear();
        }
    }

    class Audit {
        private Fund twoYearFund;
        private Division educationDivision;
        private Fund fund;
        private Division division;
        private Activity activity;
        private Activity genericActivity;
        private ObjectClass objectClass;
        private Obligation obligation;
        private ActivityProjection projection;

        static private String priorBbfy = "";
        static private String priorBudgetOrg = "";
        static private String priorDocumentNumber = "";
        static private String priorObjectClass = "";

        //        boolean validDocument = true;
        auditState auditState = ProcessDocumentsService.auditState.IGNORE;
        StringBuffer loggedChanges = new StringBuffer();

        public Audit(Fund twoYearFund, Division educationDivision) {
            this.twoYearFund = twoYearFund;
            this.educationDivision = educationDivision;
        }

        Fund validateFund(Map<String, Fund> fundMap, String fundCode) {
            if (!auditState.equals(ProcessDocumentsService.auditState.REJECT)) {
                fund = fundMap.get(fundCode);
                if (fund == null) {
                    auditState = ProcessDocumentsService.auditState.REJECT;
                    loggedChanges.append(String.format("invalid fund: %s.", fundCode));
                }
            }
            return fund;
        }

        Division validateDivision(List<Division> divisionList, String budgetOrg) {
            if (!auditState.equals(ProcessDocumentsService.auditState.REJECT)) {
                List<Division> foundDivisions = divisionList.stream()
                        .filter(d -> d.getBudgetOrg().equals(budgetOrg)
                                && (d.getFund().getFundCode().equals(fund.getFundCode())
                                || (d.equals(educationDivision)
                                && fund.getFundCode().equals(twoYearFund.getFundCode()))))
                        .toList();
                if (foundDivisions.size() != 1) {
                    auditState = ProcessDocumentsService.auditState.REJECT;
                    if (foundDivisions.isEmpty()) {
                        loggedChanges.append(String.format("invalid budgetOrg: %s.", budgetOrg));
                    } else {
                        loggedChanges.append(String.format("multiple divisions matching budgetOrg: %s.", budgetOrg));
                    }
                } else {
                    division = foundDivisions.get(0);
                }
            }
            return division;
        }

        ObjectClass validateObjectClass(Map<String, ObjectClass> objectClassMap, String budgetObjectClass) {
            if (!auditState.equals(ProcessDocumentsService.auditState.REJECT)) {
                objectClass = objectClassMap.get(budgetObjectClass);
                if (objectClass == null) {
                    auditState = ProcessDocumentsService.auditState.REJECT;
                    loggedChanges.append(String.format("invalid objectClass: %s.", budgetObjectClass));
                }
            }
            return objectClass;
        }

        Activity validateActivity(String activityNumber) {
            if (!auditState.equals(ProcessDocumentsService.auditState.REJECT)) {
                activity = getActivity(division, activityNumber);
                if (activity == null) {
                    auditState = ProcessDocumentsService.auditState.REJECT;
                    loggedChanges.append(String.format("invalid activity: %s.", activityNumber));
                } else if (activity.getFund() != fund) {
                    auditState = ProcessDocumentsService.auditState.REJECT;
                    loggedChanges.append(String.format("invalid activity fund: %s.", activity.getFund()));
                }
                if (activity != null && activity.getGroup() != null) {
                    genericActivity = getGenericActivity(division, activity.getGroup());
                }
            }
            return activity;
        }

        Obligation validateObligation(Document document) {
            if (!auditState.equals(ProcessDocumentsService.auditState.REJECT)) {
                if (!validDocumentNumber(division, document)) {
                    auditState = ProcessDocumentsService.auditState.REJECT;
                    loggedChanges.append(String.format("invalid documentNumber: %s.",
                            document.getDocumentNumber()));
                }
            }

            // handle case for duplicate BOC. allowed by JIFMS, not allowed by FIS
            if (!auditState.equals(ProcessDocumentsService.auditState.REJECT)) {
                if (document.getBbfy().equals(priorBbfy)
                        && document.getBudgetOrg().equals(priorBudgetOrg)
                        && document.getDocumentNumber().equals(priorDocumentNumber)
                        && document.getBudgetObjectClass().equals(priorObjectClass)) {
                    auditState = ProcessDocumentsService.auditState.REJECT;
                    loggedChanges.append(String.format("duplicate BOC for documentNumber: %s.",
                            document.getDocumentNumber()));
                    System.out.println(loggedChanges.toString());
                } else {
                    priorBbfy = document.getBbfy();
                    priorBudgetOrg = document.getBudgetOrg();
                    priorDocumentNumber = document.getDocumentNumber();
                    priorObjectClass = document.getBudgetObjectClass();
                }
            }

            // validate Obligation & insert if necessary
            if (!auditState.equals(ProcessDocumentsService.auditState.REJECT)) {
                obligation = getObligation(activity, objectClass, document.getDocumentNumber(), document.getLineNumber());
                if (obligation == null) {
                    createObligation(activity, objectClass, document);
                    auditState = ProcessDocumentsService.auditState.INSERT;
                    loggedChanges.append(String.format("NEW Obligation: %s.", document.getDocumentNumber()));
                } else {
                    var log = updateObligation(activity, objectClass, obligation, document);
                    auditState = ProcessDocumentsService.auditState.UPDATE;
                    loggedChanges.append(String.format("UPDATE Obligation: %s.", document.getDocumentNumber()));
                    // mention each field changed
                }
            }
            return obligation;
        }

        ActivityProjection updateActivityProjection() {

            Activity thisActivity;
            if (genericActivity != null) {
                thisActivity = genericActivity;
            } else {
                thisActivity = activity;
            }
            //find projection for thisactivity, or generic projection
            // if exists, update
            // if doesn't exist, create projection or generic projection
            // persist changes
            return null;
        }

        void validateDivisionAllocation() {
            if (!auditState.equals(ProcessDocumentsService.auditState.REJECT)) {
                ObjectCategory category = objectClass.getObjectCategory();
                var allocation = getDivisionAllocation(division, category);
                if (allocation == null) {
                    DivisionAllocation divisionAllocation = metadata.create(DivisionAllocation.class);
                    divisionAllocation.setDivision(division);
                    divisionAllocation.setObjectCategory(category);
                    divisionAllocation.setOneYearAmount(BigDecimal.ZERO);
                    divisionAllocation.setTwoYearAmount(BigDecimal.ZERO);
                    entityManager.persist(divisionAllocation);
                    loggedChanges.append(String.format(" Created zero allocation for moc %s.",
                            category.getMajorObjectClass()));
                }
            }
        }

    }

    private void createFundControlNotice(Obligation obligation, BigDecimal newAmount) {
        FundControlNotice fundControlNotice = metadata.create(FundControlNotice.class);
        fundControlNotice.setObligation(obligation);
        fundControlNotice.setAmount(newAmount.subtract(obligation.getAmount()));
        fundControlNotice.setFcnDate(LocalDate.now()); // should change this
        fundControlNotice.setVersion(1);
        entityManager.persist(fundControlNotice);
    }

    // should return string message
    private void createObligation(Activity activity, ObjectClass objectClass, Document document) {
        Obligation newObligation = metadata.create(Obligation.class);
        newObligation.setActivity(activity);
        newObligation.setObjectClass(objectClass);
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

        // is it possible to set CreatedBy and CreatedDate?
//        newObligation.setCreatedBy(document.getCreatedBy());
//        newObligation.setCreatedDate(document.getDocumentCreationDate().toInstant().atZone(timeZoneId).toOffsetDateTime());

        // what is the purpose of these on insert?
        newObligation.setLastModifiedBy(document.getLastModifiedBy());
        newObligation.setLastModifiedDate(OffsetDateTime.now());

        // what about budget org and cost org fields? Should cost org warn about activity?

        newObligation.setAoSend(false);
        newObligation.setAoSyncDate(today);
        newObligation.setStatus(document.getClosedDate() == null);
        newObligation.setVersion(1); // will updates automatically increment or do it manually?
        entityManager.persist(newObligation);
    }

    public String updateObligation(Activity activity, ObjectClass objectClass, Obligation obligation, Document document) {
        String changes = "";
        if (obligation.getAmount() != document.getAmount()) {
            createFundControlNotice(obligation, document.getAmount());
            obligation.setAmount(document.getAmount());
            changes += " -amount";
        }
        if (obligation.getVendor() != document.getTitle()) {
            obligation.setVendor(document.getTitle());
            changes += " -title/vendor";
        }
        // what about fetch plan? add to obligation fetch?
        if (obligation.getActivity().getActivityNumber() != document.getProject()) {
            obligation.setActivity(activity);
            changes += " -activity/project";
        }
        if (obligation.getTravelStartDate() != document.getTravelStartDate()) {
            obligation.setTravelStartDate(document.getTravelStartDate());
            changes += " -travel/start_date";
        }
        if (obligation.getTravelEndDate() != document.getTravelEndDate()) {
            obligation.setTravelEndDate(document.getTravelEndDate());
            changes += " -travel/end_date";
        }
        // rule from September 2019, if FIS closed and JIFMS open, keep closed. Why?
        var closed = document.getClosedDate() != null;
        if (closed && !obligation.getStatus()) {
            obligation.setStatus(false);
            changes += " -status";
        }
        // what about fetch plan? add to obligation fetch?
        if (obligation.getObjectClass().getBudgetObjectClass() != document.getBudgetObjectClass()) {
            obligation.setObjectClass(objectClass);
            changes += " -BOC";
        }
        if (obligation.getVendorCode() != document.getVendorCode()) {
            obligation.setVendorCode(document.getVendorCode());
            changes += " -vendor code";
        }

        // update amounts in audit state, to be used by projection/fcn
        // update create-FCN flag in audit state
        // update obligation boc change - affects projections
        // update activity change - affects projections

        return null;
    }

    private void setDocumentFields(DocumentAudit documentAudit, Document document) {
        documentAudit.setDocumentFundCode(document.getFundCode());
        documentAudit.setDocumentBbfy(document.getBbfy());
        documentAudit.setDocumentEbfy(document.getEbfy());
        documentAudit.setDocumentBudgetOrg(document.getBudgetOrg());
        documentAudit.setDocumentCostOrg(document.getCostOrg());
        documentAudit.setDocumentDocumentType(document.getDocumentType());
        documentAudit.setDocumentDocumentNumber(document.getDocumentNumber());
        documentAudit.setDocumentDocumentDate(document.getDocumentDate());
        documentAudit.setDocumentDocumentCreationDate(document.getDocumentCreationDate().toLocalDate());
        documentAudit.setDocumentTitle(document.getTitle());
        documentAudit.setDocumentBudgetObjectClass(document.getBudgetObjectClass());
        documentAudit.setDocumentProject(document.getProject());
        documentAudit.setDocumentAmount(document.getAmount());
        documentAudit.setDocumentLineNumber(document.getLineNumber());
        documentAudit.setDocumentTaxId(document.getTaxId());
        documentAudit.setDocumentTaxIdType(document.getTaxIdType());
        documentAudit.setDocumentAddressCode(document.getAddressCode());
        documentAudit.setDocumentVendorCode(document.getVendorCode());
        documentAudit.setDocumentVendorName(document.getVendorName());
        documentAudit.setDocumentTravelStartDate(document.getTravelStartDate());
        documentAudit.setDocumentTravelEndDate(document.getTravelEndDate());
        documentAudit.setDocumentExpendedAmount(document.getExpendedAmount());
        documentAudit.setDocumentClosedAmount(document.getClosedAmount());
        documentAudit.setDocumentClosedDate(document.getClosedDate());
        documentAudit.setDocumentLastModifiedBy(document.getLastModifiedBy());
        documentAudit.setDocumentMajorObjectClass(document.getMajorObjectClass());
        documentAudit.setDocumentFjc(document.getFjc());
    }

    private void setObligationFields(DocumentAudit documentAudit, Obligation obligation) {
        documentAudit.setObligationDocumentNumber(obligation.getDocumentNumber());
//        documentAudit.setObligationDocumentType(obligation.getDocumentType());
        documentAudit.setObligationAmount(obligation.getAmount());
        documentAudit.setObligationDocumentDate(obligation.getDocumentDate());
        documentAudit.setObligationProcessDate(obligation.getProcessDate());
        documentAudit.setObligationVendor(obligation.getVendor());
        documentAudit.setObligationStatus(obligation.getStatus());
        documentAudit.setObligationEin(obligation.getEin());
        documentAudit.setObligationTravelStartDate(obligation.getTravelStartDate());
        documentAudit.setObligationTravelEndDate(obligation.getTravelEndDate());
//        documentAudit.setObligationModifiedDate(obligation.getLastModifiedDate());
        documentAudit.setObligationActivityNumber(obligation.getActivity().getActivityNumber());
        documentAudit.setObligationBudgetObjectClass(obligation.getObjectClass().getBudgetObjectClass());
        documentAudit.setObligationDivisionCode(obligation.getActivity().getDivision().getDivisionCode());
        documentAudit.setObligationMajorObjectClass(obligation.getObjectClass().getObjectCategory().getMajorObjectClass());
//       documentAudit.setObligationAddressCode(obligation.getAddressCode());
        documentAudit.setObligationVendorCode(obligation.getVendorCode());

    }

    private void setProjectionFields(DocumentAudit documentAudit, ActivityProjection projection) {
//        documentAudit.setCurrentActivityNumber(projection.getActivity().getActivityNumber());
//        documentAudit.setCurrentProjectionBoc(projection.getObjectClass().getBudgetObjectClass());
//        documentAudit.setCurrentProjectionAmountBefore();
//        documentAudit.setCurrentProjectionAmountAfter();
//        documentAudit.setPreviousActivityNumber(projection.getActivity().getActivityNumber());
//        documentAudit.setPreviousProjectionBoc(projection.getObjectClass().getCategory().getMasterObjectClass());
//        documentAudit.setPreviousProjectionBoc(projection);
//        documentAudit.setPreviousProjectionAmountBefore();
//        documentAudit.setPreviousProjectionAmountAfter();
    }

    /**
     * logic agreed upon by OFM in 2019. Document numbers to be in form FJC20-81000.
     *
     * @param division
     * @param document
     * @return true if valid document number
     */
    private boolean validDocumentNumber(Division division, Document document) {
        var documentNumber = document.getDocumentNumber();
        return documentNumber != null && documentNumber.startsWith("FJC")
                && documentNumber.substring(3, 4).equals(document.getBbfy().substring(2, 3))
                && documentNumber.substring(5, 6).equals("-")
                && (document.getBudgetOrg().equals(obbbaBudgetOrg)
                || documentNumber.substring(7, 8).equals(division.getDivisionCode()))
                && ((documentNumber.substring(6, 7).equals("7") && travelDocumentTypes.contains(document.getDocumentType()))
                || (documentNumber.substring(6, 7).equals("8") && purchaseDocumentTypes.contains(document.getDocumentType())));
    }

    /**
     * get Documents in specific order to ensure there are no duplicate BOCs for an obligation.
     * JIFMS allows duplicates; FIS does not! Each line represents a difference BOC.
     *
     * @param bbfy   String representing beginning budget fiscal year
     * @param offset starting point for retrieval
     * @param max    maximum number of Documents to be retrieved in batch
     * @return batched List of Documents for a bbfy
     */
    private List<Document> getDocuments(String bbfy, int offset, int max) {
        return entityManager.createQuery("SELECT d FROM fis_Document d"
                        + " WHERE d.bbfy = :bbfy AND NOT EXISTS (SELECT e FROM fis_DocumentException e"
                        + " WHERE e.bbfy = d.bbfy AND e.fundCode = d.fundCode AND e.budgetOrg = d.budgetOrg"
                        + " AND e.budgetObjectClass= d.budgetObjectClass AND e.documentNumber = d.documentNumber)"
                        + " ORDER BY d.bbfy, d.budgetOrg, d.documentNumber, d.budgetObjectClass")
                .setParameter("bbfy", bbfy)
                .setFirstResult(offset)
                .setMaxResults(max)
                .getResultList();
    }

    private Map<String, Fund> getFundMap() {
        return entityManager.createQuery("SELECT f FROM fis_Fund f ORDER BY f.fundCode", Fund.class)
                .getResultStream().collect(Collectors.toMap(Fund::getFundCode, fund -> fund));
    }

    private Fund getTwoYearFund() {
        TypedQuery<Fund> query = entityManager.createQuery("SELECT e FROM fis_Fund e" +
                " WHERE e.fundCode = :twoYearFundCode", Fund.class);
        query.setParameter("twoYearFundCode", twoYearFundCode);
        List<Fund> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    private List<Appropriation> getOpenAppropriations() {
        return entityManager.createQuery("SELECT a FROM fis_Appropriation a"
                        + " WHERE a.status = TRUE ORDER BY a.budgetFiscalYear DESC")
                .getResultList();
    }

    private List<Division> getAllDivisionsWithBudgetOrgs(Appropriation appropriation) {
        return entityManager.createQuery("SELECT d FROM fis_Division d"
                        + " JOIN FETCH d.fund JOIN FETCH d.appropriation WHERE d.appropriation = :appropriation"
                        + " AND d.budgetOrg IS NOT NULL AND d.budgetOrg <> ''"
                        + " ORDER BY d.divisionCode")
                .setParameter("appropriation", appropriation)
                .getResultList();
    }

    private Division getEducationDivision(Appropriation appropriation) {
        TypedQuery<Division> query = entityManager.createQuery("SELECT e FROM fis_Division e"
                + " JOIN FETCH e.fund WHERE e.appropriation = :appropriation"
                + " AND e.divisionCode = :educationDivisionCode", Division.class);
        query.setParameter("appropriation", appropriation);
        query.setParameter("educationDivisionCode", educationDivisionCode);
        List<Division> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    private DivisionAllocation getDivisionAllocation(Division division, ObjectCategory category) {
        TypedQuery<DivisionAllocation> query = entityManager.createQuery("SELECT e FROM fis_DivisionAllocation e"
                + " WHERE e.division = :division AND e.objectCategory = :category", DivisionAllocation.class);
        query.setParameter("division", division);
        query.setParameter("category", category);
        List<DivisionAllocation> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public Map<String, ObjectClass> getObjectClassMap(Appropriation appropriation, boolean includeGenerics) {
        return entityManager.createQuery("SELECT o FROM fis_ObjectClass o"
                                + " INNER JOIN fis_ObjectCategory c ON o.objectCategory = c"
                                + " WHERE c.appropriation = :appropriation"
                                + " AND (:generic = true OR o.budgetObjectClass NOT LIKE '%00')"
                                + " ORDER BY o.budgetObjectClass",
                        ObjectClass.class)

                .setParameter("appropriation", appropriation)
                .setParameter("generic", includeGenerics)
                .getResultStream()
                .collect(Collectors.toMap(ObjectClass::getBudgetObjectClass, objectClass -> objectClass));
    }

    private Activity getActivity(Division division, String activityNumber) {
        TypedQuery<Activity> query = entityManager.createQuery("SELECT a FROM fis_Activity a"
                + " INNER JOIN fis_Division d ON d=a.division"
                + " JOIN FETCH a.fund JOIN FETCH a.group WHERE a.division = :division"
                + " AND a.activityNumber = :activityNumber", Activity.class);
        query.setParameter("division", division);
        query.setParameter("activityNumber", activityNumber);
        List<Activity> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    private Activity getGenericActivity(Division division, Group group) {
        var activityNumber = group == null ? "" : group.getGroupCode().concat("00");
        TypedQuery<Activity> query = entityManager.createQuery("SELECT a FROM fis_Activity a"
                + " INNER JOIN fis_Division d ON d=a.division"
                + " JOIN FETCH a.group JOIN FETCH a.fund"
                + " WHERE a.division = :division"
                + " AND a.group = :group AND a.activityNumber = :activityNumber", Activity.class);
        query.setParameter("division", division);
        query.setParameter("group", group);
        query.setParameter("activityNumber", activityNumber);
        List<Activity> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    private Obligation getObligation(Activity activity, ObjectClass objectClass, String documentNumber, Integer lineNumber) {
        TypedQuery<Obligation> query = entityManager.createQuery("SELECT o FROM fis_Obligation o"
                        + " INNER JOIN fis_Activity a ON a=o.activity"
                        + " WHERE o.activity = :activity AND o.objectClass = :objectClass"
                        + " AND o.documentNumber = :documentNumber AND o.lineNumber = :lineNumber",
                Obligation.class);
        query.setParameter("activity", activity);
        query.setParameter("objectClass", objectClass);
        query.setParameter("documentNumber", documentNumber);
        query.setParameter("lineNumber", lineNumber);
        List<Obligation> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}