package gov.fjc.fis.view.fundcontrolnotice;

import com.vaadin.flow.data.selection.SelectionEvent;
import gov.fjc.fis.entity.FundControlNotice;
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

@Route(value = "fundControlNotices", layout = MainView.class)
@ViewController("fis_FundControlNotice.list")
@ViewDescriptor("fund-control-notice-list-view.xml")
@LookupComponent("fundControlNoticesDataGrid")
@DialogMode(width = "64em")
public class FundControlNoticeListView extends StandardListView<FundControlNotice> {
    @Autowired
    private UiEventPublisher uiEventPublisher;
    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    private AppropriationService appropriationService;

    @ViewComponent
    private CustomSearchFragment searchFragment;
    @ViewComponent
    private DataGrid<FundControlNotice> fundControlNoticesDataGrid;
    @ViewComponent
    private JmixButton removeBtn;

    private boolean fjcFoundation = false;

    protected void setFjcFoundation() {
        this.fjcFoundation = true;
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        searchFragment.setFjcFoundation(fjcFoundation);
        searchFragment.setDataGrid(fundControlNoticesDataGrid);
    }

    @Subscribe("fundControlNoticesDataGrid.create")
    protected void onFundControlNoticesDataGridCreate(final ActionPerformedEvent event) {
        viewNavigators.detailView(this, FundControlNotice.class)
                .withViewClass(FundControlNoticeDetailView.class)
                .withAfterNavigationHandler(afterNavigationEvent -> {
                    FundControlNoticeDetailView view = afterNavigationEvent.getView();
                    view.setFjcFoundation(fjcFoundation);
                })
                .newEntity()
                .navigate();
    }

    @Subscribe("fundControlNoticesDataGrid")
    protected void onFundControlNoticesDataGridSelection(final SelectionEvent<DataGrid<FundControlNotice>, FundControlNotice> event) {
        var selectedItems = event.getAllSelectedItems();
        if (selectedItems.size() == 1) {
            var selectedItem = selectedItems.stream().findFirst();
            removeBtn.setEnabled(appropriationService.isAppropriationOpen(selectedItem.get()));
        } else {
            removeBtn.setEnabled(false);
        }

        uiEventPublisher.publishEvent(new SearchGridSelectedItemsEvent(this, fundControlNoticesDataGrid, selectedItems.size()));
    }
}