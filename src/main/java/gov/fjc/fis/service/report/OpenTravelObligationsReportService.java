package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.DocumentType;
import gov.fjc.fis.entity.dto.ObligationDto;
import gov.fjc.fis.reportdata.OpenTravelObligationsReportData;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.DivisionService;
import gov.fjc.fis.service.ObligationService;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gov.fjc.fis.app.BigDecimalUtils.sum;

@Component("fis_OpenTravelObligationsReportService")
public class OpenTravelObligationsReportService {

    private final DataManager dataManager;
    private final AppropriationService appropriationService;
    private final DivisionService divisionService;
    private final ObligationService obligationService;

    public OpenTravelObligationsReportService(DataManager dataManager, AppropriationService appropriationService,
                                              DivisionService divisionService, ObligationService obligationService) {
        this.dataManager = dataManager;
        this.appropriationService = appropriationService;
        this.divisionService = divisionService;
        this.obligationService = obligationService;
    }

    public List<KeyValueEntity> getOpenTravelObligations(Appropriation appropriation, Set<Division> divisions,
                                                         LocalDate bDate, LocalDate eDate,
                                                         boolean obbba) {
        var priorAppropriation = appropriationService.getPreviousFiscalYear(appropriation);
        List<Appropriation> appropriations = List.of(appropriation, priorAppropriation);

        Set<String> divisionCodes = divisions.stream().map(Division::getDivisionCode).collect(Collectors.toSet());

        var obbbaDivision = divisionService.fetchMandatoryDivision(appropriation);
        if (divisions.contains(obbbaDivision)) {
            obbba = false;
        }

        var travelAuthorizationId = DocumentType.TRAVEL_AUTHORIZATION.getId();
        var obligations = dataManager.loadValues(
                        "SELECT f.fundCode, app.budgetFiscalYear, dv.divisionCode, dv.budgetOrg,"
                                + " a.activityNumber, a.title, e.documentNumber, e.documentDate, e.vendor, e.amount,"
                                + " e.lineNumber, COALESCE(a.endDate, e.travelEndDate)"
                                + " FROM fis_Obligation e"
                                + " INNER JOIN fis_Activity a ON a=e.activity"
                                + " INNER JOIN fis_Division dv ON dv=a.division"
                                + " INNER JOIN fis_Fund f ON f=a.fund"
                                + " INNER JOIN fis_Appropriation app ON app=dv.appropriation"
                                + " WHERE app IN :appropriations"
                                + " AND dv.divisionCode IN :divisionCodes"
                                + " AND e.status=true"
                                + " AND e.documentType = :ta"
                                + " AND (:anyBeginDate=true OR a.endDate >=:beginDate)"
                                + " AND (:anyEndDate=true OR a.endDate <=:endDate)")
                .parameter("ta", travelAuthorizationId)
                .parameter("appropriations", appropriations)
                .parameter("divisionCodes", divisionCodes)
                .parameter("anyBeginDate", bDate == null)
                .parameter("anyEndDate", eDate == null)
                .parameter("beginDate", bDate)
                .parameter("endDate", eDate)
                .properties("fundCode", "budgetFiscalYear", "divisionCode", "budgetOrg", "activityNumber", "title",
                        "documentNumber", "documentDate", "vendor", "amount", "lineNumber", "travelEndDate")
                .list();

        if (obbba) {
            List<Division> obbbaDivisions = Stream.of(divisionService.fetchMandatoryDivision(appropriation),
                    divisionService.fetchMandatoryDivision(priorAppropriation)).filter(Objects::nonNull).toList();

            var obbbaObligations = dataManager.loadValues(
                            "SELECT f.fundCode, app.budgetFiscalYear, dv.divisionCode, dv.budgetOrg,"
                                    + " a.activityNumber, a.title, e.documentNumber, e.documentDate, e.vendor, e.amount,"
                                    + " e.lineNumber, COALESCE(a.endDate, e.travelEndDate)"
                                    + " FROM fis_Obligation e"
                                    + " INNER JOIN fis_Activity a ON a=e.activity"
                                    + " INNER JOIN fis_Division dv ON dv=a.costOrg"
                                    + " INNER JOIN fis_Fund f ON f=a.fund"
                                    + " INNER JOIN fis_Appropriation app ON app=dv.appropriation"
                                    + " WHERE app IN :appropriations"
                                    + " AND a.division IN :obbbaDivisions"
                                    + " AND dv.divisionCode IN :divisionCodes"
                                    + " AND e.status=true"
                                    + " AND e.documentType = :ta"
                                    + " AND (:anyBeginDate=true OR a.endDate >=:beginDate)"
                                    + " AND (:anyEndDate=true OR a.endDate <=:endDate)")
                    .parameter("ta", travelAuthorizationId)
                    .parameter("appropriations", appropriations)
                    .parameter("obbbaDivisions", obbbaDivisions)
                    .parameter("divisionCodes", divisionCodes)
                    .parameter("anyBeginDate", bDate == null)
                    .parameter("anyEndDate", eDate == null)
                    .parameter("beginDate", bDate)
                    .parameter("endDate", eDate)
                    .properties("fundCode", "budgetFiscalYear", "divisionCode", "budgetOrg", "activityNumber", "title",
                            "documentNumber", "documentDate", "vendor", "amount", "lineNumber", "travelEndDate")

                    .list();

            obligations.addAll(obbbaObligations);
        }

        obligations.sort(Comparator.comparing((KeyValueEntity o) -> (Date) o.getValue("travelEndDate"),
                        Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(o -> o.getValue("activityNumber"))
                .thenComparing(o -> o.getValue("documentNumber"))
                .thenComparingInt(o -> o.getValue("lineNumber")));

        return obligations;
    }

    public OpenTravelObligationsReportData generateReportData(Appropriation appropriation,
                                                              Set<Division> divisions,
                                                              LocalDate fromDate,
                                                              LocalDate toDate,
                                                              boolean obbba) {

        var reportData = new OpenTravelObligationsReportData(appropriation, divisions, fromDate, toDate);

        var obligations = getOpenTravelObligations(appropriation, divisions, fromDate, toDate, obbba);

        List<ObligationDto> obligationDtos = obligationService.convertToDto(obligations);

        var totalObligated = sum(obligationDtos, ObligationDto::getAmount);

        reportData.setObligations(obligationDtos);
        reportData.setTotalObligated(totalObligated);

        return reportData;
    }
}