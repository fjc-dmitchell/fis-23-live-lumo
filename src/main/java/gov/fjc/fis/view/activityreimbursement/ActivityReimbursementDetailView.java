package gov.fjc.fis.view.activityreimbursement;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.ActivityReimbursement;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.ObjectCategory;
import gov.fjc.fis.entity.ObjectClass;
import gov.fjc.fis.service.ObjectCategoryService;
import gov.fjc.fis.service.ObjectClassService;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.EntityStates;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "activity-reimbursements/:id", layout = MainView.class)
@ViewController(id = "fis_ActivityReimbursement.detail")
@ViewDescriptor(path = "activity-reimbursement-detail-view.xml")
@EditedEntityContainer("activityReimbursementDc")
public class ActivityReimbursementDetailView extends StandardDetailView<ActivityReimbursement> {

    @Autowired
    private EntityStates entityStates;
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
    private TypedTextField<BigDecimal> amountField;
    @ViewComponent
    private Paragraph createdByString;

    Appropriation appropriation;
    ObjectCategory category;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        ActivityReimbursement reimbursement = getEditedEntity();
        appropriation = reimbursement.getActivity().getDivision().getAppropriation();

        if (!entityStates.isNew(reimbursement)) {
            createdByString.setText(reimbursement.getCreatedByString());
            amountField.focus();
            amountField.setAutoselect(true);
        }
        categoriesDl.load();
//            objectClassField.setReadOnly(true);
    }

    @Subscribe("categoryField")
    protected void onCategoryFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<ObjectCategory>, ObjectCategory> event) {
        category = event.getValue();
        checkObjectClass();
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

    private void checkObjectClass() {
        objectClassesDl.load();
        if (objectClassField.getValue() != null) {
            objectClassField.setValue(
                    objectClassesDl.getContainer().getItems().stream()
                            .filter(boc -> boc.getBudgetObjectClass().equals(objectClassField.getValue().getBudgetObjectClass()))
                            .findFirst()
                            .orElse(null)
            );
        }
    }
}