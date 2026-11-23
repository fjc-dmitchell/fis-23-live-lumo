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
import io.jmix.core.DataManager;
import io.jmix.core.FluentValuesLoader;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gov.fjc.fis.app.BigDecimalUtils.sum;
import static gov.fjc.fis.app.FisUtilities.getCurrentDateMinusDays;

@Component("fis_OpenObligationsReportService")
public class OpenObligationsReportService {
    private final DataManager dataManager;
    private final AppropriationService appropriationService;
    private final DivisionService divisionService;
    private final ObligationService obligationService;

    public OpenObligationsReportService(DataManager dataManager, AppropriationService appropriationService,
                                        DivisionService divisionService, ObligationService obligationService) {
        this.dataManager = dataManager;
        this.appropriationService = appropriationService;
        this.divisionService = divisionService;
        this.obligationService = obligationService;
    }

    private static final String OPEN_OBLIGATION_SELECT = """
        SELECT e.amount, e.documentNumber, e.vendor, a.activityNumber, a.title, a.city,
               a.state, a.endDate, e.lineNumber, a.fund.fundCode, a.division.budgetOrg,
               dv.appropriation.budgetFiscalYear
        FROM fis_Obligation e
        INNER JOIN fis_Activity a ON a = e.activity
        """;

    private static final String[] OPEN_OBLIGATION_PROPERTIES = {
            "amount", "documentNumber", "vendor", "activityNumber", "title", "city", "state",
            "endDate", "lineNumber", "fundCode", "budgetOrg", "budgetFiscalYear"
    };

    private static final Comparator<KeyValueEntity> OPEN_OBLIGATION_ORDER =
            Comparator.comparing((KeyValueEntity o) -> (LocalDate) o.getValue("endDate"),
                            Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(o -> o.getValue("activityNumber"))
                    .thenComparing(o -> o.getValue("documentNumber"))
                    .thenComparingInt(o -> o.getValue("lineNumber"));

    public List<KeyValueEntity> getOpenObligations(Appropriation appropriation, Set<Division> divisions,
                                                   Branch branch, int numberOfDays, boolean obbba) {
        var priorAppropriation = appropriationService.getPreviousFiscalYear(appropriation);
        List<Appropriation> appropriations = List.of(appropriation, priorAppropriation);
        LocalDate endDate = numberOfDays != 0 ? getCurrentDateMinusDays(numberOfDays) : null;

        var obbbaDivision = divisionService.findObbbaDivision(appropriation);
        boolean includeObbba = obbba && !divisions.contains(obbbaDivision);

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

    private List<KeyValueEntity> loadObbbaObligations(List<Appropriation> appropriations,
                                                      List<Division> obbbaDivisions,
                                                      LocalDate endDate) {
        return openObligationLoader("a.costOrg", " AND a.division IN :obbbaDivisions", appropriations, endDate)
                .parameter("obbbaDivisions", obbbaDivisions)
                .list();
    }

    /**
     * Builds the shared open-obligation query. {@code dvPath} is the activity property the
     * division alias {@code dv} joins through; {@code scopeCondition} narrows the result set.
     * The end-date filter is only added (and its parameter only bound) when {@code endDate} is set.
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

    public OpenObligationsReportData generateReportData(Appropriation appropriation,
                                                        Set<Division> divisions,
                                                        Branch branch,
                                                        int numberOfDays,
                                                        boolean obbba) {
        var reportData = new OpenObligationsReportData(appropriation, divisions, branch);

        var obligations = getOpenObligations(appropriation, divisions, branch, numberOfDays, obbba);

        List<ObligationDto> obligationDtos = obligationService.convertToDto(obligations);

        var totalObligated = sum(obligationDtos, ObligationDto::getAmount);

        reportData.setObligations(obligationDtos);
        reportData.setTotalObligated(totalObligated);
        reportData.setNumberOfDays(numberOfDays);

        return reportData;
    }
}