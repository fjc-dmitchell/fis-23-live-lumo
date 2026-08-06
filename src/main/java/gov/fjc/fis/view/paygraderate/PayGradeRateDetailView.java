package gov.fjc.fis.view.paygraderate;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.PayGradeRate;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "pay-grade-rates/:id", layout = MainView.class)
@ViewController(id = "fis_PayGradeRate.detail")
@ViewDescriptor(path = "pay-grade-rate-detail-view.xml")
@EditedEntityContainer("payGradeRateDc")
public class PayGradeRateDetailView extends StandardDetailView<PayGradeRate> {
}