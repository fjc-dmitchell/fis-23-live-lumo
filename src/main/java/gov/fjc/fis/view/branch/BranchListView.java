package gov.fjc.fis.view.branch;

import com.vaadin.flow.data.selection.SelectionEvent;
import gov.fjc.fis.entity.Branch;

import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "branches", layout = MainView.class)
@ViewController("fis_Branch.list")
@ViewDescriptor("branch-list-view.xml")
@LookupComponent("branchesDataGrid")
@DialogMode(width = "64em")
public class BranchListView extends StandardListView<Branch> {
    @Autowired
    private AppropriationService appropriationService;
    @ViewComponent
    private JmixButton removeBtn;

    @Subscribe("branchesDataGrid")
    protected void onBranchesDataGridSelection(final SelectionEvent<DataGrid<Branch>, Branch> event) {
        var selected = event.getFirstSelectedItem();
        removeBtn.setEnabled(selected.isPresent() && appropriationService.isAppropriationOpen(selected.get()));
    }
}