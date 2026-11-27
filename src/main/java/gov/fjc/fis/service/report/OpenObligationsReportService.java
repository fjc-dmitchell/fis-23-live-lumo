package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.DocumentType;
import gov.fjc.fis.entity.dto.ObligationDto;
import gov.fjc.fis.reportdata.OpenObligationsReportData;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.DivisionService;
import gov.fjc.fis.service.ObligationService;
import gov.fjc.fis.view.report.openobligationsreport.OpenObligationsReportView;
import io.jmix.core.DataManager;
import io.jmix.core.FluentValuesLoader;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gov.fjc.fis.app.FisUtilities.getCurrentDateMinusDays;

/**
 * Generates data for the Open Obligations Report.
 * <p>
 * The service identifies open miscellaneous obligations across selected
 * divisions or branches and assembles the data required by the report. Open
 * obligations may be filtered using an aging threshold and may optionally
 * include obligations associated with One Big Beautiful Bill Act (OBBBA)
 * funding.
 * </p>
 *
 * <p>
 * Report results include obligations from the selected appropriation and, when
 * available, the preceding fiscal year's appropriation to provide a complete
 * view of outstanding obligations.
 * </p>
 *
 * @see OpenObligationsReportView
 * @see OpenObligationsReportData
 * @author Doug Mitchell
 */
@Component("fis_OpenObligationsReportService")
public class OpenObligationsReportService {

    /**
     * Base select clause used to retrieve open-obligation report data.
     */
    private static final String OPEN_OBLIGATION_SELECT = """
        SELECT e.amount, e.documentNumber, e.vendor, a.activityNumber, a.title, a.city,
               a.state, a.endDate, e.lineNumber, a.fund.fundCode, a.division.budgetOrg,
               dv.appropriation.budgetFiscalYear
        FROM fis_Obligation e
        INNER JOIN fis_Activity a ON a = e.activity
        """;

    /**
     * Properties populated on query results returned by the open-obligation query.
     */
    private static final String[] OPEN_OBLIGATION_PROPERTIES = {
            "amount", "documentNumber", "vendor", "activityNumber", "title", "city", "state",
            "endDate", "lineNumber", "fundCode", "budgetOrg", "budgetFiscalYear"
    };

