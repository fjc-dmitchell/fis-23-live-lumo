package gov.fjc.fis.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static gov.fjc.fis.FisUtilities.*;

@JmixEntity
@Table(name = "FIS_ACTIVITY", indexes = {
        @Index(name = "IDX_FIS_ACTIVITY_DIVISION", columnList = "DIVISION_ID"),
        @Index(name = "IDX_FIS_ACTIVITY_DIVISION_FUND", columnList = "DIVISION_ID, FUND_ID"),
        @Index(name = "IDX_FIS_ACTIVITY_DIVISION_BRANCH", columnList = "DIVISION_ID, BRANCH_ID"),
        @Index(name = "IDX_FIS_ACTIVITY_DIVISION_GROUP", columnList = "DIVISION_ID, GROUP_ID"),
        @Index(name = "IDX_FIS_ACTIVITY_BRANCH", columnList = "BRANCH_ID"),
        @Index(name = "IDX_FIS_ACTIVITY_GROUP", columnList = "GROUP_ID"),
        @Index(name = "IDX_FIS_ACTIVITY_DIVISION_FUND_ENDDATE", columnList = "DIVISION_ID, FUND_ID, END_DATE"),
        @Index(name = "IDX_FIS_ACTIVITY_FUND", columnList = "FUND_ID"),
        @Index(name = "IDX_FIS_ACTIVITY_COST_ORG", columnList = "COST_ORG_ID"),
        @Index(name = "IDX_FIS_ACTIVITY_DIVISION_ACTNUM", columnList = "DIVISION_ID, ACTIVITY_NUMBER")
}, uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_ACTIVITY_UNQ", columnNames = {"DIVISION_ID", "ACTIVITY_NUMBER"})
})
@Entity(name = "fis_Activity")
public class Activity implements FileAttachable {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "DIVISION_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Division division;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "FUND_ID", nullable = false)
    private Fund fund;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "BRANCH_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Branch branch;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "GROUP_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "COST_ORG_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Division costOrg;

    @Pattern(message = "Activity number must contain 4 digits", regexp = "^[0-9]{4}$")
    @Column(name = "ACTIVITY_NUMBER", nullable = false, length = 4)
    @NotNull
    private String activityNumber;

    @Column(name = "GENERIC_PROJECTION")
    private Boolean genericProjection = false;

    @Column(name = "TITLE", nullable = false)
    @NotNull
    private String title;

    @Column(name = "SHORT_TITLE")
    private String shortTitle;

    @Column(name = "CITY")
    private String city;

