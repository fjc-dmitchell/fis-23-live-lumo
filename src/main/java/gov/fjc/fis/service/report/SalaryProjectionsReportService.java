package gov.fjc.fis.service.report;

import gov.fjc.fis.service.PayPeriodService;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.dto.PositionDto;
import gov.fjc.fis.entity.personnel.PayPeriod;
import gov.fjc.fis.entity.personnel.PositionAction;
import gov.fjc.fis.reportdata.SalaryProjectionsReportData;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component("fis_SalaryProjectionsReportService")
public class SalaryProjectionsReportService {
    private final PayPeriodService payPeriodService;

    private final DataManager dataManager;

    public SalaryProjectionsReportService(PayPeriodService payPeriodService, DataManager dataManager) {
        this.payPeriodService = payPeriodService;
        this.dataManager = dataManager;
    }

    public SalaryProjectionsReportData generateReportData(Division division, PayPeriod startingPayPeriod,
                                                          BigDecimal benefitsRate, BigDecimal ficaRate,
                                                          BigDecimal bonusProjection, int scale) {

        var reportData = new SalaryProjectionsReportData(division, startingPayPeriod, benefitsRate, ficaRate);

        var payPeriods = payPeriodService.fetchPayPeriods(startingPayPeriod);
        var positions = payPeriodService.fetchPositions(division);

        var startDate = startingPayPeriod.getStartDate();
        var lastDayOfFiscalYear = payPeriodService.getLastDayOfFiscalYear(startingPayPeriod);
        var numberOfBusinessDays = payPeriodService.businessDaysBetween(startDate, lastDayOfFiscalYear);
        reportData.setNumberPaidDays(numberOfBusinessDays);

        List<PositionDto> positionDtos = new ArrayList<>();

        for (var position : positions) {
            var dto = dataManager.create(PositionDto.class);
            dto.setName(position.getName());
            dto.setTotalPay(position.getTotalPay());

            var stdHours = position.getStdHours();
            var hourlyRate = position.getHourlyRt();
            var projectionTotal = BigDecimal.ZERO;
            var lumpSum = BigDecimal.ZERO;
            var actionDescription = "";

//            PositionAction mostRecentBeforeStart = position.getActions().stream()
//                    .filter(a -> a.getEffectiveDate().isBefore(startDate))
//                    .max(Comparator.comparing(PositionAction::getEffectiveDate))
//                    .orElse(null);
//
//            if (mostRecentBeforeStart != null) {
//                stdHours = mostRecentBeforeStart.getStdHours();
//                hourlyRate = mostRecentBeforeStart.getHourlyRt();
//                actionDescription += formatMessage(mostRecentBeforeStart);
//            }

            for (var payPeriod : payPeriods) {
                int totalDaysInPayPeriod;
                int daysBeforeAction = 10;
                if (payPeriod.getEndDate().isAfter(lastDayOfFiscalYear)) {
                    totalDaysInPayPeriod = payPeriodService.businessDaysBetween(payPeriod.getStartDate(), lastDayOfFiscalYear);
                } else {
                    totalDaysInPayPeriod = 10;
                }

                var actionStdHours = stdHours;
                var actionHourlyRate = hourlyRate;

                PositionAction actionInPeriod = position.getActions().stream()
                        .filter(a -> {
                            LocalDate d = a.getEffectiveDate();
                            return !d.isBefore(payPeriod.getStartDate())
                                    && !d.isAfter(payPeriod.getEndDate());
                        })
                        .max(Comparator.comparing(PositionAction::getEffectiveDate))
                        .orElse(null);

                if (actionInPeriod != null) {
                    if (!actionInPeriod.getEffectiveDate().isAfter(lastDayOfFiscalYear)) {
                        actionStdHours = actionInPeriod.getStdHours();
                        actionHourlyRate = actionInPeriod.getHourlyRt();
                        var tempEffectiveDate = actionInPeriod.getEffectiveDate();
                        // terminations are effective at end of day
                        if (actionInPeriod.getActionCode().getNatureOfActionCode().startsWith("3")) {
                            tempEffectiveDate = actionInPeriod.getEffectiveDate().plusDays(1);
                        }
                        daysBeforeAction = payPeriodService.businessDaysBetween(payPeriod.getStartDate(), tempEffectiveDate.minusDays(1));
                        lumpSum = lumpSum.add(actionInPeriod.getLumpSumPayment());
                        actionDescription += formatMessage(actionInPeriod);
                        System.out.println("There has been an action for: " + position.getName());
                        System.out.println("Before # days: " + daysBeforeAction + " stdHours: " + stdHours + " hourlyRate: " + hourlyRate);
                        System.out.println("After # days: " + (totalDaysInPayPeriod - daysBeforeAction) + " stdHours: " + actionStdHours + " hourlyRate: " + actionHourlyRate);
                    }
                }
                var daysAfterAction = totalDaysInPayPeriod - daysBeforeAction;

                var amountBeforeAction = calculatePay(stdHours, hourlyRate, daysBeforeAction);
                var amountAfterAction = calculatePay(actionStdHours, actionHourlyRate, daysAfterAction);

                stdHours = actionStdHours;
                hourlyRate = actionHourlyRate;

                projectionTotal = projectionTotal.add(amountBeforeAction).add(amountAfterAction);

            }

//            dto.setProjectedSalary(scale(projectionTotal, scale));
//            dto.setLumpSumPayment(scale(lumpSum, scale));

            dto.setProjectedSalary(projectionTotal.setScale(0, RoundingMode.HALF_UP));
            dto.setLumpSumPayment(lumpSum.setScale(0, RoundingMode.HALF_UP));

            // hourly employees only receive FICA benefits, otherwise full benefit rate
            var positionBenefitRate = position.getGvtWorkSched().equalsIgnoreCase("I")
                    ? ficaRate
                    : benefitsRate;
            var salaryWithBenefits = dto.getProjectedSalary().multiply(positionBenefitRate).setScale(0, RoundingMode.HALF_UP);
            var lumpSomeWithFica = dto.getLumpSumPayment().multiply(ficaRate).setScale(0, RoundingMode.HALF_UP);
            dto.setProjectedBenefits(salaryWithBenefits.add(lumpSomeWithFica));

            dto.setActionDescription(actionDescription.trim());
            positionDtos.add(dto);
        }
        if (bonusProjection.signum() != 0) {
            var dto = dataManager.create(PositionDto.class);
            dto.setName("Projected bonuses");
            dto.setProjectedSalary(bonusProjection);
            var bonusBenefits = bonusProjection.multiply(ficaRate).setScale(0, RoundingMode.HALF_UP);
            dto.setLumpSumPayment(BigDecimal.ZERO);
            dto.setProjectedBenefits(bonusBenefits);
            positionDtos.add(dto);
        }
        reportData.setPositionDtos(positionDtos);
        return reportData;
    }

    private BigDecimal calculatePay(BigDecimal stdHours, BigDecimal hourlyRate, int numberOfDays) {
        var hoursPerDay = stdHours.divide(BigDecimal.valueOf(5), 6, RoundingMode.HALF_UP);
        return hoursPerDay.multiply(hourlyRate).multiply(BigDecimal.valueOf(numberOfDays)).setScale(2, RoundingMode.HALF_UP);
    }

    private String formatMessage(PositionAction action) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy");
        var message = action.getActionCode().getTitle();
        DecimalFormat df = new DecimalFormat("#,##0");
        var stdHours = action.getStdHours();
        var stdHoursMessage = "";
        if (stdHours.compareTo(BigDecimal.valueOf(40)) < 0) {
            stdHoursMessage = ". " + stdHours + " hours";
        }
        return String.format("%s (%s) - $%s%s\n", message, action.getEffectiveDate().format(formatter), df.format(action.getTotalPay()), stdHoursMessage);
    }

    private BigDecimal scale(Object value, int scale) {
        return ((BigDecimal) value).setScale(scale, RoundingMode.HALF_UP);
    }
}