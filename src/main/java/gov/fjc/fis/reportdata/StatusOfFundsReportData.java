package gov.fjc.fis.reportdata;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.dto.ObjectCategoryDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static gov.fjc.fis.app.BigDecimalUtils.sum;
import static gov.fjc.fis.app.FisUtilities.*;

/**
 * Holds the data and calculated totals used to generate a Status of Funds report.
 * <p>
 * An instance represents a snapshot of an appropriation at the time the report
 * data is created. It stores the fiscal year, one-year and two-year
 * appropriation amounts, report generation time, and the object-category data
 * included in the report.
 * </p>
 * <p>
 * The class also provides aggregate values for allocations, reimbursements,
 * projections, obligations, and remaining balances. Totals may be calculated
 * across all object categories or for a specific division.
 * </p>
 *
 * @author Doug Mitchell
 * @since 2.0
 */
public class StatusOfFundsReportData {
    private final LocalDateTime reportDateTime;
    private final String budgetFiscalYear;
    private final BigDecimal oneYearAppropriation;
    private final BigDecimal twoYearAppropriation;
    private final String obbbaDivisionCode;

    private List<ObjectCategoryDto> categoryDtos;

    /**
     * Creates a report-data snapshot for the specified appropriation.
     * <p>
     * The budget fiscal year and appropriation amounts are copied from the
     * appropriation. The report date and time are captured when this object is
     * created. If an OBBBA division is supplied, its division code is retained
     * for calculating the OBBBA balance.
     * </p>
     *
     * @param appropriation the appropriation represented by this report data
     * @param obbbaDivision the division used for OBBBA balance calculations,
     *                      or {@code null} if the report has no OBBBA division
     */
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

    /**
     * Returns the report generation date and time formatted for display.
     *
     * @return the formatted report date and time
     */
    public String getReportDateTime() {
        return getDateTimeReportString(reportDateTime);
    }

    /**
     * Returns the base file name for the generated report.
     * <p>
     * The file name contains the budget fiscal year and the date and time
     * captured when this report-data object was created.
     * </p>
     *
     * @return the generated report file name without a file extension
     */
    public String getFileName() {
        return String.format(
                "Status of Funds FY%s as of %s",
                budgetFiscalYear,
                getDateTimeFilenameString(reportDateTime)
        );
    }

    /**
     * Sums a value extracted from every object category in the report.
     *
     * @param extractor function that returns the value to include from an
     *                  object category
     * @return the sum of the extracted values
     */
    private BigDecimal sumFromCategories(Function<ObjectCategoryDto, BigDecimal> extractor) {
        return sum(categoryDtos, extractor);
    }

    /**
     * Sums a division-specific value extracted from every object category in
     * the report.
     *
     * @param extractor function that returns a value for an object category
     *                  and division code
     * @param divCode   the code of the division whose values will be summed
     * @return the sum of the extracted values for the specified division
     */
    private BigDecimal sumFromCategories(BiFunction<ObjectCategoryDto, String, BigDecimal> extractor, String divCode) {
        return sum(categoryDtos, categoryDto -> extractor.apply(categoryDto, divCode));
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

    /**
     * Calculates the remaining one-year balance.
     * <p>
     * The balance is calculated as allocations minus projections and
     * obligations, plus reimbursements.
     * </p>
     *
     * @return the remaining one-year balance
     */
    public BigDecimal getTotalOneYearBalance() {
        return getTotalOneYearAllocations()
                .subtract(getTotalOneYearProjections())
                .subtract(getTotalOneYearObligations())
                .add(getTotalOneYearReimbursements());
    }

    /**
     * Calculates the remaining two-year balance.
     * <p>
     * The balance is calculated as allocations minus projections and
     * obligations, plus reimbursements.
     * </p>
     *
     * @return the remaining two-year balance
     */
    public BigDecimal getTotalTwoYearBalance() {
        return getTotalTwoYearAllocations()
                .subtract(getTotalTwoYearProjections())
                .subtract(getTotalTwoYearObligations())
                .add(getTotalTwoYearReimbursements());
    }

    /**
     * Calculates the remaining one-year balance for the OBBBA division.
     * <p>
     * The balance is calculated as the division's one-year allocations minus
     * its one-year projections and obligations, plus its one-year
     * reimbursements. If no OBBBA division was supplied when this object was
     * created, this method returns zero.
     * </p>
     *
     * @return the remaining OBBBA balance, or {@link BigDecimal#ZERO} if no
     * OBBBA division is associated with the report
     */
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
