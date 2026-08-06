package gov.fjc.fis.reportdata;

import java.math.BigDecimal;
import java.util.Date;

public class ReconciliationReportData {

    private String budgetFiscalYear;
    private String fundCode;
    private String budgetOrg;
    private String divisionTitle;
    private String documentNumber;
    private String majorObjectClass;
    private String categoryTitle;
    private String budgetObjectClass;
    private Date documentDate;
    private BigDecimal obligationAmount;
    private String vendor;
    private String taxId;
    private Date aoSyncDate;

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

    public Date getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(Date documentDate) {
        this.documentDate = documentDate;
    }

    public BigDecimal getObligationAmount() {
        return obligationAmount;
    }

    public void setObligationAmount(BigDecimal obligationAmount) {
        this.obligationAmount = obligationAmount;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public Date getAoSyncDate() {
        return aoSyncDate;
    }

    public void setAoSyncDate(Date aoSyncDate) {
        this.aoSyncDate = aoSyncDate;
    }
}
