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

    /**
     * find obligations for division or branch that have been open for specified number
     * of days based on obligation travel end date or activity end date
     *
     * @param appropriation
     * @param divisions
     * @param branch
     * @param numberOfDays
     * @param obbba
     * @return list of key value entities containing obligation information
     */
    public List<KeyValueEntity> getOpenObligations(Appropriation appropriation, Set<Division> divisions,
                                                   Branch branch, int numberOfDays, boolean obbba) {
        var priorAppropriation = appropriationService.getPreviousFiscalYear(appropriation);
        List<Appropriation> appropriations = List.of(appropriation, priorAppropriation);

        Set<String> divisionCodes = divisions.stream().map(Division::getDivisionCode).collect(Collectors.toSet());
        var miscObligationId = DocumentType.MISCELLANEOUS_OBLIGATION.getId();
        var branchCode = branch != null ? branch.getBranchCode() : null;

        var endDate = getCurrentDateMinusDays(numberOfDays);


        var obbbaDivision = divisionService.fetchMandatoryDivision(appropriation);
        if (divisions.contains(obbbaDivision)) {
            obbba = false;
        }

        var obligations = dataManager.loadValues(
                        "SELECT e.amount, e.documentNumber, e.vendor, a.activityNumber, a.title, a.city,"
                                + " a.state, a.endDate, e.lineNumber, a.fund.fundCode, a.division.budgetOrg,"
                                + " dv.appropriation.budgetFiscalYear"
                                + " FROM fis_Obligation e"
                                + " INNER JOIN fis_Activity a ON a=e.activity"
                                + " INNER JOIN fis_Division dv ON dv=a.division"
                                + " WHERE dv.appropriation IN :appropriations"
                                + " AND ((:anyBranch = true AND dv.divisionCode IN :divisionCodes)"
                                + " OR (:anyBranch = false AND a.branch.branchCode = :branchCode))"
                                + " AND e.status=true"
                                + " AND e.documentType = :mo"
                                + " AND ((:numberOfDays<>0 AND a.endDate <= :endDate) OR :numberOfDays=0)"
                )
                .parameter("appropriations", appropriations)
                .parameter("mo", miscObligationId)
                .parameter("anyBranch", branch == null)
                .parameter("branchCode", branchCode)
                .parameter("divisionCodes", divisionCodes)
                .parameter("numberOfDays", numberOfDays)
                .parameter("endDate", endDate)
                .properties("amount", "documentNumber", "vendor", "activityNumber", "title", "city", "state",
                        "endDate", "lineNumber", "fundCode", "budgetOrg", "budgetFiscalYear")
                .list();

        if (obbba) {
            List<Division> obbbaDivisions = Stream.of(divisionService.fetchMandatoryDivision(appropriation),
                    divisionService.fetchMandatoryDivision(priorAppropriation)).filter(Objects::nonNull).toList();

            var obbbaObligations = dataManager.loadValues(
                            "SELECT e.amount, e.documentNumber, e.vendor, a.activityNumber, a.title, a.city,"
                                    + " a.state, a.endDate, e.lineNumber, a.fund.fundCode, a.division.budgetOrg,"
                                    + " dv.appropriation.budgetFiscalYear"
                                    + " FROM fis_Obligation e"
                                    + " INNER JOIN fis_Activity a ON a=e.activity"
                                    + " INNER JOIN fis_Division dv ON dv=a.costOrg"
                                    + " WHERE dv.appropriation IN :appropriations"
                                    + " AND a.division IN :obbbaDivisions"
                                    + " AND e.status=true"
                                    + " AND e.documentType = :mo"
                                    + " AND ((:numberOfDays<>0 AND a.endDate <= :endDate) OR :numberOfDays=0)"
                    )
                    .parameter("appropriations", appropriations)
                    .parameter("mo", miscObligationId)
                    .parameter("obbbaDivisions", obbbaDivisions)
                    .parameter("numberOfDays", numberOfDays)
                    .parameter("endDate", endDate)
                    .properties("amount", "documentNumber", "vendor", "activityNumber", "title", "city", "state",
                            "endDate", "lineNumber", "fundCode", "budgetOrg", "budgetFiscalYear")
                    .list();

            obligations.addAll(obbbaObligations);
        }

        obligations.sort(Comparator.comparing((KeyValueEntity o) -> (LocalDate) o.getValue("endDate"),
                        Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(o -> o.getValue("activityNumber"))
                .thenComparing(o -> o.getValue("documentNumber"))
                .thenComparingInt(o -> o.getValue("lineNumber")));

        return obligations;
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