    /**
     * Default ordering applied to open-obligation report results.
     * <p>
     * Results are ordered by activity end date, activity number, document number,
     * and line number.
     * </p>
     */
    private static final Comparator<KeyValueEntity> OPEN_OBLIGATION_ORDER =
            Comparator.comparing((KeyValueEntity o) -> (LocalDate) o.getValue("endDate"),
                            Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(o -> o.getValue("activityNumber"))
                    .thenComparing(o -> o.getValue("documentNumber"))
                    .thenComparingInt(o -> o.getValue("lineNumber"));

    private final DataManager dataManager;
    private final AppropriationService appropriationService;
    private final DivisionService divisionService;
    private final ObligationService obligationService;

    /**
     * Creates an Open Obligations Report service.
     *
     * @param dataManager data access service used to execute report queries
     * @param appropriationService service used to access appropriation data
     * @param divisionService service used to access division data
     * @param obligationService service used to convert obligations to report DTOs
     */
    public OpenObligationsReportService(DataManager dataManager, AppropriationService appropriationService,
                                        DivisionService divisionService, ObligationService obligationService) {
        this.dataManager = dataManager;
        this.appropriationService = appropriationService;
        this.divisionService = divisionService;
        this.obligationService = obligationService;
    }

    /**
     * Generates report data for the Open Obligations Report.
     *
     * @param appropriation appropriation included in the report
     * @param divisions divisions included in the report scope
     * @param branch branch included in the report scope, or {@code null} when the
     *               report spans multiple divisions
     * @param numberOfDays minimum age, in days, for obligations included in the
     *                     report
     * @param obbba whether OBBBA obligations should be included
     * @return populated report data ready for report generation
     */
    public OpenObligationsReportData generateReportData(Appropriation appropriation,
                                                        Set<Division> divisions,
                                                        Branch branch,
                                                        int numberOfDays,
                                                        boolean obbba) {
        var obligations = getOpenObligations(appropriation, divisions, branch, numberOfDays, obbba);
        List<ObligationDto> obligationDtos = obligationService.convertToDto(obligations);

        return new OpenObligationsReportData(appropriation, divisions, branch, obligationDtos, numberOfDays);
    }

    /**
     * Retrieves open obligations matching the specified reporting criteria.
     * <p>
     * Obligations are retrieved from the selected appropriation and, when
     * available, the preceding fiscal year's appropriation. Results may be
     * filtered by branch, division, aging threshold, and whether OBBBA
     * obligations are included.
     * </p>
     *
     * @param appropriation appropriation included in the report
     * @param divisions divisions included in the report scope
     * @param branch branch included in the report scope, or {@code null}
     * @param numberOfDays minimum age, in days, for obligations included in the
     *                     report
     * @param obbba whether OBBBA obligations should be included
     * @return ordered list of open obligations
     */
    public List<KeyValueEntity> getOpenObligations(Appropriation appropriation, Set<Division> divisions,
                                                   Branch branch, int numberOfDays, boolean obbba) {
        var priorAppropriation = appropriationService.getPreviousFiscalYear(appropriation);
        List<Appropriation> appropriations = Stream.of(appropriation, priorAppropriation)
                .filter(Objects::nonNull)
                .toList();
        LocalDate endDate = numberOfDays != 0 ? getCurrentDateMinusDays(numberOfDays) : null;

        var obbbaDivision = divisionService.findObbbaDivision(appropriation);
        boolean includeObbba = obbba && obbbaDivision != null && !divisions.contains(obbbaDivision);

        var obligations = loadRegularObligations(appropriations, divisions, branch, endDate);

        if (includeObbba) {
            List<Division> obbbaDivisions = Stream.of(obbbaDivision,
                            divisionService.findObbbaDivision(priorAppropriation))
                    .filter(Objects::nonNull)
                    .toList();
            obligations.addAll(loadObbbaObligations(appropriations, obbbaDivisions, endDate));
        }

        obligations.sort(OPEN_OBLIGATION_ORDER);
        return obligations;
    }

    /**
     * Loads open obligations associated with standard division and branch funding.
     *
     * @param appropriations appropriations included in the search
     * @param divisions divisions included in the report scope
     * @param branch branch included in the report scope, or {@code null}
     * @param endDate latest activity end date to include, or {@code null} if no
     *                aging filter should be applied
     * @return matching obligations
     */
    private List<KeyValueEntity> loadRegularObligations(List<Appropriation> appropriations,
                                                        Set<Division> divisions, Branch branch,
                                                        LocalDate endDate) {
        boolean byBranch = branch != null;
        String scope = byBranch
                ? " AND a.branch.branchCode = :branchCode"
                : " AND dv.divisionCode IN :divisionCodes";

        var loader = openObligationLoader("a.division", scope, appropriations, endDate);
        if (byBranch) {
            loader.parameter("branchCode", branch.getBranchCode());
        } else {
            loader.parameter("divisionCodes",
                    divisions.stream().map(Division::getDivisionCode).collect(Collectors.toSet()));
        }
        return loader.list();
    }

    /**
     * Loads open obligations associated with OBBBA funding divisions.
     *
     * @param appropriations appropriations included in the search
     * @param obbbaDivisions OBBBA divisions to include
     * @param endDate latest activity end date to include, or {@code null} if no
     *                aging filter should be applied
     * @return matching OBBBA obligations
     */
    private List<KeyValueEntity> loadObbbaObligations(List<Appropriation> appropriations,
                                                      List<Division> obbbaDivisions,
                                                      LocalDate endDate) {
        return openObligationLoader("a.costOrg", " AND a.division IN :obbbaDivisions", appropriations, endDate)
                .parameter("obbbaDivisions", obbbaDivisions)
                .list();
    }

    /**
     * Creates the query used to load open obligations.
     * <p>
     * The division alias {@code dv} is joined through the specified activity
     * property, and the supplied scope condition is appended to the query to
     * restrict the result set. An activity end-date filter is included only when
     * an aging threshold has been specified.
     * </p>
     *
     * @param dvPath activity property used to join the division alias
     * @param scopeCondition query condition used to limit the report scope
     * @param appropriations appropriations included in the search
     * @param endDate latest activity end date to include, or {@code null} if no
     *                aging filter should be applied
     * @return configured values loader for retrieving open obligations
     */
    private FluentValuesLoader openObligationLoader(String dvPath, String scopeCondition,
                                                    List<Appropriation> appropriations,
                                                    LocalDate endDate) {
        var query = new StringBuilder(OPEN_OBLIGATION_SELECT)
                .append(" INNER JOIN fis_Division dv ON dv = ").append(dvPath)
                .append(" WHERE dv.appropriation IN :appropriations")
                .append(scopeCondition)
                .append(" AND e.status = true")
                .append(" AND e.documentType = :mo");
        if (endDate != null) {
            query.append(" AND a.endDate <= :endDate");
        }

        var loader = dataManager.loadValues(query.toString())
                .parameter("appropriations", appropriations)
                .parameter("mo", DocumentType.MISCELLANEOUS_OBLIGATION.getId())
                .properties(OPEN_OBLIGATION_PROPERTIES);
        if (endDate != null) {
            loader.parameter("endDate", endDate);
        }
        return loader;
    }
}