package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.DocumentType;
import gov.fjc.fis.entity.dto.ActivityDto;
import gov.fjc.fis.entity.dto.ObjectClassDto;
import gov.fjc.fis.reportdata.EducationBranchReportData;
import gov.fjc.fis.service.*;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component("fis_EducationBranchReportService")
public class EducationBranchReportService {
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityProjectionService activityProjectionService;
    @Autowired
    private ActivityReimbursementService activityReimbursementService;
    @Autowired
    private ObligationService obligationService;

    public EducationBranchReportData generateReportData(Branch branch) {
        var division = branch.getDivision();

        var appropriation = division.getAppropriation();
        var priorYearAppropriation = appropriationService.getPreviousFiscalYear(appropriation);

        var activities = activityService.getBiFiscalActivityDtos(division, branch);

        activities.sort(
                Comparator.comparing(ActivityDto::getGroupSortCode)
                        .thenComparing(ActivityDto::getGroupCode)
                        .thenComparing(ActivityDto::getStartDate, Comparator.nullsFirst(Comparator.naturalOrder()))
        );
//                obligations.sort(Comparator.comparing((KeyValueEntity o) -> (Date) o.getValue("enddate"),
//                        Comparator.nullsFirst(Comparator.naturalOrder()))
//                .thenComparing(o -> o.getValue("actnum"))
//                .thenComparing(o -> o.getValue("docid"))
//                .thenComparingInt(o -> o.getValue("lineno")));
//        order by sortcode, generic, groupid, startdate, actnum, objc


//                        .properties("id", "fundId", "fundCode", "appropriationId", "budgetFiscalYear", "divisionId",
//                "divisionCode", "costOrg", "activityNumber", "title", "startDate", "endDate", "city",
//                "state", "branchId", "branchCode", "branchTitle", "groupId", "groupCode", "groupTitle",
//                "grpSortCode", "initialProjection", "fundingType")

        var obligations = obligationService.getObligationDtos(appropriation, activities);
        var projections = activityProjectionService.getProjectionDtos(appropriation, activities, false);
        var reimbursements = activityReimbursementService.getReimbursementDtos(appropriation, activities);
        activityService.updateActivityAmounts(activities, obligations, projections, reimbursements);

        var reportData = new EducationBranchReportData(branch, activities);
        reportData.setPriorBudgetFiscalYear(priorYearAppropriation.getBudgetFiscalYear());
        reportData.setActivities(activities);

        for (ActivityDto activityDto : activities) {
            getObjectClassDtos(activityDto);
        }

        return reportData;
    }

    @Autowired
    private DataManager dataManager;

    public List<ObjectClassDto> getObjectClassDtos(ActivityDto activity) {

        var objectClasses = dataManager.loadValues(
                        "SELECT obj.budgetObjectClass, 0, 0, 0, '',"
                                + " CASE WHEN obj.budgetObjectClass = '2359' THEN 'Rental Costs'"
                                + " WHEN obj.budgetObjectClass = '2529' THEN 'Consultant Fees'"
                                + " WHEN obj.budgetObjectClass = '2535' THEN 'Temporary Help'"
                                + " WHEN obj.budgetObjectClass = '2543' THEN 'Tuition/Educational Services/ On-line Assessments'"
                                + " WHEN obj.budgetObjectClass = '2601' THEN 'Light refreshments/Off. Supp. & Materials'"
                                + " ELSE obj.title"
                                + " END"
                                + " FROM fis_ObjectClass obj"
                                + " WHERE obj.budgetObjectClass NOT IN ('2120','2125')"
                                + " AND EXISTS (SELECT e FROM fis_ActivityProjection e WHERE e.activity.id = :activity AND e.objectClass = obj AND e.amount > 0)"
                                + " OR EXISTS (SELECT e FROM fis_Obligation e WHERE e.activity.id = :activity AND e.objectClass = obj AND e.amount > 0)")
                .parameter("activity", activity.getId())
                .properties("boc", "projected", "obligated", "disbursed", "doctype", "boctitle")
                .list();

        var projections = dataManager.loadValues(
                        "SELECT obj.budgetObjectClass, 0, 0, 0, '', CONCAT('Projection for ', obj.title)"
                                + " FROM fis_ObjectClass obj"
                                + " WHERE obj.budgetObjectClass IN ('2120','2125')"
                                + " AND EXISTS (SELECT e FROM fis_ActivityProjection e WHERE e.activity.id = :activity AND e.objectClass = obj)")//" AND e.amount > 0)")
                .parameter("activity", activity.getId())
                .properties("boc", "projected", "obligated", "disbursed", "doctype", "boctitle")
                .list();

        var obligations = dataManager.loadValues(
                        "SELECT DISTINCT obj.budgetObjectClass, 0, 0, 0, o.documentType,"
                                + " CASE WHEN o.documentType = :travel THEN 'Travel Authorization'"
                                + " WHEN o.documentType = :purchase THEN 'P.O.s for travel expenses (lodging, etc.)'"
                                + " ELSE obj.title"
                                + " END"
                                + " FROM fis_ObjectClass obj"
                                + " INNER JOIN fis_Obligation o ON o.objectClass = obj"
                                + " WHERE obj.budgetObjectClass IN ('2120','2125')"
                                + " AND o.activity.id = :activity AND o.amount > 0")
                .parameter("activity", activity.getId())
                .parameter("travel", DocumentType.TRAVEL_AUTHORIZATION.toString())
                .parameter("purchase", DocumentType.MISCELLANEOUS_OBLIGATION.toString())
                .properties("boc", "projected", "obligated", "disbursed", "doctype", "boctitle")
                .list();



        System.out.println("stop here");
        return null;
    }

}