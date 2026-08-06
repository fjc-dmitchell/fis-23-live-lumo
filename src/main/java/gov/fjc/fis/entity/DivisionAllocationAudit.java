package gov.fjc.fis.entity;

import gov.fjc.fis.FisUtilities;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static gov.fjc.fis.FisUtilities.convertOffsetDateTimeToLocalDateTime;
import static java.util.Objects.requireNonNullElse;

@JmixEntity
@Table(name = "FIS_DIVISION_ALLOCATION_AUDIT", indexes = {
        @Index(name = "IDX_FIS_DIVISION_ALLOCATION_AUDIT_DIVISION", columnList = "DIVISION_ID"),
        @Index(name = "IDX_FIS_DIVISION_ALLOCATION_AUDIT_OBJECT_CATEGORY", columnList = "OBJECT_CATEGORY_ID")
})
@Entity(name = "fis_DivisionAllocationAudit")
public class DivisionAllocationAudit {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "DIVISION_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Division division;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "OBJECT_CATEGORY_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ObjectCategory objectCategory;

    @Column(name = "OLD_ONE_YEAR_AMOUNT", precision = 19, scale = 2)
    private BigDecimal oldOneYearAmount;
    @Column(name = "NEW_ONE_YEAR_AMOUNT", precision = 19, scale = 2)
    private BigDecimal newOneYearAmount;
    @Column(name = "OLD_TWO_YEAR_AMOUNT", precision = 19, scale = 2)
    private BigDecimal oldTwoYearAmount;
    @Column(name = "NEW_TWO_YEAR_AMOUNT", precision = 19, scale = 2)
    private BigDecimal newTwoYearAmount;

    @Column(name = "CHANGE_TYPE", nullable = false, length = 1)
    @NotNull
    private String changeType;
    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;
    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;
    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;
    @Column(name = "UPDATED_ATTRIBUTES")
    private String updatedAttributes;

    public String getUpdatedAttributes() {
        return updatedAttributes;
    }

    public void setUpdatedAttributes(String updatedAttributes) {
        this.updatedAttributes = updatedAttributes;
    }

    @DependsOnProperties({"createdDate"})
    @JmixProperty
    public LocalDateTime getAuditDate() {
        return convertOffsetDateTimeToLocalDateTime(createdDate);
    }

    public AuditChangeType getChangeType() {
        return changeType == null ? null : AuditChangeType.fromId(changeType);
    }

    public void setChangeType(AuditChangeType changeType) {
        this.changeType = changeType == null ? null : changeType.getId();
    }

    @JmixProperty
    public BigDecimal getTwoYearChangeAmount() {
        return requireNonNullElse(newTwoYearAmount, BigDecimal.ZERO).subtract(requireNonNullElse(oldTwoYearAmount, BigDecimal.ZERO));
    }

    @JmixProperty
    public BigDecimal getOneYearChangeAmount() {
        return requireNonNullElse(newOneYearAmount, BigDecimal.ZERO).subtract(requireNonNullElse(oldOneYearAmount, BigDecimal.ZERO));
    }

    @DependsOnProperties({"createdBy", "createdDate"})
    @JmixProperty
    public String getCreatedByString() {
        return FisUtilities.getCreatedModifiedString(createdBy, createdDate);
    }

    public BigDecimal getNewTwoYearAmount() {
        return newTwoYearAmount;
    }

    public void setNewTwoYearAmount(BigDecimal newTwoYearAmount) {
        this.newTwoYearAmount = newTwoYearAmount;
    }

    public BigDecimal getOldTwoYearAmount() {
        return oldTwoYearAmount;
    }

    public void setOldTwoYearAmount(BigDecimal oldTwoYearAmount) {
        this.oldTwoYearAmount = oldTwoYearAmount;
    }

    public BigDecimal getNewOneYearAmount() {
        return newOneYearAmount;
    }

    public void setNewOneYearAmount(BigDecimal newOneYearAmount) {
        this.newOneYearAmount = newOneYearAmount;
    }

    public BigDecimal getOldOneYearAmount() {
        return oldOneYearAmount;
    }

    public void setOldOneYearAmount(BigDecimal oldOneYearAmount) {
        this.oldOneYearAmount = oldOneYearAmount;
    }

    public ObjectCategory getObjectCategory() {
        return objectCategory;
    }

    public void setObjectCategory(ObjectCategory objectCategory) {
        this.objectCategory = objectCategory;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
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
    @DependsOnProperties({"division", "objectCategory"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s %s",
                metadataTools.format(division),
                metadataTools.format(objectCategory));
    }
}