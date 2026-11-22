package gov.fjc.fis.service;

import gov.fjc.fis.entity.Fund;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Provides access to funds used by FIS and operations for retrieving fund
 * lists.
 *
 * <p>This service centralizes the fund codes for the appropriation, Foundation,
 * and JITF funds and provides methods for retrieving their corresponding
 * {@link Fund} entities. It also supplies fund lists used by searches and other
 * application operations.</p>
 * @author Doug Mitchell
 */
@Component("fis_FundService")
public class FundService {

    private final DataManager dataManager;

    private final String ONE_YEAR_FUND = "092800";
    private final String TWO_YEAR_FUND = "09280M";
    private final String FOUNDATION_FUND = "812300";
    private final String JITF_FUND = "51140X";

    public FundService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Loads the fund having the specified fund code.
     *
     * @param fundCode the code of the fund to load
     * @return the fund having the specified code
     * @throws IllegalStateException if no matching fund exists or more than one
     *         matching fund is found
     */
    private Fund fetchFund(String fundCode) {
        return dataManager.load(Fund.class)
                .query("SELECT e FROM fis_Fund e WHERE e.fundCode = :fundCode")
                .parameter("fundCode", fundCode)
                .one();
    }

    /**
     * Returns the one-year appropriation fund.
     *
     * @return the one-year appropriation fund
     */
    public Fund getAppropriationOneYearFund() {
        return fetchFund(ONE_YEAR_FUND);
    }

    /**
     * Returns the two-year appropriation fund.
     *
     * @return the two-year appropriation fund
     */
    public Fund getAppropriationTwoYearFund() {
        return fetchFund(TWO_YEAR_FUND);
    }

    /**
     * Returns the Foundation fund.
     *
     * @return the Foundation fund
     */
    public Fund getFoundationFund() {
        return fetchFund(FOUNDATION_FUND);
    }

    /**
     * Returns the Judiciary Information Technology Fund (JITF).
     *
     * @return the JITF fund
     */
    public Fund getJitfFund() {
        return fetchFund(JITF_FUND);
    }

    /**
     * Returns the one-year and two-year appropriation funds.
     *
     * @return the appropriation funds
     */
    public List<Fund> fetchAppropriationFunds() {
        return dataManager.load(Fund.class)
                .query("SELECT e FROM fis_Fund e"
                        + " WHERE e.fundCode IN :funds")
                .parameter("funds", List.of(ONE_YEAR_FUND, TWO_YEAR_FUND))
                .list();
    }

    /**
     * Returns the funds available for a fund search.
     *
     * <p>When {@code foundation} is {@code true}, the result contains only the
     * Foundation fund. Otherwise, the result contains every fund except the
     * Foundation fund.</p>
     *
     * @param foundation whether to return only the Foundation fund
     * @return the funds available for the requested type of search
     */
    public List<Fund> fetchFundSearchList(boolean foundation) {
        return dataManager.load(Fund.class)
                .query("SELECT e FROM fis_Fund e WHERE "
                        + "((:foundation = TRUE AND e.fundCode = :foundationFund) OR "
                        + "(:foundation = FALSE AND e.fundCode <> :foundationFund))")
                .parameter("foundation", foundation)
                .parameter("foundationFund", FOUNDATION_FUND)
                .list();
    }

    /**
     * Returns all funds ordered by fund code for Fund list view.
     *
     * @return all funds in ascending fund-code order
     */
    public List<Fund> fetchFundList() {
        return dataManager.load(Fund.class)
                .query("SELECT e FROM fis_Fund e ORDER BY e.fundCode")
                .list();
    }

    /**
     * Returns the one-year and two-year appropriated funds.
     *
     * @return the appropriated funds ordered by identifier
     */
    public List<Fund> getAppropriatedFundsList() {
        return dataManager.load(Fund.class)
                .query("SELECT e FROM fis_Fund e"
                        + " WHERE e.fundCode IN (:oneYearFund, :twoYearFund) ORDER BY e.id")
                .parameter("oneYearFund", ONE_YEAR_FUND)
                .parameter("twoYearFund", TWO_YEAR_FUND)
                .list();
    }
}