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

/**
 * Provides file attachment-specific search controls for
 * {@link CustomSearchFragment}.
 * <p>
 * Unlike most entity search fragments, this fragment does not define
 * additional property filters. Instead, it provides layout locations for
 * common search controls supplied by the parent search fragment, including
 * branch, group, and file attachment category fields.
 * </p>
 * <p>
 * Because all file attachment search criteria are managed by the parent
 * fragment, no entity-specific filter state needs to be restored.
 * </p>
 * @author Doug Mitchell
 */
@FragmentDescriptor("file-attachment-search-fragment.xml")
public class FileAttachmentSearchFragment extends EntitySearchFragment {
    @ViewComponent
    private HorizontalLayout branchBox;
    @ViewComponent
    private HorizontalLayout groupBox;
    @ViewComponent
    private HorizontalLayout fileCategoryBox;

    /**
     * Restores entity-specific search criteria.
     * <p>
     * File attachment searches do not currently define any additional
     * property-filter criteria beyond those managed by the parent search
     * fragment, so this implementation performs no action.
     * </p>
     *
     * @param filters the saved search parameters
     */
    @Override
    public void setPropertyFilters(Map<String, Object> filters) {
        // no search property filters to add
    }

    /**
     * Attaches the common branch and group search fields to the file attachment
     * search layout.
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

    /**
     * Attaches the common file attachment category search field to the file
     * attachment search layout.
     *
     * @param fileCategorySearchField the file attachment category search field
     */
    @Override
    public void attachFileCategoryField(JmixMultiSelectComboBoxPicker<FileAttachmentCategory> fileCategorySearchField) {
        fileCategoryBox.add(fileCategorySearchField);
    }
}