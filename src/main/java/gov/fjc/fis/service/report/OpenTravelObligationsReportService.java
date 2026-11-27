package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.DocumentType;
import gov.fjc.fis.entity.dto.ObligationDto;
import gov.fjc.fis.reportdata.OpenTravelObligationsReportData;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.DivisionService;
import gov.fjc.fis.service.ObligationService;
import gov.fjc.fis.view.report.opentravelobligationsreport.OpenTravelObligationsReportView;
import io.jmix.core.DataManager;
import io.jmix.core.FluentValuesLoader;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gov.fjc.fis.app.BigDecimalUtils.sum;

/**
 * Generates report data for open travel obligations associated with selected divisions.
 *
 * <p>The service retrieves open travel authorizations for the selected appropriation and its
 * preceding fiscal-year appropriation, subject to the supplied travel-end-date range. When
 * requested, it also includes qualifying obligations associated with OBBBA cost organizations.
 * The resulting obligations are converted to report DTOs, sorted consistently, and totaled for
 * use by the open travel obligations report.</p>
 *
 * @see OpenTravelObligationsReportData
 * @see OpenTravelObligationsReportView
 * @author Doug Mitchell
 */
@Component("fis_OpenTravelObligationsReportService")
public class OpenTravelObligationsReportService {

    /**
     * JPQL expression used to determine the travel end date for selection, filtering, and sorting.
     */
    private static final String TRAVEL_END_DATE = "COALESCE(a.endDate, e.travelEndDate)";

    /**
     * Associates a JPQL select expression with the corresponding key-value entity property.
     *
     * @param expression the JPQL expression included in the query projection
     * @param property   the property name assigned to the projected value
     */
    private record Column(String expression, String property) {
    }

    /**
     * Columns selected by the open travel obligations query and their result property names.
     */
    private static final List<Column> TRAVEL_OBLIGATION_COLUMNS = List.of(
            new Column("a.fund.fundCode", "fundCode"),
            new Column("dv.appropriation.budgetFiscalYear", "budgetFiscalYear"),
            new Column("dv.divisionCode", "divisionCode"),
            new Column("dv.budgetOrg", "budgetOrg"),
            new Column("a.activityNumber", "activityNumber"),
            new Column("a.title", "title"),
            new Column("e.documentNumber", "documentNumber"),
            new Column("e.documentDate", "documentDate"),
            new Column("e.vendor", "vendor"),
            new Column("e.amount", "amount"),
            new Column("e.lineNumber", "lineNumber"),
            new Column(TRAVEL_END_DATE, "travelEndDate"));

    /**
     * Shared JPQL select and source clauses for open travel obligation queries.
     */
    private static final String TRAVEL_OBLIGATION_SELECT = "SELECT "
            + TRAVEL_OBLIGATION_COLUMNS.stream().map(Column::expression).collect(Collectors.joining(", "))
            + " FROM fis_Obligation e JOIN e.activity a";

    /**
     * Property names assigned to values returned by the open travel obligations query.
     */
    private static final String[] TRAVEL_OBLIGATION_PROPERTIES = TRAVEL_OBLIGATION_COLUMNS.stream()
            .map(Column::property)
            .toArray(String[]::new);

    /**
     * Sort order applied to open travel obligations returned for the report.
     */
    private static final Comparator<KeyValueEntity> TRAVEL_OBLIGATION_ORDER =
            byValue("travelEndDate", LocalDate.class)
                    .thenComparing(byValue("activityNumber", String.class))
                    .thenComparing(byValue("documentNumber", String.class))
                    .thenComparing(byValue("lineNumber", Integer.class));

    private final DataManager dataManager;
    private final AppropriationService appropriationService;
    private final DivisionService divisionService;
    private final ObligationService obligationService;

    /**
     * Creates an open travel obligations report service.
     *
     * @param dataManager          the Jmix data manager used to execute key-value queries
     * @param appropriationService the service used to locate the preceding fiscal-year appropriation
     * @param divisionService      the service used to locate OBBBA divisions
     * @param obligationService    the service used to convert obligation query results to DTOs
     */
    public OpenTravelObligationsReportService(DataManager dataManager, AppropriationService appropriationService,
                                              DivisionService divisionService, ObligationService obligationService) {
        this.dataManager = dataManager;
        this.appropriationService = appropriationService;
        this.divisionService = divisionService;
        this.obligationService = obligationService;
    }

