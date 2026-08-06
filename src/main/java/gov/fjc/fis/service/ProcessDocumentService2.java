package gov.fjc.fis.service;

import gov.fjc.fis.entity.*;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("fis_ProcessDocumentService2")
public class ProcessDocumentService2 {
    private final UnconstrainedDataManager unconstrainedDataManager;
    private final String twoYearFundCode = "09280M";

    private final String educationDivisionCode = "2";
    private final String obbbaBudgetOrg = "JXXMAPP";
    private final List<String> travelDocumentTypes = List.of("TA", "TAJ", "JTA");
    private final List<String> purchaseDocumentTypes = List.of("MO", "MOJ");
    private final ZoneId timeZoneId = ZoneId.of("America/New_York");
    private final Date today = new Date(); // NO - cannot do this in service bean!
    private final String processingUser = "JIFMS-FIS processing";

    public ProcessDocumentService2(UnconstrainedDataManager unconstrainedDataManager) {
        this.unconstrainedDataManager = unconstrainedDataManager;
    }

    enum AuditState {
        REJECT, UPDATE, INSERT, IGNORE
    }

    public void processDocuments() {
        var fundMap = getFundMap();
        var twoYearFund = getTwoYearFund();
        var appropriations = getOpenAppropriations();

        List<Document> documents;

        for (var appropriation : appropriations) {

            List<Division> divisionList = getAllDivisionsWithBudgetOrgs(appropriation);
            Division educationDivision = getEducationDivision(appropriation);
            Map<String, ObjectClass> objectClassMap = getObjectClassMap(appropriation, true);
            var bbfy = appropriation.getBudgetFiscalYear();

            // fetch document entities in small batches to reduce memory overhead
            int offset = 0;
            int max = 100; // process documents in small batches
            while ((documents = getDocuments(bbfy, offset, max)).size() > 0) {
//                System.out.println("bbfy: " + bbfy + " offset: " + offset + " max: " + max + " size: " + documents.size());

                for (var document : documents) {

                    var auditDto = new AuditDto(twoYearFund, educationDivision);

                    auditDto.validateFund(fundMap, document.getFundCode());
                    auditDto.validateDivision(divisionList, document.getBudgetOrg());
                    auditDto.validateObjectClass(objectClassMap, document.getBudgetObjectClass());
                    auditDto.validateActivity(document.getProject());
                    auditDto.validateObligation(document.getDocumentNumber(), document.getBudgetOrg(), document.getLineNumber());

//                    System.out.println(auditDto.getAuditState().toString() + " project: " + document.getProject());
                    if (!auditDto.getAuditState().equals(AuditState.IGNORE)) {
                        System.out.println(auditDto.getLoggedChanges());
//                        System.out.println(document.getFundCode());
//                        System.out.println(document.getProject());
//                        System.out.println(document.getBudgetOrg());
                    }

                    if (auditDto.getAuditState().equals(AuditState.REJECT)) {
                        saveAuditRecord(auditDto, document);
                        continue;
                    }

                    // transaction goes here
//                    System.out.println(document.getDocumentNumber());
                    processDocument(document);

                }
                offset += documents.size();
            }
        }
    }

    // move this Dto and validation to separate Dto entity
    class AuditDto {
        AuditState auditState = AuditState.IGNORE;
        StringBuffer loggedChanges = new StringBuffer();
        Fund fund;
        Fund twoYearFund;
        Division educationDivision;
        Division division;
        ObjectClass objectClass;
        Activity activity;
        Activity genericActivity;
        Obligation obligation;

        public AuditDto(Fund twoYearFund, Division educationDivision) {
            this.twoYearFund = twoYearFund;
            this.educationDivision = educationDivision;
        }

        public AuditState getAuditState() {
            return auditState;
        }

        // create enum entity to represent these values!
        public String getAuditStateString() {
            return switch (auditState) {
                case AuditState.REJECT -> "R";
                case AuditState.UPDATE -> "U";
                case AuditState.INSERT -> "I";
                case AuditState.IGNORE -> "G";
            };
        }

        public StringBuffer getLoggedChanges() {
            return loggedChanges;
        }

        Fund validateFund(Map<String, Fund> fundMap, String fundCode) {
            if (!auditState.equals(AuditState.REJECT)) {
                fund = fundMap.get(fundCode);
                if (fund == null) {
                    auditState = AuditState.REJECT;
                    loggedChanges.append(String.format("Invalid fund: %s.", fundCode));
                }
            }
            return fund;
        }

