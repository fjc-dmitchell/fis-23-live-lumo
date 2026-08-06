package gov.fjc.fis.entity.personnel;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
@Table(name = "FIS_POSITION", uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_POSITION_UNQ", columnNames = {"POSITION_NBR"})
})
@Entity(name = "fis_Position")
public class Position {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OrderBy("effectiveDate")
    @Composition
    @OneToMany(mappedBy = "position")
    private List<PositionAction> actions;

    @Comment("Empl ID")
    @Column(name = "EMPLID", length = 11)
    private String emplid;

    @Column(name = "NAME")
    private String name;

    @Comment("Position Number")
    @Column(name = "POSITION_NBR", nullable = false, length = 8)
    @NotNull
    private String positionNbr;

    @Comment("Job Title")
    @Column(name = "JOBTITLE", nullable = false, length = 30)
    @NotNull
    private String jobtitle;

    @Comment("Regular/Temporary")
    @Column(name = "REG_TEMP", nullable = false, length = 1)
    @NotNull
    private String regTemp;

    @Comment("Standard Hours")
    @DecimalMax(message = "Standard hours cannot be greater than 40", value = "40")
    @DecimalMin(message = "Standard hours cannot be less than 0", value = "0")
    @Column(name = "STD_HOURS", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal stdHours = BigDecimal.ZERO;

    @Comment("Appointment Expiration Date")
    @Column(name = "GVT_APPT_EXPIR_DT")
    private LocalDate gvtApptExpirDt;

    @Comment("Not-to-Exceed Date")
    @Column(name = "GVT_PAR_NTE_DATE")
    private LocalDate gvtParNteDate;

    @Comment("Cost Organization")
    @Column(name = "JL_COST_ORG_CD", nullable = false, length = 7)
    @NotNull
    private String jlCostOrgCd;

    @Comment("Pay Group")
    @Column(name = "PAYGROUP", length = 3)
    private String paygroup;

    @Comment("Salary Grade")
    @Column(name = "GRADE", length = 3)
    private String grade;

    @NumberFormat(pattern = "#,##0.00")
    @PositiveOrZero(message = "Hourly rate cannot be less than zero")
    @Comment("Hourly Rate")
    @Column(name = "HOURLY_RT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal hourlyRt = BigDecimal.ZERO;

    @NumberFormat(pattern = "#,##0.00")
    @Comment("Annual Rate")
    @Column(name = "ANNUAL_RT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal annualRt = BigDecimal.ZERO;

    @NumberFormat(pattern = "###,##0")
    @PositiveOrZero(message = "Total pay cannot be negative")
    @Column(name = "TOTAL_PAY", nullable = false, precision = 19, scale = 0)
    @NotNull
    private BigDecimal totalPay = BigDecimal.ZERO;

    @Comment("Work Schedule")
    @Column(name = "GVT_WORK_SCHED", nullable = false, length = 1)
    @NotNull
    private String gvtWorkSched;

    @Column(name = "STATUS", nullable = false, length = 1)
    @NotNull
    private String status;

    @Column(name = "MEMO")
    @Lob
    private String memo;

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

    public void setGvtParNteDate(LocalDate gvtParNteDate) {
        this.gvtParNteDate = gvtParNteDate;
    }

    public LocalDate getGvtParNteDate() {
        return gvtParNteDate;
    }

    public void setGvtApptExpirDt(LocalDate gvtApptExpirDt) {
        this.gvtApptExpirDt = gvtApptExpirDt;
    }

    public LocalDate getGvtApptExpirDt() {
        return gvtApptExpirDt;
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTotalPay() {
        return totalPay;
    }

    public void setTotalPay(BigDecimal totalPay) {
        this.totalPay = totalPay;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = safeTrim(memo);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = safeToUpperCase(status);
    }

    public String getGvtWorkSched() {
        return gvtWorkSched;
    }

    public void setGvtWorkSched(String gvtWorkSched) {
        this.gvtWorkSched = safeToUpperCase(gvtWorkSched);
    }

    public BigDecimal getAnnualRt() {
        return annualRt;
    }

    public void setAnnualRt(BigDecimal annualRt) {
        this.annualRt = annualRt;
    }

    public BigDecimal getHourlyRt() {
        return hourlyRt;
    }

    public void setHourlyRt(BigDecimal hourlyRt) {
        this.hourlyRt = hourlyRt;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getPaygroup() {
        return paygroup;
    }

    public void setPaygroup(String paygroup) {
        this.paygroup = safeToUpperCase(paygroup);
    }

    public String getJlCostOrgCd() {
        return jlCostOrgCd;
    }

    public void setJlCostOrgCd(String jlCostOrgCd) {
        this.jlCostOrgCd = safeToUpperCase(jlCostOrgCd);
    }

    public BigDecimal getStdHours() {
        return stdHours;
    }

    public void setStdHours(BigDecimal stdHours) {
        this.stdHours = stdHours;
    }

    public String getRegTemp() {
        return regTemp;
    }

    public void setRegTemp(String regTemp) {
        this.regTemp = safeToUpperCase(regTemp);
    }

    public String getJobtitle() {
        return jobtitle;
    }

    public void setJobtitle(String jobtitle) {
        this.jobtitle = jobtitle;
    }

    public String getPositionNbr() {
        return positionNbr;
    }

    public void setPositionNbr(String positionNbr) {
        this.positionNbr = positionNbr;
    }

    public String getEmplid() {
        return emplid;
    }

    public void setEmplid(String emplid) {
        this.emplid = emplid;
    }

    public List<PositionAction> getActions() {
        return actions;
    }

    public void setActions(List<PositionAction> actions) {
        this.actions = actions;
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
    @DependsOnProperties({"jobtitle", "positionNbr", "name"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s %s %s",
                metadataTools.format(jobtitle),
                metadataTools.format(positionNbr),
                metadataTools.format(name));
    }
}