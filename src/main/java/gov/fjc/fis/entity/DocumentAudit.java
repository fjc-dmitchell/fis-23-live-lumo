package gov.fjc.fis.entity;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static gov.fjc.fis.FisUtilities.nonZero;
import static java.util.Objects.requireNonNullElse;

@JmixEntity
@Table(name = "FIS_DOCUMENT_AUDIT", indexes = {
        @Index(name = "IDX_FIS_DOCUMENT_AUDIT", columnList = "DOCUMENT_BBFY"),
        @Index(name = "IDX_FIS_DOCUMENT_AUDIT_REJECT_IDX", columnList = "PROCESS_STATUS, DOCUMENT_FUND_CODE, DOCUMENT_BBFY, DOCUMENT_BUDGET_ORG, DOCUMENT_DOCUMENT_TYPE, DOCUMENT_DOCUMENT_NUMBER, DOCUMENT_LINE_NUMBER, DOCUMENT_BOC, DOCUMENT_AMOUNT"),
        @Index(name = "IDX_FIS_DOCUMENT_AUDIT_PROCESS_IDX", columnList = "DOCUMENT_BBFY, DOCUMENT_FUND_CODE, DOCUMENT_BUDGET_ORG, DOCUMENT_BOC, DOCUMENT_DOCUMENT_NUMBER")
})
@Entity(name = "fis_DocumentAudit")
public class DocumentAudit {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "PROCESS_STATUS", nullable = false)
    private String processStatus;

    @NotNull
    @Column(name = "PROCESS_DATE", nullable = false)
    private LocalDate processDate;

    @Column(name = "DOCUMENT_FUND_CODE", nullable = false, length = 6)
    @NotNull
    private String documentFundCode;

    @Column(name = "DOCUMENT_BBFY", nullable = false, length = 4)
    @NotNull
    private String documentBbfy;

    @Column(name = "DOCUMENT_EBFY", length = 4)
    private String documentEbfy;

    @Column(name = "DOCUMENT_BUDGET_ORG", nullable = false, length = 7)
    @NotNull
    private String documentBudgetOrg;

    @Column(name = "DOCUMENT_COST_ORG", nullable = false, length = 7)
    @NotNull
    private String documentCostOrg;

    @Column(name = "DOCUMENT_DOCUMENT_TYPE", nullable = false, length = 5)
    @NotNull
    private String documentDocumentType;

    @Column(name = "DOCUMENT_DOCUMENT_NUMBER", nullable = false, length = 50)
    @NotNull
    private String documentDocumentNumber;

    @NotNull
    @Column(name = "DOCUMENT_DOCUMENT_DATE", nullable = false)
    private LocalDate documentDocumentDate;

    @NotNull
    @Column(name = "DOCUMENT_DOCUMENT_CREATION_DATE", nullable = false)
    private LocalDate documentDocumentCreationDate;

    @Column(name = "DOCUMENT_TITLE", length = 70)
    private String documentTitle;

    @Column(name = "DOCUMENT_BOC", nullable = false, length = 7)
    @NotNull
    private String documentBudgetObjectClass;

    @Column(name = "DOCUMENT_PROJECT", length = 4)
    private String documentProject;

    @Column(name = "DOCUMENT_AMOUNT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal documentAmount;

    @Column(name = "DOCUMENT_LINE_NUMBER", nullable = false)
    @NotNull
    private Integer documentLineNumber;

    @Column(name = "DOCUMENT_TAX_ID", length = 9)
    private String documentTaxId;

    @Column(name = "DOCUMENT_TAX_ID_TYPE", length = 1)
    private String documentTaxIdType;

    @Column(name = "DOCUMENT_ADDRESS_CODE", length = 15)
    private String documentAddressCode;

    @Column(name = "DOCUMENT_VENDOR_CODE", length = 10)
    private String documentVendorCode;

    @Column(name = "DOCUMENT_VENDOR_NAME", length = 70)
    private String documentVendorName;

    @Column(name = "DOCUMENT_TRAVEL_START_DATE")
    private LocalDate documentTravelStartDate;

    @Column(name = "DOCUMENT_TRAVEL_END_DATE")
    private LocalDate documentTravelEndDate;

    @Column(name = "DOCUMENT_EXPENDED_AMOUNT", precision = 19, scale = 2)
    private BigDecimal documentExpendedAmount;

    @Column(name = "DOCUMENT_CLOSED_AMOUNT", precision = 19, scale = 2)
    private BigDecimal documentClosedAmount;

    @Column(name = "DOCUMENT_CLOSED_DATE")
    private LocalDate documentClosedDate;

    @Column(name = "DOCUMENT_LAST_MODIFIED_BY", nullable = false, length = 45)
    @NotNull
    private String documentLastModifiedBy;

    @Column(name = "DOCUMENT_MOC", length = 2)
    private String documentMajorObjectClass;

    @Column(name = "DOCUMENT_FJC", length = 20)
    private String documentFjc;

    @Column(name = "OBLIGATION_DOCID", length = 20)
    private String obligationDocumentNumber;

    @Column(name = "OBLIGATION_DOCUMENT_TYPE", length = 5)
    private String obligationDocumentType;

    @Column(name = "OBLIGATION_AMOUNT", precision = 19, scale = 2)
    private BigDecimal obligationAmount;

    @Column(name = "OBLIGATION_DOCUMENT_DATE")
    private LocalDate obligationDocumentDate;

    @Column(name = "OBLIGATION_PROCESS_DATE")
    private LocalDate obligationProcessDate;

    @Column(name = "OBLIGATION_VENDOR")
    private String obligationVendor;

    @Column(name = "OBLIGATION_STATUS")
    private Boolean obligationStatus;

    @Column(name = "OBLIGATION_EIN", length = 10)
    private String obligationEin;

    @Column(name = "OBLIGATION_TRAVEL_START_DATE")
    private LocalDate obligationTravelStartDate;

    @Column(name = "OBLIGATION_TRAVEL_END_DATE")
    private LocalDate obligationTravelEndDate;

    @Column(name = "OBLIGATION_MODIFIED_DATE")
    private LocalDateTime obligationModifiedDate;

    @Column(name = "OBLIGATION_ACTIVITY_NUMBER", length = 4)
    private String obligationActivityNumber;

    @Column(name = "OBLIGATION_BOC", length = 4)
    private String obligationBudgetObjectClass;

    @Column(name = "OBLIGATION_DIVISION_CODE", length = 2)
    private String obligationDivisionCode;

    @Column(name = "OBLIGATION_MOC", length = 2)
    private String obligationMajorObjectClass;

    @Column(name = "OBLIGATION_ADDRESS_CODE", length = 15)
    private String obligationAddressCode;

    @Column(name = "OBLIGATION_VENDOR_CODE", length = 10)
    private String obligationVendorCode;

    @Column(name = "CURRENT_ACTIVITY_NUMBER", length = 4)
    private String currentActivityNumber;

    @Column(name = "CURRENT_PROJECTION_BOC", length = 4)
    private String currentProjectionBoc;

    @Column(name = "CURRENT_PROJECTION_AMOUNT_BEFORE", precision = 19, scale = 2)
    private BigDecimal currentProjectionAmountBefore;

    @Column(name = "CURRENT_PROJECTION_AMOUNT_AFTER", precision = 19, scale = 2)
    private BigDecimal currentProjectionAmountAfter;

    @Column(name = "PREVIOUS_ACTIVITY_NUMBER", length = 4)
    private String previousActivityNumber;

    @Column(name = "PREVIOUS_PROJECTION_BOC", length = 4)
    private String previousProjectionBoc;

    @Column(name = "PREVIOUS_PROJECTION_AMOUNT_BEFORE", precision = 19, scale = 2)
    private BigDecimal previousProjectionAmountBefore;

    @Column(name = "PREVIOUS_PROJECTION_AMOUNT_AFTER", precision = 19, scale = 2)
    private BigDecimal previousProjectionAmountAfter;

    @Column(name = "LOGGED_CHANGES")
    private String loggedChanges;

    public void setProcessDate(LocalDate processDate) {
        this.processDate = processDate;
    }

    public LocalDate getProcessDate() {
        return processDate;
    }

    public void setDocumentDocumentDate(LocalDate documentDocumentDate) {
        this.documentDocumentDate = documentDocumentDate;
    }

    public LocalDate getDocumentDocumentDate() {
        return documentDocumentDate;
    }

    public void setDocumentDocumentCreationDate(LocalDate documentDocumentCreationDate) {
        this.documentDocumentCreationDate = documentDocumentCreationDate;
    }

    public LocalDate getDocumentDocumentCreationDate() {
        return documentDocumentCreationDate;
    }

    public void setDocumentTravelStartDate(LocalDate documentTravelStartDate) {
        this.documentTravelStartDate = documentTravelStartDate;
    }

    public LocalDate getDocumentTravelStartDate() {
        return documentTravelStartDate;
    }

    public void setDocumentTravelEndDate(LocalDate documentTravelEndDate) {
        this.documentTravelEndDate = documentTravelEndDate;
    }

    public LocalDate getDocumentTravelEndDate() {
        return documentTravelEndDate;
    }

    public void setDocumentClosedDate(LocalDate documentClosedDate) {
        this.documentClosedDate = documentClosedDate;
    }

    public LocalDate getDocumentClosedDate() {
        return documentClosedDate;
    }

    public void setObligationDocumentDate(LocalDate obligationDocumentDate) {
        this.obligationDocumentDate = obligationDocumentDate;
    }

    public LocalDate getObligationDocumentDate() {
        return obligationDocumentDate;
    }

    public void setObligationProcessDate(LocalDate obligationProcessDate) {
        this.obligationProcessDate = obligationProcessDate;
    }

    public LocalDate getObligationProcessDate() {
        return obligationProcessDate;
    }

    public void setObligationTravelStartDate(LocalDate obligationTravelStartDate) {
        this.obligationTravelStartDate = obligationTravelStartDate;
    }

    public LocalDate getObligationTravelStartDate() {
        return obligationTravelStartDate;
    }

    public void setObligationTravelEndDate(LocalDate obligationTravelEndDate) {
        this.obligationTravelEndDate = obligationTravelEndDate;
    }

    public LocalDate getObligationTravelEndDate() {
        return obligationTravelEndDate;
    }

    public void setObligationModifiedDate(LocalDateTime obligationModifiedDate) {
        this.obligationModifiedDate = obligationModifiedDate;
    }

    public LocalDateTime getObligationModifiedDate() {
        return obligationModifiedDate;
    }

    public String getObligationBudgetObjectClass() {
        return obligationBudgetObjectClass;
    }

    public void setObligationBudgetObjectClass(String obligationBudgetObjectClass) {
        this.obligationBudgetObjectClass = obligationBudgetObjectClass;
    }

    public void setObligationMajorObjectClass(String obligationMajorObjectClass) {
        this.obligationMajorObjectClass = obligationMajorObjectClass;
    }

    public void setLoggedChanges(String loggedChanges) {
        this.loggedChanges = loggedChanges;
    }

    public void setPreviousProjectionAmountAfter(BigDecimal previousProjectionAmountAfter) {
        this.previousProjectionAmountAfter = previousProjectionAmountAfter;
    }

    public void setPreviousProjectionAmountBefore(BigDecimal previousProjectionAmountBefore) {
        this.previousProjectionAmountBefore = previousProjectionAmountBefore;
    }

    public void setPreviousProjectionBoc(String previousProjectionBoc) {
        this.previousProjectionBoc = previousProjectionBoc;
    }

    public void setPreviousActivityNumber(String previousActivityNumber) {
        this.previousActivityNumber = previousActivityNumber;
    }

    public void setCurrentProjectionAmountAfter(BigDecimal currentProjectionAmountAfter) {
        this.currentProjectionAmountAfter = currentProjectionAmountAfter;
    }

    public void setCurrentProjectionAmountBefore(BigDecimal currentProjectionAmountBefore) {
        this.currentProjectionAmountBefore = currentProjectionAmountBefore;
    }

    public void setCurrentProjectionBoc(String currentProjectionBoc) {
        this.currentProjectionBoc = currentProjectionBoc;
    }

    public void setCurrentActivityNumber(String currentActivityNumber) {
        this.currentActivityNumber = currentActivityNumber;
    }

    public void setObligationVendorCode(String obligationVendorCode) {
        this.obligationVendorCode = obligationVendorCode;
    }

    public void setObligationAddressCode(String obligationAddressCode) {
        this.obligationAddressCode = obligationAddressCode;
    }

    public void setObligationCategory(String obligationMajorObjectClass) {
        this.obligationMajorObjectClass = obligationMajorObjectClass;
    }

    public void setObligationDivisionCode(String obligationDivisionCode) {
        this.obligationDivisionCode = obligationDivisionCode;
    }

    public void setObligationActivityNumber(String obligationActivityNumber) {
        this.obligationActivityNumber = obligationActivityNumber;
    }

    public void setObligationEin(String obligationEin) {
        this.obligationEin = obligationEin;
    }

    public void setObligationStatus(Boolean obligationStatus) {
        this.obligationStatus = obligationStatus;
    }

    public void setObligationVendor(String obligationVendor) {
        this.obligationVendor = obligationVendor;
    }

    public void setObligationAmount(BigDecimal obligationAmount) {
        this.obligationAmount = obligationAmount;
    }

    public void setObligationDocumentNumber(String obligationDocumentNumber) {
        this.obligationDocumentNumber = obligationDocumentNumber;
    }

    public void setDocumentFjc(String documentFjc) {
        this.documentFjc = documentFjc;
    }

    public void setDocumentMajorObjectClass(String documentMajorObjectClass) {
        this.documentMajorObjectClass = documentMajorObjectClass;
    }

    public void setDocumentLastModifiedBy(String documentLastModifiedBy) {
        this.documentLastModifiedBy = documentLastModifiedBy;
    }

    public void setDocumentClosedAmount(BigDecimal documentClosedAmount) {
        this.documentClosedAmount = documentClosedAmount;
    }

    public void setDocumentExpendedAmount(BigDecimal documentExpendedAmount) {
        this.documentExpendedAmount = documentExpendedAmount;
    }

    public void setDocumentVendorName(String documentVendorName) {
        this.documentVendorName = documentVendorName;
    }

    public void setDocumentVendorCode(String documentVendorCode) {
        this.documentVendorCode = documentVendorCode;
    }

    public void setDocumentAddressCode(String documentAddressCode) {
        this.documentAddressCode = documentAddressCode;
    }

    public void setDocumentTaxIdType(String documentTaxIdType) {
        this.documentTaxIdType = documentTaxIdType;
    }

    public void setDocumentTaxId(String documentTaxId) {
        this.documentTaxId = documentTaxId;
    }

    public void setDocumentLineNumber(Integer documentLineNumber) {
        this.documentLineNumber = documentLineNumber;
    }

    public void setDocumentAmount(BigDecimal documentAmount) {
        this.documentAmount = documentAmount;
    }

    public void setDocumentProject(String documentProject) {
        this.documentProject = documentProject;
    }

    public void setDocumentBudgetObjectClass(String documentBudgetObjectClass) {
        this.documentBudgetObjectClass = documentBudgetObjectClass;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public void setDocumentDocumentNumber(String documentDocumentNumber) {
        this.documentDocumentNumber = documentDocumentNumber;
    }

    public void setDocumentDocumentType(String documentDocumentType) {
        this.documentDocumentType = documentDocumentType;
    }

    public void setDocumentCostOrg(String documentCostOrg) {
        this.documentCostOrg = documentCostOrg;
    }

    public void setDocumentBudgetOrg(String documentBudgetOrg) {
        this.documentBudgetOrg = documentBudgetOrg;
    }

    public void setDocumentEbfy(String documentEbfy) {
        this.documentEbfy = documentEbfy;
    }

    public void setDocumentBbfy(String documentBbfy) {
        this.documentBbfy = documentBbfy;
    }

    public void setDocumentFundCode(String documentFundCode) {
        this.documentFundCode = documentFundCode;
    }

    public void setProcessStatus(String processStatus) {
        this.processStatus = processStatus;
    }

    public Integer getDocumentLineNumber() {
        return documentLineNumber;
    }

    public String getDocumentTaxIdType() {
        return documentTaxIdType;
    }

    public String getDocumentTaxId() {
        return documentTaxId;
    }

    public String getDocumentLastModifiedBy() {
        return documentLastModifiedBy;
    }

    public String getDocumentDocumentNumber() {
        return documentDocumentNumber;
    }

    public String getDocumentDocumentType() {
        return documentDocumentType;
    }

    public String getDocumentCostOrg() {
        return documentCostOrg;
    }

    public String getDocumentBudgetOrg() {
        return documentBudgetOrg;
    }

    public String getDocumentFundCode() {
        return documentFundCode;
    }

    @DependsOnProperties({"id"})
    @JmixProperty
    public Integer getProcessId() {
        return getId();
    }

    @DependsOnProperties({"documentAmount", "obligationAmount"})
    @JmixProperty
    public BigDecimal getFcnAmount() {
        BigDecimal value = BigDecimal.ZERO;
        if(Objects.equals(processStatus, "U")) {
            value = documentAmount.subtract(requireNonNullElse(obligationAmount, BigDecimal.ZERO));
        }
        return nonZero(value) ? value : null;
    }

    public String getObligationVendorCode() {
        return obligationVendorCode;
    }

    public String getLoggedChanges() {
        return loggedChanges;
    }

    public BigDecimal getPreviousProjectionAmountAfter() {
        return previousProjectionAmountAfter;
    }

    public BigDecimal getPreviousProjectionAmountBefore() {
        return previousProjectionAmountBefore;
    }

    public String getPreviousProjectionBoc() {
        return previousProjectionBoc;
    }

    public String getPreviousActivityNumber() {
        return previousActivityNumber;
    }

    public BigDecimal getCurrentProjectionAmountAfter() {
        return currentProjectionAmountAfter;
    }

    public BigDecimal getCurrentProjectionAmountBefore() {
        return currentProjectionAmountBefore;
    }

    public String getCurrentProjectionBoc() {
        return currentProjectionBoc;
    }

    public String getCurrentActivityNumber() {
        return currentActivityNumber;
    }

    public String getObligationAddressCode() {
        return obligationAddressCode;
    }

    public String getObligationMajorObjectClass() {
        return obligationMajorObjectClass;
    }

    public String getObligationDivisionCode() {
        return obligationDivisionCode;
    }

    public String getObligationActivityNumber() {
        return obligationActivityNumber;
    }

    public String getObligationEin() {
        return obligationEin;
    }

    public Boolean getObligationStatus() {
        return obligationStatus;
    }

    public String getObligationVendor() {
        return obligationVendor;
    }

    public BigDecimal getObligationAmount() {
        return obligationAmount;
    }

    public String getObligationDocumentType() {
        return obligationDocumentType;
    }

    public void setObligationDocumentType(String obligationDocumentType) {
        this.obligationDocumentType = obligationDocumentType;
    }

    public String getObligationDocumentNumber() {
        return obligationDocumentNumber;
    }

    public String getDocumentVendorName() {
        return documentVendorName;
    }

    public String getDocumentFjc() {
        return documentFjc;
    }

    public String getDocumentMajorObjectClass() {
        return documentMajorObjectClass;
    }

    public BigDecimal getDocumentClosedAmount() {
        return documentClosedAmount;
    }

    public BigDecimal getDocumentExpendedAmount() {
        return documentExpendedAmount;
    }

    public String getDocumentVendorCode() {
        return documentVendorCode;
    }

    public String getDocumentAddressCode() {
        return documentAddressCode;
    }

    public BigDecimal getDocumentAmount() {
        return documentAmount;
    }

    public String getDocumentProject() {
        return documentProject;
    }

    public String getDocumentBudgetObjectClass() {
        return documentBudgetObjectClass;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public String getDocumentEbfy() {
        return documentEbfy;
    }

    public String getDocumentBbfy() {
        return documentBbfy;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}