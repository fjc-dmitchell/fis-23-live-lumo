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

    public ResolvedContext(Document document, Fund twoYearFund, Division educationDivision) {
        this.document = document;
        this.twoYearFund = twoYearFund;
        this.educationDivision = educationDivision;
    }

    // Getter/setter + builder-like fluent update methods

    public Document getDocument() {
        return document;
    }

    public ResolvedContext withDocument(Document document) {
        this.document = document;
        return this;
    }

    public Fund getTwoYearFund() {
        return twoYearFund;
    }

    public ResolvedContext withTwoYearFund(Fund twoYearFund) {
        this.twoYearFund = twoYearFund;
        return this;
    }

    public Division getEducationDivision() {
        return educationDivision;
    }

    public ResolvedContext withEducationDivision(Division educationDivision) {
        this.educationDivision = educationDivision;
        return this;
    }

    public Fund getFund() {
        return fund;
    }

    public ResolvedContext withFund(Fund fund) {
        this.fund = fund;
        return this;
    }

    public Division getDivision() {
        return division;
    }

    public ResolvedContext withDivision(Division division) {
        this.division = division;
        return this;
    }

    public Activity getActivity() {
        return activity;
    }

    public ResolvedContext withActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public Activity getProjectionActivity() {
        return projectionActivity;
    }

    public ResolvedContext withProjectionActivity(Activity projectionActivity) {
        this.projectionActivity = projectionActivity;
        return this;
    }

    public ObjectClass getObjectClass() {
        return objectClass;
    }

    public ResolvedContext withObjectClass(ObjectClass objectClass) {
        this.objectClass = objectClass;
        return this;
    }

    public ObjectClass getProjectionObjectClass() {
        return projectionObjectClass;
    }

    public ResolvedContext withProjectionObjectClass(ObjectClass projectionObjectClass) {
        this.projectionObjectClass = projectionObjectClass;
        return this;
    }

    public Obligation getObligation() {
        return obligation;
    }

    public ResolvedContext withObligation(Obligation obligation) {
        this.obligation = obligation;
        return this;
    }

    public BigDecimal getPreviousObligationAmount() {
        return previousObligationAmount;
    }

    public void setPreviousObligationAmount(BigDecimal previousObligationAmount) {
        this.previousObligationAmount = previousObligationAmount;
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

    public BigDecimal getObligationAmountDifference() {
        return obligationAmountDifference;
    }

    public void setObligationAmountDifference(BigDecimal obligationAmountDifference) {
        this.obligationAmountDifference = obligationAmountDifference;
    }

    public boolean createFcn() {
        return BigDecimal.ZERO.compareTo(obligationAmountDifference) != 0;
    }
}