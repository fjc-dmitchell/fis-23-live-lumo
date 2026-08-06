package gov.fjc.fis.view.report.divisionbalancefragment;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.dto.DivisionDto;
import gov.fjc.fis.service.AppropriationAdjustmentService;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.report.DivisionBalancesReportService;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

@FragmentDescriptor("division-balance-fragment.xml")
public class DivisionBalanceFragment extends Fragment<VerticalLayout> {

    @Autowired
    private DivisionBalancesReportService divisionBalancesReportService;

    @ViewComponent
    private CollectionLoader<DivisionDto> divisionBalancesDl;
    @ViewComponent
    private CollectionLoader<DivisionDto> divisionBalances2Dl;
    @ViewComponent
    private JmixDetails oneYearDetails;
    @ViewComponent
    private JmixDetails twoYearDetails;
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private AppropriationAdjustmentService appropriationAdjustmentService;
    @ViewComponent
    private Paragraph oneYearWarning;
    @ViewComponent
    private Paragraph twoYearWarning;

    Appropriation appropriation;
    DecimalFormat df;
    List<DivisionDto> divisionBalances;

    public void setAppropriation(Appropriation appropriation) {
        this.appropriation = appropriation;
        divisionBalances = divisionBalancesReportService.getDivisionBalances(appropriation);
        oneYearDetails.setSummaryText(appropriation.getBudgetFiscalYear().concat(" One Year Fund"));
        twoYearDetails.setSummaryText(appropriation.getBudgetFiscalYear().concat(" Two Year Fund"));
        df = new DecimalFormat("#,##0.00");

        divisionBalancesDl.load();
        divisionBalances2Dl.load();
        checkAllocations();
    }

    @Install(to = "divisionBalancesDl", target = Target.DATA_LOADER)
    protected List<DivisionDto> divisionBalancesDlLoadDelegate(final LoadContext<DivisionDto> loadContext) {
        return divisionBalances;
    }

    @Install(to = "divisionBalances2Dl", target = Target.DATA_LOADER)
    protected List<DivisionDto> divisionBalances2DlLoadDelegate(final LoadContext<DivisionDto> loadContext) {
        var twoYearDivisions = divisionBalances.stream().filter(DivisionDto::isTwoYearDivision).toList();
        twoYearDetails.setVisible(!twoYearDivisions.isEmpty());
        return twoYearDivisions;
    }

    private void checkAllocations() {
        var oneYearAllocations = divisionBalances.stream()
                .map(DivisionDto::getOneYearAllocations).reduce(BigDecimal.ZERO, BigDecimal::add);
        var oneYearAdjustments = appropriationAdjustmentService.sumOneYearAdjustments(appropriation);
        var oneYearAppropriation = appropriation.getOneYearAmount();
        var oneYearDiscrepancy = oneYearAppropriation.add(oneYearAdjustments).subtract(oneYearAllocations);
        setAllocationWarning(oneYearWarning, oneYearDiscrepancy);

        var twoYearAllocations = divisionBalances.stream()
                .map(DivisionDto::getTwoYearAllocations).reduce(BigDecimal.ZERO, BigDecimal::add);
        var twoYearAdjustments = appropriationAdjustmentService.sumTwoYearAdjustments(appropriation);
        var twoYearAppropriation = appropriation.getTwoYearAmount();
        var twoYearDiscrepancy = twoYearAppropriation.add(twoYearAdjustments).subtract(twoYearAllocations);
        setAllocationWarning(twoYearWarning, twoYearDiscrepancy);

    }

    private void setAllocationWarning(Paragraph paragraph, BigDecimal amount) {
        var prior2014message = "";
        if (appropriationService.isAppropriationBefore2014(appropriation)) {
            prior2014message = ". Prior to 2014, there were no separate 2-year allocations";
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            paragraph.setVisible(true);
            paragraph.setText(String.format("Warning: funds have been overallocated by %s" + prior2014message,
                    df.format(amount.abs())));
        } else if (amount.compareTo(BigDecimal.ZERO) > 0) {
            paragraph.setVisible(true);
            paragraph.setText(String.format("Warning: funds have been underallocated by %s" + prior2014message,
                    df.format(amount.abs())));
        } else {
            paragraph.setVisible(false);
            paragraph.setText(null);
        }
    }
}