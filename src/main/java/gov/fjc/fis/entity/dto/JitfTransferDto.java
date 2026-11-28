package gov.fjc.fis.entity.dto;

import gov.fjc.fis.service.JitfTransferService;
import gov.fjc.fis.view.dashboard.jitfdashboard.JitfDashboardView;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.math.BigDecimal;

/**
 * Contains the financial summary for a single fiscal year displayed on the
 * JITF transfer dashboard.
 * <p>
 * Each summary includes the balance carried forward from the preceding fiscal
 * year, the fiscal year's total deposits and expenses, and the remaining
 * balance to carry forward to the next fiscal year.
 * </p>
 * <p>
 * The carry-forward balance is calculated as:
 * </p>
 * <pre>
 * carried-forward balance + total deposits - total expenses
 * </pre>
 * @see JitfTransferService
 * @see JitfDashboardView
 * @author Doug Mitchell
 */
@JmixEntity(name = "fis_JitfTransferDto")
public class JitfTransferDto {

    /**
     * Budget fiscal year represented by this summary.
     */
    private String budgetFiscalYear;

    /**
     * Balance carried forward from the preceding fiscal year.
     */
    private BigDecimal carriedForward;

    /**
     * Total amount deposited into JITF during the fiscal year.
     */
    private BigDecimal totalDeposits;

    /**
     * Total JITF expenses incurred during the fiscal year.
     */
    private BigDecimal totalExpenses;

    /**
     * Remaining balance available to carry forward to the next fiscal year.
     */
    private BigDecimal carryForward;

    /**
     * Returns the remaining balance to carry forward to the next fiscal year.
     *
     * @return the balance to carry forward
     */
    public BigDecimal getCarryForward() {
        return carryForward;
    }

    /**
     * Sets the remaining balance to carry forward to the next fiscal year.
     *
     * @param carryForward the balance to carry forward
     */
    public void setCarryForward(BigDecimal carryForward) {
        this.carryForward = carryForward;
    }

    /**
     * Returns the total JITF expenses for the fiscal year.
     *
     * @return the total expenses
     */
    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    /**
     * Sets the total JITF expenses for the fiscal year.
     *
     * @param totalExpenses the total expenses
     */
    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    /**
     * Returns the total JITF deposits for the fiscal year.
     *
     * @return the total deposits
     */
    public BigDecimal getTotalDeposits() {
        return totalDeposits;
    }

    /**
     * Sets the total JITF deposits for the fiscal year.
     *
     * @param totalDeposits the total deposits
     */
    public void setTotalDeposits(BigDecimal totalDeposits) {
        this.totalDeposits = totalDeposits;
    }

    /**
     * Returns the balance carried forward from the preceding fiscal year.
     *
     * @return the balance carried forward from the preceding fiscal year
     */
    public BigDecimal getCarriedForward() {
        return carriedForward;
    }

    /**
     * Sets the balance carried forward from the preceding fiscal year.
     *
     * @param carriedForward the balance carried forward from the preceding
     *                       fiscal year
     */
    public void setCarriedForward(BigDecimal carriedForward) {
        this.carriedForward = carriedForward;
    }

    /**
     * Returns the budget fiscal year represented by this summary.
     *
     * @return the budget fiscal year
     */
    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    /**
     * Sets the budget fiscal year represented by this summary.
     *
     * @param budgetFiscalYear the budget fiscal year
     */
    public void setBudgetFiscalYear(String budgetFiscalYear) {
        this.budgetFiscalYear = budgetFiscalYear;
    }
}