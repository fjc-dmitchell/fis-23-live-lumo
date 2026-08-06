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
import java.time.OffsetDateTime;

import static gov.fjc.fis.FisUtilities.*;
import static java.util.Objects.requireNonNullElse;

@JmixEntity
@Table(name = "FIS_DIVISION_ALLOCATION", indexes = {
        @Index(name = "IDX_FIS_DIVISION_ALLOCATION_DIVISION", columnList = "DIVISION_ID"),
        @Index(name = "IDX_FIS_DIVISION_ALLOCATION_OBJECT_CATEGORY", columnList = "OBJECT_CATEGORY_ID")
}, uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_DIVISION_ALLOCATION_UNQ", columnNames = {"DIVISION_ID", "OBJECT_CATEGORY_ID"})
})
@Entity(name = "fis_DivisionAllocation")
public class DivisionAllocation {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "DIVISION_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Division division;

    @NotNull
    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "OBJECT_CATEGORY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ObjectCategory objectCategory;

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

    @DependsOnProperties({"oneYearAmount", "twoYearAmount"})
    @JmixProperty
    public BigDecimal getTotalAmount() {
        return add(oneYearAmount, twoYearAmount);
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    public BigDecimal getTwoYearAmount() {
        return twoYearAmount;
    }

    public void setTwoYearAmount(BigDecimal twoYearAmount) {
        this.twoYearAmount = requireNonNullElse(twoYearAmount, BigDecimal.ZERO);
    }

    public BigDecimal getOneYearAmount() {
        return oneYearAmount;
    }

    public void setOneYearAmount(BigDecimal oneYearAmount) {
        this.oneYearAmount = requireNonNullElse(oneYearAmount, BigDecimal.ZERO);
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
    @DependsOnProperties({"division", "objectCategory"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s %s",
                metadataTools.format(division),
                metadataTools.format(objectCategory));
    }
}