package gov.fjc.fis.view.paygrade;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.PayGrade;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;


@Route(value = "payGrades", layout = MainView.class)
@ViewController(id = "fis_PayGrade.list")
@ViewDescriptor(path = "pay-grade-list-view.xml")
@LookupComponent("payGradesDataGrid")
@DialogMode(width = "64em")
public class PayGradeListView extends StandardListView<PayGrade> {
}