package gov.fjc.fis.view.activityprojection;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.*;
import gov.fjc.fis.service.ObjectCategoryService;
import gov.fjc.fis.service.ObjectClassService;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.EntityStates;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "activityProjections/:id", layout = MainView.class)
@ViewController(id = "fis_ActivityProjection.detail")
@ViewDescriptor(path = "activity-projection-detail-view.xml")
@EditedEntityContainer("activityProjectionDc")
public class ActivityProjectionDetailView extends StandardDetailView<ActivityProjection> {
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
    private TypedTextField<Object> amountField;
    @ViewComponent
    private Paragraph createdByString;

    Appropriation appropriation;
    Activity activity;
    ActivityProjection projection;
    ObjectCategory category;
    boolean genericProjection;

//    @Subscribe
//    protected void onBeforeShow(final BeforeShowEvent event) {
////        saveAndCloseButton.addClickShortcut(Key.ENTER).resetFocusOnActiveElement();
//
//        projection = getEditedEntity();
//        activity = projection.getActivity();
//        amountField.setAutoselect(true);
//        if (entityStates.isNew(projection)) {
//            appropriation = activity.getDivision().getAppropriation();
//            genericProjection = activity.getGenericProjection();
//            categoryFormItem.setVisible(!genericProjection);
//            if(projection.getObjectClass()==null) {
//                categoriesDl.load();
//                objectClassesDl.load();
//            } else {
////                categoryFormItem.setVisible(false);
//                categoryField.setValue(projection.getObjectClass().getCategory());
//                categoryField.setReadOnly(true);
//                objectClassField.setReadOnly(true);
//            }
//        } else {
//            createdByString.setText(projection.getCreatedByString());
//            categoryField.setValue(projection.getObjectClass().getCategory());
//            categoryField.setReadOnly(true);
//            objectClassField.setReadOnly(true);
//        }
//    }
//
//    @Subscribe
//    public void onReady(final ReadyEvent event) {
//        Stream.of(categoryField, objectClassField, amountField)
//                .filter(field -> !field.isReadOnly())
//                .findFirst()
//                .ifPresent(field -> {
//                    UI ui = UI.getCurrent();
//                    if (ui != null) {
//                        ui.beforeClientResponse(field, ctx -> field.focus());
//                    }
//                });
//    }
//
//    @Subscribe("categoryField")
//    protected void onCategoryFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Category>, Category> event) {
//        category = event.getValue();
//        var boc = objectClassField.getValue();
//        if (boc != null && !boc.getCategory().equals(category)) {
//            objectClassField.setValue(null);
//        }
//        objectClassesDl.load();
//        objectClassField.focus();
//    }
//
//    @Subscribe("objectClassField")
//    protected void onObjectClassFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<ObjectClass>, ObjectClass> event) {
//        var boc = event.getValue();
//        if (boc != null && !boc.getCategory().equals(category)) {
//            categoryField.setValue(boc.getCategory());
//        }
//        amountField.focus();
//    }
//
//    @Install(to = "categoriesDl", target = Target.DATA_LOADER)
//    protected List<Category> categoriesDlLoadDelegate(final LoadContext<Category> loadContext) {
//        return categoryService.fetchCategories(appropriation);
//    }
//
//    @Install(to = "objectClassesDl", target = Target.DATA_LOADER)
//    protected List<ObjectClass> objectClassesDlLoadDelegate(final LoadContext<ObjectClass> loadContext) {
//        return objectClassService.fetchProjectionObjectClasses(activity, category);
//    }
//
//    @Install(to = "categoryField", subject = "itemLabelGenerator")
//    protected Object categoryFieldItemLabelGenerator(final Category category) {
//        return category.getTitleAndCode();
//    }
//
//    @Install(to = "objectClassField", subject = "itemLabelGenerator")
//    protected String objectClassFieldItemLabelGenerator(final ObjectClass objectClass) {
//        return objectClass.getTitleAndCode();
//    }
}