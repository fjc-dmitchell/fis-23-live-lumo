package gov.fjc.fis.view.search;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import gov.fjc.fis.entity.ObjectCategory;
import gov.fjc.fis.entity.ObjectClass;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.ViewComponent;

import java.util.Map;

/**
 * Provides Fund Control Notice (FCN) specific search criteria for
 * {@link CustomSearchFragment}.
 * <p>
 * The fragment extends the common search functionality with filters for
 * FCN amount, FCN date, obligation document number, obligation vendor,
 * obligation status, and activity number.
 * </p>
 * <p>
 * It also incorporates the common object category and object class search
 * fields supplied by the parent search fragment.
 * </p>
 * <p>
 * Saved filter values and comparison operations are restored through
 * {@link #setPropertyFilters(Map)} when search criteria are reloaded from
 * the user session.
 * </p>
 * @author Doug Mitchell
 */
@FragmentDescriptor("fcn-search-fragment.xml")
public class FcnSearchFragment extends EntitySearchFragment {

    @ViewComponent
    private HorizontalLayout mocBox;
    @ViewComponent
    private HorizontalLayout bocBox;
    @ViewComponent
    private PropertyFilter<Object> fcnAmountSearch;
    @ViewComponent
    private PropertyFilter<Object> fcnDateSearch;
    @ViewComponent
    private PropertyFilter<Object> obligationDocnumSearch;
    @ViewComponent
    private PropertyFilter<Object> obligationVendorSearch;
    @ViewComponent
    private PropertyFilter<Boolean> obligationStatusSearch;
    @ViewComponent
    private PropertyFilter<Object> actnumSearch;

    /**
     * Configures the obligation status filter to display boolean values as
     * {@code Open} and {@code Closed}.
     */
    @Override
    protected void additionalFragmentActions() {
        ((JmixSelect<Boolean>) obligationStatusSearch.getValueComponent())
                .setItemLabelGenerator(status -> status == null ? "" : status ? "Open" : "Closed");
    }

    /**
     * Restores saved FCN search values and comparison operations.
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
                case "amount" -> fcnAmountSearch.setValue(value);
                case "amount_op" -> fcnAmountSearch.setOperation((PropertyFilter.Operation) value);
                case "fcnDate" -> fcnDateSearch.setValue(value);
                case "fcnDate_op" -> fcnDateSearch.setOperation((PropertyFilter.Operation) value);
                case "obligation.documentNumber" -> obligationDocnumSearch.setValue(value);
                case "obligation.vendor" -> obligationVendorSearch.setValue(value);
                case "obligation.status" -> obligationStatusSearch.setValue((Boolean) value);
                case "obligation.activity.activityNumber" -> actnumSearch.setValue(value);
            }
        }
    }

    /**
     * Attaches the common object category and object class search fields to the
     * FCN search layout.
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
}