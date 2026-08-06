package gov.fjc.fis.view.activityprojection;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.*;
import gov.fjc.fis.service.ObjectCategoryService;
import gov.fjc.fis.service.ObjectClassService;
import io.jmix.core.EntityStates;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "activity-projection-dialog/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_ActivityProjection_dialog.detail")
@ViewDescriptor(path = "activity-projection-detail-dialog-view.xml")
@EditedEntityContainer("activityProjectionDc")
public class ActivityProjectionDetailDialogView extends StandardDetailView<ActivityProjection> {
    @Autowired
    private EntityStates entityStates;
    @ViewComponent
    private CollectionLoader<ObjectCategory> categoriesDl;
    @ViewComponent
    private CollectionLoader<ObjectClass> objectClassesDl;
    @Autowired
    private ObjectClassService objectClassService;
    @Autowired
    private ObjectCategoryService categoryService;
    @ViewComponent
    private FormLayout.FormItem categoryFormItem;
    @ViewComponent
    private EntityComboBox<ObjectCategory> categoryField;
    @ViewComponent
    private EntityComboBox<ObjectClass> objectClassField;
    @ViewComponent
    private TypedTextField<BigDecimal> amountField;
    @ViewComponent
    private Paragraph createdByString;
    @ViewComponent
    private JmixButton saveAndCloseButton;

    Appropriation appropriation;
    Activity activity;
    ObjectCategory category;
    boolean genericProjection;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        saveAndCloseButton.addClickShortcut(Key.ENTER).resetFocusOnActiveElement();

        var projection = getEditedEntity();
        activity = projection.getActivity();
        amountField.setAutoselect(true);
        if (entityStates.isNew(projection)) {
            appropriation = activity.getDivision().getAppropriation();
            genericProjection = activity.getGenericProjection();
            categoryFormItem.setVisible(!genericProjection);
            if(projection.getObjectClass()==null) {
                categoriesDl.load();
                objectClassesDl.load();
            } else {
//                categoryFormItem.setVisible(false);
                categoryField.setValue(projection.getObjectClass().getObjectCategory());
                categoryField.setReadOnly(true);
                objectClassField.setReadOnly(true);
            }
        } else {
            createdByString.setText(projection.getCreatedByString());
            categoryField.setValue(projection.getObjectClass().getObjectCategory());
            categoryField.setReadOnly(true);
            objectClassField.setReadOnly(true);
        }
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        UI ui = UI.getCurrent();
        if (ui != null) {
            if(objectClassField.isReadOnly()) {
                ui.beforeClientResponse(amountField, ctx -> amountField.focus());
            } else if(categoryField.isReadOnly()) {
                ui.beforeClientResponse(objectClassField, ctx -> objectClassField.focus());
            } else {
                ui.beforeClientResponse(categoryField, ctx -> categoryField.focus());
            }
        }
    }

    @Subscribe("categoryField")
    protected void onCategoryFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<ObjectCategory>, ObjectCategory> event) {
        category = event.getValue();
        var boc = objectClassField.getValue();
        if (boc != null && !boc.getObjectCategory().equals(category)) {
            objectClassField.setValue(null);
        }
        objectClassesDl.load();
        objectClassField.focus();
    }

    @Subscribe("objectClassField")
    protected void onObjectClassFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<ObjectClass>, ObjectClass> event) {
        var boc = event.getValue();
        if (boc != null && !boc.getObjectCategory().equals(category)) {
            categoryField.setValue(boc.getObjectCategory());
        }
        amountField.focus();
    }

    @Install(to = "categoriesDl", target = Target.DATA_LOADER)
    protected List<ObjectCategory> categoriesDlLoadDelegate(final LoadContext<ObjectCategory> loadContext) {
        return categoryService.fetchCategories(appropriation);
    }

    @Install(to = "objectClassesDl", target = Target.DATA_LOADER)
    protected List<ObjectClass> objectClassesDlLoadDelegate(final LoadContext<ObjectClass> loadContext) {
        return objectClassService.fetchProjectionObjectClasses(activity, category);
    }

    @Install(to = "categoryField", subject = "itemLabelGenerator")
    protected Object categoryFieldItemLabelGenerator(final ObjectCategory category) {
        return category.getTitleAndCode();
    }

    @Install(to = "objectClassField", subject = "itemLabelGenerator")
    protected String objectClassFieldItemLabelGenerator(final ObjectClass objectClass) {
        return objectClass.getTitleAndCode();
    }
}