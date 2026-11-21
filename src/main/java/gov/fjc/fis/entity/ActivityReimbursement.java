package gov.fjc.fis.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static gov.fjc.fis.app.FisUtilities.getCreatedModifiedString;
import static gov.fjc.fis.app.FisUtilities.safeTrim;
import static java.util.Objects.requireNonNullElse;

/**
 * Represents a reimbursement received for an {@link Activity}.
 *
 * <p>An activity reimbursement records funding received from an external
 * source to support a program, project, or activity. Unlike appropriated
 * funds provided through the FJC budget process, reimbursement funds
 * originate from outside entities and are used to offset or supplement
 * activity costs.</p>
 *
 * <p>Each reimbursement is associated with an {@link Activity} and an
 * {@link ObjectClass}, allowing reimbursed funding to be tracked and
 * reported by budget object classification. The entity also captures
 * supporting information such as the funding source, reimbursement amount,
 * document number, document date, and optional explanatory notes.</p>
 *
 * <p>The entity supports financial accountability and reporting by
 * maintaining reimbursement details along with Jmix auditing and
 * optimistic locking information.</p>
 *
 * @author Doug Mitchell
 */
@JmixEntity
@Table(name = "FIS_ACTIVITY_REIMBURSEMENT", indexes = {
        @Index(name = "IDX_FIS_ACTIVITY_REIMBURSEMENT_ACTIVITY", columnList = "ACTIVITY_ID"),
        @Index(name = "IDX_FIS_ACTIVITY_REIMBURSEMENT_OBJECT_CLASS", columnList = "OBJECT_CLASS_ID"),
        @Index(name = "IDX_FIS_ACTIVITY_REIMBURSEMENT_ACTIVITY_OBJECTCLASS", columnList = "ACTIVITY_ID, OBJECT_CLASS_ID")
})
@Entity(name = "fis_ActivityReimbursement")
public class ActivityReimbursement {
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
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ObjectClass objectClass;

    @Column(name = "SOURCE")
    private String source;

    @Column(name = "AMOUNT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "DOCUMENT_NUMBER")
    private String documentNumber;

    @Column(name = "DOCUMENT_DATE")
    private LocalDate documentDate;

    @Column(name = "MEMO")
    @Lob
    private String memo;

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

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = safeTrim(memo);
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = safeTrim(documentNumber);
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = requireNonNullElse(amount, BigDecimal.ZERO);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = safeTrim(source);
    }

    public ObjectClass getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(ObjectClass objectClass) {
        this.objectClass = objectClass;
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
    @DependsOnProperties({"activity", "objectClass"})
    public String getInstanceName(MetadataTools metadataTools) {
        // this may require more to include fy, divcode, actnum, boc
        return String.format("%s %s",
                metadataTools.format(activity),
                metadataTools.format(objectClass));
    }
}