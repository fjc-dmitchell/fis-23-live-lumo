package gov.fjc.fis.entity.dto;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.util.Objects.requireNonNullElse;

@JmixEntity(name = "fis_ObligationDto")
public class ObligationDto {
    @JmixGeneratedValue
    @JmixId
    private Integer id;

    private Integer appropriationId;

    private String budgetFiscalYear;

    private Integer fundId;

    private String fundCode;

    private Integer divisionId;

    private String divisionCode;

    private String divisionTitle;

    private Integer activityId;

    private String activityNumber;

    private String activityTitle;

    private LocalDate activityEndDate;

    private Integer branchId;

    private String branchCode;

    private String branchTitle;

    private Integer groupId;

    private String groupCode;

    private String groupTitle;

    private String documentNumber;

    private LocalDate documentDate;

    private String vendor;

    private Boolean status;

    private LocalDate travelStartDate;

    private LocalDate travelEndDate;

    private String location;

    private Integer objectClassId;

    private String budgetObjectClass;

    private Integer categoryId;

    private String majorObjectClass;

    private BigDecimal obligated = BigDecimal.ZERO;

    private BigDecimal disbursed = BigDecimal.ZERO;

    private BigDecimal invoiced = BigDecimal.ZERO;

    private BigDecimal amount = BigDecimal.ZERO;

    private BigDecimal currentOneYearAmount = BigDecimal.ZERO;

    private BigDecimal currentOneYearObligated = BigDecimal.ZERO;

    private BigDecimal currentOneYearDisbursed = BigDecimal.ZERO;

    private BigDecimal currentTwoYearAmount = BigDecimal.ZERO;

    private BigDecimal currentTwoYearObligated = BigDecimal.ZERO;

    private BigDecimal currentTwoYearDisbursed = BigDecimal.ZERO;

    private BigDecimal priorTwoYearAmount = BigDecimal.ZERO;

    private BigDecimal priorTwoYearObligated = BigDecimal.ZERO;

    private BigDecimal priorTwoYearDisbursed = BigDecimal.ZERO;

    public void setActivityEndDate(LocalDate activityEndDate) {
        this.activityEndDate = activityEndDate;
    }

    public LocalDate getActivityEndDate() {
        return activityEndDate;
    }

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public void setTravelStartDate(LocalDate travelStartDate) {
        this.travelStartDate = travelStartDate;
    }

    public LocalDate getTravelStartDate() {
        return travelStartDate;
    }

    public void setTravelEndDate(LocalDate travelEndDate) {
        this.travelEndDate = travelEndDate;
    }

