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

/**
 * Represents a file attachment associated with an entity that implements
 * {@link FileAttachable}.
 *
 * <p>An attachment stores a reference to a file in the configured Jmix file
 * storage, along with metadata such as its content length, category, creator,
 * and creation date.
 *
 * <p>A file attachment may be associated with an {@link Activity},
 * {@link Obligation}, {@link Invoice}, or {@link FundControlNotice}. Depending
 * on the type of host entity, related parent associations may also be populated.
 * For example, an attachment associated with an invoice or fund control notice
 * may retain a reference to the corresponding obligation.
 *
 * <p>The entity provides calculated properties for displaying the attachment's
 * host type, file name, formatted size, descriptive title, and instance name.
 *
 * @author Doug Mitchell
 */
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

    /**
     * Determines the type of entity to which this file is attached.
     *
     * <p>When more than one association is populated, the host type is
     * determined using the following precedence:
     * {@link FileAttachmentEntityType#INVOICE INVOICE},
     * {@link FileAttachmentEntityType#FCN FCN},
     * {@link FileAttachmentEntityType#OBLIGATION OBLIGATION}, and
     * {@link FileAttachmentEntityType#ACTIVITY ACTIVITY}.
     *
     * @return the type of the associated host entity, or
     *         {@link FileAttachmentEntityType#UNKNOWN UNKNOWN} if the
     *         attachment is not associated with a supported entity
     */
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

    /**
     * Returns the attachment size formatted as a human-readable value.
     *
     * <p>For example, a content length may be displayed using units such as
     * bytes, KB, or MB.
     *
     * @return the formatted file size, or an empty string if the content
     *         length is unavailable
     */
    @DependsOnProperties({"contentLength"})
    @JmixProperty
    public String getFormattedSize() {
        return contentLength == null ? "" : FileUtils.byteCountToDisplaySize(contentLength);
    }

    /**
     * Returns a descriptive title identifying the entity to which the file
     * is attached.
     *
     * <p>For invoice and fund control notice attachments, the title includes
     * information from the related obligation. For obligation attachments,
     * it includes the obligation document number and vendor.
     *
     * @return a descriptive attachment title, or {@code null} when a title
     *         is not defined for the associated entity type
     */
    @DependsOnProperties({"activity", "obligation", "invoice", "fundControlNotice"})
    @JmixProperty
    public String getTitle() {
        return switch (getAttachedTo()) {
            case INVOICE ->
                    obligation.getDocumentNumber()
                            + ", invoice #: "
                            + invoice.getInvoiceNumber();
            case FCN ->
                    obligation.getDocumentNumber()
                            + ", FCN dated: "
                            + fundControlNotice.getFcnDate();
            case OBLIGATION ->
                    obligation.getDocumentNumber()
                            + ", "
                            + obligation.getVendor();
            default -> null;
        };
    }

    /**
     * Returns the fund control notice associated with this attachment.
     *
     * @return the associated fund control notice, or {@code null} if this
     *         attachment does not belong to a fund control notice
     */
    public FundControlNotice getFundControlNotice() {
        return fundControlNotice;
    }

    /**
     * Associates this attachment with a fund control notice.
     *
     * @param fundControlNotice the fund control notice, or {@code null} to
     *                          remove the association
     */
    public void setFundControlNotice(FundControlNotice fundControlNotice) {
        this.fundControlNotice = fundControlNotice;
    }

    /**
     * Returns the invoice associated with this attachment.
     *
     * @return the associated invoice, or {@code null} if this attachment
     *         does not belong to an invoice
     */
    public Invoice getInvoice() {
        return invoice;
    }

    /**
     * Associates this attachment with an invoice.
     *
     * @param invoice the invoice, or {@code null} to remove the association
     */
    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    /**
     * Returns the obligation associated with this attachment.
     *
     * <p>This association may also be populated when the attachment belongs
     * directly to an invoice or fund control notice related to the obligation.
     *
     * @return the associated obligation, or {@code null} if no obligation is
     *         associated with the attachment
     */
    public Obligation getObligation() {
        return obligation;
    }

    /**
     * Associates this attachment with an obligation.
     *
     * @param obligation the obligation, or {@code null} to remove the
     *                   association
     */
    public void setObligation(Obligation obligation) {
        this.obligation = obligation;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * Returns the size of the attached file in bytes.
     *
     * @return the content length in bytes, or {@code null} if the size is
     *         unavailable
     */
    public Long getContentLength() {
        return contentLength;
    }

    /**
     * Sets the size of the attached file.
     *
     * @param contentLength the content length in bytes, or {@code null} if
     *                      the size is unavailable
     */
    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Returns the reference used to locate the attached file in Jmix file
     * storage.
     *
     * @return the file reference
     */
    public FileRef getFileReference() {
        return fileReference;
    }

    /**
     * Sets the reference used to locate the attached file in Jmix file
     * storage.
     *
     * @param fileReference the file reference; must not be {@code null} when
     *                      the entity is persisted
     */
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

    /**
     * Returns the display name used to represent this attachment in the user
     * interface.
     *
     * <p>The instance name is based on the original name contained in the file
     * reference and is formatted using the supplied metadata tools.
     *
     * @param metadataTools the Jmix metadata utility used to format the
     *                      instance name
     * @return the formatted file name, or an empty or null-formatted value
     *         when no file reference is available
     */
    @InstanceName
    @DependsOnProperties({"fileReference"})
    public String getInstanceName(MetadataTools metadataTools) {
        return metadataTools.format(fileReference == null ? null : fileReference.getFileName());
    }
}