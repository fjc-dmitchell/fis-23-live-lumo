package gov.fjc.fis.entity.personnel;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.OffsetDateTime;

/**
 * Represents an audit record generated when a {@link Position} is created or
 * updated during synchronization with employee data received from HRMIS.
 *
 * <p>Position audit records provide a historical record of changes made to
 * position information as part of the employee-to-position synchronization
 * process. Whenever a change is detected between an HRMIS employee record
 * and its corresponding position record, an audit entry is created to
 * capture the affected field and its before-and-after values.</p>
 *
 * <p>Each audit record identifies the position, the field that changed,
 * the previous value, the new value, and the date and time the change
 * occurred. This information supports reconciliation, validation, and
 * troubleshooting of position data derived from HRMIS employee feeds.</p>
 *
 * <p>The entity serves as a permanent audit trail of automated position
 * updates, providing accountability and visibility into workforce and
 * compensation-related data changes over time.</p>
 *
 * @author Doug Mitchell
 */
@JmixEntity
@Table(name = "FIS_POSITION_AUDIT")
@Entity(name = "fis_PositionAudit")
public class PositionAudit {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "POSITION_NBR", length = 8)
    private String positionNbr;

    @InstanceName
    @Column(name = "NAME", nullable = false)
    @NotNull
    private String name;

    @Column(name = "FIELD_NAME", nullable = false)
    @NotNull
    private String fieldName;

    @Column(name = "OLD_VALUE")
    private String oldValue;

    @Column(name = "NEW_VALUE")
    private String newValue;

    @Column(name = "CHANGED_DATE", nullable = false)
    @NotNull
    private OffsetDateTime changedDate;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OffsetDateTime getChangedDate() {
        return changedDate;
    }

    public void setChangedDate(OffsetDateTime changedDate) {
        this.changedDate = changedDate;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getPositionNbr() {
        return positionNbr;
    }

    public void setPositionNbr(String positionNbr) {
        this.positionNbr = positionNbr;
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

}