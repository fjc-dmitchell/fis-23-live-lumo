package gov.fjc.fis.view.payperiod;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.service.PayPeriodService;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.personnel.PayPeriod;
import gov.fjc.fis.event.FiscalYearChangeEvent;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.List;


@Route(value = "payPeriods", layout = MainView.class)
@ViewController("fis_PayPeriod.list")
@ViewDescriptor("pay-period-list-view.xml")
@LookupComponent("payPeriodsDataGrid")
@DialogMode(width = "64em")
public class PayPeriodListView extends StandardListView<PayPeriod> {
    @Autowired
    private SessionData sessionData;
    @ViewComponent
    private CollectionLoader<PayPeriod> payPeriodsDl;
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private PayPeriodService payPeriodService;
    List<Appropriation> fiscalYears;

    @Subscribe
    public void onInit(final InitEvent event) {
        fiscalYears = appropriationService.getBfyFilterField(sessionData);
    }


    @Install(to = "payPeriodsDl", target = Target.DATA_LOADER)
    private List<PayPeriod> payPeriodsDlLoadDelegate(final LoadContext<PayPeriod> loadContext) {
        return payPeriodService.getPayPeriods(fiscalYears);
    }

    /**
     * Changes the Show BFY button caption after a Fiscal Year change event
     *
     * @param event
     */
    @Async
    @EventListener
    public void handleAsyncEvent(FiscalYearChangeEvent event) {
        fiscalYears = appropriationService.getBfyFilterField(sessionData);
        payPeriodsDl.load();
    }

}