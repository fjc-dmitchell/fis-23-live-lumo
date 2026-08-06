package gov.fjc.fis.view.search;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.FileAttachmentCategory;
import gov.fjc.fis.entity.Group;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.ViewComponent;

import java.util.Map;

@FragmentDescriptor("file-attachment-search-fragment.xml")
public class FileAttachmentSearchFragment extends EntitySearchFragment {
    @ViewComponent
    private HorizontalLayout branchBox;
    @ViewComponent
    private HorizontalLayout groupBox;
    @ViewComponent
    private HorizontalLayout fileCategoryBox;

    @Override
    public void setPropertyFilters(Map<String, Object> filters) {

    }

    @Override
    public void addBranchGroup(EntityComboBox<Branch> branchSearchField,
                               EntityComboBox<Group> groupSearchField) {
        branchBox.add(branchSearchField);
        groupBox.add(groupSearchField);
    }

    @Override
    public void addFileCategory(JmixMultiSelectComboBoxPicker<FileAttachmentCategory> fileCategorySearchField) {
        fileCategoryBox.add(fileCategorySearchField);
    }
}