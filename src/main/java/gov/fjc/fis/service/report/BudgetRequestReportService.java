package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.*;
import gov.fjc.fis.entity.dto.*;
import gov.fjc.fis.reportdata.BudgetRequestReportData;
import gov.fjc.fis.service.*;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component("fis_BudgetRequestReportService")
public class BudgetRequestReportService {
    @Autowired
    private DataManager dataManager;
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private ObjectCategoryService categoryService;
    @Autowired
    private ObjectClassService objectClassService;
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


    public BudgetRequestReportData generateReportData(Appropriation appropriation) {

//        var activities = activityService.getBiFiscalActivityDtos(appropriation);
        var activities = activityService.getBiFiscalActivityDtos(appropriation);
        var obligations = obligationService.getObligationDtos(appropriation, activities);
        var projections = activityProjectionService.getProjectionDtos(appropriation, activities, false);
        var reimbursements = activityReimbursementService.getReimbursementDtos(appropriation, activities);
        activityService.updateActivityAmounts(activities, obligations, projections, reimbursements);

        // create the category reports
        var reportData = getCategoryDtos(appropriation, obligations, projections, reimbursements);


        var objectClasses = getObjectClassDtos(appropriation, obligations, projections, reimbursements);
        // categories could be derived from object classes
//        var categories = getCategoryDtos(appropriation, obligations, projections, reimbursements);
//        var hillPLan = getHillPlanCategoryDtos(categories);
        var divisions = getDivisionDtos(appropriation, obligations, projections, reimbursements);

        reportData.setDivisions(divisions);
        reportData.setActivities(activities);
        reportData.setObligations(obligations);
        reportData.setProjections(projections);
        reportData.setReimbursements(reimbursements);
        reportData.setObjectClasses(objectClasses);
//        reportData.setCategories(categories);
//        reportData.setHillPlanCategories(hillPLan);

        reportData.setPriorTwoYearCarriedForward(getPriorTwoYearBalance(appropriation));

        return reportData;
    }

    private BigDecimal getPriorTwoYearBalance(Appropriation appropriation) {
        Appropriation priorAppropriation = appropriationService.getPreviousFiscalYear(appropriation);

        var appropriationSpendingAuthority = appropriationService.getSpendingAuthority(priorAppropriation);
        BigDecimal authority = appropriationSpendingAuthority.getValue("two_year_total");
        var twoYearActivitiesInCurrentYear = activityService.getTwoYearActivitiesHeldInFiscalYear(priorAppropriation);
        return authority.subtract(activityProjectionService.sumProjections(twoYearActivitiesInCurrentYear))
                .subtract(obligationService.sumObligations(twoYearActivitiesInCurrentYear))
                .add(activityReimbursementService.sumReimbursements(twoYearActivitiesInCurrentYear));
    }

