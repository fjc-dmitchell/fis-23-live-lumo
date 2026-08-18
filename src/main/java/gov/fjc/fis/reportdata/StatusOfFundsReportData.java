package gov.fjc.fis.reportdata;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.dto.ObjectCategoryDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static gov.fjc.fis.FisUtilities.*;

public class StatusOfFundsReportData {
    private final LocalDateTime reportDateTime;
    private final String budgetFiscalYear;
    private final BigDecimal oneYearAppropriation;
    private final BigDecimal twoYearAppropriation;
    private final String obbbaDivisionCode;

    private List<ObjectCategoryDto> categoryDtos;

    public StatusOfFundsReportData(Appropriation appropriation, Division obbbaDivision) {
        this.budgetFiscalYear = appropriation.getBudgetFiscalYear();
        this.oneYearAppropriation = appropriation.getOneYearAmount();
        this.twoYearAppropriation = appropriation.getTwoYearAmount();
        this.reportDateTime = getDateTime();
        this.obbbaDivisionCode = (obbbaDivision != null)
                ? obbbaDivision.getDivisionCode()
                : null;
    }

    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    public BigDecimal getOneYearAppropriation() {
        return oneYearAppropriation;
    }

    public BigDecimal getTwoYearAppropriation() {
        return twoYearAppropriation;
    }

    public List<ObjectCategoryDto> getCategoryDtos() {
        return categoryDtos;
    }

    public void setCategoryDtos(List<ObjectCategoryDto> categoryDtos) {
        this.categoryDtos = categoryDtos;
    }

    public String getReportDateTime() {
        return getDateTimeReportString(reportDateTime);
    }

    public String getFileName() {
        return String.format(
                "Status of Funds FY%s as of %s",
                budgetFiscalYear,
                getDateTimeFilenameString(reportDateTime)
        );
    }

    private BigDecimal sumFromCategories(Function<ObjectCategoryDto, BigDecimal> extractor) {
        return categoryDtos.stream()
                .map(extractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumFromCategories(BiFunction<ObjectCategoryDto, String, BigDecimal> extractor, String divCode) {
        return categoryDtos.stream()
                .map(categoryDto -> extractor.apply(categoryDto, divCode))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalOneYearAllocations(String divCode) {
        return sumFromCategories(ObjectCategoryDto::getOneYearAllocations, divCode);
    }

    public BigDecimal getTotalOneYearReimbursements(String divCode) {
        return sumFromCategories(ObjectCategoryDto::getOneYearReimbursements, divCode);
    }

    public BigDecimal getTotalOneYearProjections(String divCode) {
        return sumFromCategories(ObjectCategoryDto::getOneYearProjections, divCode);
    }

    public BigDecimal getTotalOneYearObligations(String divCode) {
        return sumFromCategories(ObjectCategoryDto::getOneYearObligations, divCode);
    }

    public BigDecimal getTotalTwoYearAllocations(String divCode) {
        return sumFromCategories(ObjectCategoryDto::getTwoYearAllocations, divCode);
    }

    public BigDecimal getTotalTwoYearReimbursements(String divCode) {
        return sumFromCategories(ObjectCategoryDto::getTwoYearReimbursements, divCode);
    }

    public BigDecimal getTotalTwoYearProjections(String divCode) {
        return sumFromCategories(ObjectCategoryDto::getTwoYearProjections, divCode);
    }

    public BigDecimal getTotalTwoYearObligations(String divCode) {
        return sumFromCategories(ObjectCategoryDto::getTwoYearObligations, divCode);
    }

    public BigDecimal getTotalOneYearAllocations() {
        return sumFromCategories(ObjectCategoryDto::getTotalOneYearAllocations);
    }

    public BigDecimal getTotalTwoYearAllocations() {
        return sumFromCategories(ObjectCategoryDto::getTotalTwoYearAllocations);
    }

    public BigDecimal getTotalCombinedYearAllocations() {
        return getTotalOneYearAllocations()
                .add(getTotalTwoYearAllocations());
    }

    public BigDecimal getTotalOneYearObligations() {
        return sumFromCategories(ObjectCategoryDto::getTotalOneYearObligations);
    }

    public BigDecimal getTotalTwoYearObligations() {
        return sumFromCategories(ObjectCategoryDto::getTotalTwoYearObligations);
    }

    public BigDecimal getTotalCombinedYearObligations() {
        return getTotalOneYearObligations()
                .add(getTotalTwoYearObligations());
    }

    public BigDecimal getTotalOneYearProjections() {
        return sumFromCategories(ObjectCategoryDto::getTotalOneYearProjections);
    }

    public BigDecimal getTotalTwoYearProjections() {
        return sumFromCategories(ObjectCategoryDto::getTotalTwoYearProjections);
    }

    public BigDecimal getTotalCombinedYearProjections() {
        return getTotalOneYearProjections()
                .add(getTotalTwoYearProjections());
    }

    public BigDecimal getTotalOneYearReimbursements() {
        return sumFromCategories(ObjectCategoryDto::getTotalOneYearReimbursements);
    }

    public BigDecimal getTotalTwoYearReimbursements() {
        return sumFromCategories(ObjectCategoryDto::getTotalTwoYearReimbursements);
    }

    public BigDecimal getTotalCombinedYearReimbursements() {
        return getTotalOneYearReimbursements()
                .add(getTotalTwoYearReimbursements());
    }

    public BigDecimal getTotalOneYearBalance() {
        return getTotalOneYearAllocations()
                .subtract(getTotalOneYearProjections())
                .subtract(getTotalOneYearObligations())
                .add(getTotalOneYearReimbursements());
    }

    public BigDecimal getTotalTwoYearBalance() {
        return getTotalTwoYearAllocations()
                .subtract(getTotalTwoYearProjections())
                .subtract(getTotalTwoYearObligations())
                .add(getTotalTwoYearReimbursements());
    }

    public BigDecimal getObbbaBalance() {
        if (obbbaDivisionCode == null) {
            return BigDecimal.ZERO;
        }
        return getTotalOneYearAllocations(obbbaDivisionCode)
                .subtract(getTotalOneYearProjections(obbbaDivisionCode))
                .subtract(getTotalOneYearObligations(obbbaDivisionCode))
                .add(getTotalOneYearReimbursements(obbbaDivisionCode));
    }
}
