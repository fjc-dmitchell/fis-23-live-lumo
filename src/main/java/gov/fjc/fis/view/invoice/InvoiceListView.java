package gov.fjc.fis.view.invoice;

import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Invoice;
import gov.fjc.fis.event.SearchGridSelectedItemsEvent;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.view.main.MainView;
import gov.fjc.fis.view.search.CustomSearchFragment;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "invoices", layout = MainView.class)
@ViewController("fis_Invoice.list")
@ViewDescriptor("invoice-list-view.xml")
@LookupComponent("invoicesDataGrid")
@DialogMode(width = "64em")
public class InvoiceListView extends StandardListView<Invoice> {
    @Autowired
    private UiEventPublisher uiEventPublisher;
    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    private AppropriationService appropriationService;

    @ViewComponent
    private CustomSearchFragment searchFragment;
    @ViewComponent
    private DataGrid<Invoice> invoicesDataGrid;
    @ViewComponent
    private JmixButton removeBtn;

    private boolean fjcFoundation = false;

    protected void setFjcFoundation() {
        this.fjcFoundation = true;
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        searchFragment.setFjcFoundation(fjcFoundation);
        searchFragment.setDataGrid(invoicesDataGrid);
    }

    @Subscribe("invoicesDataGrid.create")
    protected void onInvoicesDataGridCreate(final ActionPerformedEvent event) {
        viewNavigators.detailView(this, Invoice.class)
                .withViewClass(InvoiceDetailView.class)
                .withAfterNavigationHandler(afterNavigationEvent -> {
                    InvoiceDetailView view = afterNavigationEvent.getView();
                    view.setFjcFoundation(fjcFoundation);
                })
                .newEntity()
                .navigate();
    }

    @Subscribe("invoicesDataGrid")
    protected void onInvoicesDataGridSelection(final SelectionEvent<DataGrid<Invoice>, Invoice> event) {
        var selectedItems = event.getAllSelectedItems();
        if (selectedItems.size() == 1) {
            var selectedItem = selectedItems.stream().findFirst();
            removeBtn.setEnabled(appropriationService.isAppropriationOpen(selectedItem.get()));
        } else {
            removeBtn.setEnabled(false);
        }

        uiEventPublisher.publishEvent(new SearchGridSelectedItemsEvent(this, invoicesDataGrid, selectedItems.size()));
    }
}