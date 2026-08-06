package gov.fjc.fis.view.group;

import com.vaadin.flow.data.selection.SelectionEvent;
import gov.fjc.fis.entity.Group;

import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "groups", layout = MainView.class)
@ViewController("fis_Group.list")
@ViewDescriptor("group-list-view.xml")
@LookupComponent("groupsDataGrid")
@DialogMode(width = "64em")
public class GroupListView extends StandardListView<Group> {
    @Autowired
    private AppropriationService appropriationService;
    @ViewComponent
    private JmixButton removeBtn;

    @Subscribe("groupsDataGrid")
    protected void onGroupsDataGridSelection(final SelectionEvent<DataGrid<Group>, Group> event) {
        var selected = event.getFirstSelectedItem();
        removeBtn.setEnabled(selected.isPresent() && appropriationService.isAppropriationOpen(selected.get()));
    }
}