    public BudgetRequestReportData getCategoryDtos(Appropriation appropriation,
                                                   List<ObligationDto> obligationDtos,
                                                   List<ActivityProjectionDto> activityProjectionDtos,
                                                   List<ActivityReimbursementDto> activityReimbursementDtos) {


        Appropriation priorAppropriation = appropriationService.getPreviousFiscalYear(appropriation);
        var reportData = new BudgetRequestReportData(appropriation, priorAppropriation);

        var appropriationSpendingAuthority = appropriationService.getSpendingAuthority(appropriation);
        reportData.setOneYearSpendingAuthority(appropriationSpendingAuthority.getValue("one_year_total"));
        reportData.setTwoYearSpendingAuthority(appropriationSpendingAuthority.getValue("two_year_total"));


        List<Appropriation> fiscalYears = new ArrayList<>();
        fiscalYears.add(appropriation);
        fiscalYears.add(priorAppropriation);

        // Nancy requested specific categories for this report
        Set<String> showCategories = Set.of("11", "12", "13", "21", "22", "23", "24", "25", "26", "31", "90", "91");

        List<ObjectCategory> categories = categoryService.fetchCategorySearchList(fiscalYears);
        List<ObjectCategoryDto> categoryDtoDtos = new ArrayList<>();

        ObjectCategoryDto uncategorized = dataManager.create(ObjectCategoryDto.class);
        uncategorized.setMajorObjectClass("00");
        uncategorized.setTitle("Uncategorized");

        ObjectCategoryDto undefinedDisbursments = dataManager.create(ObjectCategoryDto.class);
        undefinedDisbursments.setMajorObjectClass("91");
        undefinedDisbursments.setTitle("Undefined Disbursements");

        ObjectCategoryDto dto;

        BigDecimal oneYearBalance = BigDecimal.ZERO;
        BigDecimal priorTwoYearBalance = BigDecimal.ZERO; // should be service
        BigDecimal currentTwoYearBalance = BigDecimal.ZERO;

        for (ObjectCategory cat : categories) {

            var moc = cat.getMajorObjectClass();

            if (showCategories.contains(moc)) {
                // consolidate 90 and 91 into 9100
                if (moc.equals("90") || moc.equals("91")) {
                    dto = undefinedDisbursments;
                } else {
                    dto = dataManager.create(ObjectCategoryDto.class);
                    dto.setId(cat.getId());
                    dto.setMajorObjectClass(moc);
                    dto.setTitle(cat.getTitle());
                    categoryDtoDtos.add(dto);
                }
            } else {
                dto = uncategorized;
            }

            var obligations = obligationService.getObligationDtosForMajorObjectClass(obligationDtos, moc);
            dto.addPriorYearObligated(obligations.stream().map(ObligationDto::getPriorTwoYearObligated).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addPriorYearDisbursed(obligations.stream().map(ObligationDto::getPriorTwoYearDisbursed).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentYearObligated(obligations.stream().map(ObligationDto::getCurrentOneYearObligated).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentYearDisbursed(obligations.stream().map(ObligationDto::getCurrentOneYearDisbursed).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentTwoYearObligated(obligations.stream().map(ObligationDto::getCurrentTwoYearObligated).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentTwoYearDisbursed(obligations.stream().map(ObligationDto::getCurrentTwoYearDisbursed).reduce(BigDecimal.ZERO, BigDecimal::add));

            var projections = activityProjectionService.getProjectionDtosForMajorObjectClass(activityProjectionDtos, moc);
            dto.addPriorYearProjected(projections.stream().map(ActivityProjectionDto::getPriorTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentYearProjected(projections.stream().map(ActivityProjectionDto::getCurrentOneYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentTwoYearProjected(projections.stream().map(ActivityProjectionDto::getCurrentTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

            var reimbursements = activityReimbursementService.getReimbursementDtosForMajorObjectClass(activityReimbursementDtos, moc);
            dto.addPriorYearReimbursed(reimbursements.stream().map(ActivityReimbursementDto::getPriorTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentYearReimbursed(reimbursements.stream().map(ActivityReimbursementDto::getCurrentOneYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentTwoYearReimbursed(reimbursements.stream().map(ActivityReimbursementDto::getCurrentTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

            // let's segregate the OBBBA fund and create separate discretionary and mandatory collections
            dto.addDiscretionaryObligated(obligations.stream().filter(n -> !Objects.equals(n.getDivisionCode(), "9")).map(ObligationDto::getCurrentOneYearObligated).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addDiscretionaryDisbursed(obligations.stream().filter(n -> !Objects.equals(n.getDivisionCode(), "9")).map(ObligationDto::getCurrentOneYearDisbursed).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addDiscretionaryProjected(projections.stream().filter(n -> !Objects.equals(n.getDivisionCode(), "9")).map(ActivityProjectionDto::getCurrentOneYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addDiscretionaryReimbursed(reimbursements.stream().filter(n -> !Objects.equals(n.getDivisionCode(), "9")).map(ActivityReimbursementDto::getCurrentOneYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

            dto.addMandatoryObligated(obligations.stream().filter(n -> n.getDivisionCode().equals("9")).map(ObligationDto::getCurrentOneYearObligated).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addMandatoryDisbursed(obligations.stream().filter(n -> n.getDivisionCode().equals("9")).map(ObligationDto::getCurrentOneYearDisbursed).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addMandatoryProjected(projections.stream().filter(n -> n.getDivisionCode().equals("9")).map(ActivityProjectionDto::getCurrentOneYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addMandatoryReimbursed(reimbursements.stream().filter(n -> n.getDivisionCode().equals("9")).map(ActivityReimbursementDto::getCurrentOneYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

            oneYearBalance = oneYearBalance.add(dto.getOneYearProjected())
                    .add(dto.getOneYearObligated())
                    .add(dto.getOneYearDisbursed())
                    .subtract(dto.getOneYearReimbursed());
            currentTwoYearBalance = currentTwoYearBalance.add(dto.getTwoYearProjected())
                    .add(dto.getTwoYearObligated())
                    .add(dto.getTwoYearDisbursed())
                    .subtract(dto.getTwoYearReimbursed());
            priorTwoYearBalance = priorTwoYearBalance.add(dto.getPriorYearProjected())
                    .add(dto.getPriorYearObligated())
                    .add(dto.getPriorYearDisbursed())
                    .subtract(dto.getPriorYearReimbursed());
        }

        Collections.addAll(categoryDtoDtos, undefinedDisbursments, uncategorized);
//        categoryDtoDtos.add(undefinedDisbursments);
//        categoryDtoDtos.add(uncategorized);

        reportData.setCategories(categoryDtoDtos);

        // use these categories as basis for Hill PLan
        reportData.setHillPlanCategories(getHillPlanCategoryDtos(categoryDtoDtos));
        return reportData;
    }

    public List<ObjectClassDto> getObjectClassDtos(Appropriation appropriation, List<ObligationDto> obligationDtos, List<ActivityProjectionDto> activityProjectionDtos, List<ActivityReimbursementDto> activityReimbursementDtos) {

        Set<String> showCategories = Set.of("11", "12", "13", "21", "22", "23", "24", "25", "26", "31", "91");

        Appropriation priorBfy = appropriationService.getPreviousFiscalYear(appropriation);

        List<Appropriation> fiscalYears = new ArrayList<>();
        fiscalYears.add(appropriation);
        fiscalYears.add(priorBfy);

        List<ObjectClass> objectClasses = objectClassService.fetchObjectClassSearchList(fiscalYears, null, true);
        List<ObjectClassDto> objectClassDtos = new ArrayList<>();

        ObjectClassDto dto;

        for (ObjectClass obj : objectClasses) {
            var boc = obj.getBudgetObjectClass();
            dto = dataManager.create(ObjectClassDto.class);
            dto.setId(obj.getId());
            dto.setBudgetObjectClass(boc);
            dto.setTitle(obj.getTitle());

            var obligations = obligationService.getObligationDtosForBudgetObjectClass(obligationDtos, boc);
            dto.addPriorTwoYearObligated(obligations.stream().map(ObligationDto::getPriorTwoYearObligated).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addPriorTwoYearDisbursed(obligations.stream().map(ObligationDto::getPriorTwoYearDisbursed).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentOneYearObligated(obligations.stream().map(ObligationDto::getCurrentOneYearObligated).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentOneYearDisbursed(obligations.stream().map(ObligationDto::getCurrentOneYearDisbursed).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentTwoYearObligated(obligations.stream().map(ObligationDto::getCurrentTwoYearObligated).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentTwoYearDisbursed(obligations.stream().map(ObligationDto::getCurrentTwoYearDisbursed).reduce(BigDecimal.ZERO, BigDecimal::add));

            var projections = activityProjectionService.getProjectionDtosForBudgetObjectClass(activityProjectionDtos, boc);
            dto.addPriorTwoYearProjected(projections.stream().map(ActivityProjectionDto::getPriorTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentOneYearProjected(projections.stream().map(ActivityProjectionDto::getCurrentOneYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentTwoYearProjected(projections.stream().map(ActivityProjectionDto::getCurrentTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

            var reimbursements = activityReimbursementService.getReimbursementDtosForBudgetObjectClass(activityReimbursementDtos, boc);
            dto.addPriorTwoYearReimbursed(reimbursements.stream().map(ActivityReimbursementDto::getPriorTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentOneYearReimbursed(reimbursements.stream().map(ActivityReimbursementDto::getCurrentOneYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentTwoYearReimbursed(reimbursements.stream().map(ActivityReimbursementDto::getCurrentTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

            dto.setShowOnReport(showCategories.contains(boc));
            objectClassDtos.add(dto);
//            dto.checkCategoryTotals();
        }
//        objectClassDtos.add(uncategorized);

        return objectClassDtos.stream().filter(ObjectClassDto::showOnReport).toList();
    }

    public List<ObjectCategoryDto> getHillPlanCategoryDtos(List<ObjectCategoryDto> categoryDtos) {
        List<ObjectCategoryDto> hillPlanDtos = new ArrayList<>();

        ObjectCategoryDto personnel = dataManager.create(ObjectCategoryDto.class);
        personnel.setTitle("Personnel Compensation & Benefits");

        ObjectCategoryDto travel = dataManager.create(ObjectCategoryDto.class);
        travel.setTitle("Travel");

        ObjectCategoryDto rent = dataManager.create(ObjectCategoryDto.class);
        rent.setTitle("Rent, Communication and Utilities");

        ObjectCategoryDto undefined = dataManager.create(ObjectCategoryDto.class);
        undefined.setTitle("Undefined Disbursements");

        ObjectCategoryDto other = dataManager.create(ObjectCategoryDto.class);
        other.setTitle("Other");

        ObjectCategoryDto hillPlanDto;

        for (ObjectCategoryDto categoryDto : categoryDtos) {
            hillPlanDto = switch (categoryDto.getMajorObjectClass()) {
                case "11", "12", "13" -> personnel;
                case "21" -> travel;
                case "23" -> rent;
                case "90", "91" -> undefined;
                default -> other;
            };

            hillPlanDto.addPriorYearProjected(categoryDto.getPriorYearProjected());
            hillPlanDto.addCurrentYearProjected(categoryDto.getOneYearProjected());
            hillPlanDto.addCurrentTwoYearProjected(categoryDto.getTwoYearProjected());

            hillPlanDto.addPriorYearReimbursed(categoryDto.getPriorYearReimbursed());
            hillPlanDto.addCurrentYearReimbursed(categoryDto.getOneYearReimbursed());
            hillPlanDto.addCurrentTwoYearReimbursed(categoryDto.getTwoYearReimbursed());

            hillPlanDto.addPriorYearObligated(categoryDto.getPriorYearObligated());
            hillPlanDto.addPriorYearDisbursed(categoryDto.getPriorYearDisbursed());
            hillPlanDto.addCurrentYearObligated(categoryDto.getOneYearObligated());
            hillPlanDto.addCurrentYearDisbursed(categoryDto.getOneYearDisbursed());
            hillPlanDto.addCurrentTwoYearObligated(categoryDto.getTwoYearObligated());
            hillPlanDto.addCurrentTwoYearDisbursed(categoryDto.getTwoYearDisbursed());
        }
        Collections.addAll(hillPlanDtos, personnel, travel, rent, undefined, other);
        return hillPlanDtos;
    }

    public List<DivisionDto> getDivisionDtos(Appropriation appropriation, List<ObligationDto> obligationDtos, List<ActivityProjectionDto> activityProjectionDtos, List<ActivityReimbursementDto> activityReimbursementDtos) {
        List<Appropriation> appropriations = new ArrayList<>();
        Appropriation priorBfy = appropriationService.getPreviousFiscalYear(appropriation);
        appropriations.add(appropriation);
        appropriations.add(priorBfy);

        List<Division> divisions = divisionService.fetchDivisionSearchList(appropriations, false);

        List<DivisionDto> divisionDtos = new ArrayList<>();
        DivisionDto educationAndTraining = dataManager.create(DivisionDto.class);
        educationAndTraining.setTitle("Education & Training");
        DivisionDto research = dataManager.create(DivisionDto.class);
        research.setTitle("Research");
        DivisionDto programSupport = dataManager.create(DivisionDto.class);
        programSupport.setTitle("Program Support");

        DivisionDto mandatorySpending = dataManager.create(DivisionDto.class);
        mandatorySpending.setTitle("Mandatory Spending");

        DivisionDto dto;

        for (Division div : divisions) {
            var divCode = div.getDivisionCode();
            dto = switch (divCode) {
                case "2", "3", "5" -> educationAndTraining;
                case "4" -> research;
                case "9" -> mandatorySpending;
                default -> programSupport;
            };

            var projections = activityProjectionService.getProjectionDtosForDivisionCode(activityProjectionDtos, divCode);
            dto.addPriorTwoYearProjected(projections.stream().map(ActivityProjectionDto::getPriorTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentOneYearProjected(projections.stream().map(ActivityProjectionDto::getCurrentOneYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentTwoYearProjected(projections.stream().map(ActivityProjectionDto::getCurrentTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

            var reimbursements = activityReimbursementService.getReimbursementDtosForDivisionCode(activityReimbursementDtos, divCode);
            dto.addPriorTwoYearReimbursed(reimbursements.stream().map(ActivityReimbursementDto::getPriorTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentOneYearReimbursed(reimbursements.stream().map(ActivityReimbursementDto::getCurrentOneYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentTwoYearReimbursed(reimbursements.stream().map(ActivityReimbursementDto::getCurrentTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

            var obligations = obligationService.getObligationDtosForDivisionCode(obligationDtos, divCode);
            dto.addPriorTwoYearObligated(obligations.stream().map(ObligationDto::getPriorTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentOneYearObligated(obligations.stream().map(ObligationDto::getCurrentOneYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.addCurrentTwoYearObligated(obligations.stream().map(ObligationDto::getCurrentTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        Collections.addAll(divisionDtos, educationAndTraining, research, programSupport, mandatorySpending);

        return divisionDtos;
    }
}