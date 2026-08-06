package gov.fjc.fis.reportdata;

import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.dto.PositionDto;
import gov.fjc.fis.entity.personnel.PayPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static gov.fjc.fis.FisUtilities.*;

public class SalaryProjectionsReportData {
    private final LocalDateTime reportDateTime;
    private final String divisionTitle;
    private final String budgetFiscalYear;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private int numberPaidDays;
    private final BigDecimal benefitsRate;
    private final BigDecimal ficaRate;

    private List<PositionDto> positionDtos;

    public SalaryProjectionsReportData(Division division, PayPeriod startingPayPeriod,
                                       int numberPaidDays, BigDecimal benefitsRate, BigDecimal ficaRate) {
        this.divisionTitle = division.getTitle();
        this.startDate = startingPayPeriod.getStartDate();
        this.budgetFiscalYear = division.getAppropriation().getBudgetFiscalYear();
        this.numberPaidDays =numberPaidDays ;

        this.endDate = LocalDate.parse("9/30/" + this.budgetFiscalYear, DateTimeFormatter.ofPattern("M/d/yyyy"));
        this.benefitsRate = benefitsRate;
        this.ficaRate = ficaRate;
        this.reportDateTime = getDateTime();
    }

    public SalaryProjectionsReportData(Division division, PayPeriod startingPayPeriod,
                                       BigDecimal benefitsRate, BigDecimal ficaRate) {
        this.divisionTitle = division.getTitle();
        this.startDate = startingPayPeriod.getStartDate();
        this.budgetFiscalYear = division.getAppropriation().getBudgetFiscalYear();
        this.numberPaidDays = 0;

        this.endDate = LocalDate.parse("9/30/" + this.budgetFiscalYear, DateTimeFormatter.ofPattern("M/d/yyyy"));
        this.benefitsRate = benefitsRate;
        this.ficaRate = ficaRate;
        this.reportDateTime = getDateTime();
    }

    public String getDivisionTitle() {
        return divisionTitle;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getNumberPaidDays() {
        return numberPaidDays;
    }

    public void setNumberPaidDays(int numberPaidDays) {
        this.numberPaidDays = numberPaidDays;
    }

    public BigDecimal getBenefitsRate() {
        return benefitsRate;
    }

    public BigDecimal getFicaRate() {
        return ficaRate;
    }


    public List<PositionDto> getPositionDtos() {
        return positionDtos;
    }

    public void setPositionDtos(List<PositionDto> positionDtos) {
        this.positionDtos = positionDtos;
    }

    public String getReportDateTime() {
        return getDateTimeReportString(reportDateTime);
    }

    public BigDecimal getTotalProjectedSalary() {
        return positionDtos.stream().map(PositionDto::getProjectedSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalProjectedBenefits() {
        return positionDtos.stream().map(PositionDto::getProjectedBenefits).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalPayments() {
        return positionDtos.stream().map(PositionDto::getLumpSumPayment).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getFileName() {
        return "Salary Projections for "
                .concat(divisionTitle)
                .concat(" as of ")
                .concat(getDateTimeFilenameString(reportDateTime));
    }
}
