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

/**
 * Provides activity-specific search criteria for
 * {@link CustomSearchFragment}.
 * <p>
 * The fragment extends the common search functionality with filters for
 * activity number, title, location, projected and obligated amounts,
 * reimbursement totals, training and plan indicators, cancellation status,
 * initial projections, and program dates.
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
 *
 * @author Doug Mitchell
 */
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

    /**
     * Restores saved activity search values and comparison operations.
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
                case "activityNumber" -> actnumSearch.setValue(value);
                case "title" -> titleSearch.setValue(value);
                case "city" -> citySearch.setValue(value);
                case "state" -> stateSearch.setValue(value);
                case "projectedAmount" -> projectionsSearch.setValue(value);
                case "projectedAmount_op" -> projectionsSearch.setOperation((PropertyFilter.Operation) value);
                case "reimbursedAmount" -> reimbursementsSearch.setValue(value);
                case "reimbursedAmount_op" -> reimbursementsSearch.setOperation((PropertyFilter.Operation) value);
                case "obligatedAmount" -> obligationsSearch.setValue(value);
                case "obligatedAmount_op" -> obligationsSearch.setOperation((PropertyFilter.Operation) value);
                case "trainingProject" -> trainingSearch.setValue(value);
                case "canceled" -> canceledProgramSearch.setValue(value);
                case "addedToPlan" -> planSearch.setValue(value);
                case "startDate" -> programStartSearch.setValue(value);
                case "startDate_op" -> programStartSearch.setOperation((PropertyFilter.Operation) value);
                case "endDate" -> programEndSearch.setValue(value);
                case "endDate_op" -> programEndSearch.setOperation((PropertyFilter.Operation) value);
                case "initialProjection" -> initProjSearch.setValue(value);
                case "initialProjection_op" -> initProjSearch.setOperation((PropertyFilter.Operation) value);
            }
        }
    }

    /**
     * Attaches the common object category and object class search fields to the
     * activity search layout.
     *
     * @param categorySearchField    the object category search field
     * @param objectClassSearchField the object class search field
     */
    @Override
    public void attachCategoryObjectClassFields(EntityComboBox<ObjectCategory> categorySearchField,
                                                EntityComboBox<ObjectClass> objectClassSearchField) {
        mocBox.add(categorySearchField);
        bocBox.add(objectClassSearchField);
    }


    /**
     * Attaches the common branch and group search fields to the activity search
     * layout.
     *
     * @param branchSearchField the branch search field
     * @param groupSearchField  the group search field
     */
    @Override
    public void attachBranchGroupFields(EntityComboBox<Branch> branchSearchField,
                                        EntityComboBox<Group> groupSearchField) {
        branchBox.add(branchSearchField);
        groupBox.add(groupSearchField);
    }

    /**
     * Normalizes the state search value to uppercase to provide consistent
     * searching regardless of user input casing.
     *
     * @param event the value-change event
     */
    @Subscribe("stateSearch")
    protected void onStateSearchComponentValueChange(final AbstractField.ComponentValueChangeEvent<PropertyFilter<?>, ?> event) {
        if (event.getValue() != null) {
            stateSearch.setValue(stateSearch.getValue().toString().toUpperCase());
        }
    }
}