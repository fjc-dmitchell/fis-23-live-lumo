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

    @Override
    protected void additionalFragmentActions() {
        ((JmixSelect<Boolean>) obligationStatusSearch.getValueComponent())
                .setItemLabelGenerator(status -> status == null ? "" : status ? "Open" : "Closed");
    }

    @Override
    public void setPropertyFilters(Map<String, Object> filters) {
        for (var entry : filters.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            switch (key) {
                case "amount":
                    fcnAmountSearch.setValue(value);
                    break;
                case "amount_op":
                    fcnAmountSearch.setOperation((PropertyFilter.Operation) value);
                    break;
                case "fcnDate":
                    fcnDateSearch.setValue(value);
                    break;
                case "fcnDate_op":
                    fcnDateSearch.setOperation((PropertyFilter.Operation) value);
                    break;
                case "obligation.documentNumber":
                    obligationDocnumSearch.setValue(value);
                    break;
                case "obligation.vendor":
                    obligationVendorSearch.setValue(value);
                    break;
                case "obligation.status":
                    obligationStatusSearch.setValue((Boolean) value);
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