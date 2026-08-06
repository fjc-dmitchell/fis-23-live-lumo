package gov.fjc.fis.view.payadjustment;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.PayAdjustment;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;


@Route(value = "payAdjustments", layout = MainView.class)
@ViewController("fis_PayAdjustment.list")
@ViewDescriptor("pay-adjustment-list-view.xml")
@LookupComponent("payAdjustmentsDataGrid")
@DialogMode(width = "64em")
public class PayAdjustmentListView extends StandardListView<PayAdjustment> {
}