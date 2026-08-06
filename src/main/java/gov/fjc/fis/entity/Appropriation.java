package gov.fjc.fis.entity;

import io.jmix.core.metamodel.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static gov.fjc.fis.FisUtilities.add;
import static gov.fjc.fis.FisUtilities.getCreatedModifiedString;
import static java.util.Objects.requireNonNullElse;

@JmixEntity
@Table(name = "FIS_APPROPRIATION", uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_APPROPRIATION_UNQ", columnNames = {"BUDGET_FISCAL_YEAR"})
})
@Entity(name = "fis_Appropriation")
public class Appropriation {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Composition
    @OrderBy("divisionCode")
    @OneToMany(mappedBy = "appropriation")
    private List<Division> divisions;

    @Composition
    @OrderBy("majorObjectClass")
    @OneToMany(mappedBy = "appropriation")
    private List<ObjectCategory> categories;

    @Composition
    @OrderBy("adjustmentDate")
    @OneToMany(mappedBy = "appropriation")
    private List<AppropriationAdjustment> adjustments;

    @Pattern(message = "The Budget Fiscal Year must contain four digits", regexp = "^(19|20)[0-9]{2}$")
    @InstanceName
    @Column(name = "BUDGET_FISCAL_YEAR", nullable = false, length = 4)
    @NotNull
    private String budgetFiscalYear;

    @PositiveOrZero(message = "The one year amount cannot be less than zero")
    @Column(name = "ONE_YEAR_AMOUNT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal oneYearAmount = BigDecimal.ZERO;

    @PositiveOrZero(message = "The two year amount cannot be less than zero")
    @Column(name = "TWO_YEAR_AMOUNT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal twoYearAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "ONE_YEAR_ADJUSTMENT", nullable = false)
    private BigDecimal oneYearAdjustment = BigDecimal.ZERO;

    @NotNull
    @Column(name = "TWO_YEAR_ADJUSTMENT", nullable = false)
    private BigDecimal twoYearAdjustment = BigDecimal.ZERO;

    @Column(name = "REIMBURSED_AMOUNT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal reimbursedAmount = BigDecimal.ZERO;

    @Column(name = "STATUS")
    private Boolean status = false;
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

    @DependsOnProperties({"oneYearAmount", "twoYearAmount", "oneYearAdjustment", "twoYearAdjustment", "reimbursedAmount"})
    @JmixProperty
    public BigDecimal getAuthority() {
        return add(oneYearAmount, twoYearAmount, oneYearAdjustment, twoYearAdjustment, reimbursedAmount);
    }

    public BigDecimal getReimbursedAmount() {
        return reimbursedAmount;
    }

    public void setReimbursedAmount(BigDecimal reimbursedAmount) {
        this.reimbursedAmount = requireNonNullElse(reimbursedAmount, BigDecimal.ZERO);
    }

    @DependsOnProperties({"status"})
    @JmixProperty
    public String getStatusString() {
        return status ? "Open" : "Closed";
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public List<Division> getDivisions() {
        return divisions;
    }

    public void setDivisions(List<Division> divisions) {
        this.divisions = divisions;
    }

    public List<AppropriationAdjustment> getAdjustments() {
        return adjustments;
    }

    public void setAdjustments(List<AppropriationAdjustment> adjustments) {
        this.adjustments = adjustments;
    }

    public List<ObjectCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<ObjectCategory> categories) {
        this.categories = categories;
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    @DependsOnProperties({"oneYearAmount", "twoYearAmount"})
    @JmixProperty
    public BigDecimal getTotalAmount() {
        return add(oneYearAmount, twoYearAmount);
    }

    @DependsOnProperties({"oneYearAdjustment", "twoYearAdjustment"})
    @JmixProperty
    public BigDecimal getTotalAdjustment() {
        return add(oneYearAdjustment, twoYearAdjustment);
    }

    public BigDecimal getOneYearAmount() {
        return oneYearAmount;
    }

    public void setOneYearAmount(BigDecimal oneYearAmount) {
        this.oneYearAmount = requireNonNullElse(oneYearAmount, BigDecimal.ZERO);
    }

    public BigDecimal getTwoYearAmount() {
        return twoYearAmount;
    }

    public void setTwoYearAmount(BigDecimal twoYearAmount) {
        this.twoYearAmount = requireNonNullElse(twoYearAmount, BigDecimal.ZERO);
    }

    public BigDecimal getOneYearAdjustment() {
        return oneYearAdjustment;
    }

    public void setOneYearAdjustment(BigDecimal oneYearAdjustment) {
        this.oneYearAdjustment = requireNonNullElse(oneYearAdjustment, BigDecimal.ZERO);
    }

    public BigDecimal getTwoYearAdjustment() {
        return twoYearAdjustment;
    }

    public void setTwoYearAdjustment(BigDecimal twoYearAdjustment) {
        this.twoYearAdjustment = requireNonNullElse(twoYearAdjustment, BigDecimal.ZERO);
    }

    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    public void setBudgetFiscalYear(String budgetFiscalYear) {
        this.budgetFiscalYear = budgetFiscalYear;
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
}