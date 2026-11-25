package gov.fjc.fis.entity;

import gov.fjc.fis.app.StringUtils;
import io.jmix.core.DeletePolicy;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;
import java.util.List;

import static gov.fjc.fis.app.AuditTextUtils.getCreatedModifiedString;
import static gov.fjc.fis.app.FisUtilities.*;

/**
 * Represents a budget object category, also known as a major object class,
 * within an {@link Appropriation}.
 *
 * <p>A budget object category is a high-level federal budgeting and
 * accounting classification used to group obligations according to the
 * type of goods, property, services, personnel compensation, or other
 * expenditures being financed. Major object classes provide a summarized
 * view of spending and serve as the parent classification for individual
 * budget object classes.</p>
 *
 * <p>Within FIS, an object category is defined within an appropriation and
 * is identified by a two-digit major object class (MOC) code and title.
 * Each object category contains one or more {@link ObjectClass} records
 * that provide a more detailed breakdown of spending classifications.
 * Object categories are also used to allocate funding to divisions through
 * {@link DivisionAllocation} records.</p>
 *
 * <p>The entity is the composition owner of its associated budget object
 * classes and division allocations and supports budgeting, allocation,
 * reporting, and financial analysis throughout the system. Jmix auditing
 * metadata and optimistic locking are provided to ensure accountability
 * and data integrity.</p>
 *
 * @author Doug Mitchell
 */
@JmixEntity
@Table(name = "FIS_OBJECT_CATEGORY", indexes = {
        @Index(name = "IDX_FIS_OBJECT_CATEGORY_APPROPRIATION", columnList = "APPROPRIATION_ID")
}, uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_OBJECT_CATEGORY_UNQ", columnNames = {"APPROPRIATION_ID", "MOC"})
})
@Entity(name = "fis_ObjectCategory")
public class ObjectCategory {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "APPROPRIATION_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Appropriation appropriation;

    @Pattern(message = "MOC must contain 2 digits.", regexp = "^[0-9]{2}$")
    @Length(message = "The MOC must contain 2 digits.", min = 2, max = 2)
    @Column(name = "MOC", nullable = false, length = 2)
    @NotNull
    private String majorObjectClass;

    @Column(name = "TITLE", nullable = false)
    @NotNull
    private String title;

    @Composition
    @OrderBy("budgetObjectClass")
    @OneToMany(mappedBy = "objectCategory")
    private List<ObjectClass> budgetObjectClasses;

    @Composition
    @OneToMany(mappedBy = "objectCategory")
    private List<DivisionAllocation> allocations;

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

    public List<DivisionAllocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<DivisionAllocation> allocations) {
        this.allocations = allocations;
    }

    public List<ObjectClass> getBudgetObjectClasses() {
        return budgetObjectClasses;
    }

    public void setBudgetObjectClasses(List<ObjectClass> budgetObjectClasses) {
        this.budgetObjectClasses = budgetObjectClasses;
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    @DependsOnProperties({"title", "majorObjectClass"})
    @JmixProperty
    public String getTitleAndCode() {
        return String.format("%s (%s)", title, majorObjectClass);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = safeTrim(title);
    }

    public String getMajorObjectClass() {
        return majorObjectClass;
    }

    public void setMajorObjectClass(String majorObjectClass) {
        this.majorObjectClass = safeTrim(StringUtils.toUpperCase(majorObjectClass));
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
    @DependsOnProperties({"majorObjectClass", "title"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s %s",
                metadataTools.format(majorObjectClass),
                metadataTools.format(title));
    }
}