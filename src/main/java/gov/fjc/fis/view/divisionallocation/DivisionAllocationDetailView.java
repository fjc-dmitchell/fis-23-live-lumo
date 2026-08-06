package gov.fjc.fis.view.divisionallocation;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.ObjectCategory;
import gov.fjc.fis.entity.DivisionAllocation;
import gov.fjc.fis.service.ObjectCategoryService;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.EntityStates;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "division-allocations/:id", layout = MainView.class)
@ViewController(id = "fis_DivisionAllocation.detail")
@ViewDescriptor(path = "division-allocation-detail-view.xml")
@EditedEntityContainer("divisionAllocationDc")
public class DivisionAllocationDetailView extends StandardDetailView<DivisionAllocation> {
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private ObjectCategoryService categoryService;
    @ViewComponent
    private EntityComboBox<ObjectCategory> categoryField;
    @ViewComponent
    private TypedTextField<BigDecimal> oneYearAmountField;
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        var allocation = getEditedEntity();
        if (entityStates.isNew(allocation)) {
            categoryField.focus();
        } else {
            categoryField.setReadOnly(true);
            oneYearAmountField.focus();
            oneYearAmountField.setAutoselect(true);
        }
        createdByString.setText(allocation.getCreatedByString());
    }

    @Install(to = "categoriesDl", target = Target.DATA_LOADER)
    protected List<ObjectCategory> categoriesDlLoadDelegate(final LoadContext<ObjectCategory> loadContext) {
        var allocation = getEditedEntity();
        if (entityStates.isNew(allocation)) {
            var division = getEditedEntity().getDivision();
            var allCategories = categoryService.fetchCategories(division.getAppropriation());
            var allocations = division.getAllocations();
            if (allocations == null) {
                return allCategories;
            } else {
                var usedCategories = allocations.stream().map(DivisionAllocation::getObjectCategory).toList();
                return allCategories.stream().filter(c -> !usedCategories.contains(c)).toList();
            }
        } else {
            return null;
        }
    }

    @Install(to = "categoryField", subject = "itemLabelGenerator")
    protected Object categoryFieldItemLabelGenerator(final ObjectCategory category) {
        return category.getTitleAndCode();
    }
}