        Division validateDivision(List<Division> divisionList, String budgetOrg) {
            if (!auditState.equals(AuditState.REJECT)) {
                List<Division> foundDivisions = divisionList.stream()
                        .filter(d -> d.getBudgetOrg().equals(budgetOrg)
                                && (d.getFund().getFundCode().equals(fund.getFundCode())
                                || (d.equals(educationDivision)
                                && fund.getFundCode().equals(twoYearFund.getFundCode()))))
                        .toList();
                if (foundDivisions.size() != 1) {
                    auditState = AuditState.REJECT;
                    if (foundDivisions.isEmpty()) {
                        loggedChanges.append(String.format("Invalid budgetOrg: %s.", budgetOrg));
                    } else {
                        loggedChanges.append(String.format("Multiple divisions matching budgetOrg: %s.", budgetOrg));
                    }
                } else {
                    division = foundDivisions.getFirst();
                }
            }
            return division;
        }

        ObjectClass validateObjectClass(Map<String, ObjectClass> objectClassMap, String budgetObjectClass) {
            if (!auditState.equals(AuditState.REJECT)) {
                objectClass = objectClassMap.get(budgetObjectClass);
                if (objectClass == null) {
                    auditState = AuditState.REJECT;
                    loggedChanges.append(String.format("Invalid objectClass: %s.", budgetObjectClass));
                }
            }
            return objectClass;
        }

        Activity validateActivity(String activityNumber) {
            if (!auditState.equals(AuditState.REJECT)) {
                activity = getActivity(division, activityNumber);
                if (activity == null) {
                    auditState = AuditState.REJECT;
                    loggedChanges.append(String.format("Invalid activity: %s.", activityNumber));
                } else if (!activity.getFund().equals(fund)) {
                    auditState = AuditState.REJECT;
                    loggedChanges.append(String.format("Invalid activity fund: %s.", activity.getFund().getFundCode()));
                } else {
                    genericActivity = getGenericActivity(activity);
                }
            }
            return activity;
        }

        Obligation validateObligation(String documentNumber, String budgetOrg, int lineNumber) {
            if (!auditState.equals(AuditState.REJECT)) {
                obligation = getObligation(objectClass, documentNumber, null, null);
                if (obligation != null) {
                    if(obligation.getLineNumber() == lineNumber) {
                        if(obligation.getActivity()==activity) {
                            System.out.println("if projection exists, update and log changes");
                            System.out.println("if projection does not exist, create and log");
                        } else {
                            System.out.println("if projection exists for old activity, revert and log");
                            System.out.println("change activity and log");
                            System.out.println("if projection does not exist, create and log");
                        }
                    }

                }
                if (obligation == null) {
                    // create obligation
                    // update projection (if found)
                    // ensure allocation record exists
                } else {
                    if (obligation.getLineNumber() == lineNumber) {
                    }
                }
            }
            return obligation;
        }
    }

    private void saveAuditRecord(AuditDto auditDto, Document document) {
        if (!auditDto.getAuditState().equals(AuditState.IGNORE)) {
            DocumentAudit documentAudit = unconstrainedDataManager.create(DocumentAudit.class);
            documentAudit.setProcessDate(LocalDate.now());
            documentAudit.setProcessStatus(auditDto.getAuditStateString());
            documentAudit.setLoggedChanges(auditDto.getLoggedChanges().toString());
            // possibly set all fields within this method
            setDocumentFields(documentAudit, document);
            unconstrainedDataManager.save(documentAudit);
            System.out.println("Saving audit record: " + auditDto.getLoggedChanges());
        }
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

    @Transactional
    protected void processDocument(Document document) {
        var audit = new audit();

        class localInnerClass {

        }
    }

    class audit {

    }

    private Map<String, Fund> getFundMap() {
        return unconstrainedDataManager.load(Fund.class)
                .query("SELECT f FROM fis_Fund f ORDER BY f.fundCode")
                .list()
                .stream()
                .collect(Collectors.toMap(Fund::getFundCode, fund -> fund));
    }

    private Fund getTwoYearFund() {
        var results = unconstrainedDataManager.load(Fund.class)
                .query("SELECT e FROM fis_Fund e"
                        + " WHERE e.fundCode = :twoYearFundCode")
                .parameter("twoYearFundCode", twoYearFundCode)
                .list();
        return results.isEmpty() ? null : results.getFirst();
    }

    private List<Appropriation> getOpenAppropriations() {
        return unconstrainedDataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a"
                        + " WHERE a.status = TRUE"
                        + " ORDER BY a.budgetFiscalYear DESC")
                .list();
    }

