package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.Document;
import gov.fjc.fis.entity.DocumentAudit;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Creates {@link DocumentAudit} records that document the outcome of JIFMS
 * document processing.
 *
 * <p>This service records document rejections, obligation inserts, and
 * obligation updates by capturing the imported document values together with
 * any obligation and activity projection audit data collected during
 * processing. The resulting audit records explain what actions were taken,
 * what values were changed, or why a document was rejected.
 *
 * <p>Audit records are persisted using {@link UnconstrainedDataManager} so
 * that processing and audit logging are performed independently of the
 * permissions assigned to the current user.
 *
 * @author Doug Mitchell
 */
@Component("fis_DocumentAuditService")
public class DocumentAuditService {
    private final UnconstrainedDataManager unconstrainedDataManager;
    private final JifmsQueryService jifmsQueryService;

    /**
     * Creates a document audit service.
     *
     * @param unconstrainedDataManager the data manager used to create and
     *                                 persist audit records without applying
     *                                 user access constraints
     * @param jifmsQueryService        the service used to query JIFMS processing data
     */
    public DocumentAuditService(UnconstrainedDataManager unconstrainedDataManager, JifmsQueryService jifmsQueryService) {
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.jifmsQueryService = jifmsQueryService;
    }

    /**
     * Records the rejection of a JIFMS document.
     *
     * <p>The audit record contains a process status of {@code R}, the supplied
     * error message, and a snapshot of the imported document. If an equivalent
     * rejection audit already exists, another audit record is not created.
     *
     * @param ctx   the resolved context containing the rejected document
     * @param error the message explaining why the document was rejected
     */
    void recordFailure(ResolvedContext ctx, String error) {
        var document = ctx.getDocument();
        // only write audit if rejection doesn't already exist
        if (jifmsQueryService.doesRejectionExist(document)) {
            return;
        }
        var audit = unconstrainedDataManager.create(DocumentAudit.class);
        audit.setProcessDate(LocalDate.now());
        audit.setProcessStatus("R");
        audit.setLoggedChanges(error);
        setDocumentFields(audit, ctx.getDocument());
        unconstrainedDataManager.saveWithoutReload(audit);
    }

    /**
     * Records the insertion of an obligation from a JIFMS document.
     *
     * <p>The audit record contains a process status of {@code I}, the logged
     * changes accumulated during processing, a snapshot of the imported
     * document, and any activity projection changes associated with the
     * insertion.
     *
     * @param ctx         the resolved context containing the processed document
     * @param auditRecord the audit information accumulated during processing
     */
    void recordInsert(ResolvedContext ctx, AuditRecord auditRecord) {
        var audit = unconstrainedDataManager.create(DocumentAudit.class);
        audit.setProcessDate(LocalDate.now());
        audit.setProcessStatus("I");
        audit.setLoggedChanges(auditRecord.getLoggedChanges());
        setDocumentFields(audit, ctx.getDocument());
        setProjectionFields(audit, auditRecord);
        unconstrainedDataManager.saveWithoutReload(audit);
    }

    /**
     * Records an update to an existing obligation from a JIFMS document.
     *
     * <p>The audit record contains a process status of {@code U}, the logged
     * changes accumulated during processing, a snapshot of the imported
     * document, and any obligation or activity projection values captured
     * before and during the update.
     *
     * @param ctx         the resolved context containing the processed document
     * @param auditRecord the audit information accumulated during processing
     */
    void recordUpdate(ResolvedContext ctx, AuditRecord auditRecord) {
        var audit = unconstrainedDataManager.create(DocumentAudit.class);
        audit.setProcessDate(LocalDate.now());
        audit.setProcessStatus("U");
        audit.setLoggedChanges(auditRecord.getLoggedChanges());
        setDocumentFields(audit, ctx.getDocument());
        setObligationFields(audit, auditRecord);
        setProjectionFields(audit, auditRecord);
        unconstrainedDataManager.saveWithoutReload(audit);
    }

