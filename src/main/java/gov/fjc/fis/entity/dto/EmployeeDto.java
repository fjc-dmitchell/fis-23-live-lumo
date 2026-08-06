package gov.fjc.fis.entity.dto;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@JmixEntity(name = "fis_EmployeeDto")
public class EmployeeDto {

    @CsvBindByPosition(position = 0)
    private String positionNbr;

    @CsvBindByPosition(position = 1)
    private String emplid;

    @InstanceName
    @CsvBindByPosition(position = 2)
    private String name;

    @CsvBindByPosition(position = 3)
    private String deptId;

    @CsvBindByPosition(position = 4)
    private String jobcode;

    @CsvBindByPosition(position = 5)
    private String fullPartTime;

    @CsvBindByPosition(position = 6)
    private String regTemp;

    @CsvBindByPosition(position = 7)
    private String paygroup;

    @CsvBindByPosition(position = 8)
    private String grade;

    @CsvBindByPosition(position = 9)
    private String step;

    @CsvBindByPosition(position = 10)
    private String emplType;

    @CsvBindByPosition(position = 11)
    private BigDecimal stdHours;

    @CsvBindByPosition(position = 12)
    private String jobtitle;

    @CsvBindByPosition(position = 13)
    private BigDecimal hourlyRt;

    @CsvBindByPosition(position = 14)
    private BigDecimal gvtBiweeklyRt;

    @CsvBindByPosition(position = 15)
    private BigDecimal annualRt;

    @CsvBindByPosition(position = 16)
    @CsvDate("yyyy-MM-dd")
    private LocalDate gvtApptExpirDt;

    @CsvBindByPosition(position = 17)
    private String jlBudCatgCd;

    @CsvBindByPosition(position = 18)
    private String jlCostOrgCd;

    @CsvBindByPosition(position = 19)
    private BigDecimal gvtComprate;

    @CsvBindByPosition(position = 20)
    private BigDecimal gvtLocalityAdj;

    @CsvBindByPosition(position = 21)
    private String gvtWorkSched;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getJobcode() {
        return jobcode;
    }

    public void setJobcode(String jobcode) {
        this.jobcode = jobcode;
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

    public String getPaygroup() {
        return paygroup;
    }

    public void setPaygroup(String paygroup) {
        this.paygroup = paygroup;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getEmplType() {
        return emplType;
    }

    public void setEmplType(String emplType) {
        this.emplType = emplType;
    }

    public BigDecimal getStdHours() {
        return stdHours;
    }

    public void setStdHours(BigDecimal stdHours) {
        this.stdHours = stdHours;
    }

    public String getJobtitle() {
        return jobtitle;
    }

    public void setJobtitle(String jobtitle) {
        this.jobtitle = jobtitle;
    }

    public BigDecimal getHourlyRt() {
        return hourlyRt;
    }

    public void setHourlyRt(BigDecimal hourlyRt) {
        this.hourlyRt = hourlyRt;
    }

    public BigDecimal getGvtBiweeklyRt() {
        return gvtBiweeklyRt;
    }

    public void setGvtBiweeklyRt(BigDecimal gvtBiweeklyRt) {
        this.gvtBiweeklyRt = gvtBiweeklyRt;
    }

    public BigDecimal getAnnualRt() {
        return annualRt;
    }

    public void setAnnualRt(BigDecimal annualRt) {
        this.annualRt = annualRt;
    }

    public LocalDate getGvtApptExpirDt() {
        return gvtApptExpirDt;
    }

    public void setGvtApptExpirDt(LocalDate gvtApptExpirDt) {
        this.gvtApptExpirDt = gvtApptExpirDt;
    }

    public String getJlBudCatgCd() {
        return jlBudCatgCd;
    }

    public void setJlBudCatgCd(String jlBudCatgCd) {
        this.jlBudCatgCd = jlBudCatgCd;
    }

    public String getJlCostOrgCd() {
        return jlCostOrgCd;
    }

    public void setJlCostOrgCd(String jlCostOrgCd) {
        this.jlCostOrgCd = jlCostOrgCd;
    }

    public BigDecimal getGvtComprate() {
        return gvtComprate;
    }

    public void setGvtComprate(BigDecimal gvtComprate) {
        this.gvtComprate = gvtComprate;
    }

    public BigDecimal getGvtLocalityAdj() {
        return gvtLocalityAdj;
    }

    public void setGvtLocalityAdj(BigDecimal gvtLocalityAdj) {
        this.gvtLocalityAdj = gvtLocalityAdj;
    }

    public String getGvtWorkSched() {
        return gvtWorkSched;
    }

    public void setGvtWorkSched(String gvtWorkSched) {
        this.gvtWorkSched = gvtWorkSched;
    }

    @Override
    public String toString() {
        return "EmployeeDto{" +
                "positionNbr='" + positionNbr + '\'' +
                ", emplid=" + emplid +
                ", name='" + name + '\'' +
                ", deptId='" + deptId + '\'' +
                ", jobcode='" + jobcode + '\'' +
                ", fullPartTime='" + fullPartTime + '\'' +
                ", regTemp='" + regTemp + '\'' +
                ", paygroup='" + paygroup + '\'' +
                ", grade='" + grade + '\'' +
                ", step='" + step + '\'' +
                ", emplType='" + emplType + '\'' +
                ", stdHours=" + stdHours +
                ", jobtitle='" + jobtitle + '\'' +
                ", hourlyRt=" + hourlyRt +
                ", gvtBiweeklyRt=" + gvtBiweeklyRt +
                ", annualRt=" + annualRt +
                ", gvtApptExpirDt=" + gvtApptExpirDt +
                ", jlBudCatgCd='" + jlBudCatgCd + '\'' +
                ", jlCostOrgCd='" + jlCostOrgCd + '\'' +
                ", gvtComprate=" + gvtComprate +
                ", gvtLocalityAdj=" + gvtLocalityAdj +
                ", gvtWorkSched='" + gvtWorkSched + '\'' +
                '}';
    }
}