package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.DocumentType;
import gov.fjc.fis.entity.dto.*;
import gov.fjc.fis.entity.personnel.Position;
import gov.fjc.fis.reportdata.EducationBranchReportData;
import gov.fjc.fis.service.*;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("fis_EducationBranchReportService")
public class EducationBranchReportService {

    private final AppropriationService appropriationService;
    private final ActivityService activityService;
    private final ActivityProjectionService activityProjectionService;
    private final ActivityReimbursementService activityReimbursementService;
    private final ObligationService obligationService;

    public EducationBranchReportService(AppropriationService appropriationService,
                                        ActivityService activityService,
                                        ActivityProjectionService activityProjectionService,
                                        ActivityReimbursementService activityReimbursementService,
                                        ObligationService obligationService) {
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
        var reimbursements = activityReimbursementService.getReimbursementDtos(appropriation, activities);
        activityService.updateActivityAmounts(activities, obligations, projections, reimbursements);

        var objectClassIdx = fetchObjectClasses(appropriation);

        // where applicable, roll-up individual activities into generic activities
        List<ActivityDto> toRemove = new ArrayList<>();
        var genericActivities = activities.stream().filter(ActivityDto::isGenericActivity).toList();
        for (var genericActivity : genericActivities) {
            var detailActivities = activities.stream().filter(a -> a.getGroupId().equals(genericActivity.getGroupId()) && !a.isGenericActivity()).toList();
            for (var detailActivity : detailActivities) {

                // need to rethink totals issue
                genericActivity.setTotalObligated(genericActivity.getTotalObligated().add(detailActivity.getTotalObligated()));
                genericActivity.setPriorTwoYearObligations(genericActivity.getPriorTwoYearObligations().add(detailActivity.getPriorTwoYearObligations()));
                genericActivity.setCurrentOneYearObligations(genericActivity.getCurrentOneYearObligations().add(detailActivity.getCurrentOneYearObligations()));
                genericActivity.setCurrentTwoYearObligations(genericActivity.getCurrentTwoYearObligations().add(detailActivity.getCurrentTwoYearObligations()));

                genericActivity.setTotalProjected(genericActivity.getTotalProjected().add(detailActivity.getTotalProjected()));
                genericActivity.setPriorTwoYearProjections(genericActivity.getPriorTwoYearProjections().add(detailActivity.getPriorTwoYearProjections()));
                genericActivity.setCurrentOneYearProjections(genericActivity.getCurrentOneYearProjections().add(detailActivity.getCurrentOneYearProjections()));
                genericActivity.setCurrentTwoYearProjections(genericActivity.getCurrentTwoYearProjections().add(detailActivity.getCurrentTwoYearProjections()));

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

        List<GroupDto> groupDtos = new ArrayList<>();

        GroupDto groupDto = null;// = dataManager.create(GroupDto.class);
        List<ActivityDto> groupActivities = new ArrayList<>();

        for (ActivityDto activityDto : activities) {
            if ((groupDto == null) || !Objects.equals(groupDto.getGroupCode(), activityDto.getGroupCode())) {
                if (groupDto != null) {
                    groupDto.setActivities(groupActivities);
                    groupDtos.add(groupDto);
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


            groupActivities.add(activityDto);


            // START BOC
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

            List<ObjectClassDto> allObjectClassDtos = new ArrayList<>();

            for (var boc : allBocs) {

                BigDecimal obligated, disbursed, projected;


                // special case for Training Related Travel
                if (boc.equals("2125")) {
                    obligated = obligations.stream().filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                            .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                            .filter(obl -> obl.getDocumentType().equals(DocumentType.MISCELLANEOUS_OBLIGATION))
                            .map(ObligationDto::getObligated)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    disbursed = obligations.stream().filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                            .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                            .filter(obl -> obl.getDocumentType().equals(DocumentType.MISCELLANEOUS_OBLIGATION))
                            .map(ObligationDto::getDisbursed)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if ((obligated.compareTo(BigDecimal.ZERO) >= 0) || (disbursed.compareTo(BigDecimal.ZERO) >= 0)) {
                        ObjectClassDto dto = dataManager.create(ObjectClassDto.class);
                        dto.setTitle("P.O.s for travel expenses (lodging, etc.)");
                        dto.setBudgetObjectClass(boc);
                        dto.setObligated(obligated);
                        dto.setDisbursed(disbursed);
                        dto.setProjected(BigDecimal.ZERO);
                        objectClassDtos.add(dto);
                    }

                    obligated = obligations.stream().filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                            .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                            .filter(obl -> obl.getDocumentType().equals(DocumentType.TRAVEL_AUTHORIZATION))
                            .map(ObligationDto::getObligated)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    disbursed = obligations.stream().filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                            .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                            .filter(obl -> obl.getDocumentType().equals(DocumentType.TRAVEL_AUTHORIZATION))
                            .map(ObligationDto::getDisbursed)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if ((obligated.compareTo(BigDecimal.ZERO) >= 0) || (disbursed.compareTo(BigDecimal.ZERO) >= 0)) {
                        ObjectClassDto dto = dataManager.create(ObjectClassDto.class);
                        dto.setTitle("Travel Authorization");
                        dto.setBudgetObjectClass(boc);
                        dto.setObligated(obligated);
                        dto.setDisbursed(disbursed);
                        dto.setProjected(BigDecimal.ZERO);
                        objectClassDtos.add(dto);
                    }

                    projected = projections.stream().filter(projection -> projection.getActivityId().equals(activityDto.getId()))
                            .filter(projection -> projection.getBudgetObjectClass().equals(boc))
                            .map(ActivityProjectionDto::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (projected.compareTo(BigDecimal.ZERO) >= 0) {
                        ObjectClassDto dto = dataManager.create(ObjectClassDto.class);
                        dto.setTitle("Projection for Travel");
                        dto.setBudgetObjectClass(boc);
                        dto.setObligated(BigDecimal.ZERO);
                        dto.setDisbursed(BigDecimal.ZERO);
                        dto.setProjected(projected);
                        objectClassDtos.add(dto);
                    }
                    continue;
                }

                // all other budget object classes
                obligated = obligations.stream().filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                        .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                        .map(ObligationDto::getObligated)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                disbursed = obligations.stream().filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                        .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                        .map(ObligationDto::getDisbursed)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                projected = projections.stream().filter(projection -> projection.getActivityId().equals(activityDto.getId()))
                        .filter(projection -> projection.getBudgetObjectClass().equals(boc))
                        .map(ActivityProjectionDto::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if ((obligated.compareTo(BigDecimal.ZERO) >= 0) || (disbursed.compareTo(BigDecimal.ZERO) >= 0) || (projected.compareTo(BigDecimal.ZERO) >= 0)) {
                    ObjectClassDto dto = dataManager.create(ObjectClassDto.class);

                    String title = switch (boc) {
                        case "2120" -> "Non-training Travel";
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

//                System.out.println("BOC: " + boc + " "+title);
//                System.out.println("bocDto.getProjected(): " + bocDto.getProjected());
//                System.out.println("bocDto.getObligated(): " + bocDto.getObligated());
//                System.out.println("bocDto.getDisbursed(): " + bocDto.getDisbursed());


            }

            // END BOC
            activityDto.setObjectClassDtos(objectClassDtos);

        }

        if (groupDto != null) {
            groupDto.setActivities(groupActivities);
            groupDtos.add(groupDto);
        }

        reportData.setGroups(groupDtos);
        return reportData;
    }

    public EducationBranchReportData generateReportData2(Branch branch) {
        var division = branch.getDivision();

        var appropriation = division.getAppropriation();
        var priorYearAppropriation = appropriationService.getPreviousFiscalYear(appropriation);

        var activities = activityService.getBiFiscalActivityDtos(division, branch);

        activities.sort(
                Comparator.comparing(ActivityDto::getGroupSortCode)
                        .thenComparing(ActivityDto::getGroupCode)
                        .thenComparing(ActivityDto::getStartDate, Comparator.nullsFirst(Comparator.naturalOrder()))
        );

        var obligations = obligationService.getObligationDtos(appropriation, activities);
        var projections = activityProjectionService.getProjectionDtos(appropriation, activities, false);
        var reimbursements = activityReimbursementService.getReimbursementDtos(appropriation, activities);
        activityService.updateActivityAmounts(activities, obligations, projections, reimbursements);

        var objectClassIdx = fetchObjectClasses(appropriation);

        System.out.println("Size of activities: " + activities.size());

        List<ActivityDto> toRemove = new ArrayList<>();
        var genericActivities = activities.stream().filter(ActivityDto::isGenericActivity).toList();
        for (var genericActivity : genericActivities) {
            var detailActivities = activities.stream().filter(a -> a.getGroupId().equals(genericActivity.getGroupId()) && !a.isGenericActivity()).toList();
            for (var detailActivity : detailActivities) {

                // need to rethink totals issue
                genericActivity.setTotalObligated(genericActivity.getTotalObligated().add(detailActivity.getTotalObligated()));
                genericActivity.setPriorTwoYearObligations(genericActivity.getPriorTwoYearObligations().add(detailActivity.getPriorTwoYearObligations()));
                genericActivity.setCurrentOneYearObligations(genericActivity.getCurrentOneYearObligations().add(detailActivity.getCurrentOneYearObligations()));
                genericActivity.setCurrentTwoYearObligations(genericActivity.getCurrentTwoYearObligations().add(detailActivity.getCurrentTwoYearObligations()));

                genericActivity.setTotalProjected(genericActivity.getTotalProjected().add(detailActivity.getTotalProjected()));
                genericActivity.setPriorTwoYearProjections(genericActivity.getPriorTwoYearProjections().add(detailActivity.getPriorTwoYearProjections()));
                genericActivity.setCurrentOneYearProjections(genericActivity.getCurrentOneYearProjections().add(detailActivity.getCurrentOneYearProjections()));
                genericActivity.setCurrentTwoYearProjections(genericActivity.getCurrentTwoYearProjections().add(detailActivity.getCurrentTwoYearProjections()));

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

        System.out.println("Size of activities after consolidation: " + activities.size());

        var reportData = new EducationBranchReportData(branch, activities);
        reportData.setPriorBudgetFiscalYear(priorYearAppropriation.getBudgetFiscalYear());
        reportData.setActivities(activities);

        for (ActivityDto activityDto : activities) {

            System.out.println("Activity: " + activityDto.getTitle() + " " + activityDto.getActivityNumber());

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

            List<ObjectClassDto> allObjectClassDtos = new ArrayList<>();

            for (var boc : allBocs) {

                BigDecimal obligated, disbursed, projected;


                // special case for Training Related Travel
                if (boc.equals("2125")) {
                    obligated = obligations.stream().filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                            .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                            .filter(obl -> obl.getDocumentType().equals(DocumentType.MISCELLANEOUS_OBLIGATION))
                            .map(ObligationDto::getObligated)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    disbursed = obligations.stream().filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                            .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                            .filter(obl -> obl.getDocumentType().equals(DocumentType.MISCELLANEOUS_OBLIGATION))
                            .map(ObligationDto::getDisbursed)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if ((obligated.compareTo(BigDecimal.ZERO) >= 0) || (disbursed.compareTo(BigDecimal.ZERO) >= 0)) {
                        ObjectClassDto dto = dataManager.create(ObjectClassDto.class);
                        dto.setTitle("P.O.s for travel expenses (lodging, etc.)");
                        dto.setBudgetObjectClass(boc);
                        dto.setObligated(obligated);
                        dto.setDisbursed(disbursed);
                        dto.setProjected(BigDecimal.ZERO);
                        objectClassDtos.add(dto);
                    }

                    obligated = obligations.stream().filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                            .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                            .filter(obl -> obl.getDocumentType().equals(DocumentType.TRAVEL_AUTHORIZATION))
                            .map(ObligationDto::getObligated)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    disbursed = obligations.stream().filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                            .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                            .filter(obl -> obl.getDocumentType().equals(DocumentType.TRAVEL_AUTHORIZATION))
                            .map(ObligationDto::getDisbursed)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if ((obligated.compareTo(BigDecimal.ZERO) >= 0) || (disbursed.compareTo(BigDecimal.ZERO) >= 0)) {
                        ObjectClassDto dto = dataManager.create(ObjectClassDto.class);
                        dto.setTitle("Travel Authorization");
                        dto.setBudgetObjectClass(boc);
                        dto.setObligated(obligated);
                        dto.setDisbursed(disbursed);
                        dto.setProjected(BigDecimal.ZERO);
                        objectClassDtos.add(dto);
                    }

                    projected = projections.stream().filter(projection -> projection.getActivityId().equals(activityDto.getId()))
                            .filter(projection -> projection.getBudgetObjectClass().equals(boc))
                            .map(ActivityProjectionDto::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (projected.compareTo(BigDecimal.ZERO) >= 0) {
                        ObjectClassDto dto = dataManager.create(ObjectClassDto.class);
                        dto.setTitle("Travel Authorization");
                        dto.setBudgetObjectClass(boc);
                        dto.setObligated(BigDecimal.ZERO);
                        dto.setDisbursed(BigDecimal.ZERO);
                        dto.setProjected(projected);
                        objectClassDtos.add(dto);
                    }
                    continue;
                }

                // all other budget object classes
                obligated = obligations.stream().filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                        .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                        .map(ObligationDto::getObligated)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                disbursed = obligations.stream().filter(obl -> obl.getActivityId().equals(activityDto.getId()))
                        .filter(obl -> obl.getBudgetObjectClass().equals(boc))
                        .map(ObligationDto::getDisbursed)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                projected = projections.stream().filter(projection -> projection.getActivityId().equals(activityDto.getId()))
                        .filter(projection -> projection.getBudgetObjectClass().equals(boc))
                        .map(ActivityProjectionDto::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if ((obligated.compareTo(BigDecimal.ZERO) >= 0) || (disbursed.compareTo(BigDecimal.ZERO) >= 0) || (projected.compareTo(BigDecimal.ZERO) >= 0)) {
                    ObjectClassDto dto = dataManager.create(ObjectClassDto.class);

                    String title = switch (boc) {
                        case "2120" -> "Non-training Travel";
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
                activityDto.setObjectClassDtos(objectClassDtos);

//                System.out.println("BOC: " + boc + " "+title);
//                System.out.println("bocDto.getProjected(): " + bocDto.getProjected());
//                System.out.println("bocDto.getObligated(): " + bocDto.getObligated());
//                System.out.println("bocDto.getDisbursed(): " + bocDto.getDisbursed());


            }


            // for each BOC, create an object containing boc, title, projected, obligated, disbursed, total/balance
            // populate from obligation and projection, need to sum like obligations
            // case statement to update title based on boc and which amounts populated (e.g., "projection for travel")
            // add BOC list to activityDto

            System.out.println("Activity: " + activityDto.getTitle() + " " + activityDto.getActivityNumber() + " BOCs: " + obligationBoc);
//            getObjectClassDtos(activityDto);
        }

        return reportData;
    }

    @Autowired
    private DataManager dataManager;

    // exclude comp & benefits via service
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