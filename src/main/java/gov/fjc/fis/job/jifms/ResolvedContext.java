package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.*;

import java.math.BigDecimal;

public class ResolvedContext {

    private Document document;
    private Fund twoYearFund;
    private Division educationDivision;

    private Fund fund;
    private Division division;
    private Activity activity;
    private Activity projectionActivity;
    private ObjectClass objectClass;
    private ObjectClass projectionObjectClass;
    private Obligation obligation;

    private BigDecimal previousObligationAmount;
    private BigDecimal obligationAmountDifference;
    private Activity previousActivity;
    private ObjectClass previousObjectClass;

    private boolean fcnRequired;

    public ResolvedContext(Document document, Fund twoYearFund, Division educationDivision) {
        this.document = document;
        this.twoYearFund = twoYearFund;
        this.educationDivision = educationDivision;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Fund getTwoYearFund() {
        return twoYearFund;
    }

    public void setTwoYearFund(Fund twoYearFund) {
        this.twoYearFund = twoYearFund;
    }

    public Division getEducationDivision() {
        return educationDivision;
    }

    public void setEducationDivision(Division educationDivision) {
        this.educationDivision = educationDivision;
    }

    public Fund getFund() {
        return fund;
    }

    public void setFund(Fund fund) {
        this.fund = fund;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Activity getProjectionActivity() {
        return projectionActivity;
    }

    public void setProjectionActivity(Activity projectionActivity) {
        this.projectionActivity = projectionActivity;
    }

    public ObjectClass getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(ObjectClass objectClass) {
        this.objectClass = objectClass;
    }

    public ObjectClass getProjectionObjectClass() {
        return projectionObjectClass;
    }

    public void setProjectionObjectClass(ObjectClass projectionObjectClass) {
        this.projectionObjectClass = projectionObjectClass;
    }

    public Obligation getObligation() {
        return obligation;
    }

    public void setObligation(Obligation obligation) {
        this.obligation = obligation;
    }

    public BigDecimal getPreviousObligationAmount() {
        return previousObligationAmount;
    }

    public void setPreviousObligationAmount(BigDecimal previousObligationAmount) {
        this.previousObligationAmount = previousObligationAmount;
    }

    public BigDecimal getObligationAmountDifference() {
        return obligationAmountDifference;
    }

    public void setObligationAmountDifference(BigDecimal obligationAmountDifference) {
        this.obligationAmountDifference = obligationAmountDifference;
    }

    public Activity getPreviousActivity() {
        return previousActivity;
    }

    public void setPreviousActivity(Activity previousActivity) {
        this.previousActivity = previousActivity;
    }

    public ObjectClass getPreviousObjectClass() {
        return previousObjectClass;
    }

    public void setPreviousObjectClass(ObjectClass previousObjectClass) {
        this.previousObjectClass = previousObjectClass;
    }

    public boolean isFcnRequired() {
        return fcnRequired;
    }

    public void setFcnRequired(boolean fcnRequired) {
        this.fcnRequired = fcnRequired;
    }
}