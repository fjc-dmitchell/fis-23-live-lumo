package gov.fjc.fis.entity.dto;

import io.jmix.core.metamodel.annotation.JmixEntity;

import java.math.BigDecimal;

@JmixEntity(name = "fis_JitfTransferDto")
public class JitfTransferDto {
    private String budgetFiscalYear;

    private BigDecimal carriedForward;

    private BigDecimal totalDeposits;

    private BigDecimal totalExpenses;

    private BigDecimal carryForward;

    public BigDecimal getCarryForward() {
        return carryForward;
    }

    public void setCarryForward(BigDecimal carryForward) {
        this.carryForward = carryForward;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getTotalDeposits() {
        return totalDeposits;
    }

    public void setTotalDeposits(BigDecimal totalDeposits) {
        this.totalDeposits = totalDeposits;
    }

    public BigDecimal getCarriedForward() {
        return carriedForward;
    }

    public void setCarriedForward(BigDecimal carriedForward) {
        this.carriedForward = carriedForward;
    }

    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    public void setBudgetFiscalYear(String budgetFiscalYear) {
        this.budgetFiscalYear = budgetFiscalYear;
    }
}