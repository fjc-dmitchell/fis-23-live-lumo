package gov.fjc.fis.view.fileattachmentcategory;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.FileAttachmentCategory;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;


@Route(value = "file-attachment-categories", layout = MainView.class)
@ViewController(id = "fis_FileAttachmentCategory.list")
@ViewDescriptor(path = "file-attachment-category-list-view.xml")
@LookupComponent("fileAttachmentCategoriesDataGrid")
@DialogMode(width = "64em")
public class FileAttachmentCategoryListView extends StandardListView<FileAttachmentCategory> {
}