package gov.fjc.fis.job.jifms;

import java.math.BigDecimal;

public class ProjectionAuditFields {
    private String currentActivityNumber;
    private String currentProjectionBoc;
    private BigDecimal currentProjectionAmountBefore;
    private BigDecimal currentProjectionAmountAfter;
    private String previousActivityNumber;
    private String previousProjectionBoc;
    private BigDecimal previousProjectionAmountBefore;
    private BigDecimal previousProjectionAmountAfter;

    public String getCurrentActivityNumber() {
        return currentActivityNumber;
    }

    public void setCurrentActivityNumber(String currentActivityNumber) {
        this.currentActivityNumber = currentActivityNumber;
    }

    public String getCurrentProjectionBoc() {
        return currentProjectionBoc;
    }

    public void setCurrentProjectionBoc(String currentProjectionBoc) {
        this.currentProjectionBoc = currentProjectionBoc;
    }

    public BigDecimal getCurrentProjectionAmountBefore() {
        return currentProjectionAmountBefore;
    }

    public void setCurrentProjectionAmountBefore(BigDecimal currentProjectionAmountBefore) {
        this.currentProjectionAmountBefore = currentProjectionAmountBefore;
    }

    public BigDecimal getCurrentProjectionAmountAfter() {
        return currentProjectionAmountAfter;
    }

    public void setCurrentProjectionAmountAfter(BigDecimal currentProjectionAmountAfter) {
        this.currentProjectionAmountAfter = currentProjectionAmountAfter;
    }

    public String getPreviousActivityNumber() {
        return previousActivityNumber;
    }

    public void setPreviousActivityNumber(String previousActivityNumber) {
        this.previousActivityNumber = previousActivityNumber;
    }

    public String getPreviousProjectionBoc() {
        return previousProjectionBoc;
    }

    public void setPreviousProjectionBoc(String previousProjectionBoc) {
        this.previousProjectionBoc = previousProjectionBoc;
    }

    public BigDecimal getPreviousProjectionAmountBefore() {
        return previousProjectionAmountBefore;
    }

    public void setPreviousProjectionAmountBefore(BigDecimal previousProjectionAmountBefore) {
        this.previousProjectionAmountBefore = previousProjectionAmountBefore;
    }

    public BigDecimal getPreviousProjectionAmountAfter() {
        return previousProjectionAmountAfter;
    }

    public void setPreviousProjectionAmountAfter(BigDecimal previousProjectionAmountAfter) {
        this.previousProjectionAmountAfter = previousProjectionAmountAfter;
    }
}
