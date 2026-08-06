package gov.fjc.fis.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static gov.fjc.fis.FisUtilities.*;
import static java.util.Objects.requireNonNullElse;

@JmixEntity
@Table(name = "FIS_OBLIGATION", indexes = {
        @Index(name = "IDX_FIS_OBLIGATION_ACTIVITY", columnList = "ACTIVITY_ID"),
        @Index(name = "IDX_FIS_OBLIGATION_OBJECT_CLASS", columnList = "OBJECT_CLASS_ID"),
        @Index(name = "IDX_FIS_OBLIGATION_COST_ORG", columnList = "COST_ORG_ID"),
        @Index(name = "IDX_FIS_OBLIGATION", columnList = "ACTIVITY_ID, STATUS"),
        @Index(name = "IDX_FIS_OBLIGATION_ACTIVITY_OBJCLASS", columnList = "ACTIVITY_ID, OBJECT_CLASS_ID"),
        @Index(name = "IDX_FIS_OBLIGATION_VENDOR_CODE_ADDRESS_CODE", columnList = "VENDOR_CODE, ADDRESS_CODE")
}, uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_OBLIGATION_UNQ", columnNames = {"DOCID", "OBJECT_CLASS_ID"}),
        @UniqueConstraint(name = "IDX_FIS_OBLIGATION_UNQ_2", columnNames = {"DOCID", "LINE_NUMBER"})
})
@Entity(name = "fis_Obligation")
public class Obligation implements FileAttachable {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "ACTIVITY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Activity activity;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "OBJECT_CLASS_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ObjectClass objectClass;

    @Column(name = "DOCID", nullable = false, length = 20)
    @NotNull
    private String documentNumber;

    @NotNull
    @Column(name = "DOCUMENT_TYPE", nullable = false, length = 5)
    private String documentType;

    @Column(name = "LINE_NUMBER", nullable = false)
    @NotNull
    private Integer lineNumber = 1;

