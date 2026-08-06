package gov.fjc.fis.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static gov.fjc.fis.FisUtilities.*;
import static java.util.Objects.requireNonNullElse;

@JmixEntity
@Table(name = "FIS_DIVISION", indexes = {
        @Index(name = "IDX_FIS_DIVISION_APPROPRIATION", columnList = "APPROPRIATION_ID"),
        @Index(name = "IDX_FIS_DIVISION_FUND", columnList = "FUND_ID"),
        @Index(name = "IDX_FIS_DIVISION_APP_DIVCODE_FUND", columnList = "APPROPRIATION_ID, DIVISION_CODE, FUND_ID")
}, uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_DIVISION_UNQ_APPROPRIATION_DIVCODE", columnNames = {"APPROPRIATION_ID", "DIVISION_CODE"}),
        @UniqueConstraint(name = "IDX_FIS_DIVISION_UNQ_APPROPRIATION_TITLE", columnNames = {"APPROPRIATION_ID", "TITLE"})
})
@Entity(name = "fis_Division")
public class Division {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "APPROPRIATION_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Appropriation appropriation;

    @OnDeleteInverse(DeletePolicy.DENY)
    @NotNull
    @JoinColumn(name = "FUND_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Fund fund;

    @Column(name = "DIVISION_CODE", nullable = false, length = 2)
    @NotNull
    private String divisionCode;

    @Composition
    @OrderBy("objectCategory")
    @OneToMany(mappedBy = "division")
    private List<DivisionAllocation> allocations;

    @OrderBy("createdDate DESC")
    @Composition
    @OneToMany(mappedBy = "division")
    private List<DivisionAllocationAudit> auditAllocations;

    @Composition
    @OrderBy("branchCode")
    @OneToMany(mappedBy = "division")
    private List<Branch> branches;

    @Composition
    @OrderBy("groupCode")
    @OneToMany(mappedBy = "division")
    private List<Group> groups;

    @Composition
    @OrderBy("activityNumber")
    @OneToMany(mappedBy = "division")
    private List<Activity> activities;

    @Column(name = "BUDGET_ORG", length = 7)
    private String budgetOrg;

    @Column(name = "TITLE", nullable = false)
    @NotNull
    private String title;

    @Comment("abbreviated division title")
    @Column(name = "SHORT_TITLE")
    private String shortTitle;

    @NotNull
    @Column(name = "ONE_YEAR_AMOUNT", nullable = false, precision = 19, scale = 2)
    private BigDecimal oneYearAmount = BigDecimal.ZERO;

    @Column(name = "TWO_YEAR_AMOUNT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal twoYearAmount = BigDecimal.ZERO;

    @Column(name = "SORT_CODE", nullable = false)
    @NotNull
    private Integer sortCode = 0;

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

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = safeTrim(shortTitle);
    }

    public List<DivisionAllocationAudit> getAuditAllocations() {
        return auditAllocations;
    }

    public void setAuditAllocations(List<DivisionAllocationAudit> auditAllocations) {
        this.auditAllocations = auditAllocations;
    }

    public Integer getSortCode() {
        return sortCode;
    }

    public void setSortCode(Integer sortCode) {
        this.sortCode = requireNonNullElse(sortCode, 0);
    }

    @DependsOnProperties({"oneYearAmount", "twoYearAmount"})
    @JmixProperty
    public BigDecimal getTotalAmount() {
        return add(oneYearAmount, twoYearAmount);
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    @DependsOnProperties({"budgetOrg", "title"})
    @JmixProperty
    public String getTitleAndBudgetOrg() {
        return String.format("%s (%s)", title, budgetOrg);
    }

    @DependsOnProperties({"divisionCode", "title"})
    @JmixProperty
    public String getTitleAndCode() {
        return String.format("%s (%s)", title, divisionCode);
    }

    public BigDecimal getTwoYearAmount() {
        return requireNonNullElse(twoYearAmount, BigDecimal.ZERO);
    }

    public void setTwoYearAmount(BigDecimal twoYearAmount) {
        this.twoYearAmount = requireNonNullElse(twoYearAmount, BigDecimal.ZERO);
    }

    public BigDecimal getOneYearAmount() {
        return requireNonNullElse(oneYearAmount, BigDecimal.ZERO);
    }

    public void setOneYearAmount(BigDecimal oneYearAmount) {
        this.oneYearAmount = requireNonNullElse(oneYearAmount, BigDecimal.ZERO);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = safeTrim(title);
    }

    public String getBudgetOrg() {
        return budgetOrg;
    }

    public void setBudgetOrg(String budgetOrg) {
        this.budgetOrg = safeTrim(safeToUpperCase(budgetOrg));
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    public List<DivisionAllocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<DivisionAllocation> allocations) {
        this.allocations = allocations;
    }

    public String getDivisionCode() {
        return divisionCode;
    }

    public void setDivisionCode(String divisionCode) {
        this.divisionCode = safeToUpperCase(divisionCode);
    }

    public Fund getFund() {
        return fund;
    }

    public void setFund(Fund fund) {
        this.fund = fund;
    }

    public Appropriation getAppropriation() {
        return appropriation;
    }

    public void setAppropriation(Appropriation appropriation) {
        this.appropriation = appropriation;
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
    @DependsOnProperties({"appropriation", "divisionCode"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s-%s",
                metadataTools.format(appropriation),
                metadataTools.format(divisionCode));
    }
}
