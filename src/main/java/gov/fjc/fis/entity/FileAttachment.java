package gov.fjc.fis.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.FileRef;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@JmixEntity
@Table(name = "FIS_FILE_ATTACHMENT", indexes = {
        @Index(name = "IDX_FIS_FILE_ATTACHMENT_ACTIVITY", columnList = "ACTIVITY_ID"),
        @Index(name = "IDX_FIS_FILE_ATTACHMENT_OBLIGATION", columnList = "OBLIGATION_ID"),
        @Index(name = "IDX_FIS_FILE_ATTACHMENT_INVOICE", columnList = "INVOICE_ID"),
        @Index(name = "IDX_FIS_FILE_ATTACHMENT_FUND_CONTROL_NOTICE", columnList = "FUND_CONTROL_NOTICE_ID"),
        @Index(name = "IDX_FIS_FILE_ATTACHMENT_CATEGORY", columnList = "CATEGORY_ID")
})
@Entity(name = "fis_FileAttachment")
public class FileAttachment {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "FILE_REFERENCE", nullable = false, length = 1024)
    @NotNull
    private FileRef fileReference;

    @Column(name = "CONTENT_LENGTH")
    private Long contentLength;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "ACTIVITY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Activity activity;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "OBLIGATION_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Obligation obligation;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "INVOICE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Invoice invoice;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "CATEGORY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private FileAttachmentCategory category;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "FUND_CONTROL_NOTICE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private FundControlNotice fundControlNotice;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    public FileAttachmentCategory getCategory() {
        return category;
    }

    public void setCategory(FileAttachmentCategory category) {
        this.category = category;
    }

    @DependsOnProperties({"activity", "obligation", "invoice", "fundControlNotice"})
    @JmixProperty
    public FileAttachmentEntityType getAttachedTo() {
        if (invoice != null) {
            return FileAttachmentEntityType.INVOICE;
        } else if (fundControlNotice != null) {
            return FileAttachmentEntityType.FCN;
        } else if (obligation != null) {
            return FileAttachmentEntityType.OBLIGATION;
        } else if (activity != null) {
            return FileAttachmentEntityType.ACTIVITY;
        }
        return FileAttachmentEntityType.UNKNOWN;
    }

    @DependsOnProperties({"fileReference"})
    @JmixProperty
    public String getFileName() {
        return fileReference == null ? null : fileReference.getFileName();
    }

    @DependsOnProperties({"contentLength"})
    @JmixProperty
    public String getFormattedSize() {
        return contentLength == null ? "" : FileUtils.byteCountToDisplaySize(contentLength);
    }

    @DependsOnProperties({"activity", "obligation", "invoice", "fundControlNotice"})
    @JmixProperty
    public String getTitle() {
        return switch (getAttachedTo()) {
            case INVOICE -> obligation.getDocumentNumber() + ", invoice #: " + invoice.getInvoiceNumber();
            case FCN -> obligation.getDocumentNumber() + ", FCN dated: " + fundControlNotice.getFcnDate();
            case OBLIGATION -> obligation.getDocumentNumber()+", "+obligation.getVendor();
            default -> null;
        };
    }

    public FundControlNotice getFundControlNotice() {
        return fundControlNotice;
    }

    public void setFundControlNotice(FundControlNotice fundControlNotice) {
        this.fundControlNotice = fundControlNotice;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Obligation getObligation() {
        return obligation;
    }

    public void setObligation(Obligation obligation) {
        this.obligation = obligation;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public FileRef getFileReference() {
        return fileReference;
    }

    public void setFileReference(FileRef fileReference) {
        this.fileReference = fileReference;
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
    @DependsOnProperties({"fileReference"})
    public String getInstanceName(MetadataTools metadataTools) {
        return metadataTools.format(fileReference == null ? null : fileReference.getFileName());
    }
}