    @Column(name = "AMOUNT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal amount = BigDecimal.ZERO;

    @NotNull(message = "Obligation document date is required")
    @Column(name = "DOCUMENT_DATE", nullable = false)
    private LocalDate documentDate;

    @NotNull(message = "Obligation process date is required")
    @Column(name = "PROCESS_DATE", nullable = false)
    private LocalDate processDate;

    @Column(name = "AO_SEND")
    private Boolean aoSend = false;

    @Column(name = "AO_SYNC_DATE")
    private LocalDate aoSyncDate;

    @NotNull
    @Column(name = "VENDOR", nullable = false)
    private String vendor;

    @Column(name = "VENDOR_CODE", length = 10)
    private String vendorCode;

    @Column(name = "STATUS")
    private Boolean status = false;

    @Column(name = "EIN", length = 10)
    private String ein;

    @Column(name = "ADDRESS_CODE", length = 15)
    private String addressCode;

    @Column(name = "TRAVEL_START_DATE")
    private LocalDate travelStartDate;

    @Column(name = "TRAVEL_END_DATE")
    private LocalDate travelEndDate;

    @Column(name = "RECONCILED")
    private Boolean reconciled = false;

    @Column(name = "BPO")
    private Boolean blanketPurchaseOrder = false;

    @Column(name = "UPDATED")
    private Boolean updated = false;

    @Comment("legacy field for certain Foundation obligations from the 1990s")
    @Column(name = "BUDGET_ORG", length = 7)
    private String budgetOrg;

    @Comment("formerly responsible division")
    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "COST_ORG_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Division costOrg;

    @Column(name = "MEMO")
    @Lob
    private String memo;

    @OrderBy("invoiceDate, invoiceNumber")
    @Composition
    @OneToMany(mappedBy = "obligation")
    private List<Invoice> invoices;

    @OrderBy("fcnDate")
    @Composition
    @OneToMany(mappedBy = "obligation")
    private List<FundControlNotice> fundControlNotices;

    @Composition
    @OneToMany(mappedBy = "obligation")
    private List<FileAttachment> attachments;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    @LastModifiedBy
    @Column(name = "LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    private OffsetDateTime lastModifiedDate;

    public void setAoSyncDate(LocalDate aoSyncDate) {
        this.aoSyncDate = aoSyncDate;
    }

    public LocalDate getAoSyncDate() {
        return aoSyncDate;
    }

    public void setProcessDate(LocalDate processDate) {
        this.processDate = processDate;
    }

    public LocalDate getProcessDate() {
        return processDate;
    }

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public void setTravelEndDate(LocalDate travelEndDate) {
        this.travelEndDate = travelEndDate;
    }

    public LocalDate getTravelEndDate() {
        return travelEndDate;
    }

    public void setTravelStartDate(LocalDate travelStartDate) {
        this.travelStartDate = travelStartDate;
    }

    public LocalDate getTravelStartDate() {
        return travelStartDate;
    }

    public Division getCostOrg() {
        return costOrg;
    }

    public void setCostOrg(Division costOrg) {
        this.costOrg = costOrg;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType == null ? null : documentType.getId();
    }

    public DocumentType getDocumentType() {
        return documentType == null ? null : DocumentType.fromId(documentType);
    }

    @DependsOnProperties({"documentType"})
    @JmixProperty
    public String getDocumentTypeId() {
        return documentType;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = safeTrim(vendorCode);
    }

    public List<FileAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<FileAttachment> attachments) {
        this.attachments = attachments;
    }

    @DependsOnProperties({"blanketPurchaseOrder"})
    @JmixProperty
    public String getBpoString() {
        return blanketPurchaseOrder ? "BPO" : null;
    }

    @DependsOnProperties({"aoSyncDate"})
    @JmixProperty
    public String getAoSyncString() {
        return getAoSyncDateString(aoSyncDate);
    }

    public Boolean getBlanketPurchaseOrder() {
        return blanketPurchaseOrder;
    }

    public void setBlanketPurchaseOrder(Boolean blanketPurchaseOrder) {
        this.blanketPurchaseOrder = blanketPurchaseOrder;
    }

    public List<FundControlNotice> getFundControlNotices() {
        return fundControlNotices;
    }

    public void setFundControlNotices(List<FundControlNotice> fundControlNotices) {
        this.fundControlNotices = fundControlNotices;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }

    @DependsOnProperties({"documentNumber", "objectClass", "vendor"})
    @JmixProperty
    public String getSuggestion() {
        return objectClass == null ?
                String.format("%s - %s", documentNumber, vendor) :
                String.format("%s (%s) - %s", documentNumber, objectClass.getBudgetObjectClass(), vendor);
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    @DependsOnProperties({"status"})
    @JmixProperty
    public String getStatusString() {
        return status ? "Open" : "Closed";
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = safeTrim(memo);
    }

    public String getBudgetOrg() {
        return budgetOrg;
    }

    public void setBudgetOrg(String budgetOrg) {
        this.budgetOrg = safeTrim(budgetOrg);
    }

    public Boolean getUpdated() {
        return updated;
    }

    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }

    public Boolean getReconciled() {
        return reconciled;
    }

    public void setReconciled(Boolean reconciled) {
        this.reconciled = reconciled;
    }

    public String getAddressCode() {
        return addressCode;
    }

    public void setAddressCode(String addressCode) {
        this.addressCode = safeTrim(addressCode);
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = safeTrim(safeToUpperCase(ein));
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
        this.vendor = safeTrim(vendor);
    }

    public Boolean getAoSend() {
        return aoSend;
    }

    public void setAoSend(Boolean aoSend) {
        this.aoSend = aoSend;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = requireNonNullElse(amount, BigDecimal.ZERO);
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public ObjectClass getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(ObjectClass objectClass) {
        this.objectClass = objectClass;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = safeToUpperCase(documentNumber);
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public OffsetDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @InstanceName
    @DependsOnProperties({"documentNumber"})
    public String getInstanceName(MetadataTools metadataTools) {
        return metadataTools.format(documentNumber);
    }
}