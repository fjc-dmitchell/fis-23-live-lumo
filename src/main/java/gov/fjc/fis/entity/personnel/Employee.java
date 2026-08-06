package gov.fjc.fis.entity.personnel;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;

import static gov.fjc.fis.FisUtilities.add;
import static gov.fjc.fis.FisUtilities.getLoadedByString;

@JmixEntity
@Table(name = "FIS_EMPLOYEE", uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_EMPLOYEE_UNQ", columnNames = {"EMPLID"})
})
@Entity(name = "fis_Employee")
public class Employee {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "EMPLID", nullable = false, length = 11)
    private String emplid;

    @Column(name = "NAME", nullable = false, length = 50)
    @NotNull
    private String name;

    @Column(name = "JOBCODE", nullable = false, length = 6)
    @NotNull
    private String jobcode;

    @Column(name = "POSITION_NBR", nullable = false, length = 8)
    @NotNull
    private String positionNbr;

    @Column(name = "JOBTITLE", nullable = false, length = 30)
    @NotNull
    private String jobtitle;

    @Column(name = "EMPL_TYPE", nullable = false, length = 1)
    @NotNull
    private String emplType;

    @Column(name = "REG_TEMP", nullable = false, length = 1)
    @NotNull
    private String regTemp;

    @Column(name = "FULL_PART_TIME", nullable = false, length = 1)
    @NotNull
    private String fullPartTime;

    @Column(name = "STD_HOURS", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal stdHours;

    @Column(name = "GVT_APPT_EXPIR_DT")
    private LocalDate gvtApptExpirDt;

    @Column(name = "JL_BUD_CATG_CD", nullable = false, length = 4)
    @NotNull
    private String jlBudCatgCd;

    @Column(name = "JL_COST_ORG_CD", nullable = false, length = 7)
    @NotNull
    private String jlCostOrgCd;

    @Column(name = "DEPT_ID", nullable = false, length = 10)
    @NotNull
    private String deptId;

    @Column(name = "PAYGROUP", nullable = false, length = 3)
    @NotNull
    private String paygroup;

    // ToDo: 2025-05-07 - request field from gvt_job, also locality code
    @Column(name = "GVT_PAY_PLAN", length = 2)
    private String gvtPayPlan;

    // ToDo: 2025-05-07 - request field from gvt_job
    @Column(name = "SAL_ADMIN_PLAN", length = 4)
    private String salAdminPlan;

    @Column(name = "GRADE", nullable = false, length = 3)
    @NotNull
    private String grade;

    @Column(name = "STEP", nullable = false, length = 2)
    @NotNull
    private String step;

    @Column(name = "HOURLY_RT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal hourlyRt;

    @Column(name = "GVT_BIWEEKLY_RT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal gvtBiweeklyRt;

    @Column(name = "ANNUAL_RT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal annualRt;

    @Column(name = "GVT_COMPRATE", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal gvtComprate;

    @Column(name = "GVT_LOCALITY_ADJ", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal gvtLocalityAdj;

    @Column(name = "GVT_WORK_SCHED", nullable = false, length = 1)
    @NotNull
    private String gvtWorkSched;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    public void setGvtApptExpirDt(LocalDate gvtApptExpirDt) {
        this.gvtApptExpirDt = gvtApptExpirDt;
    }

    public LocalDate getGvtApptExpirDt() {
        return gvtApptExpirDt;
    }

    @DependsOnProperties({"gvtComprate", "gvtLocalityAdj"})
    @JmixProperty
    public BigDecimal getTotalPay() {
        return add(gvtComprate, gvtLocalityAdj);
    }

    public void setEmplid(String emplid) {
        this.emplid = emplid;
    }

    public String getEmplid() {
        return emplid;
    }

    public String getSalAdminPlan() {
        return salAdminPlan;
    }

    public void setSalAdminPlan(String salAdminPlan) {
        this.salAdminPlan = salAdminPlan;
    }

    public String getGvtPayPlan() {
        return gvtPayPlan;
    }

    public void setGvtPayPlan(String gvtPayPlan) {
        this.gvtPayPlan = gvtPayPlan;
    }

    @DependsOnProperties({"createdBy", "createdDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getLoadedByString(createdBy, createdDate);
    }

    public String getPositionNbr() {
        return positionNbr;
    }

    public void setPositionNbr(String positionNbr) {
        this.positionNbr = positionNbr;
    }

    public String getGvtWorkSched() {
        return gvtWorkSched;
    }

    public void setGvtWorkSched(String gvtWorkSched) {
        this.gvtWorkSched = gvtWorkSched;
    }

    public BigDecimal getGvtLocalityAdj() {
        return gvtLocalityAdj;
    }

    public void setGvtLocalityAdj(BigDecimal gvtLocalityAdj) {
        this.gvtLocalityAdj = gvtLocalityAdj;
    }

    public BigDecimal getGvtComprate() {
        return gvtComprate;
    }

    public void setGvtComprate(BigDecimal gvtComprate) {
        this.gvtComprate = gvtComprate;
    }

    public BigDecimal getAnnualRt() {
        return annualRt;
    }

    public void setAnnualRt(BigDecimal annualRt) {
        this.annualRt = annualRt;
    }

    public BigDecimal getGvtBiweeklyRt() {
        return gvtBiweeklyRt;
    }

    public void setGvtBiweeklyRt(BigDecimal gvtBiweeklyRt) {
        this.gvtBiweeklyRt = gvtBiweeklyRt;
    }

    public BigDecimal getHourlyRt() {
        return hourlyRt;
    }

    public void setHourlyRt(BigDecimal hourlyRt) {
        this.hourlyRt = hourlyRt;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
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
        this.paygroup = paygroup;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getJlCostOrgCd() {
        return jlCostOrgCd;
    }

    public void setJlCostOrgCd(String jlCostOrgCd) {
        this.jlCostOrgCd = jlCostOrgCd;
    }

    public String getJlBudCatgCd() {
        return jlBudCatgCd;
    }

    public void setJlBudCatgCd(String jlBudCatgCd) {
        this.jlBudCatgCd = jlBudCatgCd;
    }

    public BigDecimal getStdHours() {
        return stdHours;
    }

    public void setStdHours(BigDecimal stdHours) {
        this.stdHours = stdHours;
    }

    public String getFullPartTime() {
        return fullPartTime;
    }

    public void setFullPartTime(String fullPartTime) {
        this.fullPartTime = fullPartTime;
    }

    public String getRegTemp() {
        return regTemp;
    }

    public void setRegTemp(String regTemp) {
        this.regTemp = regTemp;
    }

    public String getEmplType() {
        return emplType;
    }

    public void setEmplType(String emplType) {
        this.emplType = emplType;
    }

    public String getJobtitle() {
        return jobtitle;
    }

    public void setJobtitle(String jobtitle) {
        this.jobtitle = jobtitle;
    }

    public String getJobcode() {
        return jobcode;
    }

    public void setJobcode(String jobcode) {
        this.jobcode = jobcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @DependsOnProperties({"createdDate"})
    @JmixProperty
    public LocalDate getLoadedDate() {
        return createdDate.toLocalDate();
    }

    @InstanceName
    @DependsOnProperties({"name", "emplid"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s %s",
                metadataTools.format(name),
                metadataTools.format(emplid));
    }
}