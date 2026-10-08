package gov.fjc.fis.view.obligation;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.*;
import gov.fjc.fis.service.*;
import gov.fjc.fis.view.activityprojection.ActivityProjectionUpdateView;
import gov.fjc.fis.view.fileattachmentfragment.FileAttachmentFragment;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlan;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static gov.fjc.fis.FisUtilities.refreshField;

@Route(value = "obligations/:id", layout = MainView.class)
@ViewController(id = "fis_Obligation.detail")
@ViewDescriptor(path = "obligation-detail-view.xml")
@EditedEntityContainer("obligationDc")
public class ObligationDetailView extends StandardDetailView<Obligation> {
    @Autowired
    ReadOnlyViewsSupport readOnlyViewsSupport;
    @Autowired
    SessionData sessionData;
    @Autowired
    private EntityStates entityStates;

    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ObligationService obligationService;
    @Autowired
    private ObjectCategoryService categoryService;
    @Autowired
    private ObjectClassService objectClassService;

    @ViewComponent
    private CollectionLoader<Division> divisionsDl;
    @ViewComponent
    private CollectionLoader<Activity> activitiesDl;
    @ViewComponent
    private InstanceLoader<Obligation> obligationDl;
    @ViewComponent
    private CollectionLoader<ObjectCategory> categoriesDl;
    @ViewComponent
    private CollectionLoader<ObjectClass> objectClassesDl;

    @ViewComponent
    private FileAttachmentFragment attachmentFragment;
    @ViewComponent
    private TypedTextField<String> budgetFiscalYearField;
    @ViewComponent
    private EntityComboBox<Division> divisionField;
    @ViewComponent
    private EntityComboBox<ObjectCategory> categoryField;
    @ViewComponent
    private EntityComboBox<ObjectClass> budgetObjectClassField;
    @ViewComponent
    private JmixComboBox<Boolean> statusField;
    @ViewComponent
    private JmixComboBox<Boolean> blanketPurchaseOrderField;
    @ViewComponent
    private Paragraph createdByString;
    private Appropriation entryBfy;
    private Division division;
    private ObjectCategory category;
    private Boolean fjcFoundation = false;
    @ViewComponent
    private EntityComboBox<Activity> activityField;
    @ViewComponent
    private TypedDatePicker<Date> processDateField;
    @ViewComponent
    private TypedDatePicker<Date> documentDateField;
    @ViewComponent
    private TypedDatePicker<Date> travelStartDateField;
    @ViewComponent
    private TypedDatePicker<Date> travelEndDateField;
    @ViewComponent
    private JmixSelect<DocumentType> docType;
    @Autowired
    private DialogWindows dialogWindows;

    private BigDecimal originalAmount;

    public void setFjcFoundation(Boolean fjcFoundation) {
        this.fjcFoundation = fjcFoundation;
        if (fjcFoundation) {
            var obligation = getEditedEntity();
            divisionsDl.load();
        }
    }

