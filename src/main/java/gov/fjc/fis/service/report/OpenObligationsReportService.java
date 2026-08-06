package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.DocumentType;
import gov.fjc.fis.entity.dto.ObligationDto;
import gov.fjc.fis.reportdata.OpenObligationsReportData;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static gov.fjc.fis.FisUtilities.getCurrentDateMinusDays;

@Component("fis_OpenObligationsReportService")
public class OpenObligationsReportService {
    private final DataManager dataManager;

    public OpenObligationsReportService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * find obligations for division or branch that have been open for specified number
     * of days based on obligation travel end date or activity end date
     *
     * @param division
     * @param branch
     * @param numberOfDays
     * @return list of key value entities containing obligation information
     */
    public List<KeyValueEntity> getOpenObligations(Division division, Branch branch, int numberOfDays) {
        var endDate = getCurrentDateMinusDays(numberOfDays);
        var travelAuthorizationId = DocumentType.TRAVEL_AUTHORIZATION.getId();
        var obligations = dataManager.loadValues(
                        "SELECT o.amount, o.documentNumber, o.vendor, a.activityNumber, a.title, a.city,"
                                + " a.state, a.endDate, o.lineNumber"
                                + " FROM fis_Obligation o"
                                + " INNER JOIN fis_Activity a ON a=o.activity"
                                + " WHERE ((:anyBranch = true AND a.division = :division)"
                                + " OR (:anyBranch = false AND a.branch = :branch))"
                                + " AND o.status=true"
                                + " AND o.documentType <> :ta"
                                + " AND ((:numberOfDays<>0 AND a.endDate <= :endDate) OR :numberOfDays=0)"
                )
                .parameter("ta", travelAuthorizationId)
                .parameter("anyBranch", branch == null)
                .parameter("branch", branch)
                .parameter("division", division)
                .parameter("numberOfDays", numberOfDays)
                .parameter("endDate", endDate)
                .properties("amount", "docid", "vendor", "actnum", "title", "city", "state", "enddate", "lineno")
                .list();

        obligations.sort(Comparator.comparing((KeyValueEntity o) -> (LocalDate) o.getValue("enddate"),
                        Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(o -> o.getValue("actnum"))
                .thenComparing(o -> o.getValue("docid"))
                .thenComparingInt(o -> o.getValue("lineno")));

        return obligations;
    }

    public OpenObligationsReportData generateReportData(Division division, Branch branch, int numberOfDays) {
        division = branch == null ? division : branch.getDivision();
        var appropriation = division.getAppropriation();

        var reportData = new OpenObligationsReportData(appropriation, division, branch);

        var obligations = getOpenObligations(division, branch, numberOfDays);
        var totalObligated = BigDecimal.ZERO;
        long vDays = 0;

        List<ObligationDto> obligationDtos = new ArrayList<>();
        ObligationDto dto;
        for (var kvEntity : obligations) {
            dto = dataManager.create(ObligationDto.class);
            dto.setAmount(kvEntity.getValue("amount"));
            dto.setDocumentNumber(kvEntity.getValue("docid"));
            dto.setVendor(kvEntity.getValue("vendor"));
            dto.setActivityNumber(kvEntity.getValue("actnum"));
            dto.setActivityTitle(kvEntity.getValue("title"));
            dto.setActivityEndDate(kvEntity.getValue("enddate"));
            String city = kvEntity.getValue("city");
            String state = kvEntity.getValue("state");
            if (city == null) {
                dto.setLocation(state);
            } else if (state == null) {
                dto.setLocation(city);
            } else {
                dto.setLocation(city.concat(", ").concat(state));
            }
            obligationDtos.add(dto);
            totalObligated = totalObligated.add(dto.getAmount());
//            vDays+= getNumberOfDaysFromToday(dto.getTravelEndDate());
        }

        reportData.setObligations(obligationDtos);
//        reportData.setTotalObligated(totalObligated);
        reportData.setNumberOfDays(numberOfDays);
//        var endDate = getCurrentDateMinusDays(numberOfDays);
//        reportData.setLatestTravelDate(endDate);
//        if(reportData.getNumberOfObligations() == 0) {
//            reportData.setAverageDays(0);
//        } else {
//            reportData.setAverageDays((float) vDays / reportData.getNumberOfObligations());
//        }

        return reportData;
    }
}