    /**
     * Generates the data required by the open travel obligations report.
     *
     * <p>The report includes qualifying obligations for the selected appropriation and, when
     * available, its preceding fiscal-year appropriation. The obligations are converted to report
     * DTOs, assigned to the report data object, and totaled.</p>
     *
     * @param appropriation the appropriation for which the report is generated
     * @param divisions     the divisions included in the report
     * @param fromDate      the earliest travel end date to include, or {@code null} for no lower bound
     * @param toDate        the latest travel end date to include, or {@code null} for no upper bound
     * @param obbba         whether qualifying OBBBA cost-organization obligations should be included
     * @return the populated report data
     */
    public OpenTravelObligationsReportData generateReportData(Appropriation appropriation,
                                                              Set<Division> divisions,
                                                              LocalDate fromDate,
                                                              LocalDate toDate,
                                                              boolean obbba) {
        var reportData = new OpenTravelObligationsReportData(appropriation, divisions, fromDate, toDate);

        var obligations = getOpenTravelObligations(appropriation, divisions, fromDate, toDate, obbba);
        List<ObligationDto> obligationDtos = obligationService.convertToDto(obligations);

        reportData.setObligations(obligationDtos);
        reportData.setTotalObligated(sum(obligationDtos, ObligationDto::getAmount));

        return reportData;
    }

    /**
     * Retrieves the open travel obligations matching the report criteria.
     *
     * <p>The search covers the selected appropriation and its preceding fiscal-year appropriation.
     * When OBBBA inclusion is enabled, qualifying obligations whose cost organizations belong to
     * the corresponding OBBBA divisions are added unless the current OBBBA division is already
     * included in the selected divisions.</p>
     *
     * @param appropriation the appropriation for which obligations are retrieved
     * @param divisions     the divisions included in the search
     * @param fromDate      the earliest travel end date to include, or {@code null} for no lower bound
     * @param toDate        the latest travel end date to include, or {@code null} for no upper bound
     * @param obbba         whether qualifying OBBBA cost-organization obligations should be included
     * @return the matching obligations in report sort order, or an empty list when no divisions
     * are selected
     */
    public List<KeyValueEntity> getOpenTravelObligations(Appropriation appropriation, Set<Division> divisions,
                                                         LocalDate fromDate, LocalDate toDate,
                                                         boolean obbba) {
        if (divisions.isEmpty()) {
            return new ArrayList<>();
        }

        var priorAppropriation = appropriationService.getPreviousFiscalYear(appropriation);
        List<Appropriation> appropriations = Stream.of(appropriation, priorAppropriation)
                .filter(Objects::nonNull)
                .toList();
        Set<String> divisionCodes = divisions.stream()
                .map(Division::getDivisionCode)
                .collect(Collectors.toSet());

        var obligations = new ArrayList<>(
                travelObligationLoader("a.division", "", appropriations, divisionCodes, fromDate, toDate)
                        .list());

        if (obbba) {
            obligations.addAll(loadObbbaObligations(appropriation, priorAppropriation, divisions,
                    appropriations, divisionCodes, fromDate, toDate));
        }

        obligations.sort(TRAVEL_OBLIGATION_ORDER);
        return obligations;
    }