    public LocalDate getTravelEndDate() {
        return travelEndDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getPriorTwoYearDisbursed() {
        return priorTwoYearDisbursed;
    }

    public void setPriorTwoYearDisbursed(BigDecimal priorTwoYearDisbursed) {
        this.priorTwoYearDisbursed = priorTwoYearDisbursed;
    }

    public BigDecimal getPriorTwoYearObligated() {
        return priorTwoYearObligated;
    }

    public void setPriorTwoYearObligated(BigDecimal priorTwoYearObligated) {
        this.priorTwoYearObligated = priorTwoYearObligated;
    }

    public BigDecimal getCurrentTwoYearDisbursed() {
        return currentTwoYearDisbursed;
    }

    public void setCurrentTwoYearDisbursed(BigDecimal currentTwoYearDisbursed) {
        this.currentTwoYearDisbursed = currentTwoYearDisbursed;
    }

    public BigDecimal getCurrentTwoYearObligated() {
        return currentTwoYearObligated;
    }

    public void setCurrentTwoYearObligated(BigDecimal currentTwoYearObligated) {
        this.currentTwoYearObligated = currentTwoYearObligated;
    }

    public BigDecimal getCurrentOneYearDisbursed() {
        return currentOneYearDisbursed;
    }

    public void setCurrentOneYearDisbursed(BigDecimal currentOneYearDisbursed) {
        this.currentOneYearDisbursed = currentOneYearDisbursed;
    }

    public BigDecimal getCurrentOneYearObligated() {
        return currentOneYearObligated;
    }

    public void setCurrentOneYearObligated(BigDecimal currentOneYearObligated) {
        this.currentOneYearObligated = currentOneYearObligated;
    }

    public Integer getObjectClassId() {
        return objectClassId;
    }

    public void setObjectClassId(Integer objectClassId) {
        this.objectClassId = objectClassId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

    public Integer getAppropriationId() {
        return appropriationId;
    }

    public void setAppropriationId(Integer appropriationId) {
        this.appropriationId = appropriationId;
    }

    public Integer getFundId() {
        return fundId;
    }

    public void setFundId(Integer fundId) {
        this.fundId = fundId;
    }

    public BigDecimal getInvoiced() {
        return invoiced;
    }

    public void setInvoiced(BigDecimal invoiced) {
        this.invoiced = requireNonNullElse(invoiced, BigDecimal.ZERO);
    }

    public BigDecimal getObligated() {
        return obligated;
    }

    // quick & dirty for Nancy for program analysis 6/25/2025
    // create instance variables and properly set them in report code
    // avoid hardcoding travel code; use business rule instead
    public BigDecimal getTravelObligated() {
        return majorObjectClass.equals("21") ? obligated : BigDecimal.ZERO;
    }

    public BigDecimal getNonTravelObligated() {
        return majorObjectClass.equals("21") ? BigDecimal.ZERO : obligated;
    }

    public void setObligated(BigDecimal obligated) {
        this.obligated = requireNonNullElse(obligated, BigDecimal.ZERO);
    }

    public BigDecimal getDisbursed() {
        return disbursed;
    }

    public void setDisbursed(BigDecimal disbursed) {
        this.disbursed = requireNonNullElse(disbursed, BigDecimal.ZERO);
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public BigDecimal getPriorTwoYearAmount() {
        return priorTwoYearAmount;
    }

    public void setPriorTwoYearAmount(BigDecimal priorTwoYearAmount) {
        this.priorTwoYearAmount = requireNonNullElse(priorTwoYearAmount, BigDecimal.ZERO);
    }

    public BigDecimal getCurrentTwoYearAmount() {
        return currentTwoYearAmount;
    }

    public void setCurrentTwoYearAmount(BigDecimal currentTwoYearAmount) {
        this.currentTwoYearAmount = requireNonNullElse(currentTwoYearAmount, BigDecimal.ZERO);
    }

    public BigDecimal getCurrentOneYearAmount() {
        return currentOneYearAmount;
    }

    public void setCurrentOneYearAmount(BigDecimal currentOneYearAmount) {
        this.currentOneYearAmount = requireNonNullElse(currentOneYearAmount, BigDecimal.ZERO);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = requireNonNullElse(amount, BigDecimal.ZERO);
    }

    public String getMajorObjectClass() {
        return majorObjectClass;
    }

    public void setMajorObjectClass(String majorObjectClass) {
        this.majorObjectClass = majorObjectClass;
    }

    public String getBudgetObjectClass() {
        return budgetObjectClass;
    }

    public void setBudgetObjectClass(String budgetObjectClass) {
        this.budgetObjectClass = budgetObjectClass;
    }

    public String getDivisionTitle() {
        return divisionTitle;
    }

    public void setDivisionTitle(String divisionTitle) {
        this.divisionTitle = divisionTitle;
    }

    public String getDivisionCode() {
        return divisionCode;
    }

    public void setDivisionCode(String divisionCode) {
        this.divisionCode = divisionCode;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public String getBranchTitle() {
        return branchTitle;
    }

    public void setBranchTitle(String branchTitle) {
        this.branchTitle = branchTitle;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public String getActivityNumber() {
        return activityNumber;
    }

    public void setActivityNumber(String activityNumber) {
        this.activityNumber = activityNumber;
    }

    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    public void setBudgetFiscalYear(String budgetFiscalYear) {
        this.budgetFiscalYear = budgetFiscalYear;
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatusString() {
        return status ? "Open" : "Closed";
    }
}