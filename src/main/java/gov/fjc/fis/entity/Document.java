package gov.fjc.fis.entity;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static gov.fjc.fis.FisUtilities.getLoadedByString;

@JmixEntity
@Table(name = "FIS_DOCUMENT", indexes = {
        @Index(name = "IDX_FIS_DOCUMENT", columnList = "BBFY"),
        @Index(name = "IDX_FIS_PROCESS_IDX", columnList = "BBFY, BUDGET_ORG, DOCUMENT_NUMBER, BOC")
})
@Entity(name = "fis_Document")
public class Document {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "FUND_CODE", nullable = false, length = 6)
    @NotNull
    private String fundCode;

    @Column(name = "BBFY", nullable = false, length = 4)
    @NotNull
    private String bbfy;

    @Column(name = "EBFY", length = 4)
    private String ebfy;

    @Column(name = "BUDGET_ORG", nullable = false, length = 7)
    @NotNull
    private String budgetOrg;

    @Column(name = "COST_ORG", nullable = false, length = 7)
    @NotNull
    private String costOrg;

    @Column(name = "DOCUMENT_TYPE", nullable = false, length = 5)
    @NotNull
    private String documentType;

    @Column(name = "DOCUMENT_NUMBER", nullable = false, length = 50)
    @NotNull
    private String documentNumber;

    @NotNull
    @Column(name = "DOCUMENT_DATE", nullable = false)
    private LocalDate documentDate;

    @NotNull
    @Column(name = "DOCUMENT_CREATION_DATE", nullable = false)
    private LocalDateTime documentCreationDate;

    @Column(name = "TITLE", length = 70)
    private String title;

    @Column(name = "MOC", length = 2)
    private String majorObjectClass;

    @Column(name = "BOC", nullable = false, length = 7)
    @NotNull
    private String budgetObjectClass;

    @Column(name = "PROJECT", length = 4)
    private String project;

    @Column(name = "AMOUNT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal amount;

    @Column(name = "LINE_NUMBER", nullable = false)
    @NotNull
    private Integer lineNumber;

    @Column(name = "TAX_ID", length = 9)
    private String taxId;

    @Column(name = "TAX_ID_TYPE", length = 1)
    private String taxIdType;

    @Column(name = "ADDRESS_CODE", length = 15)
    private String addressCode;

    @Column(name = "VENDOR_CODE", length = 10)
    private String vendorCode;

    @Column(name = "VENDOR_NAME", length = 70)
    private String vendorName;

    @Column(name = "TRAVEL_START_DATE")
    private LocalDate travelStartDate;

    @Column(name = "TRAVEL_END_DATE")
    private LocalDate travelEndDate;

    @Column(name = "EXPENDED_AMOUNT", precision = 19, scale = 2)
    private BigDecimal expendedAmount;

    @Column(name = "CLOSED_AMOUNT", precision = 19, scale = 2)
    private BigDecimal closedAmount;

    @Column(name = "CLOSED_DATE")
    private LocalDate closedDate;

    @Column(name = "LAST_MODIFIED_BY", nullable = false, length = 45)
    @NotNull
    private String lastModifiedBy;

    @Column(name = "FJC", length = 20)
    private String fjc;

    @Column(name = "ORDERED_AMOUNT", precision = 19, scale = 2)
    private BigDecimal orderedAmount;

    @Column(name = "OUTSTANDING_AMOUNT", precision = 19, scale = 2)
    private BigDecimal outstandingAmount;

    @Column(name = "PREPAID_AMOUNT", precision = 19, scale = 2)
    private BigDecimal prepaidAmount;

    @Column(name = "REFUNDED_AMOUNT", precision = 19, scale = 2)
    private BigDecimal refundedAmount;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public void setDocumentCreationDate(LocalDateTime documentCreationDate) {
        this.documentCreationDate = documentCreationDate;
    }

    public LocalDateTime getDocumentCreationDate() {
        return documentCreationDate;
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

    public void setClosedDate(LocalDate closedDate) {
        this.closedDate = closedDate;
    }

    public LocalDate getClosedDate() {
        return closedDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
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

    public String getTaxIdType() {
        return taxIdType;
    }

    public void setTaxIdType(String taxIdType) {
        this.taxIdType = taxIdType;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
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

    public String getCostOrg() {
        return costOrg;
    }

    public void setCostOrg(String costOrg) {
        this.costOrg = costOrg;
    }

    public String getBudgetOrg() {
        return budgetOrg;
    }

    public void setBudgetOrg(String budgetOrg) {
        this.budgetOrg = budgetOrg;
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    @DependsOnProperties({"createdBy", "createdDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getLoadedByString(createdBy, createdDate);
    }

    @DependsOnProperties({"bbfy", "ebfy"})
    @JmixProperty
    public String getBfyString() {
        if (ebfy == null || ebfy.isEmpty()) {
            return bbfy;
        }
        return String.join("/", bbfy, ebfy);
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public BigDecimal getPrepaidAmount() {
        return prepaidAmount;
    }

    public void setPrepaidAmount(BigDecimal prepaidAmount) {
        this.prepaidAmount = prepaidAmount;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(BigDecimal outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public BigDecimal getOrderedAmount() {
        return orderedAmount;
    }

    public void setOrderedAmount(BigDecimal orderedAmount) {
        this.orderedAmount = orderedAmount;
    }

    public String getFjc() {
        return fjc;
    }

    public void setFjc(String fjc) {
        this.fjc = fjc;
    }

    public BigDecimal getClosedAmount() {
        return closedAmount;
    }

    public void setClosedAmount(BigDecimal closedAmount) {
        this.closedAmount = closedAmount;
    }

    public BigDecimal getExpendedAmount() {
        return expendedAmount;
    }

    public void setExpendedAmount(BigDecimal expendedAmount) {
        this.expendedAmount = expendedAmount;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getAddressCode() {
        return addressCode;
    }

    public void setAddressCode(String addressCode) {
        this.addressCode = addressCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEbfy() {
        return ebfy;
    }

    public void setEbfy(String ebfy) {
        this.ebfy = ebfy;
    }

    public String getBbfy() {
        return bbfy;
    }

    public void setBbfy(String bbfy) {
        this.bbfy = bbfy;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @InstanceName
    @DependsOnProperties({"fundCode", "documentNumber"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s-%s",
                metadataTools.format(fundCode),
                metadataTools.format(documentNumber));
    }
}