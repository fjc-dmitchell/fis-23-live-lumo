package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.Obligation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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