package gov.fjc.fis.service;

import gov.fjc.fis.entity.DocumentAudit;
import gov.fjc.fis.entity.DocumentException;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Manages exceptions that control whether rejected JIFMS documents are included
 * in subsequent processing.
 *
 * <p>When validation results in a document being rejected, a user can create a
 * {@link DocumentException} to cause subsequent processing to ignore the
 * document. Normal processing of the document can be resumed by removing its
 * exception.</p>
 *
 * <p>Document exceptions are matched to {@link DocumentAudit} records by fund
 * code, budget organization, budget fiscal year, budget object class, document
 * type, and document number.</p>
 *
 * @author Doug Mitchell
 */
@Component("fis_DocumentExceptionService")
public class DocumentExceptionService {
    private final DataManager dataManager;

    public DocumentExceptionService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Finds the document exception associated with the specified document audit.
     *
     * <p>The exception is identified by matching the document's fund code,
     * budget organization, budget fiscal year, budget object class, document
     * type, and document number.</p>
     *
     * @param audit the document audit containing the identifying document values
     * @return an optional containing the matching document exception, or an
     *         empty optional if no exception exists
     */
    public Optional<DocumentException> fetchException(DocumentAudit audit) {
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
                .optional();
    }

    /**
     * Determines whether an exception exists for the specified document audit.
     *
     * @param audit the document audit to check
     * @return {@code true} if a matching document exception exists;
     *         otherwise, {@code false}
     */
    public boolean exceptionExists(DocumentAudit audit) {
        return fetchException(audit).isPresent();
    }

    /**
     * Creates an exception that prevents subsequent processing of the document
     * represented by the specified audit.
     *
     * <p>No exception is created if a matching exception already exists.</p>
     *
     * @param audit the document audit containing the identifying values for the
     *              exception
     * @return the saved document exception, or {@code null} if a matching
     *         exception already exists
     */
    public DocumentException createException(DocumentAudit audit) {
        if (fetchException(audit).isPresent()) {
            return null;
        }
        var exception = dataManager.create(DocumentException.class);
        exception.setFundCode(audit.getDocumentFundCode());
        exception.setBudgetOrg(audit.getDocumentBudgetOrg());
        exception.setBbfy(audit.getDocumentBbfy());
        exception.setBudgetObjectClass(audit.getDocumentBudgetObjectClass());
        exception.setDocumentType(audit.getDocumentDocumentType());
        exception.setDocumentNumber(audit.getDocumentDocumentNumber());
        return dataManager.save(exception);
    }

    /**
     * Removes the exception for the specified document audit, allowing the
     * document to be included in subsequent processing.
     *
     * @param audit the document audit whose exception is removed
     * @return {@code true} if a matching exception was found and removed;
     *         otherwise, {@code false}
     */
    public boolean removeException(DocumentAudit audit) {
        return fetchException(audit)
                .map(exception -> {
                    dataManager.remove(exception);
                    return true;
                })
                .orElse(false);
    }
}