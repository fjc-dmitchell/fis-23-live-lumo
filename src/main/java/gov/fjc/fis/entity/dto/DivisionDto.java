package gov.fjc.fis.entity.dto;

import gov.fjc.fis.entity.Division;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNullElse;

@JmixEntity(name = "fis_DivisionDto")
public class DivisionDto {
    private Integer id;

    private String divisionCode;

    @InstanceName
    private String title;

    private String budgetOrg;

    private Integer appropriationId;

    private String budgetFiscalYear;

    private Integer fundId;

    private String fundCode;

    private BigDecimal oneYearAllocations = BigDecimal.ZERO;

    private BigDecimal oneYearProjections = BigDecimal.ZERO;

    private BigDecimal oneYearObligations = BigDecimal.ZERO;

    private BigDecimal oneYearReimbursements = BigDecimal.ZERO;

    private BigDecimal twoYearAllocations = BigDecimal.ZERO;

    private BigDecimal twoYearProjections = BigDecimal.ZERO;

    private BigDecimal twoYearObligations = BigDecimal.ZERO;

    private BigDecimal twoYearReimbursements = BigDecimal.ZERO;

    private BigDecimal priorTwoYearObligated = BigDecimal.ZERO;

    private BigDecimal currentOneYearObligated = BigDecimal.ZERO;

    private BigDecimal currentTwoYearObligated = BigDecimal.ZERO;

    private BigDecimal priorTwoYearProjected = BigDecimal.ZERO;

    private BigDecimal currentOneYearProjected = BigDecimal.ZERO;

    private BigDecimal currentTwoYearProjected = BigDecimal.ZERO;

    private BigDecimal priorTwoYearReimbursed = BigDecimal.ZERO;

    private BigDecimal currentOneYearReimbursed = BigDecimal.ZERO;

    private BigDecimal currentTwoYearReimbursed = BigDecimal.ZERO;

    ;;

    private BigDecimal oneYearBalance = BigDecimal.ZERO;

    private BigDecimal twoYearBalance = BigDecimal.ZERO;


    /**
     * isTwoYearDivision determines whether any two year amounts are populated for the DivisionDto
     *
     * @return true if the DivisionDto if any two year amount is non-zero
     */
    public Boolean isTwoYearDivision() {
        return (twoYearAllocations.compareTo(BigDecimal.ZERO) != 0)
                || (twoYearProjections.compareTo(BigDecimal.ZERO) != 0)
                || (twoYearObligations.compareTo(BigDecimal.ZERO) != 0)
                || (twoYearReimbursements.compareTo(BigDecimal.ZERO) != 0);
    }

    public BigDecimal getTwoYearBalance() {
        return twoYearBalance;
    }

    public void setTwoYearBalance(BigDecimal twoYearBalance) {
        this.twoYearBalance = twoYearBalance;
    }

    public BigDecimal getOneYearBalance() {
        return oneYearBalance;
    }

