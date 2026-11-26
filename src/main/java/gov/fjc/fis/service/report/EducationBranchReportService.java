package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.DocumentType;
import gov.fjc.fis.entity.dto.*;
import gov.fjc.fis.view.report.educationbranchreport.EducationBranchReportView;
import gov.fjc.fis.reportdata.EducationBranchReportData;
import gov.fjc.fis.service.*;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static gov.fjc.fis.app.BigDecimalUtils.anyPositive;
import static gov.fjc.fis.app.BigDecimalUtils.sum;
import static gov.fjc.fis.app.BigDecimalUtils.sumNullSafe;

/**
 * Generates the Education Branch Report data used to display and export
 * education and training funding activity for a branch.
 * <p>
 * The service consolidates activity projections, obligations, and disbursements
 * across multiple fiscal years and organizes the data into a hierarchical
 * structure suitable for reporting. Activities are grouped by branch group,
 * summarized by budget object class (BOC), and categorized into prior two-year,
 * current one-year, and current two-year funding periods.
 * </p>
 *
 * <p>
 * Generic activities are used as reporting roll-ups. When present, obligations
 * and projections from related detail activities are reassigned to the generic
 * activity and the detail activities are excluded from the report output. This
 * allows the report to present consolidated funding information at the desired
 * reporting level.
 * </p>
 *
 * <p>
 * Certain training-related travel budget object classes receive specialized
 * handling to separate travel authorizations, travel projections, and travel
 * expense obligations into distinct report rows.
 * </p>
 *
 * @see EducationBranchReportView
 * @see EducationBranchReportData
 * @author Doug Mitchell
 */
@Component("fis_EducationBranchReportService")
public class EducationBranchReportService {

    /**
     * Budget object class codes used for training-related travel reporting.
     * These codes receive specialized report treatment to separate travel
     * authorizations, travel projections, and related travel expenses.
     */
    private static final Set<String> TRAINING_TRAVEL_BOCS = Set.of("2120", "2125");

    private final DataManager dataManager;
    private final AppropriationService appropriationService;
    private final ActivityService activityService;
    private final ActivityProjectionService activityProjectionService;
    private final ObligationService obligationService;

    public EducationBranchReportService(DataManager dataManager,
                                        AppropriationService appropriationService,
                                        ActivityService activityService,
                                        ActivityProjectionService activityProjectionService,
                                        ObligationService obligationService) {
        this.dataManager = dataManager;
        this.appropriationService = appropriationService;
        this.activityService = activityService;
        this.activityProjectionService = activityProjectionService;
        this.obligationService = obligationService;
    }

    /**
     * Represents report amounts for a single reporting period.
     *
     * @param projected projected funding amount
     * @param obligated obligated funding amount
     * @param disbursed disbursed funding amount
     */
    public record PeriodAmounts(BigDecimal projected, BigDecimal obligated, BigDecimal disbursed) {

        /**
         * Returns the sum of projected, obligated, and disbursed amounts.
         *
         * @return combined total amount
         */
        public BigDecimal total() {
            return sumNullSafe(projected, obligated, disbursed);
        }

        /**
         * Determines whether any amount is greater than zero.
         *
         * @return {@code true} if any projected, obligated, or disbursed amount
         *         is positive; otherwise {@code false}
         */
        public boolean hasPositiveAmount() {
            return anyPositive(projected, obligated, disbursed);
        }
    }

