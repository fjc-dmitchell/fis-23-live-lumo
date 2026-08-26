package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.*;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component("fis_AuditService")
class AuditService {
    private final UnconstrainedDataManager data;

    public AuditService(UnconstrainedDataManager unconstrainedDataManager) {
        this.data = unconstrainedDataManager;
    }
//    private final UnconstrainedDataManager data;
//
//    AuditService(UnconstrainedDataManager data) { this.data = data; }

    void record(Document doc, ProcessingOutcome outcome) {
        var audit = data.create(DocumentAudit.class);
        audit.setProcessDate(LocalDate.now());
        audit.setProcessStatus(statusId(outcome));     // "REJECT", "INSERT", "UPDATE", "IGNORE"
        audit.setLoggedChanges(outcome.summary());


        // copy over document fields (you already do this well)
        setDocumentFields(audit, doc);
        // copy obligation fields if outcome is Inserted/Updated
        if(outcome instanceof ProcessingOutcome.Inserted) {
            // old processing didn't do this but did update projection, etc
        }
        if(outcome instanceof ProcessingOutcome.Updated) {

        }
        // - ok do this later
        data.saveWithoutReload(audit);
    }

    private String statusId(ProcessingOutcome o) {
        return switch (o) {
            case ProcessingOutcome.Rejected r -> "REJECT";
            case ProcessingOutcome.Inserted i -> "INSERT";
            case ProcessingOutcome.Updated u -> "UPDATE";
            case ProcessingOutcome.Ignored i -> "IGNORE";
        };
    }

    void setDocumentFields(DocumentAudit documentAudit, Document myDoc) {

        documentAudit.setDocumentFundCode(myDoc.getFundCode());
        documentAudit.setDocumentBbfy(myDoc.getBbfy());
        documentAudit.setDocumentEbfy(myDoc.getEbfy());
        documentAudit.setDocumentBudgetOrg(myDoc.getBudgetOrg());
        documentAudit.setDocumentCostOrg(myDoc.getCostOrg());
        documentAudit.setDocumentDocumentType(myDoc.getDocumentType());
        documentAudit.setDocumentDocumentNumber(myDoc.getDocumentNumber());
        documentAudit.setDocumentDocumentDate(myDoc.getDocumentDate());
        documentAudit.setDocumentDocumentCreationDate(myDoc.getDocumentCreationDate().toLocalDate());
        documentAudit.setDocumentTitle(myDoc.getTitle());
        documentAudit.setDocumentBudgetObjectClass(myDoc.getBudgetObjectClass());
        documentAudit.setDocumentProject(myDoc.getProject());
        documentAudit.setDocumentAmount(myDoc.getAmount());
        documentAudit.setDocumentLineNumber(myDoc.getLineNumber());
        documentAudit.setDocumentTaxId(myDoc.getTaxId());
        documentAudit.setDocumentTaxIdType(myDoc.getTaxIdType());
        documentAudit.setDocumentAddressCode(myDoc.getAddressCode());
        documentAudit.setDocumentVendorCode(myDoc.getVendorCode());
        documentAudit.setDocumentVendorName(myDoc.getVendorName());
        documentAudit.setDocumentTravelStartDate(myDoc.getTravelStartDate());
        documentAudit.setDocumentTravelEndDate(myDoc.getTravelEndDate());
        documentAudit.setDocumentExpendedAmount(myDoc.getExpendedAmount());
        documentAudit.setDocumentClosedAmount(myDoc.getClosedAmount());
        documentAudit.setDocumentClosedDate(myDoc.getClosedDate());
        documentAudit.setDocumentLastModifiedBy(myDoc.getLastModifiedBy());
        documentAudit.setDocumentMajorObjectClass(myDoc.getMajorObjectClass());
        documentAudit.setDocumentFjc(myDoc.getFjc());
    }

    void setDocumentObligationFields(DocumentAudit documentAudit, Obligation myObligation) {

    }
}