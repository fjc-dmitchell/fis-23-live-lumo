package gov.fjc.fis.entity.personnel;

import io.jmix.core.DeletePolicy;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.OnDeleteInverse;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static gov.fjc.fis.FisUtilities.getCreatedModifiedString;
import static java.util.Objects.requireNonNullElse;

@JmixEntity
@Table(name = "FIS_POSITION_ACTION", indexes = {
        @Index(name = "IDX_FIS_POSITION_ACTION_PAY_PERIOD", columnList = "PAY_PERIOD_ID"),
        @Index(name = "IDX_FIS_POSITION_ACTION_ACTION_CODE", columnList = "ACTION_CODE_ID"),
        @Index(name = "IDX_FIS_POSITION_ACTION_POSITION", columnList = "POSITION_ID")
}, uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_POSITION_ACTION_UNQ", columnNames = {"POSITION_ID", "PAY_PERIOD_ID", "ACTION_CODE_ID"})
})
@Entity(name = "fis_PositionAction")
public class PositionAction {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "POSITION_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Position position;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "PAY_PERIOD_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private PayPeriod payPeriod;

    @Column(name = "EFFECTIVE_DATE", nullable = false)
    @NotNull
    private LocalDate effectiveDate;

    @DecimalMax(message = "Standard hours cannot be greater than 40", value = "40")
    @DecimalMin(message = "Standard hours cannot be less than 0", value = "0")
    @Column(name = "STD_HOURS", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal stdHours;

    @NumberFormat(pattern = "###,##0")
    @PositiveOrZero(message = "Total pay cannot be negative")
    @Comment("sf50 total pay")
    @Column(name = "TOTAL_PAY", nullable = false, precision = 19, scale = 0)
    @NotNull
    private BigDecimal totalPay;

    @NumberFormat(pattern = "#,##0.00")
    @Column(name = "HOURLY_RT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal hourlyRt;

    @NotNull
    @Column(name = "LUMP_SUM_PAYMENT", nullable = false, precision = 19, scale = 2)
    private BigDecimal lumpSumPayment = BigDecimal.ZERO;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "ACTION_CODE_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ActionCode actionCode;

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

    public BigDecimal getLumpSumPayment() {
        return lumpSumPayment;
    }

    public void setLumpSumPayment(BigDecimal lumpSumPayment) {
        this.lumpSumPayment = lumpSumPayment;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    @NumberFormat(pattern = "#,##0.00")
    @DependsOnProperties({"stdHours", "hourlyRt"})
    @JmixProperty
    public BigDecimal getAnnualRt() {
        return requireNonNullElse(hourlyRt, BigDecimal.ZERO)
                .multiply(requireNonNullElse(stdHours, BigDecimal.ZERO)
                        .multiply(new BigDecimal(52)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getHourlyRt() {
        return hourlyRt;
    }

    public void setHourlyRt(BigDecimal hourlyRt) {
        this.hourlyRt = hourlyRt;
    }

    public BigDecimal getTotalPay() {
        return totalPay;
    }

    public void setTotalPay(BigDecimal totalPay) {
        this.totalPay = totalPay;
    }

    public BigDecimal getStdHours() {
        return stdHours;
    }

    public void setStdHours(BigDecimal stdHours) {
        this.stdHours = stdHours;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    public ActionCode getActionCode() {
        return actionCode;
    }

    public void setActionCode(ActionCode actionCode) {
        this.actionCode = actionCode;
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
    @DependsOnProperties({"position", "actionCode", "payPeriod"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s %s %s",
                metadataTools.format(position),
                metadataTools.format(actionCode),
                metadataTools.format(payPeriod));
    }
}