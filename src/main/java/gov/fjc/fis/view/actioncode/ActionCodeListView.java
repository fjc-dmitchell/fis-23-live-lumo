package gov.fjc.fis.view.actioncode;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.ActionCode;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;


@Route(value = "actionCodes", layout = MainView.class)
@ViewController("fis_ActionCode.list")
@ViewDescriptor("action-code-list-view.xml")
@LookupComponent("actionCodesDataGrid")
@DialogMode(width = "64em")
public class ActionCodeListView extends StandardListView<ActionCode> {
}