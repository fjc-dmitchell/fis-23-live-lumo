package gov.fjc.fis.view.jitftransfer;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.ObjectCategory;
import gov.fjc.fis.entity.JitfTransfer;
import gov.fjc.fis.entity.ObjectClass;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.ObjectCategoryService;
import gov.fjc.fis.service.ObjectClassService;
import io.jmix.core.EntityStates;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Route(value = "jitf-transfers/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_JitfTransfer.detail")
@ViewDescriptor(path = "jitf-transfer-detail-view.xml")
@EditedEntityContainer("jitfTransferDc")
public class JitfTransferDetailView extends StandardDetailView<JitfTransfer> {
    @Autowired
    private ReadOnlyViewsSupport readOnlyViewsSupport;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private SessionData sessionData;
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private ObjectCategoryService categoryService;
    @Autowired
    private ObjectClassService objectClassService;
    @ViewComponent
    private CollectionLoader<ObjectCategory> categoriesDl;
    @ViewComponent
    private CollectionLoader<ObjectClass> objectClassesDl;
    @ViewComponent
    private TypedTextField<String> budgetFiscalYearField;
    @ViewComponent
    private EntityComboBox<ObjectCategory> categoryField;
    @ViewComponent
    private EntityComboBox<ObjectClass> budgetObjectClassField;
    @ViewComponent
    private JmixTextArea memoField;
    @ViewComponent
    private Paragraph createdByString;
    @ViewComponent
    private TypedDatePicker<Date> transferDateField;

    private Appropriation appropriation;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        var jitf = getEditedEntity();
        if (entityStates.isNew(jitf)) {
            appropriation = appropriationService.getBfyEntryAppropriation(sessionData);
            transferDateField.setValue(LocalDate.now());
        } else {
            appropriation = jitf.getObjectClass().getObjectCategory().getAppropriation();
            categoryField.setValue(jitf.getObjectClass().getObjectCategory());
            budgetObjectClassField.setValue(jitf.getObjectClass());
            createdByString.setText(jitf.getCreatedByString());
            if (!appropriation.getStatus()) {
                readOnlyViewsSupport.setViewReadOnly(this, true);
                categoryField.setReadOnly(true);
                memoField.focus();
            }
        }
        budgetFiscalYearField.setValue(appropriation.getBudgetFiscalYear());
        categoriesDl.load();
        objectClassesDl.load();
    }

    @Install(to = "categoriesDl", target = Target.DATA_LOADER)
    protected List<ObjectCategory> categoriesDlLoadDelegate(final LoadContext<ObjectCategory> loadContext) {
        return categoryService.fetchCategories(appropriation);
    }

    @Install(to = "objectClassesDl", target = Target.DATA_LOADER)
    protected List<ObjectClass> objectClassesDlLoadDelegate(final LoadContext<ObjectClass> loadContext) {
        return objectClassService.fetchObjectClasses(categoryField.getValue(), false);
    }

    @Install(to = "categoryField", subject = "itemLabelGenerator")
    protected Object categoryFieldItemLabelGenerator(final ObjectCategory category) {
        return category.getTitleAndCode();
    }

    @Install(to = "budgetObjectClassField", subject = "itemLabelGenerator")
    protected Object budgetObjectClassFieldItemLabelGenerator(final ObjectClass objectClass) {
        return objectClass.getTitleAndCode();
    }

    @Subscribe("categoryField")
    protected void onCategoryFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<ObjectCategory>, ObjectCategory> event) {
        checkObjectClass();
    }

    private void checkObjectClass() {
        objectClassesDl.load();
        if (budgetObjectClassField.getValue() != null) {
            budgetObjectClassField.setValue(
                    objectClassesDl.getContainer().getItems().stream()
                            .filter(boc -> boc.getBudgetObjectClass().equals(budgetObjectClassField.getValue().getBudgetObjectClass()))
                            .findFirst()
                            .orElse(null)
            );
        }
    }

//    private void checkObjectClass() {
//        objectClassesDl.load();
//
//        var currentBoc = budgetObjectClassField.getValue();
//        if (currentBoc == null) return;
//
//        var match = objectClassesDl.getContainer().getItems().stream()
//                .filter(boc -> boc.getBudgetObjectClass().equals(currentBoc.getBudgetObjectClass()))
//                .findFirst()
//                .orElse(null);
//
//        budgetObjectClassField.setValue(match);
//    }
}