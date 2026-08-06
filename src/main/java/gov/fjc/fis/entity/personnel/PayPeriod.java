package gov.fjc.fis.entity.personnel;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static gov.fjc.fis.FisUtilities.getCreatedModifiedString;

@JmixEntity
@Table(name = "FIS_PAY_PERIOD", uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_PAY_PERIOD_UNQ", columnNames = {"START_DATE"}),
        @UniqueConstraint(name = "IDX_FIS_PAY_PERIOD_UNQ_1", columnNames = {"PAY_YEAR", "PAY_PERIOD"})
})
@Entity(name = "fis_PayPeriod")
public class PayPeriod {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "PAY_YEAR", nullable = false)
    @NotNull
    private Integer payYear;

    @Column(name = "PAY_PERIOD", nullable = false)
    @NotNull
    private Integer payPeriod;

    @NotNull
    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "END_DATE", nullable = false)
    private LocalDate endDate;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    @DependsOnProperties({"createdBy", "createdDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, null, null);
    }

//    @DependsOnProperties({"startDate"})
//    @JmixProperty
//    public LocalDate getEndDate() {
//        return startDate == null ? null : startDate.plusDays(13);
//    }

    @DependsOnProperties({"startDate", "payPeriod", "payYear"})
    @JmixProperty
    public String getStartDateDetailed() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        return String.format("%s (pp%d-%d)", dateFormat.format(startDate), payPeriod, payYear);
    }

    public Integer getPayYear() {
        return payYear;
    }

    public void setPayYear(Integer payYear) {
        this.payYear = payYear;
    }

    public Integer getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(Integer payPeriod) {
        this.payPeriod = payPeriod;
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
    @DependsOnProperties({"startDate"})
    public String getInstanceName(DatatypeFormatter datatypeFormatter) {
        return datatypeFormatter.formatLocalDate(startDate);
    }
}