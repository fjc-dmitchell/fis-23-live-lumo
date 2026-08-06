package gov.fjc.fis.view.objectclass;

import com.vaadin.flow.data.selection.SelectionEvent;
import gov.fjc.fis.entity.ObjectClass;

import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "objectClasses", layout = MainView.class)
@ViewController("fis_ObjectClass.list")
@ViewDescriptor("object-class-list-view.xml")
@LookupComponent("objectClassesDataGrid")
@DialogMode(width = "64em")
public class ObjectClassListView extends StandardListView<ObjectClass> {
    @Autowired
    private AppropriationService appropriationService;
    @ViewComponent
    private JmixButton removeBtn;

    @Subscribe("objectClassesDataGrid")
    protected void onObjectClassesDataGridSelection(final SelectionEvent<DataGrid<ObjectClass>, ObjectClass> event) {
        event.getFirstSelectedItem().ifPresentOrElse(
                item -> removeBtn.setEnabled(appropriationService.isAppropriationOpen(item)),
                () -> removeBtn.setEnabled(false)
        );
    }
}