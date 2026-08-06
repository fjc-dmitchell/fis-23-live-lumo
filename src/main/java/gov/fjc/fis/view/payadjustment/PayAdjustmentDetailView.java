package gov.fjc.fis.view.payadjustment;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.PayAdjustment;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "payAdjustments/:id", layout = MainView.class)
@ViewController("fis_PayAdjustment.detail")
@ViewDescriptor("pay-adjustment-detail-view.xml")
@EditedEntityContainer("payAdjustmentDc")
public class PayAdjustmentDetailView extends StandardDetailView<PayAdjustment> {
}