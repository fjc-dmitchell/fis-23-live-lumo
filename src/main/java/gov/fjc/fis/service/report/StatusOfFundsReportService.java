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
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static gov.fjc.fis.app.BigDecimalUtils.*;

/**
 * Spring service responsible for assembling data used by Status of Funds reports.
 * <p>
 * This service retrieves allocations, projections, obligations, and
 * reimbursements for an appropriation and organizes the results by division,
 * major object class, and fund type. The assembled data is used by PDF,
 * spreadsheet, and dashboard reports.
 * </p>
 * <p>
 * Financial amounts are rounded to the requested scale before being assigned
 * to the report DTOs. Data associated with the mandatory division is tracked
 * separately from the one-year and two-year division totals.
 * </p>
 *
 * @author Doug Mitchell
 * @since 2.0
 */
@Component("fis_StatusOfFundsReportService")
public class StatusOfFundsReportService {

    private final FundService fundService;
    private final ObjectCategoryService categoryService;
    private final DivisionService divisionService;
    private final DivisionAllocationService divisionAllocationService;
    private final ActivityProjectionService activityProjectionService;
    private final ActivityReimbursementService activityReimbursementService;
    private final ObligationService obligationService;

    public StatusOfFundsReportService(FundService fundService,
                                      ObjectCategoryService categoryService,
                                      DivisionService divisionService,
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

    /**
     * Composite key used to group financial data by division code and
     * major object class.
     *
     * @param divisionCode     the division code
     * @param majorObjectClass the major object class code
     */
    private record DivMocKey(String divisionCode, String majorObjectClass) {
    }

    /**
     * Generates the data model used to produce the Status of Funds report.
     * <p>
     * The resulting model contains the appropriation, its mandatory division,
     * and the prepared financial data for the standard report categories.
     * </p>
     *
     * @param appropriation the fiscal-year appropriation for which report data
     *                      will be generated
     * @param scale         the number of decimal places to which financial
     *                      amounts are rounded
     * @return the assembled Status of Funds report data
     */
    public StatusOfFundsReportData generateReportData(Appropriation appropriation, int scale) {
        var mandatoryDivision = divisionService.findObbbaDivision(appropriation);
        var reportData = new StatusOfFundsReportData(appropriation, mandatoryDivision);
        reportData.setCategoryDtos(prepareCategoryData(appropriation, scale, true));
        return reportData;
    }

    /**
     * Prepares financial data grouped by object category and division.
     * <p>
     * This method retrieves the divisions and financial values associated with
     * the specified appropriation. Allocations, projections, obligations, and
     * reimbursements are indexed by division code and major object class before
     * being applied to the corresponding DTOs.
     * </p>
     * <p>
     * One-year, two-year, and mandatory-division values are accumulated
     * separately. After all divisions have been processed, totals are calculated
     * for each object category and categories not marked for display are removed
     * from the returned list.
     * </p>
     *
     * @param appropriation         the fiscal-year appropriation for which
     *                              category data will be prepared
     * @param scale                 the number of decimal places to which
     *                              financial amounts are rounded
     * @param showDefaultCategories {@code true} to include the standard report
     *                              categories; {@code false} to rely on each
     *                              category's calculated display status
     * @return the object-category DTOs selected for inclusion in the report
     */
    private List<ObjectCategoryDto> prepareCategoryData(Appropriation appropriation,
                                                        int scale,
                                                        boolean showDefaultCategories) {

        Fund oneYearFund = fundService.getAppropriationOneYearFund();
        Fund twoYearFund = fundService.getAppropriationTwoYearFund();
        List<Fund> funds = List.of(oneYearFund, twoYearFund);

        var categoryDtos = categoryService.getCategoryDtos(appropriation);

        Set<String> showCategories = showDefaultCategories
                ? categoryService.getStandardReportCategoryCodes()
                : Collections.emptySet();

        var divisions = divisionService.fetchDivisions(appropriation, oneYearFund);
        var mandatoryDivision = divisionService.findObbbaDivision(appropriation);

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

    /**
     * Groups financial query results by division code and major object class.
     * <p>
     * Each source entity is expected to contain non-null {@code divisionCode} and
     * {@code majorObjectClass} values.
     * </p>
     *
     * @param entities the financial query results to group
     * @return a map whose keys identify a division and major object class and
     * whose values contain the corresponding query results
     */
    private Map<DivMocKey, List<KeyValueEntity>> indexByDivAndMoc(List<KeyValueEntity> entities) {
        return entities.stream().collect(
                Collectors.groupingBy(e -> new DivMocKey(
                        e.getValue("divisionCode").toString(),
                        e.getValue("majorObjectClass").toString())));
    }


    /**
     * Applies allocation results to a division and its associated object category.
     * <p>
     * Allocation records contain separate one-year and two-year values. Values
     * belonging to the mandatory division are stored as mandatory allocations;
     * all other values are added to the applicable one-year and two-year
     * category totals.
     * </p>
     *
     * @param matches             allocation results for the division and major
     *                            object class, or {@code null} if none exist
     * @param divisionDto         the division DTO to update
     * @param categoryDto         the object-category DTO to update
     * @param isMandatoryDivision {@code true} if the division is the mandatory
     *                            division
     * @param scale               the number of decimal places to which allocation
     *                            amounts are rounded
     */
    private void applyAllocation(List<KeyValueEntity> matches,
                                 DivisionDto divisionDto,
                                 ObjectCategoryDto categoryDto,
                                 boolean isMandatoryDivision,
                                 int scale) {
        if (matches == null) return;
        for (var kv : matches) {
            BigDecimal oneYear = roundToScale(kv.getValue("oneYearAmount"), scale);
            BigDecimal twoYear = roundToScale(kv.getValue("twoYearAmount"), scale);
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

    /**
     * Applies fund-based financial results to a division and its associated
     * object category.
     * <p>
     * This shared operation is used for projections, obligations, and
     * reimbursements. Each amount is assigned to either the one-year or
     * two-year totals according to its fund. For the mandatory division,
     * one-year amounts are also assigned to the corresponding mandatory
     * category field.
     * </p>
     *
     * @param matches             financial results for the division and major
     *                            object class, or {@code null} if none exist
     * @param divisionDto         the division DTO to update
     * @param categoryDto         the object-category DTO to update
     * @param isMandatoryDivision {@code true} if the division is the mandatory
     *                            division
     * @param scale               the number of decimal places to which financial
     *                            amounts are rounded
     * @param oneYearFund         the fund identifying one-year amounts
     * @param setDivOneYear       operation that assigns a one-year amount to the
     *                            division DTO
     * @param addCatOneYear       operation that adds a one-year amount to the
     *                            category DTO
     * @param setDivTwoYear       operation that assigns a two-year amount to the
     *                            division DTO
     * @param addCatTwoYear       operation that adds a two-year amount to the
     *                            category DTO
     * @param setCatMandatory     operation that assigns a mandatory amount to the
     *                            category DTO
     */
    private void applyFundSplit(List<KeyValueEntity> matches,
                                DivisionDto divisionDto,
                                ObjectCategoryDto categoryDto,
                                boolean isMandatoryDivision,
                                int scale,
                                Fund oneYearFund,
                                BiConsumer<DivisionDto, BigDecimal> setDivOneYear,
                                BiConsumer<ObjectCategoryDto, BigDecimal> addCatOneYear,
                                BiConsumer<DivisionDto, BigDecimal> setDivTwoYear,
                                BiConsumer<ObjectCategoryDto, BigDecimal> addCatTwoYear,
                                BiConsumer<ObjectCategoryDto, BigDecimal> setCatMandatory) {
        if (matches == null) return;
        for (var kv : matches) {
            final BigDecimal amount = roundToScale(kv.getValue("amount"), scale);
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

    /**
     * Returns object-category data used by Status of Funds dashboard reports.
     * <p>
     * In addition to the prepared category data, this method calculates the
     * difference between the appropriation amounts and the total allocated
     * amounts. When applicable, the difference is appended as an
     * {@code ALLOCATION DISCREPANCY} category.
     * </p>
     *
     * @param appropriation         the fiscal-year appropriation for which
     *                              category data will be prepared
     * @param scale                 the number of decimal places to which
     *                              financial amounts are rounded
     * @param showDefaultCategories {@code true} to include the standard report
     *                              categories; {@code false} to include only
     *                              categories selected by their display rules
     * @return the category data, including an allocation-discrepancy category when it is eligible for display
     */
    public List<ObjectCategoryDto> getStatusOfFundsCategoryData(Appropriation appropriation,
                                                                int scale, boolean showDefaultCategories) {
        var categoryDtos = new ArrayList<>(prepareCategoryData(appropriation, scale, showDefaultCategories));

        var oneYearAppropriation = appropriation.getOneYearAmount();
        var twoYearAppropriation = appropriation.getTwoYearAmount();

        var oneYearAlloc = sum(categoryDtos, ObjectCategoryDto::getTotalOneYearAllocations);
        var twoYearAlloc = sum(categoryDtos, ObjectCategoryDto::getTotalTwoYearAllocations);

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
     * Returns object-category data used by the Status of Funds dashboard
     * spending chart, including calculated unspent funds.
     * <p>
     * Unspent funds are calculated separately for one-year and two-year funds
     * by adding reimbursements to the appropriation amount and subtracting
     * projections and obligations. A combined value is also calculated from
     * the total appropriation, reimbursements, projections, and obligations.
     * </p>
     * <p>
     * When applicable, the calculated values are appended as an
     * {@code UNSPENT FUNDS} category.
     * </p>
     *
     * @param appropriation the fiscal-year appropriation for which spending data
     *                      will be prepared
     * @param scale         the number of decimal places to which financial
     *                      amounts are rounded
     * @return the category data, including an unspent-funds category when it is eligible for display
     */
    public List<ObjectCategoryDto> getStatusOfFundsCategoryDataWithUnspent(Appropriation appropriation, int scale) {
        var categoryDtos = new ArrayList<>(prepareCategoryData(appropriation, scale, false));

        var oneYearAppropriation = appropriation.getOneYearAmount();
        var twoYearAppropriation = appropriation.getTwoYearAmount();
        var totalAppropriation = oneYearAppropriation.add(twoYearAppropriation);

        var oneYearProj = sum(categoryDtos, ObjectCategoryDto::getTotalOneYearProjections);
        var twoYearProj = sum(categoryDtos, ObjectCategoryDto::getTotalTwoYearProjections);
        var totalProj = oneYearProj.add(twoYearProj);

        var oneYearOblig = sum(categoryDtos, ObjectCategoryDto::getTotalOneYearObligations);
        var twoYearOblig = sum(categoryDtos, ObjectCategoryDto::getTotalTwoYearObligations);
        var totalOblig = oneYearOblig.add(twoYearOblig);

        var oneYearReim = sum(categoryDtos, ObjectCategoryDto::getTotalOneYearReimbursements);
        var twoYearReim = sum(categoryDtos, ObjectCategoryDto::getTotalTwoYearReimbursements);
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
}