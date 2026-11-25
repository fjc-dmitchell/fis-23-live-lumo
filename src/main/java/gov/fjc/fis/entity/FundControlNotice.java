package gov.fjc.fis.entity;

import gov.fjc.fis.app.StringUtils;
import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.*;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
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

import static gov.fjc.fis.app.AsciiSanitizer.sanitizeToNull;
import static gov.fjc.fis.app.AuditTextUtils.getAoSyncDateString;
import static gov.fjc.fis.app.AuditTextUtils.getCreatedModifiedString;

/**
 * Represents a Fund Control Notice (FCN) issued for an
 * {@link Obligation}.
 *
 * <p>A Fund Control Notice is an official budget authorization document
 * used to allocate, adjust, or restrict spending authority for specific
 * funds, programs, projects, or organizational units. Within FIS, an FCN
 * is used to increase or decrease the amount of an obligation, such as
 * a purchase order or travel authorization.</p>
 *
 * <p>Each FCN records the effective date of the authorization, the amount
 * of the adjustment, and supporting administrative information necessary
 * for budget execution and financial management. Additional information,
 * including travel dates, budget organization, employee identification
 * information, explanatory notes, and supporting documentation, may also
 * be maintained as part of the notice.</p>
 *
 * <p>The entity supports integration with administrative systems,
 * maintains associated file attachments, and provides a historical record
 * of funding authority changes affecting an obligation. Jmix auditing and
 * optimistic locking facilities are used to maintain accountability and
 * data integrity.</p>
 *
 * @author Doug Mitchell
 */
@JmixEntity
@Table(name = "FIS_FCN", indexes = {
        @Index(name = "IDX_FIS_FCN_OBLIGATION", columnList = "OBLIGATION_ID")
})
@Entity(name = "fis_FundControlNotice")
public class FundControlNotice implements FileAttachable {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "OBLIGATION_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Obligation obligation;

    @NotNull
    @Column(name = "FCN_DATE", nullable = false)
    private LocalDate fcnDate;

    @Column(name = "AMOUNT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "TRAVEL_START_DATE")
    private LocalDate travelStartDate;

    @Column(name = "TRAVEL_END_DATE")
    private LocalDate travelEndDate;

    @Column(name = "BUDGET_ORG", length = 7)
    private String budgetOrg;

    @Column(name = "EIN", length = 10)
    private String ein;

    @Column(name = "MEMO")
    @Lob
    private String memo;

    @Column(name = "AO_SEND", nullable = false)
    @NotNull
    private Boolean aoSend = false;

    @Column(name = "AO_SYNC_DATE")
    private LocalDate aoSyncDate;

    @Composition
    @OneToMany(mappedBy = "fundControlNotice")
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

    public void setFcnDate(LocalDate fcnDate) {
        this.fcnDate = fcnDate;
    }

    public LocalDate getFcnDate() {
        return fcnDate;
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

    public List<FileAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<FileAttachment> attachments) {
        this.attachments = attachments;
    }

    @DependsOnProperties({"aoSyncDate"})
    @JmixProperty
    public String getAoSyncString() {
        return getAoSyncDateString(aoSyncDate);
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    public Boolean getAoSend() {
        return aoSend;
    }

    public void setAoSend(Boolean aoSend) {
        this.aoSend = aoSend;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = sanitizeToNull(memo);
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = sanitizeToNull(StringUtils.toUpperCase(ein));
    }

    public String getBudgetOrg() {
        return budgetOrg;
    }

    public void setBudgetOrg(String budgetOrg) {
        this.budgetOrg = sanitizeToNull(budgetOrg);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Obligation getObligation() {
        return obligation;
    }

    public void setObligation(Obligation obligation) {
        this.obligation = obligation;
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
    @DependsOnProperties({"fcnDate"})
    public String getInstanceName(DatatypeFormatter datatypeFormatter) {
        return datatypeFormatter.formatLocalDate(fcnDate);
    }
}