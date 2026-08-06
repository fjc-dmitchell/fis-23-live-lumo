package gov.fjc.fis.view.activityreimbursement;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.ActivityReimbursement;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;


@Route(value = "activity-reimbursements", layout = MainView.class)
@ViewController(id = "fis_ActivityReimbursement.list")
@ViewDescriptor(path = "activity-reimbursement-list-view.xml")
@LookupComponent("activityReimbursementsDataGrid")
@DialogMode(width = "64em")
public class ActivityReimbursementListView extends StandardListView<ActivityReimbursement> {
}