package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.dto.ActivityDto;
import gov.fjc.fis.entity.dto.BranchDto;
import gov.fjc.fis.reportdata.EducationProgramsReportData;
import gov.fjc.fis.service.*;
import io.jmix.core.DataManager;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static gov.fjc.fis.FisUtilities.nonZero;

@Component("fis_EducationProgramsAnalysis")
public class EducationProgramsAnalysis {
    @Autowired
    private UiReportRunner uiReportRunner;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityProjectionService activityProjectionService;
    @Autowired
    private ActivityReimbursementService activityReimbursementService;
    @Autowired
    private ObligationService obligationService;

    public void generateReportData() {

        // includes 2017 2-yr funded programs held in 2018
        List<String> budgetFiscalYears = Arrays.asList("2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025");
        List<Appropriation> appropriations = appropriationService.getAppropriations(budgetFiscalYears);

        // branch codes provided by Nancy
        //    22 (Probation and Pretrial)
        //    23 (Executive Education)
        //    25 (Management and Professional Development Education)
        //    61 (Article III Judges Programs)
        //    62 (Legal Staff)
        //    66 (Programs for MJs)
        //    68 (Misc Programs for Js)
        //    69 (Programs for BJs)
        List<String> branchCodes = Arrays.asList("22", "23", "25", "61", "62", "66", "68", "69");

        // group codes provided by Nanticha
        //    10	National Programs for Court Staff
        //    11	Circuit Workshops for Circuit & District Judges
        //    12	Phase II Orientation Seminars for District Judges
        //    13	Workshops for Appellate Judges
        //    14	Phase II Orientation Seminars for Bankruptcy Judges
        //    15	National Workshops for Bankruptcy Judges
        //    16	National Programs for Chief Judges
        //    17	Phase I Orientation Seminars for Bankruptcy Judges
        //    18	Special Focus Programs for Article III Judges
        //    19	Phase I Orientation Seminars for District Judges
        //    20	Court Staff Training For Trainers
        //    21	Pilot Programs for Court Staff
        //    27	National Workshops for Magistrate Judges
        //    28	Special Focus Workshops for Magistrate Judges
        //    29	Programs for Legal Staff
        //    30	Federal Defender Programs
        //    31	Phase I Orientation Seminars for Magistrate Judges
        //    32	Phase II Orientation Seminars for Magistrate Judges
        //    40    Conferences for Chief Bankruptcy Judges
        //    41	Special Focus Workshops for Bankruptcy Judges
        //    47    EBE Supervision
        //    97	Foundation-funded Programs for Judges

        List<String> groupCodes = Arrays.asList("10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "27", "28", "29", "30", "31", "32", "40", "41", "47", "97");

        Map<String, EducationProgramsReportData> fiscalYearReportData = new HashMap<>();

        for (var appropriation : appropriations) {

            var priorYearAppropriation = appropriationService.getPreviousFiscalYear(appropriation);
            var division = divisionService.getEducationDivision(appropriation);
            var activities = activityService.getBiFiscalActivityDtos(division, branchCodes, groupCodes);

            // for self-service reporting, rewrite to provide aggregations by activity (memory issue)
            var obligations = obligationService.getObligationDtos(appropriation, activities);
            var projections = activityProjectionService.getProjectionDtos(appropriation, activities, false);
            var reimbursements = activityReimbursementService.getReimbursementDtos(appropriation, activities);
            activityService.updateActivityAmounts(activities, obligations, projections, reimbursements);

            List<BranchDto> branches = new ArrayList<>();
            BranchDto branchDto = null;

//            Map<String, List<ActivityDto>> branchActivities = new HashMap<>();
            String lastBranch = "";
            List<ActivityDto> activityDtos = new ArrayList<>();

            for (var activity : activities) {
//                activity.setTotalBalance(add(
//                        activity.getPriorTwoYearProjections(), activity.getPriorTwoYearObligations(),
//                        activity.getPriorTwoYearDisbursements(), activity.getCurrentOneYearProjections(),
//                        activity.getCurrentOneYearObligations(), activity.getCurrentOneYearDisbursements(),
//                        activity.getCurrentTwoYearProjections(), activity.getCurrentTwoYearObligations(),
//                        activity.getCurrentTwoYearDisbursements()));

                if (!activity.getBranchTitleAndCode().equals(lastBranch)) {
                    activityDtos = new ArrayList<>();
                    activityDtos.add(activity);
                    branchDto = dataManager.create(BranchDto.class);
                    branchDto.setBranchCode(activity.getBranchCode());
                    branchDto.setTitle(activity.getBranchTitle());
//                    branchDto.addTotalAmount(activity.getTotalBalance());
                    branchDto.setActivities(activityDtos);
                    branches.add(branchDto);
                    lastBranch = activity.getBranchTitleAndCode();
                } else {
//                    assert branchDto != null;
//                    branchDto.addTotalAmount(activity.getTotalBalance());
                    activityDtos.add(activity);
                }
            }

            EducationProgramsReportData reportData = new EducationProgramsReportData(appropriation, division, null);
            reportData.setPriorBudgetFiscalYear(priorYearAppropriation);
//            reportData.setProjections(projections);
//            reportData.setReimbursements(reimbursements);
//            reportData.setObligations(obligations);
//            reportData.setActivities(activities);
//            reportData.setBranchTitles(branchList);
//            reportData.setBranchActivities(branchActivities);
            reportData.setBranches(branches);

            fiscalYearReportData.put(appropriation.getBudgetFiscalYear(), reportData);
        }

        var bfy1 = fiscalYearReportData.get("2018");
        var bfy2 = fiscalYearReportData.get("2019");
        var bfy3 = fiscalYearReportData.get("2020");
        var bfy4 = fiscalYearReportData.get("2021");
        var bfy5 = fiscalYearReportData.get("2022");
        var bfy6 = fiscalYearReportData.get("2023");
        var bfy7 = fiscalYearReportData.get("2024");
        var bfy8 = fiscalYearReportData.get("2025");

        var fluentUiReportRunner = uiReportRunner.byReportCode("program-analysis");

        fluentUiReportRunner
                .addParam("bfy1", bfy1)
                .addParam("bfy2", bfy2)
                .addParam("bfy3", bfy3)
                .addParam("bfy4", bfy4)
                .addParam("bfy5", bfy5)
                .addParam("bfy6", bfy6)
                .addParam("bfy7", bfy7)
                .addParam("bfy8", bfy8)
                .withOutputType(ReportOutputType.XLSX)
                .withOutputNamePattern("Program Analysis 2018-2025")
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .runAndShow();
    }

    /**
     * Report requested by Nancy 6/24/2025 per John's request to ED to further analyze
     * decreasing number of programs/participants. Much of John's info from annual reports.
     * <p>
     * John's request to Julie, then delegated to Mike Zubrensky. A near impossible task for uninitiated!
     * <p>
     * Provide calendar year data 2017-2025 separating Travel versus non-Travel costs using same
     * branch/group criteria from 2/23/25 fiscal year analysis. Ignore appropriations and funds.
     * Doug is using program start/end dates, not document dates. Projections included for 2025.
     * <p>
     * 6/25/2025 - Doug Mitchell wrote this code plus services. This should not be a self-service
     * report as written as it is memory intensive.
     */
    public void generateCalendarYearReportData() {
        List<String> budgetFiscalYears = Arrays.asList("2017", "2018", "2019", "2020", "2021", "2022",
                "2023", "2024", "2025");
        List<Appropriation> appropriations = appropriationService.getAppropriations(budgetFiscalYears);

        List<String> branchCodes = Arrays.asList("22", "23", "25", "61", "62", "66", "68", "69");

        List<String> groupCodes = Arrays.asList("10", "11", "12", "13", "14", "15", "16", "17", "18",
                "19", "20", "21", "27", "28", "29", "30", "31", "32", "40", "41", "47", "97");

        Map<String, EducationProgramsReportData> fiscalYearReportData = new HashMap<>();

        for (var appropriation : appropriations) {

            var priorYearAppropriation = appropriationService.getPreviousFiscalYear(appropriation);
            var division = divisionService.getEducationDivision(appropriation);

            var activities = activityService.getCalendarYearActivityDtos(appropriation, branchCodes, groupCodes);

            List<BranchDto> branches = new ArrayList<>();
            BranchDto branchDto = null;

            String lastBranch = "";
            List<ActivityDto> activityDtos = new ArrayList<>();

            for (var activity : activities) {
                activity.setTravelObligated(obligationService.sumObligations(activity, true));
                activity.setNonTravelObligated(obligationService.sumObligations(activity, false));
                activity.setTravelProjected(activityProjectionService.sumProjections(activity, true));
                activity.setNonTravelProjected(activityProjectionService.sumProjections(activity, false));

                if (!activity.getBranchTitleAndCode().equals(lastBranch)) {
                    activityDtos = new ArrayList<>();
                    activityDtos.add(activity);
                    branchDto = dataManager.create(BranchDto.class);
                    branchDto.setBranchCode(activity.getBranchCode());
                    branchDto.setTitle(activity.getBranchTitle());
                    branchDto.setActivities(activityDtos);
                    branches.add(branchDto);
                    lastBranch = activity.getBranchTitleAndCode();
                } else {
                    // if not generic OR expenses exist, include in sheet
                    if (!activity.getActivityNumber().equals(activity.getGroupCode().concat("00"))
                            || nonZero(activity.getTravelObligated(), activity.getNonTravelObligated(),
                            activity.getTravelProjected(), activity.getNonTravelProjected())) {
                        activityDtos.add(activity);
                    }
                }
            }

            EducationProgramsReportData reportData = new EducationProgramsReportData(appropriation, division, null);
            reportData.setPriorBudgetFiscalYear(priorYearAppropriation);
            reportData.setBranches(branches);

            fiscalYearReportData.put(appropriation.getBudgetFiscalYear(), reportData);
        }

        var bfy1 = fiscalYearReportData.get("2017");
        var bfy2 = fiscalYearReportData.get("2018");
        var bfy3 = fiscalYearReportData.get("2019");
        var bfy4 = fiscalYearReportData.get("2020");
        var bfy5 = fiscalYearReportData.get("2021");
        var bfy6 = fiscalYearReportData.get("2022");
        var bfy7 = fiscalYearReportData.get("2023");
        var bfy8 = fiscalYearReportData.get("2024");
        var bfy9 = fiscalYearReportData.get("2025");

        var fluentUiReportRunner = uiReportRunner.byReportCode("calendar-program-analysis");

        fluentUiReportRunner
                .addParam("bfy1", bfy1)
                .addParam("bfy2", bfy2)
                .addParam("bfy3", bfy3)
                .addParam("bfy4", bfy4)
                .addParam("bfy5", bfy5)
                .addParam("bfy6", bfy6)
                .addParam("bfy7", bfy7)
                .addParam("bfy8", bfy8)
                .addParam("bfy9", bfy9)
                .withOutputType(ReportOutputType.XLSX)
                .withOutputNamePattern("Program Analysis calendar years 2017-2025")
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .runAndShow();
    }
}