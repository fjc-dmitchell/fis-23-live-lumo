package gov.fjc.fis.view.search;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.Group;
import gov.fjc.fis.entity.ObjectCategory;
import gov.fjc.fis.entity.ObjectClass;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.ViewComponent;

import java.util.Map;

@FragmentDescriptor("activity-projection-search-fragment.xml")
public class ActivityProjectionSearchFragment extends EntitySearchFragment {
    @ViewComponent
    private HorizontalLayout branchBox;
    @ViewComponent
    private HorizontalLayout groupBox;
    @ViewComponent
    private HorizontalLayout mocBox;
    @ViewComponent
    private HorizontalLayout bocBox;

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
    private PropertyFilter<Object> actnumSearch;
    @ViewComponent
    private PropertyFilter<Object> projectionsSearch;

    @Override
    public void setPropertyFilters(Map<String, Object> filters) {
        for (var entry : filters.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            switch (key) {
                case "activity.activityNumber":
                    actnumSearch.setValue(value);
                    break;
                case "activity.title":
                    titleSearch.setValue(value);
                    break;
                case "amount":
                    projectionsSearch.setValue(value);
                    break;
                case "amount_op":
                    projectionsSearch.setOperation((PropertyFilter.Operation) value);
                    break;
                case "activity.trainingProject":
                    trainingSearch.setValue(value);
                    break;
                case "activity.canceled":
                    canceledProgramSearch.setValue(value);
                    break;
                case "activity.startDate":
                    programStartSearch.setValue(value);
                    break;
                case "activity.startDate_op":
                    programStartSearch.setOperation((PropertyFilter.Operation) value);
                    break;
                case "activity.endDate":
                    programEndSearch.setValue(value);
                    break;
                case "activity.endDate_op":
                    programEndSearch.setOperation((PropertyFilter.Operation) value);
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
}
