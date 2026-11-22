package gov.fjc.fis.view.search;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import gov.fjc.fis.entity.ObjectCategory;
import gov.fjc.fis.entity.ObjectClass;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.*;

import java.util.Map;

/**
 * Provides obligation-specific search criteria for
 * {@link CustomSearchFragment}.
 * <p>
 * The fragment allows obligations to be filtered by document number, vendor,
 * activity number, amount, status, blanket purchase order, program dates,
 * travel dates, and document type. It also incorporates the common object
 * category and object class fields supplied by the parent search fragment.
 * </p>
 * <p>
 * Saved filter values and editable comparison operations are restored through
 * {@link #setPropertyFilters(Map)}. Obligation status values are displayed as
 * {@code Open} and {@code Closed}.
 * </p>
 * @author Doug Mitchell
 */
@FragmentDescriptor("obligation-search-fragment.xml")
public class ObligationSearchFragment extends EntitySearchFragment {

    @ViewComponent
    private HorizontalLayout mocBox;
    @ViewComponent
    private HorizontalLayout bocBox;
    @ViewComponent
    private PropertyFilter<Object> docnumSearch;
    @ViewComponent
    private PropertyFilter<Object> vendorSearch;
    @ViewComponent
    private PropertyFilter<Object> actnumSearch;
    @ViewComponent
    private PropertyFilter<Object> amountSearch;
    @ViewComponent
    private PropertyFilter<Boolean> statusSearch;
    @ViewComponent
    private PropertyFilter<Object> bpoSearch;
    @ViewComponent
    private PropertyFilter<Object> programStartSearch;
    @ViewComponent
    private PropertyFilter<Object> programEndSearch;
    @ViewComponent
    private PropertyFilter<Object> travelStartSearch;
    @ViewComponent
    private PropertyFilter<Object> travelEndSearch;
    @ViewComponent
    private PropertyFilter<Object> docTypeSearch;

    /**
     * Configures the obligation status filter to display boolean values as
     * {@code Open} and {@code Closed}.
     */
    @Override
    protected void additionalFragmentActions() {
        ((JmixSelect<Boolean>) statusSearch.getValueComponent())
                .setItemLabelGenerator(status -> status == null ? "" : status ? "Open" : "Closed");
    }

    /**
     * Restores saved obligation search values and comparison operations.
     * <p>
     * Entries are matched to the entity property searched by each filter.
     * Operation entries are identified by the corresponding property name
     * followed by {@code _op}. Unrecognized entries are ignored because the map
     * may also contain common search parameters managed by
     * {@link CustomSearchFragment}.
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
                case "documentNumber" -> docnumSearch.setValue(value);
                case "vendor" -> vendorSearch.setValue(value);
                case "activity.activityNumber" -> actnumSearch.setValue(value);
                case "amount" -> amountSearch.setValue(value);
                case "amount_op" -> amountSearch.setOperation((PropertyFilter.Operation) value);
                case "status" -> statusSearch.setValue((Boolean) value);
                case "blanketPurchaseOrder" -> bpoSearch.setValue(value);
                case "activity.startDate" -> programStartSearch.setValue(value);
                case "activity.startDate_op" -> programStartSearch.setOperation((PropertyFilter.Operation) value);
                case "activity.endDate" -> programEndSearch.setValue(value);
                case "activity.endDate_op" -> programEndSearch.setOperation((PropertyFilter.Operation) value);
                case "travelStartDate" -> travelStartSearch.setValue(value);
                case "travelStartDate_op" -> travelStartSearch.setOperation((PropertyFilter.Operation) value);
                case "travelEndDate" -> travelEndSearch.setValue(value);
                case "travelEndDate_op" -> travelEndSearch.setOperation((PropertyFilter.Operation) value);
                case "documentType" -> docTypeSearch.setValue(value);
            }
        }
    }

    /**
     * Adds the common object-category and object-class search fields to the
     * corresponding obligation search layouts.
     *
     * @param categorySearchField the object-category search field
     * @param objectClassSearchField the object-class search field
     */
    @Override
    public void attachCategoryObjectClassFields(EntityComboBox<ObjectCategory> categorySearchField,
                                                EntityComboBox<ObjectClass> objectClassSearchField) {
        mocBox.add(categorySearchField);
        bocBox.add(objectClassSearchField);
    }
}