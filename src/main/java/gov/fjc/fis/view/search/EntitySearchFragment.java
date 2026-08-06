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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@FragmentDescriptor("entity-search-fragment.xml")
public abstract class EntitySearchFragment extends Fragment<VerticalLayout> {
    @ViewComponent
    private VerticalLayout root;

//    private final List<Condition> propertyFilterConditions = new ArrayList<>();
    protected final List<PropertyFilter<?>> propertyFilters = new ArrayList<>();

    @Subscribe
    protected void onReady(final ReadyEvent event) {
        for (var component : UiComponentUtils.getComponents(root)) {
            if (component instanceof PropertyFilter<?> propertyFilter) {
//                addFilterCondition(propertyFilter);
                propertyFilters.add(propertyFilter);
            }
        }
        additionalFragmentActions();
    }

//    private void addFilterCondition(PropertyFilter<?> filter) {
////        filter.setAutoApply(false);
////        filter.setLabelPosition(SupportsLabelPosition.LabelPosition.TOP);
//        propertyFilters.add(filter);
////        propertyFilterConditions.add(filter.getQueryCondition());
//    }

    protected void additionalFragmentActions() {
        // subclass may need to perform additional actions
    }

    public final List<Condition> getPropertyFilterConditions() {
        List<Condition> propertyFilterConditions = new ArrayList<>();
        for(var component: UiComponentUtils.getComponents(root)) {
            if(component instanceof PropertyFilter<?> propertyFilter) {
                propertyFilterConditions.add(propertyFilter.getQueryCondition());

            }
        }
        return propertyFilterConditions;
    }

    public void clearPropertyFilters() {
        for (PropertyFilter<?> filter : propertyFilters) {
            filter.clear();
        }
    }

    // new - used to set query params
    public List<PropertyFilter<?>> getPropertyFilters() {
        return propertyFilters;
    }

//    public void setPropertFilters(QueryParameters queryParameters) {
//            Map<String, List<String>> parametersMap = queryParameters.getParameters();
//
//            for(PropertyFilter<?> filter : propertyFilters) {
//               filter.getParent();
//            }
//    }

    public abstract void setPropertyFilters(Map<String, Object> filters);

    public void addCategoryObjectClass(EntityComboBox<ObjectCategory> categorySearchField,
                                       EntityComboBox<ObjectClass> objectClassSearchField) {
        // by default, do nothing. Subclasses can override this method.
    }

    public void addBranchGroup(EntityComboBox<Branch> branchSearchField,
                               EntityComboBox<Group> groupSearchField) {
        // by default, do nothing. Subclasses can override this method.
    }

    public void addFileCategory(JmixMultiSelectComboBoxPicker<FileAttachmentCategory> fileCategorySearchField) {
        // by default, do nothing. Subclasses can override this method.
    }
}