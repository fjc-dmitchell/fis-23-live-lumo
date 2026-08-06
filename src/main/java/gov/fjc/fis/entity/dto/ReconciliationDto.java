package gov.fjc.fis.entity.dto;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@JmixEntity(name = "fis_ReconciliationDto")
public class ReconciliationDto {

    @JmixGeneratedValue
    @JmixId
    private Integer id;

    private String budgetFiscalYear;

    private String fundCode;

    private String budgetOrg;

    private String divisionTitle;

    private String documentNumber;

    private String majorObjectClass;

    private String categoryTitle;

    private String budgetObjectClass;

    private LocalDate documentDate;

    private BigDecimal amount;

    private String vendor;

    private String ein;

    private Boolean aoSync;

    private LocalDate aoSyncDate;

    public Boolean getAoSync() {
        return aoSync;
    }

    public void setAoSync(Boolean aoSync) {
        this.aoSync = aoSync;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getBudgetOrg() {
        return budgetOrg;
    }

    public void setBudgetOrg(String budgetOrg) {
        this.budgetOrg = budgetOrg;
    }

    public String getDivisionTitle() {
        return divisionTitle;
    }

    public void setDivisionTitle(String divisionTitle) {
        this.divisionTitle = divisionTitle;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getMajorObjectClass() {
        return majorObjectClass;
    }

    public void setMajorObjectClass(String majorObjectClass) {
        this.majorObjectClass = majorObjectClass;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public String getBudgetObjectClass() {
        return budgetObjectClass;
    }

    public void setBudgetObjectClass(String budgetObjectClass) {
        this.budgetObjectClass = budgetObjectClass;
    }

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public LocalDate getAoSyncDate() {
        return aoSyncDate;
    }

    public void setAoSyncDate(LocalDate aoSyncDate) {
        this.aoSyncDate = aoSyncDate;
    }
}