    @Column(name = "STATE", length = 2)
    private String state;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "PROGRAM_DIRECTOR")
    private String programDirector;

    @Column(name = "NUMBER_PARTICIPANTS")
    private Integer numberParticipants;

    @Column(name = "NUMBER_PROGRAMS")
    private Integer numberPrograms;

    @Column(name = "NUMBER_FACULTY")
    private Integer numberFaculty;

    @Column(name = "SORT_CODE", nullable = false)
    @NotNull
    private Integer sortCode = 0;

    @Column(name = "MEMO")
    @Lob
    private String memo;

    @Column(name = "NOTE")
    private String note;

    @Column(name = "TRAINING_PROJECT")
    private Boolean trainingProject = false;

    @Column(name = "CANCELED")
    private Boolean canceled = false;

    @NotNull
    @Column(name = "PROJECTED_AMOUNT", nullable = false, precision = 19, scale = 2)
    private BigDecimal projectedAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "REIMBURSED_AMOUNT", nullable = false, precision = 19, scale = 2)
    private BigDecimal reimbursedAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "OBLIGATED_AMOUNT", nullable = false, precision = 19, scale = 2)
    private BigDecimal obligatedAmount = BigDecimal.ZERO;

    @Composition
    @OneToMany(mappedBy = "activity")
    private List<ActivityProjection> projections;

    @OrderBy("createdDate DESC")
    @Composition
    @OneToMany(mappedBy = "activity")
    private List<ActivityProjectionAudit> auditProjections;

    @Composition
    @OneToMany(mappedBy = "activity")
    private List<ActivityReimbursement> reimbursements;

    @OrderBy("documentNumber")
    @Composition
    @OneToMany(mappedBy = "activity")
    private List<Obligation> obligations;

    @Composition
    @OneToMany(mappedBy = "activity")
    private List<FileAttachment> attachments;

    @Column(name = "REPORT_NOTE")
    private String reportNote;

    @Column(name = "PARTICIPANT_COUNT_FINAL")
    private Boolean participantCountFinal;

    @Column(name = "ADDED_TO_PLAN")
    private Boolean addedToPlan = false;

    @Column(name = "INITIAL_PROJECTION")
    private BigDecimal initialProjection;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    @LastModifiedBy
    @Column(name = "LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    private OffsetDateTime lastModifiedDate;

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    @DependsOnProperties({"costOrg"})
    @JmixProperty
    public String getBudgetOrgOfCostOrg() {
        return costOrg == null ? null : costOrg.getBudgetOrg();
    }

    public Division getCostOrg() {
        return costOrg;
    }

    public void setCostOrg(Division costOrg) {
        this.costOrg = costOrg;
    }

    public List<FileAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<FileAttachment> attachments) {
        this.attachments = attachments;
    }

    public BigDecimal getObligatedAmount() {
        return obligatedAmount;
    }

    public void setObligatedAmount(BigDecimal obligatedAmount) {
        this.obligatedAmount = obligatedAmount;
    }

    public Boolean getCanceled() {
        return canceled;
    }

    public void setCanceled(Boolean canceled) {
        this.canceled = canceled;
    }

    public Boolean getGenericProjection() {
        return genericProjection;
    }

    public void setGenericProjection(Boolean genericProjection) {
        this.genericProjection = genericProjection;
    }

    public Boolean getParticipantCountFinal() {
        return participantCountFinal;
    }

    public void setParticipantCountFinal(Boolean participantCountFinal) {
        this.participantCountFinal = participantCountFinal;
    }

    @DependsOnProperties({"addedToPlan"})
    @JmixProperty
    public String getAddedToPlanString() {
        return addedToPlan ? "Yes" : "No";
    }

    @DependsOnProperties({"activityNumber"})
    @JmixProperty
    public String getGenericActivityNumber() {
        if (activityNumber == null || activityNumber.length() < 2) {
            return "";
        } else {
            return activityNumber.substring(0, 2) + "%";
        }
    }

    public List<ActivityProjectionAudit> getAuditProjections() {
        return auditProjections;
    }

    public void setAuditProjections(List<ActivityProjectionAudit> auditProjections) {
        this.auditProjections = auditProjections;
    }

    public void setInitialProjection(BigDecimal initialProjection) {
        this.initialProjection = initialProjection;
    }

    public BigDecimal getInitialProjection() {
        return initialProjection;
    }

    public Integer getSortCode() {
        return sortCode;
    }

    public void setSortCode(Integer sortCode) {
        this.sortCode = sortCode;
    }

    public List<ActivityReimbursement> getReimbursements() {
        return reimbursements;
    }

    public void setReimbursements(List<ActivityReimbursement> reimbursements) {
        this.reimbursements = reimbursements;
    }

    public List<ActivityProjection> getProjections() {
        return projections;
    }

    public void setProjections(List<ActivityProjection> projections) {
        this.projections = projections;
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    @DependsOnProperties({"title", "activityNumber"})
    @JmixProperty
    public String getTitleAndCode() {
        return String.format("%s (%s)", title, activityNumber);
    }

    @DependsOnProperties({"activityNumber", "group"})
    @JmixProperty
    public Boolean getIsGeneric() {
        return group != null
                && activityNumber != null
                && activityNumber.equals(group.getGroupCode() + "00");
    }

    public Boolean getAddedToPlan() {
        return addedToPlan;
    }

    public void setAddedToPlan(Boolean addedToPlan) {
        this.addedToPlan = addedToPlan;
    }

    public String getReportNote() {
        return reportNote;
    }

    public void setReportNote(String reportNote) {
        this.reportNote = reportNote;
    }

    public BigDecimal getReimbursedAmount() {
        return reimbursedAmount;
    }

    public void setReimbursedAmount(BigDecimal reimbursedAmount) {
        this.reimbursedAmount = reimbursedAmount;
    }

    public BigDecimal getProjectedAmount() {
        return projectedAmount;
    }

    public void setProjectedAmount(BigDecimal projectedAmount) {
        this.projectedAmount = projectedAmount;
    }

    public Boolean getTrainingProject() {
        return trainingProject;
    }

    public void setTrainingProject(Boolean trainingProject) {
        this.trainingProject = trainingProject;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = safeTrim(note);
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = safeTrim(memo);
    }

    public Integer getNumberFaculty() {
        return numberFaculty;
    }

    public void setNumberFaculty(Integer numberFaculty) {
        this.numberFaculty = numberFaculty;
    }

    public Integer getNumberPrograms() {
        return numberPrograms;
    }

    public void setNumberPrograms(Integer numberPrograms) {
        this.numberPrograms = numberPrograms;
    }

    public Integer getNumberParticipants() {
        return numberParticipants;
    }

    public void setNumberParticipants(Integer numberParticipants) {
        this.numberParticipants = numberParticipants;
    }

    public String getProgramDirector() {
        return programDirector;
    }

    public void setProgramDirector(String programDirector) {
        this.programDirector = safeTrim(programDirector);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = safeTrim(safeToUpperCase(state));
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = safeTrim(city);
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = safeTrim(shortTitle);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = safeTrim(title);
    }

    public List<Obligation> getObligations() {
        return obligations;
    }

    public void setObligations(List<Obligation> obligations) {
        this.obligations = obligations;
    }

    public String getActivityNumber() {
        return activityNumber;
    }

    public void setActivityNumber(String activityNumber) {
        this.activityNumber = activityNumber;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Fund getFund() {
        return fund;
    }

    public void setFund(Fund fund) {
        this.fund = fund;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public OffsetDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @InstanceName
    @DependsOnProperties({"activityNumber", "division"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s-%s",
                metadataTools.format(division),
                metadataTools.format(activityNumber));
    }
}