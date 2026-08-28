package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.Document;
import gov.fjc.fis.entity.DocumentAudit;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component("fis_DocumentAuditService")
public class DocumentAuditService {
    private final UnconstrainedDataManager unconstrainedDataManager;
    private final JifmsQueryService jifmsQueryService;

    public DocumentAuditService(UnconstrainedDataManager unconstrainedDataManager, JifmsQueryService jifmsQueryService) {
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.jifmsQueryService = jifmsQueryService;
    }

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

    void recordInsert(ResolvedContext ctx, AuditRecord auditRecord) {
        var audit = unconstrainedDataManager.create(DocumentAudit.class);
        audit.setProcessDate(LocalDate.now());
        audit.setProcessStatus("I");
        audit.setLoggedChanges(auditRecord.getLoggedChanges());
        setDocumentFields(audit, ctx.getDocument());
        setProjectionFields(audit, auditRecord);
        unconstrainedDataManager.saveWithoutReload(audit);
    }

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