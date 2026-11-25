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

import static gov.fjc.fis.app.AsciiSanitizer.sanitizeToNull;
import static gov.fjc.fis.app.AuditTextUtils.getCreatedModifiedString;
import static gov.fjc.fis.app.BigDecimalUtils.sumNullSafe;
import static java.util.Objects.requireNonNullElse;

/**
 * Represents an adjustment to an {@link Appropriation} that affects the
 * available spending authority for a budget fiscal year.
 *
 * <p>Appropriation adjustments are used to record changes to enacted
 * appropriations after the initial budget authority has been established.
 * Examples include rescissions, sequestrations, reimbursements, transfers,
 * and other funding actions that increase or decrease available authority.
 * Each adjustment is recorded with an effective date, description, and
 * separate one-year and two-year funding amounts.</p>
 *
 * <p>The entity provides a historical record of appropriation-level funding
 * changes and supports the calculation of total available authority for the
 * fiscal year. Multiple adjustments may be associated with a single
 * appropriation, allowing the complete lifecycle of funding changes to be
 * tracked and reported.</p>
 *
 * <p>The entity also includes Jmix auditing metadata, calculated reporting
 * properties, and optimistic locking support.</p>
 *
 * @author Doug Mitchell
 */
@JmixEntity
@Table(name = "FIS_APPROPRIATION_ADJUSTMENT", indexes = {
        @Index(name = "IDX_FIS_APPROPRIATION_ADJUSTMENT_APPROPRIATION", columnList = "APPROPRIATION_ID")
})
@Entity(name = "fis_AppropriationAdjustment")
public class AppropriationAdjustment {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "APPROPRIATION_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Appropriation appropriation;

    @NotNull
    @Column(name = "ADJUSTMENT_DATE", nullable = false)
    private LocalDate adjustmentDate;

    @Column(name = "TITLE", nullable = false)
    @NotNull
    private String title;

    @Column(name = "ONE_YEAR_AMOUNT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal oneYearAmount = BigDecimal.ZERO;

    @Column(name = "TWO_YEAR_AMOUNT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal twoYearAmount = BigDecimal.ZERO;

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

    public void setAdjustmentDate(LocalDate adjustmentDate) {
        this.adjustmentDate = adjustmentDate;
    }

    public LocalDate getAdjustmentDate() {
        return adjustmentDate;
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    @DependsOnProperties({"oneYearAmount", "twoYearAmount"})
    @JmixProperty
    public BigDecimal getTotalAmount() {
        return sumNullSafe(oneYearAmount, twoYearAmount);
    }

    public BigDecimal getTwoYearAmount() {
        return requireNonNullElse(twoYearAmount, BigDecimal.ZERO);
    }

    public void setTwoYearAmount(BigDecimal twoYearAmount) {
        this.twoYearAmount = requireNonNullElse(twoYearAmount, BigDecimal.ZERO);
    }

    public BigDecimal getOneYearAmount() {
        return requireNonNullElse(oneYearAmount, BigDecimal.ZERO);
    }

    public void setOneYearAmount(BigDecimal oneYearAmount) {
        this.oneYearAmount = requireNonNullElse(oneYearAmount, BigDecimal.ZERO);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = sanitizeToNull(title);
    }

    public Appropriation getAppropriation() {
        return appropriation;
    }

    public void setAppropriation(Appropriation appropriation) {
        this.appropriation = appropriation;
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
    @DependsOnProperties({"appropriation", "title"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s %s",
                metadataTools.format(appropriation),
                metadataTools.format(title));
    }
}