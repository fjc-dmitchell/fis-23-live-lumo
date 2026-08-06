package gov.fjc.fis.entity.personnel;

import io.jmix.core.DeletePolicy;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

import static gov.fjc.fis.FisUtilities.getCreatedModifiedString;
import static java.util.Objects.requireNonNullElse;

@JmixEntity
@Table(name = "FIS_PAY_ADJUSTMENT", uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_PAY_ADJUSTMENT_UNQ", columnNames = {"PAY_PERIOD_ID"})
})
@Entity(name = "fis_PayAdjustment")
public class PayAdjustment {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "PAY_PERIOD_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PayPeriod payPeriod;

    @Column(name = "ADJUSTMENT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal adjustment = BigDecimal.ZERO;

    @Column(name = "LOCALITY_RATE", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal localityRate = BigDecimal.ZERO;

    @Column(name = "OLD_LOCALITY_RATE", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal oldLocalityRate = BigDecimal.ZERO;

    @Column(name = "SALARY_CAP", precision = 19, scale = 2)
    private BigDecimal salaryCap;

    @Column(name = "RATES_FINAL", nullable = false)
    @NotNull
    private Boolean ratesFinal = false;

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

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    @JmixProperty
    public BigDecimal getNetIncrease() {
        var oneHundred = new BigDecimal("100");
        var tempAdj = adjustment.divide(oneHundred, 5, RoundingMode.HALF_UP).add(BigDecimal.ONE);
        var tempNewLoc = localityRate.divide(oneHundred, 5, RoundingMode.HALF_UP).add(BigDecimal.ONE);
        var tempOldLoc = oldLocalityRate.divide(oneHundred, 5, RoundingMode.HALF_UP).add(BigDecimal.ONE);
        return tempAdj.multiply(tempNewLoc).divide(tempOldLoc, 4, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(oneHundred);
    }

    @JmixProperty
    public String getRatesFinalString() {
        return ratesFinal ? "Final" : "Estimate";
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Boolean getRatesFinal() {
        return ratesFinal;
    }

    public void setRatesFinal(Boolean ratesFinal) {
        this.ratesFinal = ratesFinal;
    }

    public BigDecimal getSalaryCap() {
        return salaryCap;
    }

    public void setSalaryCap(BigDecimal salaryCap) {
        this.salaryCap = salaryCap;
    }

    public BigDecimal getOldLocalityRate() {
        return oldLocalityRate;
    }

    public void setOldLocalityRate(BigDecimal oldLocalityRate) {
        this.oldLocalityRate = oldLocalityRate;
    }

    public BigDecimal getLocalityRate() {
        return localityRate;
    }

    public void setLocalityRate(BigDecimal localityRate) {
        this.localityRate = localityRate;
    }

    public BigDecimal getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(BigDecimal adjustment) {
        this.adjustment = requireNonNullElse(adjustment, BigDecimal.ZERO);
    }

    public PayPeriod getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(PayPeriod payPeriod) {
        this.payPeriod = payPeriod;
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
    @DependsOnProperties({"payPeriod"})
    public String getInstanceName(MetadataTools metadataTools) {
        return metadataTools.format(payPeriod);
    }
}