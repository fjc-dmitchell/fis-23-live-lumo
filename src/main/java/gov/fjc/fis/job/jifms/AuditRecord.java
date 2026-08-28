package gov.fjc.fis.job.jifms;

import java.util.Optional;

public class AuditRecord {
    private String loggedChanges;
    private ObligationAuditFields obligationAuditFields;   // null if obligation wasn't touched
    private ProjectionAuditFields projectionAuditFields;   // null if projection wasn't touched

    public String getLoggedChanges() {
        return loggedChanges;
    }

    public void setLoggedChanges(String loggedChanges) {
        this.loggedChanges = loggedChanges;
    }

    public Optional<ObligationAuditFields> getObligationAuditFields() {
        return Optional.ofNullable(obligationAuditFields);
    }

    public void setObligationAuditFields(ObligationAuditFields obligationAuditFields) {
        this.obligationAuditFields = obligationAuditFields;
    }

    public Optional<ProjectionAuditFields> getProjectionAuditFields() {
        return Optional.ofNullable(projectionAuditFields);
    }

    public void setProjectionAuditFields(ProjectionAuditFields projectionAuditFields) {
        this.projectionAuditFields = projectionAuditFields;
    }
}
