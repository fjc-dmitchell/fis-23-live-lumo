package gov.fjc.fis.entity.personnel;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;
import java.util.List;

import static gov.fjc.fis.app.FisUtilities.getCreatedModifiedString;

/**
 * Represents a personnel action code used to classify changes affecting an
 * employee's status, pay, or position.
 *
 * <p>Within the Financial Information System (FIS), an action code identifies
 * the type of personnel change represented by a {@link PositionAction}. The
 * code is used to trigger the corresponding change in projected employee
 * compensation and benefit costs.</p>
 *
 * <p>Action codes generally correspond to the three-digit federal Nature of
 * Action Codes used for personnel actions, including appointments,
 * separations, returns to duty, conversions, nonpay or nonduty actions,
 * position changes, and pay changes. Each action code is uniquely identified
 * by its Nature of Action Code and includes a descriptive title.</p>
 *
 * <p>The entity is the composition owner of its associated personnel actions
 * and includes Jmix auditing metadata and optimistic locking support.</p>
 *
 * @author Doug Mitchell
 */
@JmixEntity
@Table(name = "FIS_ACTION_CODE", uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_ACTION_CODE_UNQ", columnNames = {"NOA_CODE"})
})
@Entity(name = "fis_ActionCode")
public class ActionCode {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "NOA_CODE", nullable = false, length = 3)
    @NotNull
    private String natureOfActionCode;

    @Column(name = "TITLE", nullable = false)
    @NotNull
    private String title;

    @Composition
    @OneToMany(mappedBy = "actionCode")
    private List<PositionAction> actions;

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

    public List<PositionAction> getActions() {
        return actions;
    }

    public void setActions(List<PositionAction> actions) {
        this.actions = actions;
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
        this.title = title;
    }

    public String getNatureOfActionCode() {
        return natureOfActionCode;
    }

    public void setNatureOfActionCode(String natureOfActionCode) {
        this.natureOfActionCode = natureOfActionCode;
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
    @DependsOnProperties({"natureOfActionCode", "title"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s-%s",
                metadataTools.format(natureOfActionCode),
                metadataTools.format(title));
    }
}