package gov.fjc.fis.view.positionaudit;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.PositionAudit;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;


@Route(value = "position-audits", layout = MainView.class)
@ViewController(id = "fis_PositionAudit.list")
@ViewDescriptor(path = "position-audit-list-view.xml")
@LookupComponent("positionAuditsDataGrid")
@DialogMode(width = "64em")
public class PositionAuditListView extends StandardListView<PositionAudit> {
}