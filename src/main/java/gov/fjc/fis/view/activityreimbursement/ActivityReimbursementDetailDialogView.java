package gov.fjc.fis.view.activityreimbursement;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.*;
import gov.fjc.fis.service.ObjectCategoryService;
import gov.fjc.fis.service.ObjectClassService;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static gov.fjc.fis.FisUtilities.*;

@Route(value = "activity-reimbursement-dialog/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_ActivityReimbursement_dialog.detail")
@ViewDescriptor(path = "activity-reimbursement-detail-dialog-view.xml")
@EditedEntityContainer("activityReimbursementDc")
public class ActivityReimbursementDetailDialogView extends StandardDetailView<ActivityReimbursement> {
    @Autowired
    private ObjectCategoryService categoryService;
    @Autowired
    private ObjectClassService objectClassService;

    @ViewComponent
    private CollectionLoader<ObjectCategory> categoriesDl;
    @ViewComponent
    private CollectionLoader<ObjectClass> objectClassesDl;
    @ViewComponent
    private EntityComboBox<ObjectCategory> categoryField;
    @ViewComponent
    private EntityComboBox<ObjectClass> objectClassField;
    @ViewComponent
    private TypedTextField<Object> sourceField;
    @ViewComponent
    private TypedTextField<BigDecimal> amountField;
    @ViewComponent
    private Paragraph createdByString;
    @ViewComponent
    private JmixButton saveAndCloseButton;

    Appropriation appropriation;
    ObjectCategory category;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        saveAndCloseButton.addClickShortcut(Key.ENTER).resetFocusOnActiveElement();

        var reimbursement = getEditedEntity();
        appropriation = reimbursement.getActivity().getDivision().getAppropriation();
        amountField.setAutoselect(true);
        categoryField.setRequired(true);

        if (reimbursement.getObjectClass() != null) {
            categoryField.setValue(reimbursement.getObjectClass().getObjectCategory());
        }

        createdByString.setText(reimbursement.getCreatedByString());
        categoriesDl.load();
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        UI ui = UI.getCurrent();
        if (ui != null) {
            if (categoryField.getValue() == null) {
                ui.beforeClientResponse(categoryField, ctx -> categoryField.focus());
            } else if (objectClassField.getValue() == null) {
                ui.beforeClientResponse(objectClassField, ctx -> objectClassField.focus());
            } else if(Objects.equals(sourceField.getValue(), "")) {
                ui.beforeClientResponse(sourceField, ctx -> sourceField.focus());
            } else {
                ui.beforeClientResponse(amountField, ctx -> amountField.focus());
            }
        }
    }

    @Subscribe("categoryField")
    protected void onCategoryFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<ObjectCategory>, ObjectCategory> event) {
        category = event.getValue();
        refreshField(objectClassesDl, objectClassField, ObjectClass::getBudgetObjectClass);
    }

    @Install(to = "categoriesDl", target = Target.DATA_LOADER)
    protected List<ObjectCategory> categoriesDlLoadDelegate(final LoadContext<ObjectCategory> loadContext) {
        return categoryService.fetchCategories(appropriation);
    }

    @Install(to = "objectClassesDl", target = Target.DATA_LOADER)
    protected List<ObjectClass> objectClassesDlLoadDelegate(final LoadContext<ObjectClass> loadContext) {
        return objectClassService.fetchObjectClasses(category, true);
    }

    @Install(to = "categoryField", subject = "itemLabelGenerator")
    protected Object categoryFieldItemLabelGenerator(final ObjectCategory category) {
        return category.getTitleAndCode();
    }

    @Install(to = "objectClassField", subject = "itemLabelGenerator")
    protected Object objectClassFieldItemLabelGenerator(final ObjectClass objectClass) {
        return objectClass.getTitleAndCode();
    }
}