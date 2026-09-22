package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.DocumentType;
import gov.fjc.fis.entity.dto.*;
import gov.fjc.fis.reportdata.EducationBranchReportData;
import gov.fjc.fis.service.*;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("fis_EducationBranchReportService")
public class EducationBranchReportService {

    private final DataManager dataManager;
    private final AppropriationService appropriationService;
    private final ActivityService activityService;
    private final ActivityProjectionService activityProjectionService;
    private final ActivityReimbursementService activityReimbursementService;
    private final ObligationService obligationService;

    public EducationBranchReportService(DataManager dataManager,
                                        AppropriationService appropriationService,
                                        ActivityService activityService,
                                        ActivityProjectionService activityProjectionService,
                                        ActivityReimbursementService activityReimbursementService,
                                        ObligationService obligationService) {
        this.dataManager = dataManager;
        this.appropriationService = appropriationService;
        this.activityService = activityService;
        this.activityProjectionService = activityProjectionService;
        this.activityReimbursementService = activityReimbursementService;
        this.obligationService = obligationService;
    }

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
        var projections = activityProjectionService.getProjectionDtos(appropriation, activities, false);
//        var reimbursements = activityReimbursementService.getReimbursementDtos(appropriation, activities);
//        activityService.updateActivityAmounts(activities, obligations, projections, reimbursements);

        var objectClassIdx = fetchObjectClasses(appropriation);

