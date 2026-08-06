package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.*;
import gov.fjc.fis.entity.dto.ActivityDto;
import gov.fjc.fis.entity.dto.ObligationDto;
import gov.fjc.fis.reportdata.DivisionObligationReportData;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("fis_DivisionObligationsReportService")
public class DivisionObligationsReportService {
    @Autowired
    private DataManager dataManager;

    public DivisionObligationReportData generateReportDate(Division division, Branch branch) {
        division = branch == null ? division : branch.getDivision();
        var appropriation = division.getAppropriation();

        var reportData = new DivisionObligationReportData(appropriation, division, branch);


        var activities = getActivities(division, branch);
        var activityDtos = convertActivitiesToActivityDtos(activities);

        reportData.setActivityDtos(activityDtos);
        reportData.setActivitiesWithObligations(activityDtos.stream().filter(ActivityDto::hasObligations).collect(Collectors.toList()));

        return reportData;
    }

    private List<Activity> getActivities(Division division, Branch branch) {
        return dataManager.load(Activity.class)
                .query("SELECT a FROM fis_Activity a"
                        + " LEFT JOIN fis_Branch b ON b = a.branch"
                        + " WHERE (:anyBranch = true AND a.division = :division)"
                        + " OR (:anyBranch = false AND a.branch = :branch)"
                        + " ORDER BY a.sortCode, a.activityNumber")
                .parameter("anyBranch", branch == null)
                .parameter("branch", branch)
                .parameter("division", division)
                .fetchPlan("division-obligations-report-fetch-plan")
                .list();
    }

    private List<ActivityDto> convertActivitiesToActivityDtos(List<Activity> activities) {
        List<ActivityDto> activityDtos = new ArrayList<>();
        ActivityDto activityDto;
        List<ObligationDto> obligationDtos;
        ObligationDto obligationDto;

        for (Activity activity : activities) {
            activityDto = dataManager.create(ActivityDto.class);
            activityDto.setId(activity.getId());
            activityDto.setTitle(activity.getTitle());
            activityDto.setActivityNumber(activity.getActivityNumber());
            activityDto.setMemo(activity.getMemo());
            activityDto.setBudgetFiscalYear(activity.getDivision().getAppropriation().getBudgetFiscalYear());
            activityDto.setFundCode(activity.getFund().getFundCode());
            if (activity.getBranch() != null) {
                activityDto.setBranchTitle(activity.getBranch().getTitle());
                activityDto.setBranchCode(activity.getBranch().getBranchCode());
            }

            BigDecimal projections = BigDecimal.ZERO;
            for (var projection : activity.getProjections()) {
                projections = projections.add(projection.getAmount());
            }
            activityDto.setTotalProjected(projections);

            BigDecimal totalObligated = BigDecimal.ZERO;
            BigDecimal totalDisbursed = BigDecimal.ZERO;

            obligationDtos = new ArrayList<>();
            for (var obligation : activity.getObligations()) {
                obligationDto = dataManager.create(ObligationDto.class);
                obligationDto.setDocumentNumber(obligation.getDocumentNumber());
                obligationDto.setBudgetObjectClass(obligation.getObjectClass().getBudgetObjectClass());
                obligationDto.setVendor(obligation.getVendor());
                obligationDto.setAmount(obligation.getAmount());
                totalObligated = totalObligated.add(obligation.getAmount());
                obligationDto.setStatus(obligation.getStatus());

                if (obligation.getStatus()) {
                    BigDecimal invoices = BigDecimal.ZERO;
                    for (var invoice : obligation.getInvoices()) {
                        invoices = invoices.add(invoice.getAmount());
                    }
                    obligationDto.setDisbursed(invoices);

                } else {
                    obligationDto.setDisbursed(obligation.getAmount());
                }
                totalDisbursed = totalDisbursed.add(obligationDto.getDisbursed());

                obligationDtos.add(obligationDto);
            }
            activityDto.setTotalObligated(totalObligated);
            activityDto.setTotalDisbursed(totalDisbursed);
            activityDto.setObligationDtos(obligationDtos);

            activityDtos.add(activityDto);
        }
        return activityDtos;
    }
}
