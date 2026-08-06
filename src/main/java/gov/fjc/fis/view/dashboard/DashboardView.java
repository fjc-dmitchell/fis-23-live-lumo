package gov.fjc.fis.view.dashboard;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.event.FiscalYearChangeEvent;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.view.main.MainView;
import gov.fjc.fis.view.reconciliationfragment.ReconciliationFragment;
import gov.fjc.fis.view.report.divisionbalancefragment.DivisionBalanceFragment;
import gov.fjc.fis.view.report.spendingchartfragment.SpendingChartFragment;
import gov.fjc.fis.view.report.statusoffundsfragment.StatusOfFundsFragment;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

@Route(value = "dashboard-view", layout = MainView.class)
@ViewController("fis_DashboardView")
@ViewDescriptor("dashboard-view.xml")
public class DashboardView extends StandardView {
    @Autowired
    private SessionData sessionData;
    @ViewComponent
    private CollectionLoader<Appropriation> appropriationsDl;
    @Autowired
    private AppropriationService appropriationService;
    @ViewComponent
    private EntityComboBox<Appropriation> appropriationsComboBox;
    Appropriation appropriation;

    @ViewComponent
    private StatusOfFundsFragment sofFragment;
    @ViewComponent
    private SpendingChartFragment spendFragment;
    @ViewComponent
    private DivisionBalanceFragment balanceFragment;
    @ViewComponent
    private ReconciliationFragment reconFragment;

    @Subscribe
    protected void onInit(final InitEvent event) {
        reconFragment.setApplicationContext(getApplicationContext());
        appropriationRefresh();
        appropriationsDl.load();
    }

    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    protected List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return appropriationService.getAppropriations();
    }

    @Subscribe("appropriationsComboBox")
    protected void onAppropriationsComboBoxComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {
        appropriation = appropriationsComboBox.getValue();
        sofFragment.setAppropriation(appropriation);
        spendFragment.setAppropriation(appropriation);
        balanceFragment.setAppropriation(appropriation);
        reconFragment.setAppropriation(appropriation);
    }

    private void appropriationRefresh() {
        Appropriation bfyEntry = appropriationService.getBfyEntryAppropriation(sessionData);
        if (bfyEntry != appropriation) {
            appropriation = bfyEntry;
            appropriationsComboBox.setValue(appropriation);
            //setTitles()
        }
    }

    @Async
    @EventListener
    public void handleAsyncEvent(FiscalYearChangeEvent event) {
        appropriationRefresh();
    }
}