        // where applicable, roll-up individual activities & obligations into generic activities
        List<ActivityDto> toRemove = new ArrayList<>();
        var genericActivities = activities.stream().filter(ActivityDto::isGenericActivity).toList();
        for (var genericActivity : genericActivities) {
            var detailActivities = activities.stream().filter(a -> a.getGroupId().equals(genericActivity.getGroupId()) && !a.isGenericActivity()).toList();
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

        for (var activity : activities) {
            var activityObligations = obligationsByActivity.getOrDefault(activity.getId(), List.of());
            var byStatus = activityObligations.stream()
                    .collect(Collectors.partitioningBy(ObligationDto::getStatus));
            var obligated = byStatus.get(true);
            var disbursed = byStatus.get(false);

            activity.setPriorTwoYearObligations(sumField(obligated, ObligationDto::getPriorTwoYearObligated));
            activity.setPriorTwoYearDisbursements(sumField(disbursed, ObligationDto::getPriorTwoYearDisbursed));
            activity.setCurrentOneYearObligations(sumField(obligated, ObligationDto::getCurrentOneYearObligated));
            activity.setCurrentOneYearDisbursements(sumField(disbursed, ObligationDto::getCurrentOneYearDisbursed));
            activity.setCurrentTwoYearObligations(sumField(obligated, ObligationDto::getCurrentTwoYearObligated));
            activity.setCurrentTwoYearDisbursements(sumField(disbursed, ObligationDto::getCurrentTwoYearDisbursed));

            activity.setTotalObligated(activity.getPriorTwoYearObligations()
                    .add(activity.getCurrentOneYearObligations())
                    .add(activity.getCurrentTwoYearObligations()));
            activity.setTotalDisbursed(activity.getPriorTwoYearDisbursements()
                    .add(activity.getCurrentOneYearDisbursements())
                    .add(activity.getCurrentTwoYearDisbursements()));
            activity.setTotalProjected(activity.getPriorTwoYearProjections()
                    .add(activity.getCurrentOneYearProjections())
                    .add(activity.getCurrentTwoYearProjections()));
        }

        reportData.setPriorTwoYearProjected(activities.stream().map(ActivityDto::getPriorTwoYearProjections).reduce(BigDecimal.ZERO, BigDecimal::add));
        reportData.setPriorTwoYearObligated(activities.stream().map(ActivityDto::getPriorTwoYearObligations).reduce(BigDecimal.ZERO, BigDecimal::add));
        reportData.setPriorTwoYearDisbursed(activities.stream().map(ActivityDto::getPriorTwoYearDisbursements).reduce(BigDecimal.ZERO, BigDecimal::add));

        reportData.setCurrentOneYearProjected(activities.stream().map(ActivityDto::getCurrentOneYearProjections).reduce(BigDecimal.ZERO, BigDecimal::add));
        reportData.setCurrentOneYearObligated(activities.stream().map(ActivityDto::getCurrentOneYearObligations).reduce(BigDecimal.ZERO, BigDecimal::add));
        reportData.setCurrentOneYearDisbursed(activities.stream().map(ActivityDto::getCurrentOneYearDisbursements).reduce(BigDecimal.ZERO, BigDecimal::add));

        reportData.setCurrentTwoYearProjected(activities.stream().map(ActivityDto::getCurrentTwoYearProjections).reduce(BigDecimal.ZERO, BigDecimal::add));
        reportData.setCurrentTwoYearObligated(activities.stream().map(ActivityDto::getCurrentTwoYearObligations).reduce(BigDecimal.ZERO, BigDecimal::add));
        reportData.setCurrentTwoYearDisbursed(activities.stream().map(ActivityDto::getCurrentTwoYearDisbursements).reduce(BigDecimal.ZERO, BigDecimal::add));

        List<GroupDto> groupDtos = new ArrayList<>();

        GroupDto groupDto = null;
        List<ActivityDto> groupActivities = new ArrayList<>();

        for (ActivityDto activityDto : activities) {
            if ((groupDto == null) || !Objects.equals(groupDto.getGroupCode(), activityDto.getGroupCode())) {
                if (groupDto != null) {
                    if (!groupActivities.isEmpty()) {
                        groupDto.setActivities(groupActivities);
                        groupDtos.add(groupDto);
                    }
                }
                groupDto = dataManager.create(GroupDto.class);
                groupDto.setId(activityDto.getGroupId());
                groupDto.setGroupCode(activityDto.getGroupCode());
                if (activityDto.getGroupCode().isEmpty()) {
                    groupDto.setTitle("Uncategorized");
                } else {
                    groupDto.setTitle(activityDto.getGroupTitle());
                }
                groupActivities = new ArrayList<>();
            }


//            groupActivities.add(activityDto);


            List<ObjectClassDto> objectClassDtos = new ArrayList<>();

            var obligationBoc =
                    obligations.stream()
                            .filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                            .filter(obl -> obl.getAmount().signum() != 0)
                            .map(ObligationDto::getBudgetObjectClass)
                            .collect(Collectors.toSet());
            var projectionBoc =
                    projections.stream()
                            .filter(projection -> projection.getActivityId().equals(activityDto.getId()))
                            .filter(projection -> projection.getAmount().signum() != 0)
                            .map(ActivityProjectionDto::getBudgetObjectClass)
                            .collect(Collectors.toSet());

            TreeSet<String> allBocs = new TreeSet<>();
            allBocs.addAll(obligationBoc);
            allBocs.addAll(projectionBoc);

//            List<ObjectClassDto> allObjectClassDtos = new ArrayList<>();

            for (var boc : allBocs) {

                BigDecimal obligated, disbursed, projected;

                // special case for Training Related Travel
                if (boc.equals("2125") || boc.equals("2120")) {


                    obligated = obligations.stream()
                            .filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                            .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                            .filter(obl -> obl.getDocumentType().equals(DocumentType.TRAVEL_AUTHORIZATION))
                            .map(ObligationDto::getObligated)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    disbursed = obligations.stream()
                            .filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                            .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                            .filter(obl -> obl.getDocumentType().equals(DocumentType.TRAVEL_AUTHORIZATION))
                            .map(ObligationDto::getDisbursed)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if ((obligated.compareTo(BigDecimal.ZERO) > 0) || (disbursed.compareTo(BigDecimal.ZERO) > 0)) {
                        ObjectClassDto dto = dataManager.create(ObjectClassDto.class);
                        dto.setTitle("Travel Authorization");
                        dto.setBudgetObjectClass(boc);
                        dto.setProjected(BigDecimal.ZERO);
                        dto.setObligated(obligated);
                        dto.setDisbursed(disbursed);
                        objectClassDtos.add(dto);
                    }

                    projected = projections.stream()
                            .filter(projection -> projection.getActivityId().equals(activityDto.getId()))
                            .filter(projection -> projection.getBudgetObjectClass().equals(boc))
                            .map(ActivityProjectionDto::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (projected.compareTo(BigDecimal.ZERO) > 0) {
                        ObjectClassDto dto = dataManager.create(ObjectClassDto.class);
                        dto.setTitle("Projection for Travel");
                        dto.setBudgetObjectClass(boc);
                        dto.setProjected(projected);
                        dto.setObligated(BigDecimal.ZERO);
                        dto.setDisbursed(BigDecimal.ZERO);
                        objectClassDtos.add(dto);
                    }

                    obligated = obligations.stream()
                            .filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                            .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                            .filter(obl -> obl.getDocumentType().equals(DocumentType.MISCELLANEOUS_OBLIGATION))
                            .map(ObligationDto::getObligated)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    disbursed = obligations.stream()
                            .filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                            .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                            .filter(obl -> obl.getDocumentType().equals(DocumentType.MISCELLANEOUS_OBLIGATION))
                            .map(ObligationDto::getDisbursed)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if ((obligated.compareTo(BigDecimal.ZERO) > 0) || (disbursed.compareTo(BigDecimal.ZERO) > 0)) {
                        ObjectClassDto dto = dataManager.create(ObjectClassDto.class);
                        dto.setTitle("P.O.s for travel expenses (lodging, etc.)");
                        dto.setBudgetObjectClass(boc);
                        dto.setProjected(BigDecimal.ZERO);
                        dto.setObligated(obligated);
                        dto.setDisbursed(disbursed);
                        objectClassDtos.add(dto);
                    }

                    continue;
                }

                // all other budget object classes
                obligated = obligations.stream()
                        .filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                        .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                        .map(ObligationDto::getObligated)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                disbursed = obligations.stream()
                        .filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                        .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                        .map(ObligationDto::getDisbursed)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                projected = projections.stream()
                        .filter(projection -> projection.getActivityId().equals(activityDto.getId()))
                        .filter(projection -> projection.getBudgetObjectClass().equals(boc))
                        .map(ActivityProjectionDto::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if ((obligated.compareTo(BigDecimal.ZERO) > 0)
                        || (disbursed.compareTo(BigDecimal.ZERO) > 0)
                        || (projected.compareTo(BigDecimal.ZERO) > 0)) {
                    ObjectClassDto dto = dataManager.create(ObjectClassDto.class);

                    String title = switch (boc) {
                        case "2359" -> "Rental Costs";
                        case "2529" -> "Consultant Fees";
                        case "2535" -> "Temporary Help";
                        case "2543" -> "Tuition/Educational Services/On-line Assessments";
                        case "2601" -> "Light refreshments/Off. Supp. & Materials";
                        default -> objectClassIdx.get(boc);
                    };

                    dto.setTitle(title);
                    dto.setBudgetObjectClass(boc);
                    dto.setObligated(obligated);
                    dto.setDisbursed(disbursed);
                    dto.setProjected(projected);
                    objectClassDtos.add(dto);
                }
            }
            if (!objectClassDtos.isEmpty()) {
                activityDto.setObjectClassDtos(objectClassDtos);
            }

            var total = activityDto.getTotalProjected().add(activityDto.getTotalObligated()).add(activityDto.getTotalDisbursed());

            if(total.compareTo(BigDecimal.ZERO) > 0) {
                groupActivities.add(activityDto);
            }
        }

        if (groupDto != null) {
            if (!groupActivities.isEmpty()) {
                groupDto.setActivities(groupActivities);
                groupDtos.add(groupDto);
            }
        }

        reportData.setGroups(groupDtos);
        return reportData;
    }

    private static BigDecimal sumField(List<ObligationDto> list, Function<ObligationDto, BigDecimal> extractor) {
        return list.stream().map(extractor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // factor to boc service. exclude comp & benefits via service
    private Map<String, String> fetchObjectClasses(Appropriation appropriation) {
        List<KeyValueEntity> bocList = dataManager.loadValues(
                        "SELECT o.budgetObjectClass AS boc,"
                                + " o.title AS title "
                                + " FROM fis_ObjectClass o"
                                + " WHERE o.objectCategory.appropriation = :appropriation"
                                + " AND o.objectCategory.majorObjectClass NOT IN ('11','12','13')")
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