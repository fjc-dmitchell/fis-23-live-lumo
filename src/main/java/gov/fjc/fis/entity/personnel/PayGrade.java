package gov.fjc.fis.entity.personnel;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;
import java.util.List;

import static gov.fjc.fis.FisUtilities.getCreatedModifiedString;

@JmixEntity
@Table(name = "FIS_PAY_GRADE", indexes = {
        @Index(name = "IDX_FIS_PAY_GRADE_UNQ", columnList = "SETID, SAL_ADMIN_PLAN, GRADE", unique = true)
})
@Entity(name = "fis_PayGrade")
public class PayGrade {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Comment("Set ID")
    @Column(name = "SETID", nullable = false, length = 5)
    @NotNull
    private String setid;
    @Comment("Salary Administration Plan")
    @Column(name = "SAL_ADMIN_PLAN", nullable = false, length = 4)
    @NotNull
    private String salAdminPlan;
    @Comment("Salary Grade")
    @Column(name = "GRADE", nullable = false, length = 3)
    @NotNull
    private String grade;
    @Comment("Grade Name")
    @Column(name = "GRADE_TITLE_JPN")
    private String gradeTitleJpn;
    @Column(name = "LOCALITY_ENTITLED", nullable = false)
    @NotNull
    private Boolean localityEntitled = false;
    @Column(name = "LOCALITY_FORFEITURE", nullable = false)
    @NotNull
    private Boolean localityForfeiture = false;
    @Comment("Description")
    @Column(name = "DESCR")
    private String descr;
    @Comment("Short Description")
    @Column(name = "DESCRSHORT")
    private String descrshort;
    @OrderBy("effdate DESC")
    @Composition
    @OneToMany(mappedBy = "payGrade")
    private List<PayGradeRate> rates;
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

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    @JmixProperty
    public String getLocalityString() {
        if (localityEntitled) {
            return localityForfeiture ? "Entitled / Forfeit" : "Entitled";
        } else {
            return "No Locality";
        }
    }

    public Boolean getLocalityForfeiture() {
        return localityForfeiture;
    }

    public void setLocalityForfeiture(Boolean localityForfeiture) {
        this.localityForfeiture = localityForfeiture;
    }

    public Boolean getLocalityEntitled() {
        return localityEntitled;
    }

    public void setLocalityEntitled(Boolean localityEntitled) {
        this.localityEntitled = localityEntitled;
    }

    public List<PayGradeRate> getRates() {
        return rates;
    }

    public void setRates(List<PayGradeRate> rates) {
        this.rates = rates;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getDescrshort() {
        return descrshort;
    }

    public void setDescrshort(String descrshort) {
        this.descrshort = descrshort;
    }

    public String getGradeTitleJpn() {
        return gradeTitleJpn;
    }

    public void setGradeTitleJpn(String gradeTitleJpn) {
        this.gradeTitleJpn = gradeTitleJpn;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getSalAdminPlan() {
        return salAdminPlan;
    }

    public void setSalAdminPlan(String salAdminPlan) {
        this.salAdminPlan = salAdminPlan;
    }

    public String getSetid() {
        return setid;
    }

    public void setSetid(String setid) {
        this.setid = setid;
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
    @DependsOnProperties({"setid", "salAdminPlan", "grade"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s-%s-%s",
                metadataTools.format(setid),
                metadataTools.format(salAdminPlan),
                metadataTools.format(grade));
    }
}