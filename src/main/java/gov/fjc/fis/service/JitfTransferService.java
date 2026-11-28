package gov.fjc.fis.service;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Fund;
import gov.fjc.fis.entity.dto.JitfTransferDto;
import gov.fjc.fis.view.dashboard.jitfdashboard.JitfDashboardView;
import io.jmix.core.DataManager;
import io.jmix.core.Sort;
import io.jmix.core.querycondition.PropertyCondition;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides fiscal-year summary data for the JITF transfer dashboard.
 * <p>
 * The service summarizes JITF financial activity for each applicable fiscal
 * year, including the amount carried forward from the preceding fiscal year,
 * total deposits, total expenses, and the balance available to carry forward
 * to the next fiscal year.
 * </p>
 * <p>
 * Fiscal years are processed chronologically. The ending balance calculated
 * for one fiscal year becomes the carried-forward balance for the following
 * fiscal year.
 * </p>
 *
 * @see JitfTransferDto
 * @see JitfDashboardView
 * @author Doug Mitchell
 */
@Component("fis_JitfTransferService")
public class JitfTransferService {

    private final DataManager dataManager;
    private final FundService fundService;

    /**
     * Creates a new JITF transfer service.
     *
     * @param dataManager the data manager used to load financial data and
     *                    create report data transfer objects
     * @param fundService the service used to retrieve the JITF fund
     */
    public JitfTransferService(DataManager dataManager, FundService fundService) {
        this.dataManager = dataManager;
        this.fundService = fundService;
    }

    /**
     * Generates the fiscal-year summaries displayed on the JITF transfer
     * dashboard.
     * <p>
     * For each applicable appropriation, this method calculates:
     * </p>
     * <ul>
     *     <li>the balance carried forward from the preceding fiscal year,</li>
     *     <li>the total JITF deposits for the fiscal year,</li>
     *     <li>the total JITF expenses for the fiscal year, and</li>
     *     <li>the remaining balance to carry forward to the next fiscal year.</li>
     * </ul>
     * <p>
     * The carry-forward balance is calculated as:
     * </p>
     * <pre>
     * carried-forward balance + deposits - expenses
     * </pre>
     * <p>
     * The first fiscal year begins with a carried-forward balance of zero.
     * If no JITF transfer data exists, an empty list is returned.
     * </p>
     *
     * @return the JITF transfer summaries in ascending fiscal-year order;
     *         never {@code null}
     */
    public List<JitfTransferDto> generateReport() {
        var jitfFund = fundService.getJitfFund();
        var report = new ArrayList<JitfTransferDto>();
        var balance = BigDecimal.ZERO;

        for (Appropriation appropriation : fetchAppropriations()) {
            var deposits = sumDeposits(appropriation);
            var expenses = sumExpenses(appropriation, jitfFund);

            var dto = dataManager.create(JitfTransferDto.class);
            dto.setBudgetFiscalYear(appropriation.getBudgetFiscalYear());
            dto.setCarriedForward(balance);

            balance = balance.add(deposits).subtract(expenses);

            dto.setTotalDeposits(deposits);
            dto.setTotalExpenses(expenses);
            dto.setCarryForward(balance);
            report.add(dto);
        }
        return report;
    }

    /**
     * Retrieves the appropriations included in the JITF transfer dashboard.
     * <p>
     * The first applicable fiscal year is determined by finding the earliest
     * appropriation associated with a JITF transfer. All appropriations from
     * that fiscal year forward are then returned so that the running
     * carry-forward balance remains continuous, including fiscal years with
     * no deposits or expenses.
     * </p>
     *
     * @return the applicable appropriations, ordered by fiscal year and
     *         identifier; or an empty list if no JITF transfers exist
     */
    private List<Appropriation> fetchAppropriations() {
        var minYear = dataManager.loadValue("""
                        SELECT MIN(a.budgetFiscalYear)
                        FROM fis_JitfTransfer j
                        JOIN j.objectClass obj
                        JOIN obj.objectCategory cat
                        JOIN cat.appropriation a
                        """, String.class)
                .optional()
                .orElse(null);

        if (minYear == null) {
            return List.of();
        }

        return dataManager.load(Appropriation.class)
                .condition(PropertyCondition.greaterOrEqual("budgetFiscalYear", minYear))
                .sort(Sort.by("budgetFiscalYear", "id"))
                .list();
    }

    /**
     * Calculates the total JITF deposits associated with an appropriation.
     * <p>
     * Deposits are identified through the appropriation associated with each
     * transfer's object category. A zero value is returned when the
     * appropriation has no associated deposits.
     * </p>
     *
     * @param appropriation the appropriation for which deposits are totaled
     * @return the total amount deposited for the appropriation; never
     *         {@code null}
     */
    private BigDecimal sumDeposits(Appropriation appropriation) {
        return dataManager.loadValue("""
                        SELECT COALESCE(SUM(e.amount), 0)
                        FROM fis_JitfTransfer e
                        JOIN e.objectClass obj
                        JOIN obj.objectCategory cat
                        WHERE cat.appropriation = :appropriation
                        """, BigDecimal.class)
                .parameter("appropriation", appropriation)
                .one();
    }

    /**
     * Calculates the total JITF expenses associated with an appropriation.
     * <p>
     * Expenses are derived from obligations assigned to the JITF fund whose
     * activities belong to divisions associated with the specified
     * appropriation. A zero value is returned when no matching obligations
     * exist.
     * </p>
     *
     * @param appropriation the appropriation for which expenses are totaled
     * @param jitfFund      the JITF fund used to identify applicable
     *                      obligations
     * @return the total JITF expenses for the appropriation; never
     *         {@code null}
     */
    private BigDecimal sumExpenses(Appropriation appropriation, Fund jitfFund) {
        return dataManager.loadValue("""
                        SELECT COALESCE(SUM(e.amount), 0)
                        FROM fis_Obligation e
                        JOIN e.activity a
                        JOIN a.division d
                        WHERE a.fund = :fund
                          AND d.appropriation = :appropriation
                        """, BigDecimal.class)
                .parameter("fund", jitfFund)
                .parameter("appropriation", appropriation)
                .one();
    }
}