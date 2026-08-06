package gov.fjc.fis.entity.dto;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JmixEntity(name = "fis_PurchaseOrderDto")
public class PurchaseOrderDto {

    @CsvBindByPosition(position = 1)
    private String bbfy;

    @CsvBindByPosition(position = 2)
    private String ebfy;

    @CsvBindByPosition(position = 9)
    private String title;

    @CsvBindByPosition(position = 10)
    private String budgetObjectClass;

    @CsvBindByPosition(position = 20)
    private String project;

    @CsvBindByPosition(position = 11)
    private BigDecimal amount;

    @CsvBindByPosition(position = 17)
    private String addressCode;

    @CsvBindByPosition(position = 16)
    private String vendorCode;

    @CsvBindByPosition(position = 18)
    private String vendorName;

    @CsvBindByPosition(position = 19)
    private BigDecimal expendedAmount;

    @CsvBindByPosition(position = 12)
    private BigDecimal closedAmount;

    @CsvBindByPosition(position = 22)
    @CsvDate(value = "yyyy/MM/dd HH:mm:ss")
    private LocalDate closedDate;

    @CsvBindByPosition(position = 21)
    private String fjc;

    @CsvBindByPosition(position = 24)
    private BigDecimal orderedAmount;

    @CsvBindByPosition(position = 25)
    private BigDecimal outstandingAmount;

    @CsvBindByPosition(position = 26)
    private BigDecimal prepaidAmount;

    @CsvBindByPosition(position = 27)
    private BigDecimal refundedAmount;

    @CsvBindByPosition(position = 0)
    private String fundCode;

    @CsvBindByPosition(position = 3)
    private String budgetOrg;

    @CsvBindByPosition(position = 4)
    private String costOrg;

    @CsvBindByPosition(position = 8)
    @CsvDate(value = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime documentCreationDate;

    @CsvBindByPosition(position = 7)
    @CsvDate(value = "yyyy/MM/dd")
    private LocalDate documentDate;

    @CsvBindByPosition(position = 6)
    private String documentNumber;

    @CsvBindByPosition(position = 5)
    private String documentType;

    @CsvBindByPosition(position = 23)
    private String lastModifiedBy;

    @CsvBindByPosition(position = 13)
    private Integer lineNumber;

    @CsvBindByPosition(position = 14)
    private String taxId;

    @CsvBindByPosition(position = 15)
    private String taxIdType;

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public String getBbfy() {
        return bbfy;
    }

    public void setBbfy(String bbfy) {
        this.bbfy = bbfy;
    }

    public String getEbfy() {
        return ebfy;
    }

    public void setEbfy(String ebfy) {
        this.ebfy = ebfy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBudgetObjectClass() {
        return budgetObjectClass;
    }

    public void setBudgetObjectClass(String budgetObjectClass) {
        this.budgetObjectClass = budgetObjectClass;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getAddressCode() {
        return addressCode;
    }

    public void setAddressCode(String addressCode) {
        this.addressCode = addressCode;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public BigDecimal getExpendedAmount() {
        return expendedAmount;
    }

    public void setExpendedAmount(BigDecimal expendedAmount) {
        this.expendedAmount = expendedAmount;
    }

    public BigDecimal getClosedAmount() {
        return closedAmount;
    }

    public void setClosedAmount(BigDecimal closedAmount) {
        this.closedAmount = closedAmount;
    }

    public LocalDate getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(LocalDate closedDate) {
        this.closedDate = closedDate;
    }

    public String getFjc() {
        return fjc;
    }

    public void setFjc(String fjc) {
        this.fjc = fjc;
    }

    public BigDecimal getOrderedAmount() {
        return orderedAmount;
    }

    public void setOrderedAmount(BigDecimal orderedAmount) {
        this.orderedAmount = orderedAmount;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(BigDecimal outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public BigDecimal getPrepaidAmount() {
        return prepaidAmount;
    }

    public void setPrepaidAmount(BigDecimal prepaidAmount) {
        this.prepaidAmount = prepaidAmount;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
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

    public String getCostOrg() {
        return costOrg;
    }

    public void setCostOrg(String costOrg) {
        this.costOrg = costOrg;
    }

    public LocalDateTime getDocumentCreationDate() {
        return documentCreationDate;
    }

    public void setDocumentCreationDate(LocalDateTime documentCreationDate) {
        this.documentCreationDate = documentCreationDate;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getTaxIdType() {
        return taxIdType;
    }

    public void setTaxIdType(String taxIdType) {
        this.taxIdType = taxIdType;
    }

    public String getMajorObjectClass() {
        return budgetObjectClass.length() > 2 ? budgetObjectClass.substring(0, 2) : null;
    }

    @Override
    public String toString() {
        return "PurchaseOrderDto{" +
                "bbfy='" + bbfy + '\'' +
                ", ebfy='" + ebfy + '\'' +
                ", title='" + title + '\'' +
                ", boc='" + budgetObjectClass + '\'' +
                ", project='" + project + '\'' +
                ", amount=" + amount +
                ", addressCode='" + addressCode + '\'' +
                ", vendorCode='" + vendorCode + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", expendedAmount=" + expendedAmount +
                ", closedAmount=" + closedAmount +
                ", closedDate=" + closedDate +
                ", fjc='" + fjc + '\'' +
                ", orderedAmount=" + orderedAmount +
                ", outstandingAmount=" + outstandingAmount +
                ", prepaidAmount=" + prepaidAmount +
                ", refundedAmount=" + refundedAmount +
                ", fundCode='" + fundCode + '\'' +
                ", budgetOrg='" + budgetOrg + '\'' +
                ", costOrg='" + costOrg + '\'' +
                ", documentCreationDate=" + documentCreationDate +
                ", documentDate=" + documentDate +
                ", documentNumber='" + documentNumber + '\'' +
                ", documentType='" + documentType + '\'' +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", lineNumber=" + lineNumber +
                ", taxId='" + taxId + '\'' +
                ", taxIdType='" + taxIdType + '\'' +
                '}';
    }
}