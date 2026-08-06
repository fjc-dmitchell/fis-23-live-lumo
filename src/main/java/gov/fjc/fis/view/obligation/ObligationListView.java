package gov.fjc.fis.view.obligation;

import com.vaadin.flow.data.selection.SelectionEvent;
import gov.fjc.fis.entity.Obligation;
import gov.fjc.fis.event.SearchGridSelectedItemsEvent;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.view.main.MainView;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.view.search.CustomSearchFragment;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "obligations", layout = MainView.class)
@ViewController("fis_Obligation.list")
@ViewDescriptor("obligation-list-view.xml")
@LookupComponent("obligationsDataGrid")
@DialogMode(width = "64em")
public class ObligationListView extends StandardListView<Obligation> {
    @Autowired
    private UiEventPublisher uiEventPublisher;
    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    private AppropriationService appropriationService;
    @ViewComponent
    private CustomSearchFragment searchFragment;
    @ViewComponent
    private DataGrid<Obligation> obligationsDataGrid;
    @ViewComponent
    private JmixButton removeBtn;

    private boolean fjcFoundation = false;

    protected void setFjcFoundation() {
        this.fjcFoundation = true;
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        searchFragment.setFjcFoundation(fjcFoundation);
        searchFragment.setDataGrid(obligationsDataGrid);
    }

    @Subscribe("obligationsDataGrid.create")
    protected void onObligationsDataGridCreate(final ActionPerformedEvent event) {
        viewNavigators.detailView(this, Obligation.class)
                .withViewClass(ObligationDetailView.class)
                .withAfterNavigationHandler(afterNavigationEvent -> {
                    ObligationDetailView view = afterNavigationEvent.getView();
                    view.setFjcFoundation(fjcFoundation);
                })
                .newEntity()
                .navigate();
    }

    @Subscribe("obligationsDataGrid")
    protected void onObligationsDataGridSelection(final SelectionEvent<DataGrid<Obligation>, Obligation> event) {
        var selectedItems = event.getAllSelectedItems();
        if (selectedItems.size() == 1) {
            var selectedItem = selectedItems.stream().findFirst();
            removeBtn.setEnabled(appropriationService.isAppropriationOpen(selectedItem.get()));
        } else {
            removeBtn.setEnabled(false);
        }

        uiEventPublisher.publishEvent(new SearchGridSelectedItemsEvent(this, obligationsDataGrid, selectedItems.size()));
    }

    @Install(to = "obligationsDataGrid.costOrg", subject = "partNameGenerator")
    protected String obligationsDataGridCostOrgPartNameGenerator(final Obligation obligation) {
        var costOrg = obligation.getCostOrg();
        return costOrg == null ? null : obligation.getCostOrg().getBudgetOrg();
    }
}