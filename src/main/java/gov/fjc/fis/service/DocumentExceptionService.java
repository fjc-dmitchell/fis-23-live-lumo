package gov.fjc.fis.service;

import gov.fjc.fis.entity.DocumentAudit;
import gov.fjc.fis.entity.DocumentException;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

@Component("fis_DocumentExceptionService")
public class DocumentExceptionService {
    private final DataManager dataManager;

    public DocumentExceptionService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public DocumentException fetchException(DocumentAudit audit) {
        return dataManager.load(DocumentException.class)
                .query("SELECT e FROM fis_DocumentException e"
                        + " WHERE e.fundCode= :fundCode"
                        + " AND e.budgetOrg = :budgetOrg"
                        + " AND e.bbfy = :bbfy"
                        + " AND e.budgetObjectClass = :budgetObjectClass"
                        + " AND e.documentType = :documentType"
                        + " AND e.documentNumber = :documentNumber")
                .parameter("fundCode", audit.getDocumentFundCode())
                .parameter("budgetOrg", audit.getDocumentBudgetOrg())
                .parameter("bbfy", audit.getDocumentBbfy())
                .parameter("budgetObjectClass", audit.getDocumentBudgetObjectClass())
                .parameter("documentType", audit.getDocumentDocumentType())
                .parameter("documentNumber", audit.getDocumentDocumentNumber())
                .optional()
                .orElse(null);
    }

    public boolean exceptionExists(DocumentAudit audit) {
        return fetchException(audit) != null;
    }

    public DocumentException createException(DocumentAudit audit) {
        if (exceptionExists(audit)) {
            return null;
        }
        var exception = dataManager.create(DocumentException.class);
        exception.setFundCode(audit.getDocumentFundCode());
        exception.setBudgetOrg(audit.getDocumentBudgetOrg());
        exception.setBbfy(audit.getDocumentBbfy());
        exception.setBudgetObjectClass(audit.getDocumentBudgetObjectClass());
        exception.setDocumentType(audit.getDocumentDocumentType());
        exception.setDocumentNumber(audit.getDocumentDocumentNumber());
        exception.setDocumentType(audit.getDocumentDocumentType());
        exception.setDocumentNumber(audit.getDocumentDocumentNumber());
        return dataManager.save(exception);
    }

    public boolean removeException(DocumentAudit audit) {
        var exception = fetchException(audit);
        if (exception == null) return false;

        dataManager.remove(exception);
        return true;
    }
}