    /**
     * Copies the imported document values to a document audit record.
     *
     * <p>The copied values preserve the state of the JIFMS document that was
     * evaluated by the processing pipeline.
     *
     * @param audit the audit record that receives the document values
     * @param doc   the imported JIFMS document
     */
    void setDocumentFields(DocumentAudit audit, Document doc) {
        audit.setDocumentFundCode(doc.getFundCode());
        audit.setDocumentBbfy(doc.getBbfy());
        audit.setDocumentEbfy(doc.getEbfy());
        audit.setDocumentBudgetOrg(doc.getBudgetOrg());
        audit.setDocumentCostOrg(doc.getCostOrg());
        audit.setDocumentDocumentType(doc.getDocumentType());
        audit.setDocumentDocumentNumber(doc.getDocumentNumber());
        audit.setDocumentDocumentDate(doc.getDocumentDate());
        audit.setDocumentDocumentCreationDate(doc.getDocumentCreationDate().toLocalDate());
        audit.setDocumentTitle(doc.getTitle());
        audit.setDocumentBudgetObjectClass(doc.getBudgetObjectClass());
        audit.setDocumentProject(doc.getProject());
        audit.setDocumentAmount(doc.getAmount());
        audit.setDocumentLineNumber(doc.getLineNumber());
        audit.setDocumentTaxId(doc.getTaxId());
        audit.setDocumentTaxIdType(doc.getTaxIdType());
        audit.setDocumentAddressCode(doc.getAddressCode());
        audit.setDocumentVendorCode(doc.getVendorCode());
        audit.setDocumentVendorName(doc.getVendorName());
        audit.setDocumentTravelStartDate(doc.getTravelStartDate());
        audit.setDocumentTravelEndDate(doc.getTravelEndDate());
        audit.setDocumentExpendedAmount(doc.getExpendedAmount());
        audit.setDocumentClosedAmount(doc.getClosedAmount());
        audit.setDocumentClosedDate(doc.getClosedDate());
        audit.setDocumentLastModifiedBy(doc.getLastModifiedBy());
        audit.setDocumentMajorObjectClass(doc.getMajorObjectClass());
        audit.setDocumentFjc(doc.getFjc());
    }

    /**
     * Copies the captured obligation values to a document audit record.
     *
     * <p>No fields are copied when the processing pipeline did not capture an
     * {@link ObligationAuditFields} snapshot.
     *
     * @param audit       the audit record that receives the obligation values
     * @param auditRecord the accumulated audit information that may contain
     *                    the obligation snapshot
     */
    void setObligationFields(DocumentAudit audit, AuditRecord auditRecord) {
        if (auditRecord.getObligationAuditFields().isPresent()) {
            var obligationAuditFields = auditRecord.getObligationAuditFields().get();
            audit.setObligationDocumentNumber(obligationAuditFields.documentNumber());
            audit.setObligationDocumentType(obligationAuditFields.documentType());
            audit.setObligationAmount(obligationAuditFields.amount());
            audit.setObligationProcessDate(obligationAuditFields.processDate());
            audit.setObligationVendor(obligationAuditFields.vendor());
            audit.setObligationStatus(obligationAuditFields.status());
            audit.setObligationEin(obligationAuditFields.ein());
            audit.setObligationTravelStartDate(obligationAuditFields.travelStartDate());
            audit.setObligationTravelEndDate(obligationAuditFields.travelEndDate());
            audit.setObligationModifiedDate(obligationAuditFields.modifiedDate());
            audit.setObligationActivityNumber(obligationAuditFields.activityNumber());
            audit.setObligationBudgetObjectClass(obligationAuditFields.budgetObjectClass());
            audit.setObligationDivisionCode(obligationAuditFields.divisionCode());
            audit.setObligationAddressCode(obligationAuditFields.addressCode());
            audit.setObligationVendorCode(obligationAuditFields.vendorCode());
        }
    }

    /**
     * Copies the captured activity projection values to a document audit
     * record.
     *
     * <p>The copied values describe the projection affected by the current
     * obligation and, when the obligation was reassigned, the previous
     * projection whose balance was restored. No fields are copied when the
     * processing pipeline did not capture projection audit information.
     *
     * @param audit       the audit record that receives the projection values
     * @param auditRecord the accumulated audit information that may contain
     *                    the projection changes
     */
    void setProjectionFields(DocumentAudit audit, AuditRecord auditRecord) {
        if (auditRecord.getProjectionAuditFields().isPresent()) {
            var projectionAuditFields = auditRecord.getProjectionAuditFields().get();
            audit.setCurrentActivityNumber(projectionAuditFields.getCurrentActivityNumber());
            audit.setCurrentProjectionBoc(projectionAuditFields.getCurrentProjectionBoc());
            audit.setCurrentProjectionAmountBefore(projectionAuditFields.getCurrentProjectionAmountBefore());
            audit.setCurrentProjectionAmountAfter(projectionAuditFields.getCurrentProjectionAmountAfter());
            audit.setPreviousActivityNumber(projectionAuditFields.getPreviousActivityNumber());
            audit.setPreviousProjectionBoc(projectionAuditFields.getPreviousProjectionBoc());
            audit.setPreviousProjectionAmountBefore(projectionAuditFields.getPreviousProjectionAmountBefore());
            audit.setPreviousProjectionAmountAfter(projectionAuditFields.getPreviousProjectionAmountAfter());
        }
    }
}