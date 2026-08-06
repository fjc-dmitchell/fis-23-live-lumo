package gov.fjc.fis.entity.personnel;

import io.jmix.core.metamodel.annotation.Comment;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static gov.fjc.fis.FisUtilities.getCreatedModifiedString;

@JmixEntity
@Table(name = "FIS_LOCALITY_AREA", indexes = {
        @Index(name = "IDX_FIS_LOCALITY_AREA_UNQ", columnList = "GVT_LOCALITY_AREA, EFFDT", unique = true)
})
@Entity(name = "fis_LocalityArea")
public class LocalityArea {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Comment("locality area code")
    @Column(name = "GVT_LOCALITY_AREA", nullable = false, length = 2)
    @NotNull
    private String gvtLocalityArea;

    @NotNull
    @Comment("Effective Date")
    @Column(name = "EFFDT", nullable = false)
    private LocalDate effdt;

    @Comment("locality description")
    @Column(name = "DESCR")
    private String descr;

    @Comment("locality rate")
    @Column(name = "GVT_LOCALITY_PCT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal gvtLocalityPct;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    public LocalDate getEffdt() {
        return effdt;
    }

    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate);
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

    public BigDecimal getGvtLocalityPct() {
        return gvtLocalityPct;
    }

    public String getDescr() {
        return descr;
    }

    public String getGvtLocalityArea() {
        return gvtLocalityArea;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}