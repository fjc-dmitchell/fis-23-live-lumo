package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Fund;
import gov.fjc.fis.entity.dto.ObjectCategoryDto;
import gov.fjc.fis.entity.dto.DivisionDto;
import gov.fjc.fis.reportdata.StatusOfFundsReportData;
import gov.fjc.fis.service.*;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("fis_StatusOfFundsReportService")
public class StatusOfFundsReportService {

    private final ObligationService obligationService;
    private final ActivityReimbursementService activityReimbursementService;
    private final ActivityProjectionService activityProjectionService;
    private final DivisionAllocationService divisionAllocationService;
    private final DivisionService divisionService;
    private final ObjectCategoryService categoryService;
    private final FundService fundService;

    private record DivMocKey(String divCode, String moc) {
    }

    public StatusOfFundsReportService(FundService fundService,
                                      ObjectCategoryService categoryService, DivisionService divisionService,
                                      DivisionAllocationService divisionAllocationService,
                                      ActivityProjectionService activityProjectionService,
                                      ActivityReimbursementService activityReimbursementService,
                                      ObligationService obligationService) {
        this.fundService = fundService;
        this.categoryService = categoryService;
        this.divisionService = divisionService;
        this.divisionAllocationService = divisionAllocationService;
        this.activityProjectionService = activityProjectionService;
        this.activityReimbursementService = activityReimbursementService;
        this.obligationService = obligationService;
    }

    public StatusOfFundsReportData generateReportData(Appropriation appropriation, int scale) {
        var mandatoryDivision = divisionService.fetchMandatoryDivision(appropriation);
        var reportData = new StatusOfFundsReportData(appropriation, mandatoryDivision);
        reportData.setCategoryDtos(prepareCategoryData(appropriation, scale, true));
        return reportData;
    }

    /**
     * Returns category data for used by PDF, Excel, and dashboard reports
     *
     * @param appropriation
     * @param scale
     * @param showDefaultCategories
     * @return Category DTOs
     */
    private List<ObjectCategoryDto> prepareCategoryData(Appropriation appropriation,
                                                        int scale, boolean showDefaultCategories) {

        Fund oneYearFund = fundService.getAppropriationOneYearFund();
        Fund twoYearFund = fundService.getAppropriationTwoYearFund();
        var funds = fundService.getFundListForReports(twoYearFund);

        var categoryDtos = categoryService.getCategoryDtos(appropriation);

        Set<String> showCategories = showDefaultCategories
                ? categoryService.getStandardReportCategoryCodes()
                : Collections.emptySet();

        var divisions = divisionService.fetchDivisions(appropriation, oneYearFund);
        var mandatoryDivision = divisionService.fetchMandatoryDivision(appropriation);

        // Pre-index all data sets by (divcode, moc) for O(1) lookup
        var allocationIndex = indexByDivAndMoc(divisionAllocationService.fetchAllocations(appropriation, funds));
        var obligationIndex = indexByDivAndMoc(obligationService.fetchObligationSums(appropriation, funds));
        var projectionIndex = indexByDivAndMoc(activityProjectionService.fetchProjectionSums(appropriation, funds));
        var reimbursementIndex = indexByDivAndMoc(activityReimbursementService.fetchReimbursementSums(appropriation, funds));

        for (var categoryDto : categoryDtos) {
            for (var division : divisions) {
                var divisionDto = divisionService.divisionToDivisionDto(division);
                boolean isMandatory = division.equals(mandatoryDivision);
                var key = new DivMocKey(divisionDto.getDivisionCode(), categoryDto.getMajorObjectClass());

                categoryDto.addOneYearDivision(divisionDto);
                categoryDto.addTwoYearDivision(divisionDto);

                applyAllocation(allocationIndex.get(key), divisionDto, categoryDto, isMandatory, scale);

                applyFundSplit(projectionIndex.get(key), divisionDto, categoryDto, isMandatory, scale, oneYearFund,
                        DivisionDto::setOneYearProjections, ObjectCategoryDto::addOneYearProjection,
                        DivisionDto::setTwoYearProjections, ObjectCategoryDto::addTwoYearProjection, ObjectCategoryDto::setMandatoryProjected);

                applyFundSplit(obligationIndex.get(key), divisionDto, categoryDto, isMandatory, scale, oneYearFund,
                        DivisionDto::setOneYearObligations, ObjectCategoryDto::addOneYearObligation,
                        DivisionDto::setTwoYearObligations, ObjectCategoryDto::addTwoYearObligation, ObjectCategoryDto::setMandatoryObligated);

                applyFundSplit(reimbursementIndex.get(key), divisionDto, categoryDto, isMandatory, scale, oneYearFund,
                        DivisionDto::setOneYearReimbursements, ObjectCategoryDto::addOneYearReimbursement,
                        DivisionDto::setTwoYearReimbursements, ObjectCategoryDto::addTwoYearReimbursement, ObjectCategoryDto::setMandatoryReimbursed);
            }
            categoryDto.calculateTotals();
            categoryDto.setShowOnReport(showCategories.contains(categoryDto.getMajorObjectClass()));
        }
        return categoryDtos.stream()
                .filter(ObjectCategoryDto::getShowOnReport)
                .toList();
    }

    // Groups a flat list into a map keyed by (divcode, moc)
    private Map<DivMocKey, List<KeyValueEntity>> indexByDivAndMoc(List<KeyValueEntity> entities) {
        return entities.stream().collect(
                Collectors.groupingBy(e -> new DivMocKey(
                        e.getValue("divcode").toString(),
                        e.getValue("moc").toString())));
    }

