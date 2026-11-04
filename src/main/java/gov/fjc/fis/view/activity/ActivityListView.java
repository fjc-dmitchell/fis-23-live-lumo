package gov.fjc.fis.view.activity;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.data.selection.SelectionEvent;
import gov.fjc.fis.entity.Activity;
import gov.fjc.fis.entity.Obligation;
import gov.fjc.fis.event.SearchGridSelectedItemsEvent;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.view.main.MainView;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.view.search.CustomSearchFragment;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Route(value = "activities", layout = MainView.class)
@ViewController("fis_Activity.list")
@ViewDescriptor("activity-list-view.xml")
@LookupComponent("activitiesDataGrid")
@DialogMode(width = "64em")
public class ActivityListView extends StandardListView<Activity> {
    @Autowired
    private UiEventPublisher uiEventPublisher;
    @Autowired
    private SessionData sessionData;
    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    private AppropriationService appropriationService;

    @ViewComponent
    private CustomSearchFragment searchFragment;
    @ViewComponent
    private DataGrid<Activity> activitiesDataGrid;
    @ViewComponent
    private JmixButton showObligationsBtn;
    @ViewComponent
    private JmixButton removeBtn;

    private List<Integer> activityIds;

    private boolean fjcFoundation = false;

    protected void setFjcFoundation() {
        this.fjcFoundation = true;
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        searchFragment.setFjcFoundation(fjcFoundation);
        searchFragment.setDataGrid(activitiesDataGrid);
    }

    @Subscribe("activitiesDataGrid.create")
    protected void onActivitiesDataGridCreate(final ActionPerformedEvent event) {
        viewNavigators.detailView(this, Activity.class)
                .withViewClass(ActivityDetailView.class)
                .withAfterNavigationHandler(afterNavigationEvent -> {
                    ActivityDetailView view = afterNavigationEvent.getView();
                    view.setFjcFoundation(fjcFoundation);
                })
                .newEntity()
                .navigate();
    }

    @Subscribe("activitiesDataGrid")
    protected void onActivitiesDataGridSelection(final SelectionEvent<DataGrid<Activity>, Activity> event) {
        var selectedItems = event.getAllSelectedItems();
        showObligationsBtn.setEnabled(!selectedItems.isEmpty());
        activityIds = new ArrayList<>();
        selectedItems.forEach(act -> activityIds.add(act.getId()));

        if (selectedItems.size() == 1) {
            var selectedItem = selectedItems.stream().findFirst();
            removeBtn.setEnabled(appropriationService.isAppropriationOpen(selectedItem.get()));
        } else {
            removeBtn.setEnabled(false);
        }

        uiEventPublisher.publishEvent(new SearchGridSelectedItemsEvent(this, activitiesDataGrid, selectedItems.size()));
    }

    @Subscribe(id = "showObligationsBtn", subject = "clickListener")
    public void onShowObligationsBtnClick(final ClickEvent<JmixButton> event) {
        sessionData.setAttribute("ActivityObligations.activityIds", activityIds);
        viewNavigators.listView(this, Obligation.class).navigate();
    }
}