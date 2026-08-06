package gov.fjc.fis.entity.dto;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

import static gov.fjc.fis.FisUtilities.getTotalNullAllowed;
import static gov.fjc.fis.FisUtilities.nonZero;

@JmixEntity(name = "fis_ObjectClassDto")
public class ObjectClassDto {
    @JmixGeneratedValue
    @JmixId
    private Integer id;

    private String budgetObjectClass;
    private String title;

    private Boolean showOnReport = Boolean.FALSE;

    private BigDecimal priorTwoYearProjected = BigDecimal.ZERO;
    private BigDecimal priorTwoYearReimbursed = BigDecimal.ZERO;
    private BigDecimal priorTwoYearObligated = BigDecimal.ZERO;
    private BigDecimal priorTwoYearDisbursed = BigDecimal.ZERO;

    private BigDecimal currentOneYearProjected = BigDecimal.ZERO;
    private BigDecimal currentOneYearReimbursed = BigDecimal.ZERO;
    private BigDecimal currentOneYearObligated = BigDecimal.ZERO;
    private BigDecimal currentOneYearDisbursed = BigDecimal.ZERO;

    private BigDecimal currentTwoYearProjected = BigDecimal.ZERO;
    private BigDecimal currentTwoYearReimbursed = BigDecimal.ZERO;
    private BigDecimal currentTwoYearObligated = BigDecimal.ZERO;
    private BigDecimal currentTwoYearDisbursed = BigDecimal.ZERO;

    private BigDecimal projected = BigDecimal.ZERO;
    private BigDecimal obligated = BigDecimal.ZERO;
    private BigDecimal disbursed = BigDecimal.ZERO;

    private BigDecimal total = BigDecimal.ZERO;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBudgetObjectClass() {
        return budgetObjectClass;
    }