    // Handles allocations, which have separate one-year/two-year amount fields
    private void applyAllocation(List<KeyValueEntity> matches, DivisionDto divisionDto,
                                 ObjectCategoryDto categoryDto, boolean isMandatoryDivision, int scale) {
        if (matches == null) return;
        for (var kv : matches) {
            BigDecimal oneYear = scale(kv.getValue("oneyearamount"), scale);
            BigDecimal twoYear = scale(kv.getValue("twoyearamount"), scale);
            divisionDto.setOneYearAllocations(oneYear);
            divisionDto.setTwoYearAllocations(twoYear);
            if (isMandatoryDivision) {
                categoryDto.setMandatoryAllocated(oneYear);
            } else {
                categoryDto.addOneYearAllocation(oneYear);
                categoryDto.addTwoYearAllocation(twoYear);
            }
        }
    }

    // Handles projections/obligations/reimbursements, which split on fund type
    private void applyFundSplit(
            List<KeyValueEntity> matches, DivisionDto divisionDto, ObjectCategoryDto categoryDto,
            boolean isMandatoryDivision, int scale, Fund oneYearFund,
            BiConsumer<DivisionDto, BigDecimal> setDivOneYear,
            BiConsumer<ObjectCategoryDto, BigDecimal> addCatOneYear,
            BiConsumer<DivisionDto, BigDecimal> setDivTwoYear,
            BiConsumer<ObjectCategoryDto, BigDecimal> addCatTwoYear,
            BiConsumer<ObjectCategoryDto, BigDecimal> setCatMandatory) {
        if (matches == null) return;
        for (var kv : matches) {
            final BigDecimal amount = scale(kv.getValue("amount"), scale);
            if (kv.getValue("fund").equals(oneYearFund)) {
                setDivOneYear.accept(divisionDto, amount);
                if (isMandatoryDivision) setCatMandatory.accept(categoryDto, amount);
                else addCatOneYear.accept(categoryDto, amount);
            } else {
                setDivTwoYear.accept(divisionDto, amount);
                if (!isMandatoryDivision) addCatTwoYear.accept(categoryDto, amount);
            }
        }
    }

    private BigDecimal scale(Object value, int scale) {
        return ((BigDecimal) value).setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * returns data used by dashboard reports
     *
     * @param appropriation
     * @param scale
     * @param showDefaultCategories
     * @return Category DTOs
     */
    public List<ObjectCategoryDto> getStatusOfFundsCategoryData(Appropriation appropriation,
                                                                int scale, boolean showDefaultCategories) {
        var categoryDtos = new ArrayList<>(prepareCategoryData(appropriation, scale, showDefaultCategories));

        var oneYearAppropriation = appropriation.getOneYearAmount();
        var twoYearAppropriation = appropriation.getTwoYearAmount();

        var oneYearAlloc = sumField(categoryDtos, ObjectCategoryDto::getTotalOneYearAllocations);
        var twoYearAlloc = sumField(categoryDtos, ObjectCategoryDto::getTotalTwoYearAllocations);

        // build "discrepancy" row
        var unallocated = categoryService.createCategoryDto();
        unallocated.setTitleAndCode("ALLOCATION DISCREPANCY");
        unallocated.setTotalOneYearAllocations(oneYearAppropriation.subtract(oneYearAlloc));
        unallocated.setTotalTwoYearAllocations(twoYearAppropriation.subtract(twoYearAlloc));
        if (unallocated.getShowOnReport()) {
            categoryDtos.add(unallocated);
        }

        return categoryDtos;
    }

    /**
     * returns data used by dashboard spending chart, includes unspent funds data
     *
     * @param appropriation
     * @param scale
     * @return Category DTOs
     */
    public List<ObjectCategoryDto> getStatusOfFundsCategoryDataWithUnspent(Appropriation appropriation, int scale) {
        var categoryDtos = new ArrayList<>(prepareCategoryData(appropriation, scale, false));

        var oneYearAppropriation = appropriation.getOneYearAmount();
        var twoYearAppropriation = appropriation.getTwoYearAmount();
        var totalAppropriation = oneYearAppropriation.add(twoYearAppropriation);

        var oneYearProj = sumField(categoryDtos, ObjectCategoryDto::getTotalOneYearProjections);
        var twoYearProj = sumField(categoryDtos, ObjectCategoryDto::getTotalTwoYearProjections);
        var totalProj = oneYearProj.add(twoYearProj);

        var oneYearOblig = sumField(categoryDtos, ObjectCategoryDto::getTotalOneYearObligations);
        var twoYearOblig = sumField(categoryDtos, ObjectCategoryDto::getTotalTwoYearObligations);
        var totalOblig = oneYearOblig.add(twoYearOblig);

        var oneYearReim = sumField(categoryDtos, ObjectCategoryDto::getTotalOneYearReimbursements);
        var twoYearReim = sumField(categoryDtos, ObjectCategoryDto::getTotalTwoYearReimbursements);
        var totalReim = oneYearReim.add(twoYearReim);

        // build "unspent funds" row
        var unspent = categoryService.createCategoryDto();
        unspent.setTitleAndCode("UNSPENT FUNDS");
        unspent.setTotalOneYearObligations(oneYearAppropriation.add(oneYearReim).subtract(oneYearProj).subtract(oneYearOblig));
        unspent.setTotalTwoYearObligations(twoYearAppropriation.add(twoYearReim).subtract(twoYearProj).subtract(twoYearOblig));
        unspent.setTotalObligations(totalAppropriation.add(totalReim).subtract(totalProj).subtract(totalOblig));
        if (unspent.getShowOnReport()) {
            categoryDtos.add(unspent);
        }

        return categoryDtos;
    }

    private BigDecimal sumField(List<ObjectCategoryDto> dtos, Function<ObjectCategoryDto, BigDecimal> getter) {
        return dtos.stream().map(getter).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}