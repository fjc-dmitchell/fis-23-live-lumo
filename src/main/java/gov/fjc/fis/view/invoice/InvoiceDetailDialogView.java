package gov.fjc.fis.view.invoice;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Invoice;
import gov.fjc.fis.view.fileattachmentfragment.FileAttachmentFragment;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.EntityStates;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;

@Route(value = "invoices-dialog/:id", layout = MainView.class)
@ViewController(id = "fis_Invoice_dialog.detail")
@ViewDescriptor(path = "invoice-detail-dialog-view.xml")
@EditedEntityContainer("invoiceDc")
public class InvoiceDetailDialogView extends StandardDetailView<Invoice> {
    @Autowired
    private EntityStates entityStates;
    @ViewComponent
    private FileAttachmentFragment attachmentFragment;
    @ViewComponent
    private TypedDatePicker<Date> invoiceDateField;
    @ViewComponent
    private TypedDatePicker<Date> paymentDateField;
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        var invoice = getEditedEntity();
        attachmentFragment.setHostEntity(invoice);
        if (entityStates.isNew(invoice)) {
            invoiceDateField.setValue(LocalDate.now());
            paymentDateField.setValue(LocalDate.now());
        } else {
            createdByString.setText(invoice.getCreatedByString());
        }
    }

    @Subscribe("memoField")
    protected void onMemoFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixTextArea, ?> event) {
        event.getSource().setValue(((String) event.getValue()).trim());
    }
}