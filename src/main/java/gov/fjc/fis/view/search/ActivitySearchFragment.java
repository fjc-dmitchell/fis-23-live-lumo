package gov.fjc.fis.view.search;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.ObjectCategory;
import gov.fjc.fis.entity.Group;
import gov.fjc.fis.entity.ObjectClass;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

import java.util.Map;

@FragmentDescriptor("activity-search-fragment.xml")
public class ActivitySearchFragment extends EntitySearchFragment {

    @ViewComponent
    private HorizontalLayout branchBox;
    @ViewComponent
    private HorizontalLayout groupBox;
    @ViewComponent
    private HorizontalLayout mocBox;
    @ViewComponent
    private HorizontalLayout bocBox;
    @ViewComponent
    private PropertyFilter<Object> stateSearch;
    @ViewComponent
    private PropertyFilter<Object> trainingSearch;
    @ViewComponent
    private PropertyFilter<Object> canceledProgramSearch;
    @ViewComponent
    private PropertyFilter<Object> titleSearch;
    @ViewComponent
    private PropertyFilter<Object> programStartSearch;
    @ViewComponent
    private PropertyFilter<Object> programEndSearch;
    @ViewComponent
    private PropertyFilter<Object> planSearch;
    @ViewComponent
    private PropertyFilter<Object> initProjSearch;
    @ViewComponent
    private PropertyFilter<Object> citySearch;
    @ViewComponent
    private PropertyFilter<Object> actnumSearch;
    @ViewComponent
    private PropertyFilter<Object> reimbursementsSearch;
    @ViewComponent
    private PropertyFilter<Object> projectionsSearch;
    @ViewComponent
    private PropertyFilter<Object> obligationsSearch;

    @Override
    public void setPropertyFilters(Map<String, Object> filters) {
        for (var entry : filters.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            switch (key) {
                case "activityNumber":
                    actnumSearch.setValue(value);
                    break;
                case "title":
                    titleSearch.setValue(value);
                    break;
                case "city":
                    citySearch.setValue(value);
                    break;
                case "state":
                    stateSearch.setValue(value);
                    break;
                case "projectedAmount":
                    projectionsSearch.setValue(value);
                    break;
                case "projectedAmount_op":
                    projectionsSearch.setOperation((PropertyFilter.Operation) value);
                    break;
                case "reimbursedAmount":
                    reimbursementsSearch.setValue(value);
                    break;
                case "reimbursedAmount_op":
                    reimbursementsSearch.setOperation((PropertyFilter.Operation) value);
                    break;
                case "obligatedAmount":
                    obligationsSearch.setValue(value);
                    break;
                case "obligatedAmount_op":
                    obligationsSearch.setOperation((PropertyFilter.Operation) value);
                    break;
                case "trainingProject":
                    trainingSearch.setValue(value);
                    break;
                case "canceled":
                    canceledProgramSearch.setValue(value);
                    break;
                case "addedToPlan":
                    planSearch.setValue(value);
                    break;
                case "startDate":
                    programStartSearch.setValue(value);
                    break;
                case "startDate_op":
                    programStartSearch.setOperation((PropertyFilter.Operation) value);
                    break;
                case "endDate":
                    programEndSearch.setValue(value);
                    break;
                case "endDate_op":
                    programEndSearch.setOperation((PropertyFilter.Operation) value);
                    break;
                case "initialProjection":
                    initProjSearch.setValue(value);
                    break;
                case "initialProjection_op":
                    initProjSearch.setOperation((PropertyFilter.Operation) value);
                    break;
            }
        }
    }

    @Override
    public void addCategoryObjectClass(EntityComboBox<ObjectCategory> categorySearchField,
                                       EntityComboBox<ObjectClass> objectClassSearchField) {
        mocBox.add(categorySearchField);
        bocBox.add(objectClassSearchField);
    }

    @Override
    public void addBranchGroup(EntityComboBox<Branch> branchSearchField,
                               EntityComboBox<Group> groupSearchField) {
        branchBox.add(branchSearchField);
        groupBox.add(groupSearchField);
    }

    @Subscribe("stateSearch")
    protected void onStateSearchComponentValueChange(final AbstractField.ComponentValueChangeEvent<PropertyFilter<?>, ?> event) {
        if (event.getValue() != null) {
            stateSearch.setValue(stateSearch.getValue().toString().toUpperCase());
        }
    }
}