    /**
     * Generates report data for the specified branch.
     * <p>
     * Activity, obligation, and projection information is collected and aggregated
     * into report structures organized by group, activity, and budget object class.
     * Fiscal period totals are calculated for each activity and overall report
     * totals are calculated for the branch.
     * </p>
     *
     * @param branch the branch for which report data should be generated
     * @return populated report data ready for report rendering
     */
    public EducationBranchReportData generateReportData(Branch branch) {
        var division = branch.getDivision();

        var appropriation = division.getAppropriation();
        var priorYearAppropriation = appropriationService.getPreviousFiscalYear(appropriation);

        var reportData = new EducationBranchReportData(branch);
        reportData.setPriorBudgetFiscalYear(priorYearAppropriation.getBudgetFiscalYear());

        // retrieve bi-fiscal activities for branch
        var activities = activityService.getBiFiscalActivityDtos(division, branch);

        activities.sort(
                Comparator.comparing(ActivityDto::getGroupSortCode)
                        .thenComparing(ActivityDto::getGroupCode)
                        .thenComparing(ActivityDto::getStartDate, Comparator.nullsFirst(Comparator.naturalOrder()))
        );

        var obligations = obligationService.getObligationDtos(appropriation, activities);
        var projections = activityProjectionService.fetchProjectionDtos(appropriation, activities, false);
//        var reimbursements = activityReimbursementService.getReimbursementDtos(appropriation, activities);
//        activityService.updateActivityAmounts(activities, obligations, projections, reimbursements);

        var objectClassIdx = fetchObjectClasses(appropriation);

        // where applicable, roll-up individual activities & obligations into generic activities
        List<ActivityDto> toRemove = new ArrayList<>();
        var genericActivities = activities.stream().filter(ActivityDto::isGenericActivity).toList();
        for (var genericActivity : genericActivities) {
            var detailActivities = activities.stream()
                    .filter(a -> a.getGroupId().equals(genericActivity.getGroupId()) && !a.isGenericActivity())
                    .toList();
            for (var detailActivity : detailActivities) {

                obligations.stream()
                        .filter(obligation -> obligation.getActivityId().equals(detailActivity.getId()))
                        .forEach(obligation -> obligation.setActivityId(genericActivity.getId()));

                projections.stream()
                        .filter(projection -> projection.getActivityId().equals(detailActivity.getId()))
                        .forEach(projection -> projection.setActivityId(genericActivity.getId()));

                toRemove.add(detailActivity);
            }
        }
        activities.removeAll(toRemove);

        // start totals
        Map<Integer, List<ObligationDto>> obligationsByActivity = obligations.stream()
                .collect(Collectors.groupingBy(ObligationDto::getActivityId));
        Map<Integer, List<ActivityProjectionDto>> projectionsByActivity = projections.stream()
                .collect(Collectors.groupingBy(ActivityProjectionDto::getActivityId));

        for (var activity : activities) {
            var activityObligations = obligationsByActivity.getOrDefault(activity.getId(), List.of());
            var projected = projectionsByActivity.getOrDefault(activity.getId(), List.of());
            var byStatus = activityObligations.stream()
                    .collect(Collectors.partitioningBy(ObligationDto::getStatus));
            var obligated = byStatus.get(true);
            var disbursed = byStatus.get(false);

            activity.setPriorTwoYearProjections(sum(projected, ActivityProjectionDto::getPriorTwoYearAmount));
            activity.setPriorTwoYearObligations(sum(obligated, ObligationDto::getPriorTwoYearObligated));
            activity.setPriorTwoYearDisbursements(sum(disbursed, ObligationDto::getPriorTwoYearDisbursed));
            activity.setCurrentOneYearProjections(sum(projected, ActivityProjectionDto::getCurrentOneYearAmount));
            activity.setCurrentOneYearObligations(sum(obligated, ObligationDto::getCurrentOneYearObligated));
            activity.setCurrentOneYearDisbursements(sum(disbursed, ObligationDto::getCurrentOneYearDisbursed));
            activity.setCurrentTwoYearProjections(sum(projected, ActivityProjectionDto::getCurrentTwoYearAmount));
            activity.setCurrentTwoYearObligations(sum(obligated, ObligationDto::getCurrentTwoYearObligated));
            activity.setCurrentTwoYearDisbursements(sum(disbursed, ObligationDto::getCurrentTwoYearDisbursed));

            activity.setTotalObligated(sumNullSafe(
                    activity.getPriorTwoYearObligations(),
                    activity.getCurrentOneYearObligations(),
                    activity.getCurrentTwoYearObligations()));
            activity.setTotalDisbursed(sumNullSafe(
                    activity.getPriorTwoYearDisbursements(),
                    activity.getCurrentOneYearDisbursements(),
                    activity.getCurrentTwoYearDisbursements()));
            activity.setTotalProjected(sumNullSafe(
                    activity.getPriorTwoYearProjections(),
                    activity.getCurrentOneYearProjections(),
                    activity.getCurrentTwoYearProjections()));
        }

        reportData.setPriorTwoYearProjected(sum(activities, ActivityDto::getPriorTwoYearProjections));
        reportData.setPriorTwoYearObligated(sum(activities, ActivityDto::getPriorTwoYearObligations));
        reportData.setPriorTwoYearDisbursed(sum(activities, ActivityDto::getPriorTwoYearDisbursements));

        reportData.setCurrentOneYearProjected(sum(activities, ActivityDto::getCurrentOneYearProjections));
        reportData.setCurrentOneYearObligated(sum(activities, ActivityDto::getCurrentOneYearObligations));
        reportData.setCurrentOneYearDisbursed(sum(activities, ActivityDto::getCurrentOneYearDisbursements));

        reportData.setCurrentTwoYearProjected(sum(activities, ActivityDto::getCurrentTwoYearProjections));
        reportData.setCurrentTwoYearObligated(sum(activities, ActivityDto::getCurrentTwoYearObligations));
        reportData.setCurrentTwoYearDisbursed(sum(activities, ActivityDto::getCurrentTwoYearDisbursements));

        List<GroupDto> groupDtos = new ArrayList<>();

        GroupDto groupDto = null;
        List<ActivityDto> groupActivities = new ArrayList<>();

        for (ActivityDto activityDto : activities) {
            if (groupDto == null || !Objects.equals(groupDto.getGroupCode(), activityDto.getGroupCode())) {
                addGroupIfNotEmpty(groupDtos, groupDto, groupActivities);
                groupDto = createGroupDto(activityDto);
                groupActivities = new ArrayList<>();
            }

            var activityObligations = obligationsByActivity.getOrDefault(activityDto.getId(), List.of());
            var activityProjections = projectionsByActivity.getOrDefault(activityDto.getId(), List.of());

            var allBocs = new TreeSet<String>();
            activityObligations.stream()
                    .filter(obl -> obl.getAmount().signum() != 0)
                    .map(ObligationDto::getBudgetObjectClass)
                    .forEach(allBocs::add);
            activityProjections.stream()
                    .filter(projection -> projection.getAmount().signum() != 0)
                    .map(ActivityProjectionDto::getBudgetObjectClass)
                    .forEach(allBocs::add);

            List<ObjectClassDto> objectClassDtos = new ArrayList<>();

            for (var boc : allBocs) {
                var bocObligations = activityObligations.stream()
                        .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                        .toList();
                var bocProjections = activityProjections.stream()
                        .filter(projection -> projection.getBudgetObjectClass().equals(boc))
                        .toList();

                if (TRAINING_TRAVEL_BOCS.contains(boc)) {
                    addObjectClassDto(objectClassDtos, "Travel Authorization", boc,
                            amounts(withDocumentType(bocObligations, DocumentType.TRAVEL_AUTHORIZATION), List.of()));
                    addObjectClassDto(objectClassDtos, "Projection for Travel", boc,
                            amounts(List.of(), bocProjections));
                    addObjectClassDto(objectClassDtos, "P.O.s for travel expenses (lodging, etc.)", boc,
                            amounts(withDocumentType(bocObligations, DocumentType.MISCELLANEOUS_OBLIGATION), List.of()));
                } else {
                    addObjectClassDto(objectClassDtos, objectClassTitle(boc, objectClassIdx), boc,
                            amounts(bocObligations, bocProjections));
                }
            }
            if (!objectClassDtos.isEmpty()) {
                activityDto.setObjectClassDtos(objectClassDtos);
            }

            var activityTotals = new PeriodAmounts(
                    activityDto.getTotalProjected(),
                    activityDto.getTotalObligated(),
                    activityDto.getTotalDisbursed());

            if (activityTotals.total().signum() > 0) {
                groupActivities.add(activityDto);
            }
        }

        addGroupIfNotEmpty(groupDtos, groupDto, groupActivities);

        reportData.setGroups(groupDtos);
        return reportData;
    }

