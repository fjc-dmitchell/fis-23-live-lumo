package gov.fjc.fis.entity.dto;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;

@JmixEntity(name = "fis_PositionDto")
public class PositionDto {
    @JmixGeneratedValue
    @JmixId
    private Integer id;

    @InstanceName
    private String name;

    private BigDecimal totalPay = BigDecimal.ZERO;

    private String actionDescription;

    private BigDecimal stdHours = BigDecimal.ZERO;

    private BigDecimal hourlyRt = BigDecimal.ZERO;

    private BigDecimal lumpSumPayment = BigDecimal.ZERO;

    private BigDecimal projectedSalary = BigDecimal.ZERO;

    private BigDecimal projectedBenefits = BigDecimal.ZERO;

    public BigDecimal getLumpSumPayment() {
        return lumpSumPayment;
    }

    public void setLumpSumPayment(BigDecimal lumpSumPayment) {
        this.lumpSumPayment = lumpSumPayment;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public BigDecimal getHourlyRt() {
        return hourlyRt;
    }

    public void setHourlyRt(BigDecimal hourlyRt) {
        this.hourlyRt = hourlyRt;
    }

    public BigDecimal getStdHours() {
        return stdHours;
    }

    public void setStdHours(BigDecimal stdHours) {
        this.stdHours = stdHours;
    }

    public BigDecimal getProjectedBenefits() {
        return projectedBenefits;
    }

    public void setProjectedBenefits(BigDecimal projectedBenefits) {
        this.projectedBenefits = projectedBenefits;
    }

    public BigDecimal getProjectedSalary() {
        return projectedSalary;
    }

    public void calculateProjectedSalary(int numberPaidDays) {
        var hoursPerDay = this.stdHours
                .divide(BigDecimal.valueOf(40), RoundingMode.UNNECESSARY)
                .multiply(BigDecimal.valueOf(8));
        this.projectedSalary = hoursPerDay
                .multiply(hourlyRt)
                .multiply(BigDecimal.valueOf(numberPaidDays));
    }

    public void setProjectedSalary(BigDecimal projectedSalary) {
        this.projectedSalary = projectedSalary;
    }

    public BigDecimal getProjectedTotal() {
        return projectedSalary.add(projectedBenefits);
    }

    public BigDecimal getTotalPay() {
        return totalPay;
    }

    public void setTotalPay(BigDecimal totalPay) {
        this.totalPay = totalPay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}