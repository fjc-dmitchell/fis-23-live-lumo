package gov.fjc.fis.view.fileattachmentcategory;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.FileAttachmentCategory;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;

@Route(value = "file-attachment-categories/:id", layout = MainView.class)
@ViewController(id = "fis_FileAttachmentCategory.detail")
@ViewDescriptor(path = "file-attachment-category-detail-view.xml")
@EditedEntityContainer("fileAttachmentCategoryDc")
public class FileAttachmentCategoryDetailView extends StandardDetailView<FileAttachmentCategory> {
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        createdByString.setText(getEditedEntity().getCreatedByString());
    }
}