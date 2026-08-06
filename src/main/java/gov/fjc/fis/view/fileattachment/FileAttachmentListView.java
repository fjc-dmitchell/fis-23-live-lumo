package gov.fjc.fis.view.fileattachment;

import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.FileAttachment;
import gov.fjc.fis.event.SearchGridSelectedItemsEvent;
import gov.fjc.fis.view.main.MainView;
import gov.fjc.fis.view.search.CustomSearchFragment;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "file-attachments", layout = MainView.class)
@ViewController(id = "fis_FileAttachment.list")
@ViewDescriptor(path = "file-attachment-list-view.xml")
@LookupComponent("fileAttachmentsDataGrid")
@DialogMode(width = "64em")
public class FileAttachmentListView extends StandardListView<FileAttachment> {

    @Autowired
    private UiEventPublisher uiEventPublisher;
    @ViewComponent
    private CustomSearchFragment searchFragment;
    @ViewComponent
    private DataGrid<FileAttachment> fileAttachmentsDataGrid;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        searchFragment.setDataGrid(fileAttachmentsDataGrid);
    }

    @Subscribe("fileAttachmentsDataGrid")
    protected void onFileAttachmentsDataGridSelection(final SelectionEvent<DataGrid<FileAttachment>, FileAttachment> event) {
        uiEventPublisher.publishEvent(new SearchGridSelectedItemsEvent(this, fileAttachmentsDataGrid, event.getAllSelectedItems().size()));
    }
}