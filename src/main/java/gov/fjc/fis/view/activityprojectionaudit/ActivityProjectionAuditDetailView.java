package gov.fjc.fis.view.activityprojectionaudit;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.ActivityProjectionAudit;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;

@Route(value = "activity-projection-audits/:id", layout = MainView.class)
@ViewController(id = "fis_ActivityProjectionAudit.detail")
@ViewDescriptor(path = "activity-projection-audit-detail-view.xml")
@EditedEntityContainer("activityProjectionAuditDc")
public class ActivityProjectionAuditDetailView extends StandardDetailView<ActivityProjectionAudit> {
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        var audit = getEditedEntity();
        createdByString.setText(audit.getCreatedByString());
    }
}