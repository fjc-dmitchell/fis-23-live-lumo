package gov.fjc.fis.view.activityprojectionaudit;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.ActivityProjectionAudit;
import io.jmix.flowui.view.*;


@Route(value = "activity-projection-audits", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_ActivityProjectionAudit.list")
@ViewDescriptor(path = "activity-projection-audit-list-view.xml")
@LookupComponent("activityProjectionAuditsDataGrid")
@DialogMode(width = "64em")
public class ActivityProjectionAuditListView extends StandardListView<ActivityProjectionAudit> {
}