    @Subscribe
    protected void onInit(final InitEvent event) {
        ComponentUtils.setItemsMap(statusField, getStatusItemsMap());
        ComponentUtils.setItemsMap(blanketPurchaseOrderField, getBpoItemsMap());
        divisionField.setRequired(true);
        categoryField.setRequired(true);
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        entryBfy = appropriationService.getBfyEntryAppropriation(sessionData);
        obligationDl.load();
        var obligation = getEditedEntity();

        attachmentFragment.setHostEntity(obligation);
        if (entityStates.isNew(obligation)) {
//           entryBfy = appropriationService.getBfyEntryAppropriation(sessionData);
            if (entryBfy != null) {
                budgetFiscalYearField.setValue(entryBfy.getBudgetFiscalYear());
            }
            var today = LocalDate.now();
            documentDateField.setValue(today);
            processDateField.setValue(today);
            docType.setValue(DocumentType.MISCELLANEOUS_OBLIGATION);
//            divisionField.focus();

        } else {
            Appropriation appropriation = obligation.getObjectClass().getObjectCategory().getAppropriation();
            if (!appropriation.getStatus()) {
                readOnlyViewsSupport.setViewReadOnly(this, true);
                attachmentFragment.setReadOnly(true);
            }
            budgetFiscalYearField.setValue(appropriation.getBudgetFiscalYear());

            divisionField.setValue(obligation.getActivity().getDivision());
            categoryField.setValue(obligation.getObjectClass().getObjectCategory());
//            budgetObjectClassField.setValue(obligation.getObjectClass());
//            activityField.setValue(obligation.getActivity());

            divisionField.setReadOnly(true);
            categoryField.setReadOnly(true);
            budgetFiscalYearField.setReadOnly(true);
            activityField.setReadOnly(true);

//            divisionField.focus();
            createdByString.setText(obligation.getCreatedByString());
        }
        originalAmount = obligation.getAmount();
        divisionsDl.load();
        categoriesDl.load();
        objectClassesDl.load();
    }

    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    protected List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.getDivisions(entryBfy, fjcFoundation);
    }

    @Install(to = "activitiesDl", target = Target.DATA_LOADER)
    protected List<Activity> activitiesDlLoadDelegate(final LoadContext<Activity> loadContext) {
        return activityService.getActivities(division);
    }

    @Install(to = "categoriesDl", target = Target.DATA_LOADER)
    protected List<ObjectCategory> categoriesDlLoadDelegate(final LoadContext<ObjectCategory> loadContext) {
        return categoryService.fetchCategories(entryBfy);
    }

    @Install(to = "objectClassesDl", target = Target.DATA_LOADER)
    protected List<ObjectClass> objectClassesDlLoadDelegate(final LoadContext<ObjectClass> loadContext) {
        return objectClassService.fetchObjectClasses(categoryField.getValue(), false);
    }

    @Install(to = "divisionField", subject = "itemLabelGenerator")
    protected Object divisionFieldItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }

    @Install(to = "categoryField", subject = "itemLabelGenerator")
    protected Object categoryFieldItemLabelGenerator(final ObjectCategory category) {
        return category.getTitleAndCode();
    }

    @Install(to = "budgetObjectClassField", subject = "itemLabelGenerator")
    protected Object budgetObjectClassFieldItemLabelGenerator(final ObjectClass objectClass) {
        return objectClass.getTitleAndCode();
    }

    @Install(to = "activityField", subject = "itemLabelGenerator")
    protected Object activityFieldItemLabelGenerator(final Activity activity) {
        return activity.getTitleAndCode();
    }

    @Subscribe("docType")
    public void onDocTypeComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixSelect<DocumentType>, DocumentType> event) {
        travelStartDateField.setEnabled(DocumentType.TRAVEL_AUTHORIZATION.equals(event.getValue()));
        travelEndDateField.setEnabled(DocumentType.TRAVEL_AUTHORIZATION.equals(event.getValue()));
    }

    @Subscribe("divisionField")
    protected void onDivisionFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Division>, Division> event) {
        division = event.getValue();
        refreshField(activitiesDl, activityField, Activity::getActivityNumber);
//        checkActivity();
    }

    @Subscribe("categoryField")
    protected void onCategoryFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<ObjectCategory>, ObjectCategory> event) {
        category = event.getValue();
        refreshField(objectClassesDl, budgetObjectClassField, ObjectClass::getBudgetObjectClass);
//        checkObjectClass();
    }

    // NO! refresh is getting all attachments, not just those for this obligation!
    @Subscribe(id = "invoicesDc", target = Target.DATA_CONTAINER)
    public void onInvoicesDcCollectionChange(final CollectionContainer.CollectionChangeEvent<Invoice> event) {
        attachmentFragment.refresh();
    }

    @Subscribe(id = "fundControlNoticesDc", target = Target.DATA_CONTAINER)
    public void onFundControlNoticesDcCollectionChange(final CollectionContainer.CollectionChangeEvent<FundControlNotice> event) {
        attachmentFragment.refresh();
    }

    protected Map<Boolean, String> getStatusItemsMap() {
        LinkedHashMap<Boolean, String> map = new LinkedHashMap<>();
        map.put(Boolean.TRUE, "Open");
        map.put(Boolean.FALSE, "Closed");
        return map;
    }

    protected Map<Boolean, String> getBpoItemsMap() {
        LinkedHashMap<Boolean, String> map = new LinkedHashMap<>();
        map.put(Boolean.TRUE, "Yes");
        map.put(Boolean.FALSE, "No");
        return map;
    }

    // ---------------------------------------------------------------
    // if necessary, increment line number immediately before save
    // ---------------------------------------------------------------
    @Subscribe
    public void onBeforeSave(BeforeSaveEvent event) {
        Obligation obligation = getEditedEntity();
        if (entityStates.isNew(obligation)) {
            obligation.setLineNumber(obligationService
                    .fetchNextObligationLineNumber(entryBfy, obligation.getDocumentNumber()));
        }
    }

    // ---------------------------------------------------------------
    // After the save, compare and open the projection dialog if changed
    // ---------------------------------------------------------------
    @Subscribe
    public void onAfterSave(AfterSaveEvent event) {
        var obligation = getEditedEntity();
        BigDecimal savedAmount = obligation.getAmount();
        var activity = obligation.getActivity();
        var objectClass = obligation.getObjectClass();

        BigDecimal delta = originalAmount.subtract(savedAmount);

        if (delta.compareTo(BigDecimal.ZERO) != 0) {
            var projection = fetchActivityProjection(activity, objectClass);
            openProjectionDialog(projection, delta);
        }
    }

    private void openProjectionDialog(ActivityProjection projection, BigDecimal delta) {
        var dialog = dialogWindows.detail(this, ActivityProjection.class)
                .withViewClass(ActivityProjectionUpdateView.class)
                .editEntity(projection)
                .withAfterCloseListener(e -> {
                    if (e.closedWith(StandardOutcome.SAVE)) {
                        // react to saved changes
                    }
                })
                .build();

        dialog.getView().setAdjustment(delta);
        dialog.open();
    }

    @Autowired
    private DataManager dataManager;

    Activity getGenericActivity(Activity activity) {
        return activity.getGroup() == null ? null : getActivity(activity.getDivision(),
                activity.getGroup().getGroupCode().concat("00"));
    }

    Activity getActivity(Division division, String activityNumber) {
        return dataManager.load(Activity.class)
                .query("SELECT a FROM fis_Activity a"
                        + " WHERE a.division = :division AND a.activityNumber = :activityNumber")
                .parameter("division", division)
                .parameter("activityNumber", activityNumber)
                .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE)
                        .add("fund", FetchPlan.BASE)
                        .add("group", FetchPlan.BASE))
                .optional().orElse(null);
    }

    /**
     * Returns the activity that should contain the budget projection.
     *
     * @param activity any activity
     * @return activity for budgeting
     */
    Activity fetchProjectionActivity(Activity activity) {
        if (activity == null) {
            return null;
        }

        Group group = activity.getGroup();
        if (group == null) {
            return activity;
        }

        String genericActivityNumber = group.getGroupCode() + "00";

        return dataManager.load(Activity.class)
                .query("SELECT e FROM fis_Activity e " +
                        "WHERE e.division = :division AND e.activityNumber = :activityNumber")
                .parameter("division", activity.getDivision())
                .parameter("activityNumber", genericActivityNumber)
                .optional()
                .orElse(activity);
    }

    ActivityProjection fetchActivityProjection(Activity activity, ObjectClass objectClass) {
        Activity projectionActivity = fetchProjectionActivity(activity);
        if (projectionActivity == null || objectClass == null) {
            return null;
        }
        String projectionObjectClass = projectionActivity.getGenericProjection()
                ? objectClass.getBudgetObjectClass().substring(0, 2).concat("00")
                : objectClass.getBudgetObjectClass();

        ObjectClass projectionBoc = dataManager.load(ObjectClass.class)
                .query("SELECT e FROM fis_ObjectClass e "
                        + " WHERE e.objectCategory = :objectCategory"
                        + " AND e.budgetObjectClass = :budgetObjectClass")
                .parameter("objectCategory", objectClass.getObjectCategory())
                .parameter("budgetObjectClass", projectionObjectClass)
                .optional()
                .orElse(objectClass);

        return dataManager.load(ActivityProjection.class)
                .query("SELECT e FROM fis_ActivityProjection e"
                        + " WHERE e.activity = :projectionActivity AND e.objectClass = :projectionObjectClass")
                .parameter("projectionActivity", projectionActivity)
                .parameter("projectionObjectClass", projectionBoc)
                .optional()
                .orElseGet(() -> {
                    ActivityProjection newProjection = dataManager.create(ActivityProjection.class);
                    newProjection.setActivity(projectionActivity);
                    newProjection.setObjectClass(projectionBoc);
                    newProjection.setAmount(BigDecimal.ZERO);
                    return newProjection;
                });
    }
}