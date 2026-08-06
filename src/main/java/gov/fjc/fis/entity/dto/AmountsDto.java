package gov.fjc.fis.entity.dto;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@JmixEntity(name = "fis_AmountsDto")
public class AmountsDto {
    @JmixGeneratedValue
    @JmixId
    private Integer id;

    @InstanceName
    @JmixProperty(mandatory = true)
    @NotNull
    private String title;

    @JmixProperty(mandatory = true)
    @NotNull
    private BigDecimal oneYearAmount;

    @JmixProperty(mandatory = true)
    @NotNull
    private BigDecimal twoYearAmount;

    @JmixProperty(mandatory = true)
    @NotNull
    private BigDecimal totalAmount;

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTwoYearAmount() {
        return twoYearAmount;
    }

    public void setTwoYearAmount(BigDecimal twoYearAmount) {
        this.twoYearAmount = twoYearAmount;
    }

    public BigDecimal getOneYearAmount() {
        return oneYearAmount;
    }

    public void setOneYearAmount(BigDecimal oneYearAmount) {
        this.oneYearAmount = oneYearAmount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}