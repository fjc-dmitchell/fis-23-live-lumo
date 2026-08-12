package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.DocumentType;
import gov.fjc.fis.entity.dto.ObligationDto;
import gov.fjc.fis.reportdata.OpenTravelObligationsReportData;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.DivisionService;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import jakarta.persistence.TemporalType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("fis_OpenTravelObligationsReportService")
public class OpenTravelObligationsReportService {
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private DataManager dataManager;

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
                        "SELECT f.fundCode, app.budgetFiscalYear, dv.divisionCode, a.activityNumber,"
                                + " a.title,o.documentNumber, o.documentDate, o.vendor, o.amount,"
                                + " o.lineNumber, COALESCE(a.endDate, o.travelEndDate)"
                                + " FROM fis_Obligation o"
                                + " INNER JOIN fis_Activity a ON a=o.activity"
                                + " INNER JOIN fis_Division dv ON dv=a.division"
                                + " INNER JOIN fis_Fund f ON f=a.fund"
                                + " INNER JOIN fis_Appropriation app ON app=dv.appropriation"
                                + " WHERE app IN :appropriations"
                                + " AND dv.divisionCode IN :divisionCodes"
                                + " AND o.status=true"
                                + " AND o.documentType = :ta"
                                + " AND (:anyBeginDate=true OR a.endDate >=:beginDate)"
                                + " AND (:anyEndDate=true OR a.endDate <=:endDate)")
                .parameter("ta", travelAuthorizationId)
                .parameter("appropriations", appropriations)
                .parameter("divisionCodes", divisionCodes)
                .parameter("anyBeginDate", bDate == null)
                .parameter("anyEndDate", eDate == null)
                .parameter("beginDate", bDate)
                .parameter("endDate", eDate)
                .properties("fundCode", "budgetFiscalYear", "divisionCode", "activityNumber", "title",
                        "documentNumber", "documentDate", "vendor", "amount", "lineNumber", "endDate")
                .list();

        if (obbba) {
            List<Division> obbaDivisions = Stream.of(divisionService.fetchMandatoryDivision(appropriation),
                    divisionService.fetchMandatoryDivision(priorAppropriation)).filter(Objects::nonNull).toList();

            var obbbaObligations = dataManager.loadValues(
                            "SELECT f.fundCode, app.budgetFiscalYear, dv.divisionCode, a.activityNumber,"
                                    + " a.title,o.documentNumber, o.documentDate, o.vendor, o.amount,"
                                    + " o.lineNumber, COALESCE(a.endDate, o.travelEndDate)"
                                    + " FROM fis_Obligation o"
                                    + " INNER JOIN fis_Activity a ON a=o.activity"
                                    + " INNER JOIN fis_Division dv ON dv=a.costOrg"
                                    + " INNER JOIN fis_Fund f ON f=a.fund"
                                    + " INNER JOIN fis_Appropriation app ON app=dv.appropriation"
                                    + " WHERE app IN :appropriations"
                                    + " AND a.division IN :obbaDivisions"
                                    + " AND dv.divisionCode IN :divisionCodes"
                                    + " AND o.status=true"
                                    + " AND o.documentType = :ta"
                                    + " AND (:anyBeginDate=true OR a.endDate >=:beginDate)"
                                    + " AND (:anyEndDate=true OR a.endDate <=:endDate)")
                    .parameter("ta", travelAuthorizationId)
                    .parameter("appropriations", appropriations)
                    .parameter("obbaDivisions", obbaDivisions)
                    .parameter("divisionCodes", divisionCodes)
                    .parameter("anyBeginDate", bDate == null)
                    .parameter("anyEndDate", eDate == null)
                    .parameter("beginDate", bDate)
                    .parameter("endDate", eDate)
                    .properties("fundCode", "budgetFiscalYear", "divisionCode", "activityNumber", "title",
                            "documentNumber", "documentDate", "vendor", "amount", "lineNumber", "endDate")
                    .list();

            obligations.addAll(obbbaObligations);
        }

        obligations.sort(Comparator.comparing((KeyValueEntity o) -> (Date) o.getValue("endDate"),
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
        var totalObligated = BigDecimal.ZERO;

        List<ObligationDto> obligationDtos = new ArrayList<>();
        ObligationDto dto;
        for (var kvEntity : obligations) {
            dto = dataManager.create(ObligationDto.class);
            dto.setFundCode(kvEntity.getValue("fundCode"));
            dto.setBudgetFiscalYear(kvEntity.getValue("budgetFiscalYear"));
            dto.setDivisionCode(kvEntity.getValue("divisionCode"));
            dto.setActivityNumber(kvEntity.getValue("activityNumber"));
            dto.setActivityTitle(kvEntity.getValue("title"));
            dto.setDocumentNumber(kvEntity.getValue("documentNumber"));
            dto.setDocumentDate(kvEntity.getValue("documentDate"));
            dto.setVendor(kvEntity.getValue("vendor"));
            dto.setAmount(kvEntity.getValue("amount"));
            dto.setTravelEndDate(((java.sql.Date) kvEntity.getValue("endDate")).toLocalDate());
            obligationDtos.add(dto);
            totalObligated = totalObligated.add(dto.getAmount());
        }

        reportData.setObligations(obligationDtos);
        reportData.setTotalObligated(totalObligated);

        return reportData;
    }
}