    public void setBudgetObjectClass(String budgetObjectClass) {
        this.budgetObjectClass = budgetObjectClass;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getShowOnReport() {
        return showOnReport;
    }

    public void setShowOnReport(Boolean showOnReport) {
        this.showOnReport = showOnReport;
    }

    public BigDecimal getPriorTwoYearProjected() {
        return priorTwoYearProjected;
    }

    public void setPriorTwoYearProjected(BigDecimal priorTwoYearProjected) {
        this.priorTwoYearProjected = priorTwoYearProjected;
    }

    public BigDecimal getPriorTwoYearReimbursed() {
        return priorTwoYearReimbursed;
    }

    public void setPriorTwoYearReimbursed(BigDecimal priorTwoYearReimbursed) {
        this.priorTwoYearReimbursed = priorTwoYearReimbursed;
    }

    public BigDecimal getPriorTwoYearObligated() {
        return priorTwoYearObligated;
    }

    public void setPriorTwoYearObligated(BigDecimal priorTwoYearObligated) {
        this.priorTwoYearObligated = priorTwoYearObligated;
    }

    public BigDecimal getPriorTwoYearDisbursed() {
        return priorTwoYearDisbursed;
    }

    public void setPriorTwoYearDisbursed(BigDecimal priorTwoYearDisbursed) {
        this.priorTwoYearDisbursed = priorTwoYearDisbursed;
    }

    public BigDecimal getCurrentOneYearProjected() {
        return currentOneYearProjected;
    }

    public void setCurrentoneYearProjected(BigDecimal currentoneYearProjected) {
        this.currentOneYearProjected = currentoneYearProjected;
    }

    public BigDecimal getCurrentOneYearReimbursed() {
        return currentOneYearReimbursed;
    }

    public void setCurrentOneYearReimbursed(BigDecimal currentOneYearReimbursed) {
        this.currentOneYearReimbursed = currentOneYearReimbursed;
    }

    public BigDecimal getCurrentOneYearObligated() {
        return currentOneYearObligated;
    }

    public void setCurrentOneYearObligated(BigDecimal currentOneYearObligated) {
        this.currentOneYearObligated = currentOneYearObligated;
    }

    public BigDecimal getCurrentOneYearDisbursed() {
        return currentOneYearDisbursed;
    }

    public void setCurrentOneYearDisbursed(BigDecimal currentOneYearDisbursed) {
        this.currentOneYearDisbursed = currentOneYearDisbursed;
    }

    public BigDecimal getCurrentTwoYearProjected() {
        return currentTwoYearProjected;
    }

    public void setCurrentTwoYearProjected(BigDecimal currentTwoYearProjected) {
        this.currentTwoYearProjected = currentTwoYearProjected;
    }

    public BigDecimal getCurrentTwoYearReimbursed() {
        return currentTwoYearReimbursed;
    }

    public void setCurrentTwoYearReimbursed(BigDecimal currentTwoYearReimbursed) {
        this.currentTwoYearReimbursed = currentTwoYearReimbursed;
    }

    public BigDecimal getCurrentTwoYearObligated() {
        return currentTwoYearObligated;
    }

    public void setCurrentTwoYearObligated(BigDecimal currentTwoYearObligated) {
        this.currentTwoYearObligated = currentTwoYearObligated;
    }

    public BigDecimal getCurrentTwoYearDisbursed() {
        return currentTwoYearDisbursed;
    }

    public void setCurrentTwoYearDisbursed(BigDecimal currentTwoYearDisbursed) {
        this.currentTwoYearDisbursed = currentTwoYearDisbursed;
    }

    public void addPriorTwoYearProjected(BigDecimal priorTwoYearProjected) {
        this.priorTwoYearProjected = getTotalNullAllowed(this.priorTwoYearProjected, priorTwoYearProjected);
    }

    public void addPriorTwoYearReimbursed(BigDecimal priorTwoYearReimbursed) {
        this.priorTwoYearReimbursed = getTotalNullAllowed(this.priorTwoYearReimbursed, priorTwoYearReimbursed);
    }

    public void addPriorTwoYearObligated(BigDecimal priorTwoYearObligated) {
        this.priorTwoYearObligated = getTotalNullAllowed(this.priorTwoYearObligated, priorTwoYearObligated);
    }

    public void addPriorTwoYearDisbursed(BigDecimal priorTwoYearDisbursed) {
        this.priorTwoYearDisbursed = getTotalNullAllowed(this.priorTwoYearDisbursed, priorTwoYearDisbursed);
    }

    public void addCurrentOneYearProjected(BigDecimal currentOneYearProjected) {
        this.currentOneYearProjected = getTotalNullAllowed(this.currentOneYearProjected, currentOneYearProjected);
    }

    public void addCurrentOneYearReimbursed(BigDecimal currentOneYearReimbursed) {
        this.currentOneYearReimbursed = getTotalNullAllowed(this.currentOneYearReimbursed, currentOneYearReimbursed);
    }

    public void addCurrentOneYearObligated(BigDecimal currentOneYearObligated) {
        this.currentOneYearObligated = getTotalNullAllowed(this.currentOneYearObligated, currentOneYearObligated);
    }

    public void addCurrentOneYearDisbursed(BigDecimal currentOneYearDisbursed) {
        this.currentOneYearDisbursed = getTotalNullAllowed(this.currentOneYearDisbursed, currentOneYearDisbursed);
    }

    public void addCurrentTwoYearProjected(BigDecimal currenteTwoYearProjected) {
        this.currentTwoYearProjected = getTotalNullAllowed(this.currentTwoYearProjected, currenteTwoYearProjected);
    }

    public void addCurrentTwoYearReimbursed(BigDecimal currenteTwoYearReimbursed) {
        this.currentTwoYearReimbursed = getTotalNullAllowed(this.currentTwoYearReimbursed, currenteTwoYearReimbursed);
    }

    public void addCurrentTwoYearObligated(BigDecimal currenteTwoYearObligated) {
        this.currentTwoYearObligated = getTotalNullAllowed(this.currentTwoYearObligated, currenteTwoYearObligated);
    }

    public void addCurrentTwoYearDisbursed(BigDecimal currenteTwoYearDisbursed) {
        this.currentTwoYearDisbursed = getTotalNullAllowed(this.currentTwoYearDisbursed, currenteTwoYearDisbursed);
    }

    public boolean showOnReport() {
        return showOnReport ||
                nonZero(priorTwoYearProjected, priorTwoYearReimbursed, priorTwoYearObligated, priorTwoYearDisbursed,
                        currentOneYearProjected, currentOneYearReimbursed, currentOneYearObligated, currentOneYearDisbursed,
                        currentTwoYearProjected, currentTwoYearReimbursed, currentTwoYearObligated, currentTwoYearDisbursed);
    }
}