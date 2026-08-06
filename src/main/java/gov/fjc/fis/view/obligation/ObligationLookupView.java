package gov.fjc.fis.view.obligation;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.*;
import gov.fjc.fis.service.*;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Route(value = "obligations-lookup", layout = MainView.class)
@ViewController(id = "fis_ObligationLookup")
@ViewDescriptor(path = "obligation-lookup-view.xml")
@LookupComponent("obligationsDataGrid")
@DialogMode(width = "64em")
public class ObligationLookupView extends StandardListView<Obligation> {
    /**
     * services
     */
    @Autowired
    private FundService fundService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ObjectCategoryService categoryService;
    @Autowired
    private ObjectClassService objectClassService;

    @ViewComponent
    private CollectionContainer<ObjectCategory> categoriesDc;
    @ViewComponent
    private CollectionContainer<ObjectClass> objectClassesDc;

    /**
     * data loaders
     */
    @ViewComponent
    private CollectionLoader<Obligation> obligationsDl;
    @ViewComponent
    private CollectionLoader<Division> divisionsDl;
    @ViewComponent
    private CollectionLoader<Activity> activitiesDl;
    @ViewComponent
    private CollectionLoader<ObjectCategory> categoriesDl;
    @ViewComponent
    private CollectionLoader<ObjectClass> objectClassesDl;

    /**
     * view components
     */
    @ViewComponent
    private TypedTextField<String> budgetFiscalYearField;
    @ViewComponent
    private EntityComboBox<Division> divisionSearchField;
    @ViewComponent
    private EntityComboBox<Activity> activitySearchField;
    @ViewComponent
    private EntityComboBox<ObjectCategory> categorySearchField;
    @ViewComponent
    private EntityComboBox<ObjectClass> objectClassSearchField;

    /**
     * instance variables
     */
    private Appropriation appropriation;
    private boolean fjcFoundation;
    private List<Fund> funds;

    public void setFjcFoundation(boolean fjcFoundation) {
        this.fjcFoundation = fjcFoundation;
//       if(fjcFoundation) {
//           divisionsDl.load();
//           activitiesDl.load();
//
//           funds = fundService.getFundSearchList(fjcFoundation);
//           searchObligations();
//
//       }
    }

    public Appropriation getAppropriation() {
        return appropriation;
    }

    public void setAppropriation(Appropriation appropriation) {
        this.appropriation = appropriation;
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        budgetFiscalYearField.setValue(appropriation.getBudgetFiscalYear());
        funds = fundService.fetchFundSearchList(fjcFoundation);
        searchObligations();
        divisionSearchField.focus();
    }

    @Subscribe("divisionSearchField")
    protected void onDivisionSearchFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Division>, Division> event) {
        activitySearchField.setValue(null);
        activitiesDl.load();
        categorySearchField.setValue(null);
        categoriesDl.load();
        objectClassSearchField.setValue(null);
        objectClassesDl.load();
        searchObligations();
    }

    @Subscribe("activitySearchField")
    protected void onActivitySearchFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Activity>, Activity> event) {
        categorySearchField.setValue(null);
        categoriesDl.load();
        objectClassSearchField.setValue(null);
        objectClassesDl.load();
        if (categoriesDc.getItems().size() == 1) {
            categorySearchField.setValue(categoriesDc.getItems().getFirst());
        }
        if (objectClassesDc.getItems().size() == 1) {
            objectClassSearchField.setValue(objectClassesDc.getItems().getFirst());
        }
        searchObligations();
    }

    @Subscribe("categorySearchField")
    protected void onCategorySearchFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<ObjectCategory>, ObjectCategory> event) {
        objectClassSearchField.setValue(null);
        objectClassesDl.load();
        if (objectClassesDc.getItems().size() == 1) {
            objectClassSearchField.setValue(objectClassesDc.getItems().getFirst());
        }
        searchObligations();
    }

    @Subscribe("objectClassSearchField")
    protected void onObjectClassSearchFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<ObjectClass>, ObjectClass> event) {
        searchObligations();
    }

    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    protected List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.getObligationDivisionsForAppropriationFoundation(appropriation, fjcFoundation);
    }

    @Install(to = "activitiesDl", target = Target.DATA_LOADER)
    protected List<Activity> activitiesDlLoadDelegate(final LoadContext<Activity> loadContext) {
        return activityService.getObligationActivities(appropriation, divisionSearchField.getValue(), fjcFoundation);
    }

    @Install(to = "categoriesDl", target = Target.DATA_LOADER)
    protected List<ObjectCategory> categoriesDlLoadDelegate(final LoadContext<ObjectCategory> loadContext) {
        return categoryService.getObligationCategoriesForDivision(appropriation, divisionSearchField.getValue(), activitySearchField.getValue(), fjcFoundation);
    }

    @Install(to = "objectClassesDl", target = Target.DATA_LOADER)
    protected List<ObjectClass> objectClassesDlLoadDelegate(final LoadContext<ObjectClass> loadContext) {
        return objectClassService.getObligationObjectClasses(appropriation, divisionSearchField.getValue(), activitySearchField.getValue(), categorySearchField.getValue(), fjcFoundation);
    }

    @Install(to = "divisionSearchField", subject = "itemLabelGenerator")
    protected Object divisionSearchFieldItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }

    @Install(to = "activitySearchField", subject = "itemLabelGenerator")
    protected Object activitySearchFieldItemLabelGenerator(final Activity activity) {
        return activity.getTitleAndCode();
    }

    @Install(to = "categorySearchField", subject = "itemLabelGenerator")
    protected Object categorySearchFieldItemLabelGenerator(final ObjectCategory category) {
        return category.getTitleAndCode();
    }

    @Install(to = "objectClassSearchField", subject = "itemLabelGenerator")
    protected Object objectClassSearchFieldItemLabelGenerator(final ObjectClass objectClass) {
        return objectClass.getTitleAndCode();
    }

    @Subscribe(id = "clearSearchBtn", subject = "clickListener")
    protected void onClearSearchBtnClick(final ClickEvent<JmixButton> event) {
        Set<String> params = new HashSet<>(obligationsDl.getParameters().keySet());
        params.forEach((k) -> obligationsDl.removeParameter(k));

        obligationsDl.setParameter("appropriationFilterField", appropriation);
        divisionSearchField.setValue(null);
        activitySearchField.setValue(null);
        categorySearchField.setValue(null);
        objectClassSearchField.setValue(null);
    }

    private void searchObligations() {
        obligationsDl.setParameter("appropriationFilterField", appropriation);
        obligationsDl.setParameter("fundFilterField", funds);
        obligationsDl.setParameter("divisionFilterField", divisionSearchField.getValue());
        obligationsDl.setParameter("activityFilterField", activitySearchField.getValue());
        obligationsDl.setParameter("categoryFilterField", categorySearchField.getValue());
        obligationsDl.setParameter("objectClassFilterField", objectClassSearchField.getValue());
        obligationsDl.load();
    }
}