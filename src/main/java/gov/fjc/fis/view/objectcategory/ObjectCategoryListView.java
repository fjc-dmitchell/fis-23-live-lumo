package gov.fjc.fis.view.objectcategory;

import com.vaadin.flow.data.selection.SelectionEvent;
import gov.fjc.fis.entity.ObjectCategory;

import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "objectCategories", layout = MainView.class)
@ViewController("fis_ObjectCategory.list")
@ViewDescriptor("object-category-list-view.xml")
@LookupComponent("categoriesDataGrid")
@DialogMode(width = "64em")
public class ObjectCategoryListView extends StandardListView<ObjectCategory> {
    @Autowired
    private AppropriationService appropriationService;
    @ViewComponent
    private JmixButton removeBtn;

    @Subscribe("categoriesDataGrid")
    protected void onCategoriesDataGridSelection(final SelectionEvent<DataGrid<ObjectCategory>, ObjectCategory> event) {
        event.getFirstSelectedItem().ifPresentOrElse(
                item -> removeBtn.setEnabled(appropriationService.isAppropriationOpen(item)),
                () -> removeBtn.setEnabled(false)
        );
    }
}