    /**
     * Creates a report group DTO from an activity.
     *
     * @param activityDto the activity providing the group information
     * @return a group DTO initialized with the activity's group data
     */
    private GroupDto createGroupDto(ActivityDto activityDto) {
        var groupDto = dataManager.create(GroupDto.class);
        groupDto.setId(activityDto.getGroupId());
        groupDto.setGroupCode(activityDto.getGroupCode());
        var hasGroupCode = activityDto.getGroupCode() != null && !activityDto.getGroupCode().isEmpty();
        groupDto.setTitle(hasGroupCode ? activityDto.getGroupTitle() : "Uncategorized");
        return groupDto;
    }

    /**
     * Adds a group to the report output when it contains activities.
     *
     * @param groupDtos target collection of report groups
     * @param groupDto group to add
     * @param groupActivities activities assigned to the group
     */
    private static void addGroupIfNotEmpty(List<GroupDto> groupDtos,
                                           GroupDto groupDto,
                                           List<ActivityDto> groupActivities) {
        if (groupDto != null && !groupActivities.isEmpty()) {
            groupDto.setActivities(groupActivities);
            groupDtos.add(groupDto);
        }
    }

    /**
     * Calculates projected, obligated, and disbursed totals for a collection of
     * obligations and projections.
     *
     * @param obligations obligations to summarize
     * @param projections projections to summarize
     * @return consolidated period amounts
     */
    private static PeriodAmounts amounts(List<ObligationDto> obligations,
                                         List<ActivityProjectionDto> projections) {
        return new PeriodAmounts(
                sum(projections, ActivityProjectionDto::getAmount),
                sum(obligations, ObligationDto::getObligated),
                sum(obligations, ObligationDto::getDisbursed));
    }

