package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.DocumentAudit;

import java.util.Optional;

/**
 * Collects audit information generated while processing a JIFMS document.
 *
 * <p>This class serves as a temporary container used by the processing
 * pipeline to accumulate details about changes made to an obligation and its
 * activity projections. The collected information is subsequently persisted
 * as a {@link DocumentAudit} by {@link DocumentAuditService}.
 *
 * <p>The obligation and projection audit fields are optional because a
 * processing outcome may not affect both types of records. For example, a
 * processor that does not modify an activity projection does not populate
 * projection audit fields.
 *
 * @author Doug Mitchell
 */
public class AuditRecord {
    private String loggedChanges;
    private ObligationAuditFields obligationAuditFields;   // null if obligation wasn't touched
    private ProjectionAuditFields projectionAuditFields;   // null if projection wasn't touched

    /**
     * Returns the description of the changes made during processing.
     *
     * @return the logged change description, or {@code null} if no description
     *         has been recorded
     */
    public String getLoggedChanges() {
        return loggedChanges;
    }

    /**
     * Sets the description of the changes made during processing.
     *
     * @param loggedChanges the change description
     */
    public void setLoggedChanges(String loggedChanges) {
        this.loggedChanges = loggedChanges;
    }

    /**
     * Returns the captured obligation values.
     *
     * @return the obligation audit fields, or an empty optional if the
     *         obligation was not modified
     */
    public Optional<ObligationAuditFields> getObligationAuditFields() {
        return Optional.ofNullable(obligationAuditFields);
    }

    /**
     * Sets the obligation values captured for auditing.
     *
     * @param obligationAuditFields the captured obligation values
     */
    public void setObligationAuditFields(ObligationAuditFields obligationAuditFields) {
        this.obligationAuditFields = obligationAuditFields;
    }

    /**
     * Returns the captured activity projection changes.
     *
     * @return the projection audit fields, or an empty optional if no
     *         projection was modified
     */
    public Optional<ProjectionAuditFields> getProjectionAuditFields() {
        return Optional.ofNullable(projectionAuditFields);
    }

    /**
     * Sets the activity projection changes captured for auditing.
     *
     * @param projectionAuditFields the captured projection changes
     */
    public void setProjectionAuditFields(ProjectionAuditFields projectionAuditFields) {
        this.projectionAuditFields = projectionAuditFields;
    }
}
