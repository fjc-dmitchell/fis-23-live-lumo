package gov.fjc.fis.service;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.dto.JitfTransferDto;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("fis_JitfTransferService")
public class JitfTransferService {

    private final DataManager dataManager;
    private final FundService fundService;

    public JitfTransferService(DataManager dataManager, FundService fundService) {
        this.dataManager = dataManager;
        this.fundService = fundService;
    }

    public List<Appropriation> fetchAppropriations() {
        // EclipseLink has "IN" issues! Do this in 3 steps...
        var jitfAppropriations = dataManager.load(Appropriation.class)
                .query("SELECT cat.appropriation FROM fis_JitfTransfer j"
                        + " INNER JOIN fis_ObjectClass obj ON j.objectClass=obj"
                        + " INNER JOIN fis_ObjectCategory cat ON cat=obj.objectCategory")
                .list();

        if (jitfAppropriations.isEmpty()) {
            return Collections.emptyList();
        }

        var minYear = dataManager.loadValue(
                        "SELECT MIN(a.budgetFiscalYear) FROM fis_Appropriation a"
                                + " WHERE a IN :appropriations", String.class)
                .parameter("appropriations", jitfAppropriations)
                .optional().orElse(null);

        if (minYear == null) {
            return Collections.emptyList();
        }

        return dataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a"
                        + " WHERE a.budgetFiscalYear >= :year"
                        + " ORDER BY a.budgetFiscalYear ASC")
                .parameter("year", minYear)
                .list();
    }

    public BigDecimal getJitfAmount(Appropriation appropriation) {
        return dataManager.loadValue(
                        "SELECT COALESCE(SUM(j.amount),0)"
                                + " FROM fis_JitfTransfer j"
                                + " INNER JOIN fis_ObjectClass obj ON obj=j.objectClass"
                                + " INNER JOIN fis_ObjectCategory cat ON cat=obj.objectCategory"
                                + " WHERE cat.appropriation = :appropriation",
                        BigDecimal.class)
                .parameter("appropriation", appropriation)
                .one();
    }

    public BigDecimal getJitfExpenses(Appropriation appropriation) {
        var jitfFund = fundService.getJitfFund();
        return dataManager.loadValue(
                        "SELECT COALESCE(SUM(o.amount),0)"
                                + " FROM fis_Obligation o"
                                + " INNER JOIN fis_Activity a ON a=o.activity"
                                + " INNER JOIN fis_Division d ON d=a.division"
                                + " WHERE a.fund = :fund"
                                + " AND d.appropriation = :appropriation", BigDecimal.class)
                .parameter("fund", jitfFund)
                .parameter("appropriation", appropriation)
                .one();
    }

    public List<JitfTransferDto> generateReport() {
        var appropriations = fetchAppropriations();

        List<JitfTransferDto> jitfDtos = new ArrayList<>();
        JitfTransferDto dto;

        BigDecimal balance = BigDecimal.ZERO;

        for (Appropriation appropriation : appropriations) {
            dto = dataManager.create(JitfTransferDto.class);
            dto.setBudgetFiscalYear(appropriation.getBudgetFiscalYear());
            dto.setCarriedForward(balance);

            var deposits = getJitfAmount(appropriation);
            var expenses = getJitfExpenses(appropriation);
            balance = balance.add(deposits).subtract(expenses);

            dto.setTotalDeposits(deposits);
            dto.setTotalExpenses(expenses);
            dto.setCarryForward(balance);
            jitfDtos.add(dto);
        }
        return jitfDtos;
    }
}