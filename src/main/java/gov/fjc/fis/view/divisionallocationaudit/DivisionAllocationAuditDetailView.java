package gov.fjc.fis.view.divisionallocationaudit;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.DivisionAllocationAudit;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.view.*;

@Route(value = "division-allocation-audits/:id", layout = MainView.class)
@ViewController(id = "fis_DivisionAllocationAudit.detail")
@ViewDescriptor(path = "division-allocation-audit-detail-view.xml")
@EditedEntityContainer("divisionAllocationAuditDc")
public class DivisionAllocationAuditDetailView extends StandardDetailView<DivisionAllocationAudit> {
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        var audit = getEditedEntity();
        createdByString.setText(audit.getCreatedByString());
    }
}