    /**
     * Loads obligations whose cost organizations belong to OBBBA divisions associated with the
     * current or preceding fiscal-year appropriation.
     *
     * <p>No additional obligations are loaded when the current OBBBA division is already selected,
     * because the primary query covers it, or when neither appropriation has an OBBBA division.</p>
     *
     * @param appropriation      the current appropriation
     * @param priorAppropriation the preceding fiscal-year appropriation, or {@code null} when none exists
     * @param divisions          the divisions selected for the report
     * @param appropriations     the current and available preceding fiscal-year appropriations
     * @param divisionCodes      the selected division codes
     * @param fromDate           the earliest travel end date to include, or {@code null} for no lower bound
     * @param toDate             the latest travel end date to include, or {@code null} for no upper bound
     * @return qualifying OBBBA obligations, or an empty list when no additional query is required
     */
    private List<KeyValueEntity> loadObbbaObligations(Appropriation appropriation,
                                                      Appropriation priorAppropriation,
                                                      Set<Division> divisions,
                                                      List<Appropriation> appropriations,
                                                      Set<String> divisionCodes,
                                                      LocalDate fromDate, LocalDate toDate) {
        var obbbaDivision = divisionService.findObbbaDivision(appropriation);
        if (obbbaDivision != null && divisions.contains(obbbaDivision)) {
            return List.of();
        }

        var priorObbbaDivision = priorAppropriation == null
                ? null
                : divisionService.findObbbaDivision(priorAppropriation);

        List<Division> obbbaDivisions = Stream.of(obbbaDivision, priorObbbaDivision)
                .filter(Objects::nonNull)
                .toList();
        if (obbbaDivisions.isEmpty()) {
            return List.of();
        }

        return travelObligationLoader("a.costOrg", " AND a.division IN :obbbaDivisions",
                appropriations, divisionCodes, fromDate, toDate)
                .parameter("obbbaDivisions", obbbaDivisions)
                .list();
    }

    /**
     * Builds a values loader for the shared open travel obligations query.
     *
     * <p>The division alias is joined through the supplied activity path. The optional additional
     * condition further restricts the query. Date conditions and their parameters are included only
     * when the corresponding bounds are supplied.</p>
     *
     * @param dvPath         the activity property through which the division alias is joined
     * @param extraCondition an additional JPQL condition beginning with {@code AND}, or an empty string
     * @param appropriations the appropriations included in the query
     * @param divisionCodes  the division codes included in the query
     * @param fromDate       the earliest travel end date to include, or {@code null} for no lower bound
     * @param toDate         the latest travel end date to include, or {@code null} for no upper bound
     * @return a configured values loader ready for additional parameters or execution
     */
    private FluentValuesLoader travelObligationLoader(String dvPath, String extraCondition,
                                                      List<Appropriation> appropriations,
                                                      Set<String> divisionCodes,
                                                      LocalDate fromDate, LocalDate toDate) {
        var query = new StringBuilder(TRAVEL_OBLIGATION_SELECT)
                .append(" INNER JOIN fis_Division dv ON dv = ").append(dvPath)
                .append(" WHERE dv.appropriation IN :appropriations")
                .append(" AND dv.divisionCode IN :divisionCodes")
                .append(extraCondition)
                .append(" AND e.status = true")
                .append(" AND e.documentType = :ta");
        if (fromDate != null) {
            query.append(" AND ").append(TRAVEL_END_DATE).append(" >= :beginDate");
        }
        if (toDate != null) {
            query.append(" AND ").append(TRAVEL_END_DATE).append(" <= :endDate");
        }

        var loader = dataManager.loadValues(query.toString())
                .parameter("ta", DocumentType.TRAVEL_AUTHORIZATION.getId())
                .parameter("appropriations", appropriations)
                .parameter("divisionCodes", divisionCodes)
                .properties(TRAVEL_OBLIGATION_PROPERTIES);
        if (fromDate != null) {
            loader.parameter("beginDate", fromDate);
        }
        if (toDate != null) {
            loader.parameter("endDate", toDate);
        }
        return loader;
    }

    /**
     * Creates a null-safe comparator for a comparable key-value entity property.
     *
     * <p>Entities with a {@code null} property value are ordered before entities with a non-null
     * value.</p>
     *
     * @param property the property to compare
     * @param type     the property's value type, used to support generic type inference
     * @param <T>      the comparable property type
     * @return a nulls-first comparator for the specified property
     */
    private static <T extends Comparable<? super T>> Comparator<KeyValueEntity> byValue(String property,
                                                                                        Class<T> type) {
        return Comparator.comparing(o -> o.<T>getValue(property), Comparator.nullsFirst(Comparator.naturalOrder()));
    }
}