    private List<Division> getAllDivisionsWithBudgetOrgs(Appropriation appropriation) {
        return unconstrainedDataManager.load(Division.class)
                .query("SELECT d FROM fis_Division d"
                        + " JOIN FETCH d.fund JOIN FETCH d.appropriation WHERE d.appropriation = :appropriation"
                        + " AND d.budgetOrg IS NOT NULL AND d.budgetOrg <> ''"
                        + " ORDER BY d.divisionCode")
                .parameter("appropriation", appropriation)
                .list();
    }

    private Division getEducationDivision(Appropriation appropriation) {
        var results = unconstrainedDataManager.load(Division.class)
                .query("SELECT e FROM fis_Division e"
                        + " JOIN FETCH e.fund WHERE e.appropriation = :appropriation"
                        + " AND e.divisionCode = :educationDivisionCode")
                .parameter("appropriation", appropriation)
                .parameter("educationDivisionCode", educationDivisionCode)
                .list();
        return results.isEmpty() ? null : results.getFirst();
    }

    public Map<String, ObjectClass> getObjectClassMap(Appropriation appropriation, boolean includeGenerics) {
        return unconstrainedDataManager.load(ObjectClass.class)
                .query("SELECT o FROM fis_ObjectClass o"
                        + " JOIN FETCH o.category WHERE o.category.appropriation = :appropriation"
                        + " AND (:generic = true OR o.budgetObjectClass NOT LIKE '%00')"
                        + " ORDER BY o.budgetObjectClass")
                .parameter("appropriation", appropriation)
                .parameter("generic", includeGenerics)
                .list()
                .stream()
                .collect(Collectors.toMap(ObjectClass::getBudgetObjectClass, objectClass -> objectClass));
    }

    private Activity getActivity(Division division, String activityNumber) {
        var results = unconstrainedDataManager.load(Activity.class)
                .query("SELECT a FROM fis_Activity a"
                        + " JOIN FETCH a.fund"
                        + " LEFT JOIN FETCH a.group"
                        + " WHERE a.division = :division AND a.activityNumber = :activityNumber")
                .parameter("division", division)
                .parameter("activityNumber", activityNumber)
                .list();
        return results.isEmpty() ? null : results.getFirst();
    }

    private Activity getGenericActivity(Activity activity) {
        return activity.getGroup() == null ? null : getActivity(activity.getDivision(),
                activity.getGroup().getGroupCode().concat("00"));
    }

    private Obligation getObligation(ObjectClass objectClass, String documentNumber,
                                     String budgetOrg, Integer lineNumber) {
        var results = unconstrainedDataManager.load(Obligation.class)
                .query("SELECT o FROM fis_Obligation o"
                        + " JOIN FETCH o.objectClass JOIN FETCH o.activity"
                        + " WHERE o.objectClass = :objectClass AND o.documentNumber=:documentNumber"
                        + " AND (:anyLineNumber=true OR o.lineNumber = :lineNumber)"
                        + " AND (:anyBudgetOrg=true OR o.budgetOrg = :budgetOrg)")
                .parameter("objectClass", objectClass)
                .parameter("anyLineNumber", lineNumber == null)
                .parameter("lineNumber", lineNumber)
                .parameter("anyBudgetOrg", budgetOrg == null)
                .parameter("budgetOrg", budgetOrg)
                .list();
        return results.isEmpty() ? null : results.getFirst();
    }

    /**
     * get Documents in specific order to allow for detection of duplicate obligation BOCs.
     * JIFMS allows duplicates; FIS does not! Each line represents a difference BOC.
     *
     * @param bbfy   String representing beginning budget fiscal year
     * @param offset starting point for retrieval
     * @param max    maximum number of Documents to be retrieved in batch
     * @return batched List of Documents for a bbfy
     */
    private List<Document> getDocuments(String bbfy, int offset, int max) {
        return unconstrainedDataManager.load(Document.class)
                .query("SELECT d FROM fis_Document d"
                        + " WHERE d.bbfy = :bbfy AND NOT EXISTS (SELECT e FROM fis_DocumentException e"
                        + " WHERE e.bbfy = d.bbfy AND e.fundCode = d.fundCode AND e.budgetOrg = d.budgetOrg"
                        + " AND e.budgetObjectClass= d.budgetObjectClass AND e.documentNumber = d.documentNumber)"
                        + " ORDER BY d.bbfy, d.budgetOrg, d.documentNumber, d.budgetObjectClass")
                .parameter("bbfy", bbfy)
                .firstResult(offset)
                .maxResults(max)
                .list();
    }
}