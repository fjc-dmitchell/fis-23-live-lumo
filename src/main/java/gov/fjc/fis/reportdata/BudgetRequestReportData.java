package gov.fjc.fis.reportdata;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.dto.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static gov.fjc.fis.FisUtilities.*;

public class BudgetRequestReportData {
    private final String budgetFiscalYear;
    private final String priorBudgetFiscalYear;

//    private BigDecimal uncommittedPriorTwoYearBalance = BigDecimal.ZERO;
//    private BigDecimal uncommittedCurrentOneYearBalance = BigDecimal.ZERO;
//    private BigDecimal uncommittedCurrentTwoYearBalance = BigDecimal.ZERO;

    private BigDecimal oneYearSpendingAuthority = BigDecimal.ZERO;
    private BigDecimal twoYearSpendingAuthority = BigDecimal.ZERO;
    private BigDecimal priorTwoYearCarriedForward = BigDecimal.ZERO;

    List<DivisionDto> divisions;
    List<ActivityDto> activities;
    List<ActivityProjectionDto> projections;
    List<ActivityReimbursementDto> reimbursements;
    List<ObligationDto> obligations;
    List<ObjectCategoryDto> categories;
    List<ObjectCategoryDto> hillPlanCategories;
    List<ObjectClassDto> objectClasses;

    private final LocalDateTime reportDateTime;

    public BudgetRequestReportData(Appropriation appropriation, Appropriation previousAppropriation) {
        this.budgetFiscalYear = appropriation.getBudgetFiscalYear();
        this.priorBudgetFiscalYear = previousAppropriation.getBudgetFiscalYear();
        reportDateTime = getDateTime();
    }

    public String getBudgetFiscalYear() {
        return budgetFiscalYear;
    }

    public String getPriorBudgetFiscalYear() {
        return priorBudgetFiscalYear;
    }

    public BigDecimal getOneYearSpendingAuthority() {
        return oneYearSpendingAuthority;
    }

    public void setOneYearSpendingAuthority(BigDecimal oneYearSpendingAuthority) {
        this.oneYearSpendingAuthority = oneYearSpendingAuthority;
    }

    public BigDecimal getTwoYearSpendingAuthority() {
        return twoYearSpendingAuthority;
    }

    public void setTwoYearSpendingAuthority(BigDecimal twoYearSpendingAuthority) {
        this.twoYearSpendingAuthority = twoYearSpendingAuthority;
    }

    public BigDecimal getPriorTwoYearCarriedForward() {
        return priorTwoYearCarriedForward;
    }

    public void setPriorTwoYearCarriedForward(BigDecimal priorTwoYearCarriedForward) {
        this.priorTwoYearCarriedForward = priorTwoYearCarriedForward;
    }

//    public BigDecimal getUncommittedPriorTwoYearBalance() {
//        return uncommittedPriorTwoYearBalance;
//    }
//
//    public void setUncommittedPriorTwoYearBalance(BigDecimal uncommittedPriorTwoYearBalance) {
//        this.uncommittedPriorTwoYearBalance = uncommittedPriorTwoYearBalance;
//    }
//
//    public BigDecimal getUncommittedCurrentOneYearBalance() {
//        return uncommittedCurrentOneYearBalance;
//    }
//
//    public void setUncommittedCurrentOneYearBalance(BigDecimal uncommittedCurrentOneYearBalance) {
//        this.uncommittedCurrentOneYearBalance = uncommittedCurrentOneYearBalance;
//    }
//
//    public BigDecimal getUncommittedCurrentTwoYearBalance() {
//        return uncommittedCurrentTwoYearBalance;
//    }
//
//    public void setUncommittedCurrentTwoYearBalance(BigDecimal uncommittedCurrentTwoYearBalance) {
//        this.uncommittedCurrentTwoYearBalance = uncommittedCurrentTwoYearBalance;
//    }

    public List<DivisionDto> getDivisions() {
        return divisions;
    }

    public void setDivisions(List<DivisionDto> divisions) {
        this.divisions = divisions;
    }

    public List<ActivityDto> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityDto> activities) {
        this.activities = activities;
    }

    public List<ActivityProjectionDto> getProjections() {
        return projections;
    }

    public void setProjections(List<ActivityProjectionDto> projections) {
        this.projections = projections;
    }

    public List<ActivityReimbursementDto> getReimbursements() {
        return reimbursements;
    }

    public void setReimbursements(List<ActivityReimbursementDto> reimbursements) {
        this.reimbursements = reimbursements;
    }

    public List<ObligationDto> getObligations() {
        return obligations;
    }


    public void setObligations(List<ObligationDto> obligations) {
        this.obligations = obligations;
    }

    public List<ObjectCategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<ObjectCategoryDto> categories) {
        this.categories = categories;
    }

    public List<ObjectCategoryDto> getHillPlanCategories() {
        return hillPlanCategories;
    }

    public void setHillPlanCategories(List<ObjectCategoryDto> hillPlanCategories) {
        this.hillPlanCategories = hillPlanCategories;
    }

    public List<ObjectClassDto> getObjectClasses() {
        return objectClasses;
    }

    public void setObjectClasses(List<ObjectClassDto> objectClasses) {
        this.objectClasses = objectClasses;
    }

    public String getReportDateTime() {
        return getDateTimeReportString(reportDateTime);
    }

    public String getFileName() {
        return String.format(
                "Budget Request FY%s as of %s",
                budgetFiscalYear,
                getDateTimeFilenameString(reportDateTime)
        );
    }
}
