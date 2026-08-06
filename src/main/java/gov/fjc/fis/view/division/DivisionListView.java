package gov.fjc.fis.view.division;

import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.view.main.MainView;
import gov.fjc.fis.view.search.CustomSearchFragment;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "divisions", layout = MainView.class)
@ViewController(id = "fis_Division.list")
@ViewDescriptor(path = "division-list-view.xml")
@LookupComponent("divisionsDataGrid")
@DialogMode(width = "64em")
public class DivisionListView extends StandardListView<Division> {
    @Autowired
    private ViewNavigators viewNavigators;
    @ViewComponent
    private CustomSearchFragment searchFragment;
    @Autowired
    private AppropriationService appropriationService;
    @ViewComponent
    private JmixButton removeBtn;

    private boolean fjcFoundation = false;

    public void setFjcFoundation() {
        this.fjcFoundation = true;
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        searchFragment.setFjcFoundation(fjcFoundation);
    }

    @Subscribe("divisionsDataGrid.createAction")
    protected void onDivisionsDataGridCreateAction(final ActionPerformedEvent event) {
        viewNavigators.detailView(this, Division.class)
                .withViewClass(DivisionDetailView.class)
                .withAfterNavigationHandler(afterNavigationEvent -> {
                    DivisionDetailView view = afterNavigationEvent.getView();
                    view.setFjcFoundation(fjcFoundation);
                })
                .newEntity()
                .navigate();
    }

    @Subscribe("divisionsDataGrid")
    protected void onDivisionsDataGridSelection(final SelectionEvent<DataGrid<Division>, Division> event) {
        var selectedItems = event.getAllSelectedItems();
        if (selectedItems.size() == 1) {
            var selectedItem = selectedItems.stream().findFirst();
            removeBtn.setEnabled(appropriationService.isAppropriationOpen(selectedItem.get()));
        } else {
            removeBtn.setEnabled(false);
        }
    }
}