    public void setOneYearBalance(BigDecimal oneYearBalance) {
        this.oneYearBalance = oneYearBalance;
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public Integer getFundId() {
        return fundId;
    }

    public void setFundId(Integer fundId) {
        this.fundId = fundId;
    }

    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    public void setBudgetFiscalYear(String budgetFiscalYear) {
        this.budgetFiscalYear = budgetFiscalYear;
    }

    public Integer getAppropriationId() {
        return appropriationId;
    }

    public void setAppropriationId(Integer appropriationId) {
        this.appropriationId = appropriationId;
    }

    public void configureDivisionDto(Division division) {
        if (division != null) {
            this.id = division.getId();
            this.divisionCode = division.getDivisionCode();
            this.title = division.getTitle();
            this.budgetOrg = division.getBudgetOrg();
            this.appropriationId = division.getAppropriation().getId();
            this.budgetFiscalYear = division.getAppropriation().getBudgetFiscalYear();
            this.fundId = division.getFund().getId();
            this.fundCode = division.getFund().getFundCode();
        }
    }

    public BigDecimal getCurrentTwoYearReimbursed() {
        return currentTwoYearReimbursed;
    }

    public void setCurrentTwoYearReimbursed(BigDecimal currentTwoYearReimbursed) {
        this.currentTwoYearReimbursed = currentTwoYearReimbursed;
    }

    public void addCurrentTwoYearReimbursed(BigDecimal amount) {
        if (currentTwoYearReimbursed == null) {
            currentTwoYearReimbursed = BigDecimal.ZERO;
        }
        currentTwoYearReimbursed = currentTwoYearReimbursed.add(amount);
    }

    public BigDecimal getCurrentOneYearReimbursed() {
        return currentOneYearReimbursed;
    }

    public void setCurrentOneYearReimbursed(BigDecimal currentOneYearReimbursed) {
        this.currentOneYearReimbursed = currentOneYearReimbursed;
    }

    public void addCurrentOneYearReimbursed(BigDecimal amount) {
        if (currentOneYearReimbursed == null) {
            currentOneYearReimbursed = BigDecimal.ZERO;
        }
        currentOneYearReimbursed = currentOneYearReimbursed.add(amount);
    }

    public BigDecimal getPriorTwoYearReimbursed() {
        return priorTwoYearReimbursed;
    }

    public void setPriorTwoYearReimbursed(BigDecimal priorTwoYearReimbursed) {
        this.priorTwoYearReimbursed = priorTwoYearReimbursed;
    }

    public void addPriorTwoYearReimbursed(BigDecimal amount) {
        if (priorTwoYearReimbursed == null) {
            priorTwoYearReimbursed = BigDecimal.ZERO;
        }
        priorTwoYearReimbursed = priorTwoYearReimbursed.add(amount);
    }

    public BigDecimal getCurrentTwoYearProjected() {
        return currentTwoYearProjected;
    }

    public void setCurrentTwoYearProjected(BigDecimal currentTwoYearProjected) {
        this.currentTwoYearProjected = currentTwoYearProjected;
    }

    public void addCurrentTwoYearProjected(BigDecimal amount) {
        if (currentTwoYearProjected == null) {
            currentTwoYearProjected = BigDecimal.ZERO;
        }
        currentTwoYearProjected = currentTwoYearProjected.add(amount);
    }

    public BigDecimal getCurrentOneYearProjected() {
        return currentOneYearProjected;
    }

    public void setCurrentOneYearProjected(BigDecimal currentOneYearProjected) {
        this.currentOneYearProjected = currentOneYearProjected;
    }

    public void addCurrentOneYearProjected(BigDecimal amount) {
        if (currentOneYearProjected == null) {
            currentOneYearProjected = BigDecimal.ZERO;
        }
        currentOneYearProjected = currentOneYearProjected.add(amount);
    }

    public BigDecimal getPriorTwoYearProjected() {
        return priorTwoYearProjected;
    }

    public void setPriorTwoYearProjected(BigDecimal priorTwoYearProjected) {
        this.priorTwoYearProjected = priorTwoYearProjected;
    }

    public void addPriorTwoYearProjected(BigDecimal amount) {
        if (priorTwoYearProjected == null) {
            priorTwoYearProjected = BigDecimal.ZERO;
        }
        priorTwoYearProjected = priorTwoYearProjected.add(amount);
    }

    public BigDecimal getCurrentTwoYearObligated() {
        return currentTwoYearObligated;
    }

    public void setCurrentTwoYearObligated(BigDecimal currentTwoYearObligated) {
        this.currentTwoYearObligated = currentTwoYearObligated;
    }

    public void addCurrentTwoYearObligated(BigDecimal amount) {
        if (currentTwoYearObligated == null) {
            currentTwoYearObligated = BigDecimal.ZERO;
        }
        currentTwoYearObligated = currentTwoYearObligated.add(amount);
    }

    public BigDecimal getPriorTwoYearObligated() {
        return priorTwoYearObligated;
    }

    public void setPriorTwoYearObligated(BigDecimal priorTwoYearObligated) {
        this.priorTwoYearObligated = priorTwoYearObligated;
    }

    public void addPriorTwoYearObligated(BigDecimal amount) {
        if (priorTwoYearObligated == null) {
            priorTwoYearObligated = BigDecimal.ZERO;
        }
        priorTwoYearObligated = priorTwoYearObligated.add(amount);
    }

    public BigDecimal getCurrentOneYearObligated() {
        return currentOneYearObligated;
    }

    public void setCurrentOneYearObligated(BigDecimal currentOneYearObligated) {
        this.currentOneYearObligated = currentOneYearObligated;
    }

    public void addCurrentOneYearObligated(BigDecimal amount) {
        if (currentOneYearObligated == null) {
            currentOneYearObligated = BigDecimal.ZERO;
        }
        currentOneYearObligated = currentOneYearObligated.add(amount);
    }

    public void setOneYearReimbursements(BigDecimal oneYearReimbursements) {
        this.oneYearReimbursements = oneYearReimbursements;
    }

    public BigDecimal getOneYearReimbursements() {
        return oneYearReimbursements;
    }

    public BigDecimal getTwoYearReimbursements() {
        return twoYearReimbursements;
    }

    public void setTwoYearReimbursements(BigDecimal twoYearReimbursements) {
        this.twoYearReimbursements = twoYearReimbursements;
    }

    public BigDecimal getTwoYearObligations() {
        return twoYearObligations;
    }

    public void setTwoYearObligations(BigDecimal twoYearObligations) {
        this.twoYearObligations = twoYearObligations;
    }

    public BigDecimal getTwoYearProjections() {
        return twoYearProjections;
    }

    public void setTwoYearProjections(BigDecimal twoYearProjections) {
        this.twoYearProjections = twoYearProjections;
    }

    public BigDecimal getTwoYearAllocations() {
        return twoYearAllocations;
    }

    public void setTwoYearAllocations(BigDecimal twoYearAllocations) {
        this.twoYearAllocations = requireNonNullElse(twoYearAllocations, BigDecimal.ZERO);
    }

    public BigDecimal getOneYearObligations() {
        return oneYearObligations;
    }

    public void setOneYearObligations(BigDecimal oneYearObligations) {
        this.oneYearObligations = oneYearObligations;
    }

    public BigDecimal getOneYearProjections() {
        return oneYearProjections;
    }

    public void setOneYearProjections(BigDecimal oneYearProjections) {
        this.oneYearProjections = oneYearProjections;
    }

    public BigDecimal getOneYearAllocations() {
        return oneYearAllocations;
    }

    public void setOneYearAllocations(BigDecimal oneYearAllocations) {
        this.oneYearAllocations = oneYearAllocations;
    }

    public String getBudgetOrg() {
        return budgetOrg;
    }

    public void setBudgetOrg(String budgetOrg) {
        this.budgetOrg = budgetOrg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDivisionCode() {
        return divisionCode;
    }

    public void setDivisionCode(String divisionCode) {
        this.divisionCode = divisionCode;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}