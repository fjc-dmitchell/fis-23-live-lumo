package gov.fjc.fis.view.payperiod;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.PayPeriod;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "payPeriods/:id", layout = MainView.class)
@ViewController("fis_PayPeriod.detail")
@ViewDescriptor("pay-period-detail-view.xml")
@EditedEntityContainer("payPeriodDc")
public class PayPeriodDetailView extends StandardDetailView<PayPeriod> {
}