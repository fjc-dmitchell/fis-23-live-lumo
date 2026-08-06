package gov.fjc.fis.view.usersettingsitem;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.settings.UserSettingsService;
import io.jmix.flowui.view.*;
import io.jmix.flowuidata.entity.UserSettingsItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@Route(value = "userSettingsItems", layout = MainView.class)
@ViewController(id = "flowui_UserSettingsItem.list")
@ViewDescriptor(path = "user-settings-item-list-view.xml")
@LookupComponent("userSettingsItemsDataGrid")
@DialogMode(width = "64em")
public class UserSettingsItemListView extends StandardListView<UserSettingsItem> {
    @ViewComponent
    private DataGrid<UserSettingsItem> userSettingsItemsDataGrid;
    @Autowired
    private UserSettingsCache userSettingsCache;
    @Autowired
    private UserSettingsService userSettingsService;
    @Autowired
    private Dialogs dialogs;
    @ViewComponent
    private JmixButton removeButton;
    @ViewComponent
    private CollectionLoader<UserSettingsItem> userSettingsItemsDl;

    @Subscribe("userSettingsItemsDataGrid")
    public void onUserSettingsItemsDataGridSelection(final SelectionEvent<DataGrid<UserSettingsItem>, UserSettingsItem> event) {
        removeButton.setEnabled(!event.getAllSelectedItems().isEmpty());
    }

    @Subscribe(id = "removeButton", subject = "clickListener")
    public void onRemoveButtonClick(final ClickEvent<JmixButton> event) {
        Set<UserSettingsItem> selected = userSettingsItemsDataGrid.getSelectedItems();

        dialogs.createOptionDialog()
                .withHeader("Confirm Deletion")
                .withText("Delete %d selected item(s)? This cannot be undone.".formatted(selected.size()))
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withText("Delete")
                                .withHandler(e -> deleteSelectedItems(selected)),
                        new DialogAction(DialogAction.Type.NO)
                                .withText("Cancel")
                )
                .open();
    }

    private void deleteSelectedItems(Set<UserSettingsItem> items) {
        items.forEach(e -> userSettingsCache.delete(e.getKey()));
        items.forEach(e -> userSettingsService.delete(e.getKey()));
        userSettingsItemsDl.load();
    }
}