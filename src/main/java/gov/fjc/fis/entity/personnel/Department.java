package gov.fjc.fis.entity.personnel;

import io.jmix.core.metamodel.annotation.Comment;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static gov.fjc.fis.FisUtilities.getCreatedModifiedString;

@JmixEntity
@Table(name = "FIS_DEPARTMENT", indexes = {
        @Index(name = "IDX_FIS_DEPARTMENT_UNQ", columnList = "DEPTID", unique = true)
})
@Entity(name = "fis_Department")
public class Department {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Comment("Department")
    @Column(name = "DEPTID", nullable = false, length = 10)
    @NotNull
    private String deptid;

    @NotNull
    @Comment("Effective Date")
    @Column(name = "EFFDT", nullable = false)
    private LocalDate effdt;

    @Comment("Status")
    @Column(name = "EFF_STATUS", nullable = false, length = 1)
    @NotNull
    private String effStatus;

    @Comment("Description")
    @Column(name = "DESCR", nullable = false)
    @NotNull
    private String descr;

    @Comment("Short Description")
    @Column(name = "DESCRSHORT", length = 10)
    private String descrshort;

    @Comment("Cost Organization")
    @Column(name = "JL_COST_ORG_CD", nullable = false, length = 7)
    @NotNull
    private String jlCostOrgCd;

    @Comment("Budget Category Code")
    @Column(name = "JL_BUD_CATG_CD", nullable = false, length = 4)
    @NotNull
    private String jlBudCatgCd;

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

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    public void setEffdt(LocalDate effdt) {
        this.effdt = effdt;
    }

    public LocalDate getEffdt() {
        return effdt;
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
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

    public String getDescrshort() {
        return descrshort;
    }

    public void setDescrshort(String descrshort) {
        this.descrshort = descrshort;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getEffStatus() {
        return effStatus;
    }

    public void setEffStatus(String effStatus) {
        this.effStatus = effStatus;
    }

    public String getDeptid() {
        return deptid;
    }

    public void setDeptid(String deptid) {
        this.deptid = deptid;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}