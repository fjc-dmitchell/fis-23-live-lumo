package gov.fjc.fis.entity;

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

import static gov.fjc.fis.FisUtilities.*;

@JmixEntity
@Table(name = "FIS_OBJECT_CLASS", indexes = {
        @Index(name = "IDX_FIS_OBJECT_CLASS_CATEGORY", columnList = "OBJECT_CATEGORY_ID")
}, uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_OBJECT_CLASS_UNQ", columnNames = {"OBJECT_CATEGORY_ID", "BOC"})
})
@Entity(name = "fis_ObjectClass")
public class ObjectClass {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "OBJECT_CATEGORY_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ObjectCategory objectCategory;

    @Pattern(message = "BOC must contain 4 digits", regexp = "^[0-9]{4}$")
    @Length(message = "The BOC must contain 4 digits.", min = 4, max = 4)
    @Column(name = "BOC", nullable = false, length = 4)
    @NotNull
    private String budgetObjectClass;

    @Column(name = "TITLE", nullable = false)
    @NotNull
    private String title;

    @Composition
    @OrderBy("documentNumber")
    @OneToMany(mappedBy = "objectClass")
    private List<Obligation> obligations;

    @Composition
    @OneToMany(mappedBy = "objectClass")
    private List<ActivityProjection> projections;

    @Composition
    @OneToMany(mappedBy = "objectClass")
    private List<ActivityReimbursement> reimbursements;

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

    public List<ActivityReimbursement> getReimbursements() {
        return reimbursements;
    }

    public void setReimbursements(List<ActivityReimbursement> reimbursements) {
        this.reimbursements = reimbursements;
    }

    public List<ActivityProjection> getProjections() {
        return projections;
    }

    public void setProjections(List<ActivityProjection> projections) {
        this.projections = projections;
    }

    public List<Obligation> getObligations() {
        return obligations;
    }

    public void setObligations(List<Obligation> obligations) {
        this.obligations = obligations;
    }

    @DependsOnProperties({"title", "budgetObjectClass"})
    @JmixProperty
    public String getTitleAndCode() {
        return String.format("%s (%s)", title, budgetObjectClass);
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = safeTrim(title);
    }

    public String getBudgetObjectClass() {
        return budgetObjectClass;
    }

    public void setBudgetObjectClass(String budgetObjectClass) {
        this.budgetObjectClass = safeToUpperCase(budgetObjectClass);
    }

    public ObjectCategory getObjectCategory() {
        return objectCategory;
    }

    public void setObjectCategory(ObjectCategory category) {
        this.objectCategory = category;
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
    @DependsOnProperties({"budgetObjectClass", "title"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s %s",
                metadataTools.format(budgetObjectClass),
                metadataTools.format(title));
    }
}