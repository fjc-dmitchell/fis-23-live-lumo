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
import io.jmix.core.FluentValuesLoader;
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

    private static final String TRAVEL_OBLIGATION_SELECT = """
        SELECT a.fund.fundCode, dv.appropriation.budgetFiscalYear, dv.divisionCode, dv.budgetOrg,
               a.activityNumber, a.title, e.documentNumber, e.documentDate, e.vendor, e.amount,
               e.lineNumber, COALESCE(a.endDate, e.travelEndDate)
        FROM fis_Obligation e
        INNER JOIN fis_Activity a ON a = e.activity
        """;

    private static final String[] TRAVEL_OBLIGATION_PROPERTIES = {
            "fundCode", "budgetFiscalYear", "divisionCode", "budgetOrg", "activityNumber", "title",
            "documentNumber", "documentDate", "vendor", "amount", "lineNumber", "travelEndDate"
    };

    private static final Comparator<KeyValueEntity> TRAVEL_OBLIGATION_ORDER =
            Comparator.comparing((KeyValueEntity o) -> (Date) o.getValue("travelEndDate"),
                            Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(o -> o.getValue("activityNumber"))
                    .thenComparing(o -> o.getValue("documentNumber"))
                    .thenComparingInt(o -> o.getValue("lineNumber"));

    public List<KeyValueEntity> getOpenTravelObligations(Appropriation appropriation, Set<Division> divisions,
                                                         LocalDate bDate, LocalDate eDate,
                                                         boolean obbba) {
        var priorAppropriation = appropriationService.getPreviousFiscalYear(appropriation);
        List<Appropriation> appropriations = List.of(appropriation, priorAppropriation);
        Set<String> divisionCodes = divisions.stream().map(Division::getDivisionCode).collect(Collectors.toSet());

        var obbbaDivision = divisionService.findObbbaDivision(appropriation);
        boolean includeObbba = obbba && !divisions.contains(obbbaDivision);

        var obligations = travelObligationLoader("a.division", "", appropriations, divisionCodes, bDate, eDate)
                .list();

        if (includeObbba) {
            List<Division> obbbaDivisions = Stream.of(obbbaDivision,
                            divisionService.findObbbaDivision(priorAppropriation))
                    .filter(Objects::nonNull)
                    .toList();

            obligations.addAll(travelObligationLoader("a.costOrg", " AND a.division IN :obbbaDivisions",
                    appropriations, divisionCodes, bDate, eDate)
                    .parameter("obbbaDivisions", obbbaDivisions)
                    .list());
        }

        obligations.sort(TRAVEL_OBLIGATION_ORDER);
        return obligations;
    }

    /**
     * Builds the shared open-travel-obligation query. {@code dvPath} is the activity property the
     * division alias {@code dv} joins through, and {@code extraCondition} (empty or starting with
     * " AND") narrows the result set further. The begin/end date filters are only added, and their
     * parameters only bound, when the corresponding date is set.
     */
    private FluentValuesLoader travelObligationLoader(String dvPath, String extraCondition,
                                                      List<Appropriation> appropriations,
                                                      Set<String> divisionCodes,
                                                      LocalDate bDate, LocalDate eDate) {
        var query = new StringBuilder(TRAVEL_OBLIGATION_SELECT)
                .append(" INNER JOIN fis_Division dv ON dv = ").append(dvPath)
                .append(" WHERE dv.appropriation IN :appropriations")
                .append(" AND dv.divisionCode IN :divisionCodes")
                .append(extraCondition)
                .append(" AND e.status = true")
                .append(" AND e.documentType = :ta");
        if (bDate != null) {
            query.append(" AND a.endDate >= :beginDate");
        }
        if (eDate != null) {
            query.append(" AND a.endDate <= :endDate");
        }

        var loader = dataManager.loadValues(query.toString())
                .parameter("ta", DocumentType.TRAVEL_AUTHORIZATION.getId())
                .parameter("appropriations", appropriations)
                .parameter("divisionCodes", divisionCodes)
                .properties(TRAVEL_OBLIGATION_PROPERTIES);
        if (bDate != null) {
            loader.parameter("beginDate", bDate);
        }
        if (eDate != null) {
            loader.parameter("endDate", eDate);
        }
        return loader;
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