package gov.fjc.fis.entity.personnel;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDeleteInverse;
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

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@JmixEntity
@Table(name = "FIS_PAY_GRADE_RATE", indexes = {
        @Index(name = "IDX_FIS_PAY_GRADE_RATE_PAY_GRADE", columnList = "PAY_GRADE_ID"),
        @Index(name = "IDX_FIS_PAY_GRADE_RATE_UNQ", columnList = "PAY_GRADE_ID, EFFDATE", unique = true),
        @Index(name = "IDX_FIS_PAY_GRADE_RATE_EFFECTIVE_PAY_PERIOD", columnList = "PAY_PERIOD_ID")
})
@Entity(name = "fis_PayGradeRate")
public class PayGradeRate {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "PAY_GRADE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PayGrade payGrade;
    @NotNull
    @Comment("Effective Date")
    @Column(name = "EFFDATE", nullable = false)
    private LocalDate effdate;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "PAY_PERIOD_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PayPeriod effectivePayPeriod;

    @Column(name = "MIN_RT_ANNUAL", nullable = false)
    @NotNull
    private Integer minRtAnnual = 0;

    @Column(name = "MAX_RT_ANNUAL", nullable = false)
    @NotNull
    private Integer maxRtAnnual = 0;

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

    private static final ThreadLocal<DecimalFormat> RANGE_NUMBER_FORMAT =
            ThreadLocal.withInitial(() -> new DecimalFormat("#,###"));

    public void setEffdate(LocalDate effdate) {
        this.effdate = effdate;
    }

    public LocalDate getEffdate() {
        return effdate;
    }

    public PayPeriod getEffectivePayPeriod() {
        return effectivePayPeriod;
    }

    public void setEffectivePayPeriod(PayPeriod effectivePayPeriod) {
        this.effectivePayPeriod = effectivePayPeriod;
    }

    public Integer getMaxRtAnnual() {
        return maxRtAnnual;
    }

    public void setMaxRtAnnual(Integer maxRtAnnual) {
        this.maxRtAnnual = maxRtAnnual;
    }

    public Integer getMinRtAnnual() {
        return minRtAnnual;
    }

    public void setMinRtAnnual(Integer minRtAnnual) {
        this.minRtAnnual = minRtAnnual;
    }

    public gov.fjc.fis.entity.personnel.PayGrade getPayGrade() {
        return payGrade;
    }

    public void setPayGrade(gov.fjc.fis.entity.personnel.PayGrade payGrade) {
        this.payGrade = payGrade;
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

    @JmixProperty
    @DependsOnProperties({"minRtAnnual", "maxRtAnnual"})
    public Integer getQuartile1Cap() {
        return computeQuartileCaps()[0];
    }

    @JmixProperty
    @DependsOnProperties({"minRtAnnual", "maxRtAnnual"})
    public Integer getQuartile2Cap() {
        return computeQuartileCaps()[1];
    }

    @JmixProperty
    @DependsOnProperties({"minRtAnnual", "maxRtAnnual"})
    public Integer getQuartile3Cap() {
        return computeQuartileCaps()[2];
    }

    @JmixProperty
    @DependsOnProperties({"minRtAnnual", "maxRtAnnual"})
    public Integer getQuartile4Cap() {
        return computeQuartileCaps()[3];
    }

    @JmixProperty
    @DependsOnProperties({"minRtAnnual", "maxRtAnnual"})
    public String getQuartile1Range() {
        int[] caps = computeQuartileCaps();
        return formatRange(minRtAnnual, caps[0]);
    }

    @JmixProperty
    @DependsOnProperties({"minRtAnnual", "maxRtAnnual"})
    public String getQuartile2Range() {
        int[] caps = computeQuartileCaps();
        return formatRange(caps[0] + 1, caps[1]);
    }

    @JmixProperty
    @DependsOnProperties({"minRtAnnual", "maxRtAnnual"})
    public String getQuartile3Range() {
        int[] caps = computeQuartileCaps();
        return formatRange(caps[1] + 1, caps[2]);
    }

    @JmixProperty
    @DependsOnProperties({"minRtAnnual", "maxRtAnnual"})
    public String getQuartile4Range() {
        int[] caps = computeQuartileCaps();
        return formatRange(caps[2] + 1, caps[3]);
    }

    private int[] computeQuartileCaps() {
        if (minRtAnnual == null || maxRtAnnual == null) {
            return new int[4];
        }

        double step = (maxRtAnnual - minRtAnnual) / 4.0;
        int[] caps = new int[4];
        for (int i = 0; i < 3; i++) {
            caps[i] = (int) Math.round(minRtAnnual + step * (i + 1)) - 1;
        }
        caps[3] = maxRtAnnual;
        return caps;
    }

    private static String formatRange(int rangeMin, int rangeMax) {
        DecimalFormat format = RANGE_NUMBER_FORMAT.get();
        return String.format("%s - %s",
                format.format(rangeMin),
                format.format(rangeMax));
    }
}