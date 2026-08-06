package gov.fjc.fis.view.actioncode;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.ActionCode;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.view.*;

@Route(value = "action-codes/:id", layout = MainView.class)
@ViewController(id = "fis_ActionCode.detail")
@ViewDescriptor(path = "action-code-detail-view.xml")
@EditedEntityContainer("actionCodeDc")
public class ActionCodeDetailView extends StandardDetailView<ActionCode> {
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        createdByString.setText(getEditedEntity().getCreatedByString());
    }
}
