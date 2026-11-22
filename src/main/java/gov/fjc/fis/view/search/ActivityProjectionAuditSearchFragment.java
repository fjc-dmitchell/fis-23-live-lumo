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

/**
 * Provides activity projection audit-specific search criteria for
 * {@link CustomSearchFragment}.
 * <p>
 * The fragment extends the common search functionality with filters for
 * activity number, activity title, previous projection amount, new
 * projection amount, training status, cancellation status, and activity
 * start and end dates.
 * </p>
 * <p>
 * It also incorporates the common object category, object class, branch,
 * and group search fields supplied by the parent search fragment.
 * </p>
 * <p>
 * Saved filter values and comparison operations are restored through
 * {@link #setPropertyFilters(Map)} when search criteria are reloaded from
 * the user session.
 * </p>
 * @author Doug Mitchell
 */
@FragmentDescriptor("activity-projection-audit-search-fragment.xml")
public class ActivityProjectionAuditSearchFragment extends EntitySearchFragment {
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
    private PropertyFilter<Object> oldAmountSearch;
    @ViewComponent
    private PropertyFilter<Object> newAmountSearch;

    /**
     * Restores saved activity projection audit search values and comparison
     * operations.
     * <p>
     * Entries are matched to the entity property searched by each filter.
     * Operation entries are identified by the corresponding property name
     * followed by {@code _op}. Unrecognized entries are ignored because the
     * map may contain parameters managed by the parent search fragment.
     * </p>
     *
     * @param filters the saved search parameters to restore
     */
    @Override
    public void setPropertyFilters(Map<String, Object> filters) {
        for (var entry : filters.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            switch (key) {
                case "activity.activityNumber" -> actnumSearch.setValue(value);
                case "activity.title" -> titleSearch.setValue(value);
                case "newAmount" -> newAmountSearch.setValue(value);
                case "newAmount_op" -> newAmountSearch.setOperation((PropertyFilter.Operation) value);
                case "oldAmount" -> oldAmountSearch.setValue(value);
                case "oldAmount_op" -> oldAmountSearch.setOperation((PropertyFilter.Operation) value);
                case "activity.trainingProject" -> trainingSearch.setValue(value);
                case "activity.canceled" -> canceledProgramSearch.setValue(value);
                case "activity.startDate" -> programStartSearch.setValue(value);
                case "activity.startDate_op" -> programStartSearch.setOperation((PropertyFilter.Operation) value);
                case "activity.endDate" -> programEndSearch.setValue(value);
                case "activity.endDate_op" -> programEndSearch.setOperation((PropertyFilter.Operation) value);
            }
        }
    }

    /**
     * Attaches the common object category and object class search fields to the
     * activity projection audit search layout.
     *
     * @param categorySearchField the object category search field
     * @param objectClassSearchField the object class search field
     */
    @Override
    public void attachCategoryObjectClassFields(EntityComboBox<ObjectCategory> categorySearchField,
                                                EntityComboBox<ObjectClass> objectClassSearchField) {
        mocBox.add(categorySearchField);
        bocBox.add(objectClassSearchField);
    }

    /**
     * Attaches the common branch and group search fields to the activity
     * projection audit search layout.
     *
     * @param branchSearchField the branch search field
     * @param groupSearchField the group search field
     */
    @Override
    public void attachBranchGroupFields(EntityComboBox<Branch> branchSearchField,
                                        EntityComboBox<Group> groupSearchField) {
        branchBox.add(branchSearchField);
        groupBox.add(groupSearchField);
    }
}
