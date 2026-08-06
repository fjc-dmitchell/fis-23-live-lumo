package gov.fjc.fis.entity.dto;

import io.jmix.core.metamodel.annotation.JmixEntity;

import java.math.BigDecimal;
import java.util.List;

import static gov.fjc.fis.FisUtilities.getTotalNullAllowed;

@JmixEntity(name = "fis_BranchDto")
public class BranchDto {

    private Integer id;

    private String branchCode;

    private String title;

    private BigDecimal totalAmount;

    private List<ActivityDto> activities;

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTitleAndCode() {
        return String.format("%s (%s)", title, branchCode);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public List<ActivityDto> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityDto> activities) {
        this.activities = activities;
    }

    public void addTotalAmount(BigDecimal amount) {
        totalAmount = getTotalNullAllowed(totalAmount, amount);
    }

    public Integer getActivitiesCount() {
        return activities == null ? 0 : activities.size();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}