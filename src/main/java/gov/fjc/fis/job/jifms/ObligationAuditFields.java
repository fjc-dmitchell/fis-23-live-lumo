package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.Obligation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Immutable snapshot of selected {@link Obligation} values used for document
 * processing audits.
 *
 * <p>This record captures the obligation's identifying information, financial
 * values, vendor details, travel dates, activity assignment, object class,
 * division, and modification timestamp at a specific point in time. It is
 * primarily used to record the state of an obligation before and after JIFMS
 * processing so that changes can be written to audit records without retaining
 * a reference to the mutable entity.
 *
 * <p>Instances are typically created from an existing {@link Obligation}
 * using {@link #from(Obligation)}.
 *
 * @author Doug Mitchell
 */
public record ObligationAuditFields(
        String documentNumber,
        String documentType,
        BigDecimal amount,
        LocalDate documentDate,
        LocalDate processDate,
        String vendor,
        Boolean status,
        String ein,
        LocalDate travelStartDate,
        LocalDate travelEndDate,
        LocalDateTime modifiedDate,
        String activityNumber,
        String budgetObjectClass,
        String divisionCode,
        String addressCode,
        String vendorCode) {

    /**
     * Creates an audit snapshot of an obligation before it is modified.
     *
     * <p>The returned record preserves the obligation's current values so they
     * can be compared with the updated values and recorded in a document audit.
     *
     * @param o the obligation to capture
     * @return an immutable snapshot of the obligation's current state
     */
    static ObligationAuditFields from(Obligation o) {
        return new ObligationAuditFields(
                o.getDocumentNumber(),
                o.getDocumentType().getId(),
                o.getAmount(),
                o.getDocumentDate(),
                o.getProcessDate(),
                o.getVendor(),
                o.getStatus(),
                o.getEin(),
                o.getTravelStartDate(),
                o.getTravelEndDate(),
                o.getLastModifiedDate().toLocalDateTime(),
                o.getActivity().getActivityNumber(),
                o.getObjectClass().getBudgetObjectClass(),
                o.getActivity().getDivision().getDivisionCode(),
                o.getAddressCode(),
                o.getVendorCode()
        );
    }
}