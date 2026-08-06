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

    @Override
    protected void additionalFragmentActions() {
        ((JmixSelect<Boolean>) statusSearch.getValueComponent())
                .setItemLabelGenerator(status -> status == null ? "" : status ? "Open" : "Closed");
    }

    @Override
    public void setPropertyFilters(Map<String, Object> filters) {
        for (var entry : filters.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            switch (key) {
                case "documentNumber":
                    docnumSearch.setValue(value);
                    break;
                case "vendor":
                    vendorSearch.setValue(value);
                    break;
                case "activity.activityNumber":
                    actnumSearch.setValue(value);
                    break;
                case "amount":
                    amountSearch.setValue(value);
                    break;
                case "amount_op":
                    amountSearch.setOperation((PropertyFilter.Operation) value);
                    break;
                case "status":
                    statusSearch.setValue((Boolean) value);
                    break;
                case "blanketPurchaseOrder":
                    bpoSearch.setValue(value);
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
                case "travelStartDate":
                    travelStartSearch.setValue(value);
                    break;
                case "travelStartDate_op":
                    travelStartSearch.setOperation((PropertyFilter.Operation) value);
                    break;
                case "travelEndDate":
                    travelEndSearch.setValue(value);
                    break;
                case "travelEndDate_op":
                    travelEndSearch.setOperation((PropertyFilter.Operation) value);
                    break;
                case "documentType":
                    docTypeSearch.setValue(value);
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
}