    /**
     * Filters obligations by document type.
     *
     * @param obligations obligations to evaluate
     * @param type required document type
     * @return obligations matching the specified document type
     */
    private static List<ObligationDto> withDocumentType(List<ObligationDto> obligations, DocumentType type) {
        return obligations.stream()
                .filter(obl -> obl.getDocumentType().equals(type))
                .toList();
    }

    /**
     * Returns the report display title for a budget object class.
     * <p>
     * Certain budget object classes use custom report labels that differ from
     * their configured object class titles.
     * </p>
     *
     * @param boc budget object class code
     * @param objectClassIdx map of object class codes to titles
     * @return display title for the budget object class
     */
    private static String objectClassTitle(String boc, Map<String, String> objectClassIdx) {
        return switch (boc) {
            case "2359" -> "Rental Costs";
            case "2529" -> "Consultant Fees";
            case "2535" -> "Temporary Help";
            case "2543" -> "Tuition/Educational Services/On-line Assessments";
            case "2601" -> "Light refreshments/Off. Supp. & Materials";
            default -> objectClassIdx.get(boc);
        };
    }

    /**
     * Creates and adds an object class report entry when funding amounts are
     * present.
     *
     * @param target target collection
     * @param title object class title
     * @param boc budget object class code
     * @param amounts funding amounts associated with the object class
     */
    private void addObjectClassDto(List<ObjectClassDto> target,
                                   String title,
                                   String boc,
                                   PeriodAmounts amounts) {
        if (!amounts.hasPositiveAmount()) {
            return;
        }
        var dto = dataManager.create(ObjectClassDto.class);
        dto.setTitle(title);
        dto.setBudgetObjectClass(boc);
        dto.setProjected(amounts.projected());
        dto.setObligated(amounts.obligated());
        dto.setDisbursed(amounts.disbursed());
        target.add(dto);
    }


    /**
     * Retrieves budget object class titles available for the specified
     * appropriation.
     *
     * @param appropriation appropriation whose object classes should be retrieved
     * @return map of budget object class codes to titles
     */
    private Map<String, String> fetchObjectClasses(Appropriation appropriation) {
        List<KeyValueEntity> bocList = dataManager.loadValues(
                        "SELECT e.budgetObjectClass AS boc,"
                                + " e.title AS title "
                                + " FROM fis_ObjectClass e"
                                + " WHERE e.objectCategory.appropriation = :appropriation")
                .parameter("appropriation", appropriation)
                .properties("boc", "title")
                .list();

        return bocList.stream()
                .collect(Collectors.toMap(
                        boc -> boc.getValue("boc"),
                        boc -> boc.getValue("title")
                ));
    }
}