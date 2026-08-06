package gov.fjc.fis.service;

import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.Fund;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("fis_FundService")
public class FundService {

    private final DataManager dataManager;
    private final DivisionService divisionService;

    // this is the only place in FIS that references fund codes
    private final String oneYearFund = "092800";
    private final String twoYearFund = "09280M";
    private final String foundationFund = "812300";
    private final String jitfFund = "51140X";

    public FundService(DataManager dataManager, DivisionService divisionService) {
        this.dataManager = dataManager;
        this.divisionService = divisionService;
    }

    private Fund fetchFund(String fundCode) {
        return dataManager.load(Fund.class)
                .query("SELECT f FROM fis_Fund f WHERE f.fundCode = :fundcode")
                .parameter("fundcode", fundCode)
                .one();
    }

    public Fund getAppropriationOneYearFund() {
        return fetchFund(oneYearFund);
    }

    public Fund getAppropriationTwoYearFund() {
        return fetchFund(twoYearFund);
    }

    public Fund getFoundationFund() {
        return fetchFund(foundationFund);
    }

    public Fund getJitfFund() {
        return fetchFund(jitfFund);
    }

    public List<Fund> getAppropriationFunds() {
        var funds = Arrays.asList(oneYearFund, twoYearFund);
        return dataManager.load(Fund.class)
                .query("SELECT f FROM fis_Fund f"
                        + " WHERE f.fundCode IN :funds")
                .parameter("funds", funds)
                .list();
    }

    public List<Fund> fetchFundSearchList(boolean foundation) {
        return dataManager.load(Fund.class)
                .query("SELECT f FROM fis_Fund f WHERE "
                        + "((:foundation = TRUE AND f.fundCode = :foundationFund) OR "
                        + "(:foundation = FALSE AND f.fundCode <> :foundationFund))")
                .parameter("foundation", foundation)
                .parameter("foundationFund", foundationFund)
                .list();
    }

    public List<Fund> getFundList() {
        return dataManager.load(Fund.class)
                .query("SELECT f FROM fis_Fund f ORDER BY f.fundCode")
                .list();
    }

    public List<Fund> getFundReportList() {
        return dataManager.load(Fund.class)
                .query("SELECT f FROM fis_Fund f ORDER BY f.id")
                .list();
    }

    public List<Fund> getAppropriatedFundsList() {
        return dataManager.load(Fund.class)
                .query("SELECT f FROM fis_Fund f"
                        + " WHERE f.fundCode IN (:oneYearFund, :twoYearFund) ORDER BY f.id")
                .parameter("oneYearFund", oneYearFund)
                .parameter("twoYearFund", twoYearFund)
                .list();
    }

    /**
     * used by Activity and search to restrict funds for a given Division
     *
     * @param division a persisted Division entity
     * @return List of valid Funds for division
     */
    public List<Fund> getDivisionFundList(Division division) {
        List<Fund> funds = new ArrayList<>();
        if (division != null) {
            funds.add(division.getFund());
            if (division.equals(divisionService.getEducationDivision(division.getAppropriation()))) {
                funds.add(getAppropriationTwoYearFund());
            }
            if (division.equals(divisionService.getAdministrationDivision(division.getAppropriation()))) {
                funds.add(getJitfFund());
            }
        }
        return funds;
    }

    /**
     * Used in reporting, turn single fund into collection of funds. Purpose is to allow
     * user to select "combined year fund" by selecting two year but getting both 1 and 2 year funds
     *
     * @param fund reference to a Fund entity instance
     * @return List of Funds
     */
    public List<Fund> getFundListForReports(Fund fund) {
        List<Fund> funds = new ArrayList<>();
        funds.add(fund);
        if (fund.equals(getAppropriationTwoYearFund())) {
            funds.add(getAppropriationOneYearFund());
        }
        return funds;
    }
}