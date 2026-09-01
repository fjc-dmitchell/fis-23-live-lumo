package gov.fjc.fis.view.fileattachment;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.FileAttachment;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;

@Route(value = "file-attachments/:id", layout = MainView.class)
@ViewController(id = "fis_FileAttachment.detail")
@ViewDescriptor(path = "file-attachment-detail-view.xml")
@EditedEntityContainer("fileAttachmentDc")
public class FileAttachmentDetailView extends StandardDetailView<FileAttachment> {
    @ViewComponent
    private FormLayout.FormItem obligationItem;
    @ViewComponent
    private FormLayout.FormItem invoiceItem;
    @ViewComponent
    private FormLayout.FormItem fcnItem;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        var attachment = getEditedEntity();
        obligationItem.setVisible(attachment.getObligation() != null);
        invoiceItem.setVisible(attachment.getInvoice() != null);
        fcnItem.setVisible(attachment.getFundControlNotice() != null);
    }
}