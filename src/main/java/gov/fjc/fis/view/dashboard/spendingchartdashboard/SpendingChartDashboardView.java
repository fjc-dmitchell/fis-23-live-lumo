package gov.fjc.fis.view.dashboard.spendingchartdashboard;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.event.FiscalYearChangeEvent;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.view.main.MainView;
import gov.fjc.fis.view.report.spendingchartfragment.SpendingChartFragment;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

/**
 * Dashboard view that displays graphical spending information for a selected
 * appropriation.
 *
 * <p>The view allows users to select an appropriation and presents spending
 * data through a {@link SpendingChartFragment}. The dashboard provides a
 * visual representation of appropriation activity, enabling users to quickly
 * analyze spending trends and financial activity for the selected budget
 * fiscal year.
 *
 * <p>The initial appropriation is determined from the budget fiscal year
 * stored in the user's session and is automatically refreshed when a
 * {@link FiscalYearChangeEvent} occurs, ensuring that the displayed chart
 * remains synchronized with the user's current fiscal year context.
 *
 * @author Doug Mitchell
 */
@Route(value = "spending-chart-dashboard", layout = MainView.class)
@ViewController(id = "fis_SpendingChartDashboardView")
@ViewDescriptor(path = "spending-chart-dashboard-view.xml")
public class SpendingChartDashboardView extends StandardView {
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
    private SpendingChartFragment spendFragment;

    @Subscribe
    protected void onInit(final InitEvent event) {
        appropriationRefresh();
        appropriationsDl.load();
    }

    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    protected List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return appropriationService.getAppropriations();
    }

    @Subscribe("appropriationsComboBox")
    protected void onAppropriationsComboBoxComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {
        if (event.getValue() == null) {
            appropriationsComboBox.setValue(event.getOldValue());
        }
        appropriation = appropriationsComboBox.getValue();
        spendFragment.setAppropriation(appropriation);
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
