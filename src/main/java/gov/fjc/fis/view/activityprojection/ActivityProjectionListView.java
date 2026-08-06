package gov.fjc.fis.view.activityprojection;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.ActivityProjection;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;


@Route(value = "activityProjections", layout = MainView.class)
@ViewController("fis_ActivityProjection.list")
@ViewDescriptor("activity-projection-list-view.xml")
@LookupComponent("activityProjectionsDataGrid")
@DialogMode(width = "64em")
public class ActivityProjectionListView extends StandardListView<ActivityProjection> {
}