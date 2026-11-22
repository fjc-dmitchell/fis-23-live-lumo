package gov.fjc.fis.view.search;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gov.fjc.fis.entity.*;
import io.jmix.core.querycondition.Condition;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

import java.util.*;

/**
 * Base class for entity-specific search fragments used by
 * {@link CustomSearchFragment}.
 * <p>
 * A subclass defines the additional {@link PropertyFilter} components needed
 * to search a particular entity type. This base class discovers those filters,
 * preserves their initial operations, exposes their query conditions to the
 * parent fragment, and provides common support for clearing and restoring
 * search criteria.
 * </p>
 * <p>
 * Subclasses must implement {@link #setPropertyFilters(Map)} to restore saved
 * filter values and operations. They may override
 * {@link #additionalFragmentActions()} to perform configuration after the
 * property filters have been discovered.
 * </p>
 * <p>
 * The optional {@code add...} methods allow a subclass to place common search
 * components owned by {@link CustomSearchFragment} within its own layout.
 * Their default implementations perform no action.
 * </p>
 * @author Doug Mitchell
 */
@FragmentDescriptor("entity-search-fragment.xml")
public abstract class EntitySearchFragment extends Fragment<VerticalLayout> {
    @ViewComponent
    private VerticalLayout root;

    protected final List<PropertyFilter<?>> propertyFilters = new ArrayList<>();
    protected final Map<PropertyFilter<?>, PropertyFilter.Operation> defaultOperations = new HashMap<>();

    /**
     * Discovers the property filters contained in the fragment and records their
     * initial operations before invoking entity-specific initialization.
     *
     * @param event the fragment ready event
     */
    @Subscribe
    protected void onReady(final ReadyEvent event) {
        for (var component : UiComponentUtils.getComponents(root)) {
            if (component instanceof PropertyFilter<?> propertyFilter) {
//                addFilterCondition(propertyFilter);
                propertyFilters.add(propertyFilter);
                defaultOperations.put(propertyFilter, propertyFilter.getOperation());
            }
        }
        additionalFragmentActions();
    }

    /**
     * Performs entity-specific initialization after the fragment's property
     * filters have been discovered.
     * <p>
     * The default implementation performs no action.
     * </p>
     */
    protected void additionalFragmentActions() {
        // subclass may need to perform additional actions
    }

    /**
     * Returns the query conditions produced by the property filters contained in
     * this fragment.
     *
     * @return a list containing the current property-filter conditions
     */
    public final List<Condition> getPropertyFilterConditions() {
        List<Condition> propertyFilterConditions = new ArrayList<>();
        for(var component: UiComponentUtils.getComponents(root)) {
            if(component instanceof PropertyFilter<?> propertyFilter) {
                propertyFilterConditions.add(propertyFilter.getQueryCondition());

            }
        }
        return propertyFilterConditions;
    }

    /**
     * Clears all property-filter values and restores each filter's initial
     * operation.
     */
    public void clearPropertyFilters() {
        for (PropertyFilter<?> filter : propertyFilters) {
            filter.clear();
            filter.setOperation(defaultOperations.get(filter));
        }
    }

    /**
     * Returns the property filters discovered when the fragment became ready.
     *
     * @return the mutable list of property filters managed by this fragment
     */
    public List<PropertyFilter<?>> getPropertyFilters() {
//        return propertyFilters;
        return Collections.unmodifiableList(propertyFilters);
    }

    /**
     * Restores the entity-specific property-filter values and operations from the
     * supplied search parameters.
     *
     * @param filters the saved search parameters, keyed by entity property name;
     *                operation entries use the property name followed by
     *                {@code _op}
     */
    public abstract void setPropertyFilters(Map<String, Object> filters);

    /**
     * Adds the common object-category and object-class search components to this
     * fragment when supported by the entity-specific layout.
     * <p>
     * The default implementation performs no action.
     * </p>
     *
     * @param categorySearchField the object-category search field
     * @param objectClassSearchField the object-class search field
     */
    public void attachCategoryObjectClassFields(EntityComboBox<ObjectCategory> categorySearchField,
                                                EntityComboBox<ObjectClass> objectClassSearchField) {
        // by default, do nothing. Subclasses can override this method.
    }

    /**
     * Adds the common branch and group search components to this fragment when
     * supported by the entity-specific layout.
     * <p>
     * The default implementation performs no action.
     * </p>
     *
     * @param branchSearchField the branch search field
     * @param groupSearchField the group search field
     */
    public void attachBranchGroupFields(EntityComboBox<Branch> branchSearchField,
                                        EntityComboBox<Group> groupSearchField) {
        // by default, do nothing. Subclasses can override this method.
    }

    /**
     * Adds the common file attachment category search component to this fragment
     * when supported by the entity-specific layout.
     * <p>
     * The default implementation performs no action.
     * </p>
     *
     * @param fileCategorySearchField the file attachment category search field
     */
    public void attachFileCategoryField(JmixMultiSelectComboBoxPicker<FileAttachmentCategory> fileCategorySearchField) {
        // by default, do nothing. Subclasses can override this method.
    }
}