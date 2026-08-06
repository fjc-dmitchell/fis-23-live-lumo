package gov.fjc.fis.view.fundcontrolnotice;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.FundControlNotice;
import gov.fjc.fis.view.fileattachmentfragment.FileAttachmentFragment;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.EntityStates;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;

@Route(value = "fund-control-notices-dialog/:id", layout = MainView.class)
@ViewController(id = "fis_FundControlNotice_dialog.detail")
@ViewDescriptor(path = "fund-control-notice-detail-dialog-view.xml")
@EditedEntityContainer("fundControlNoticeDc")
public class FundControlNoticeDetailDialogView extends StandardDetailView<FundControlNotice> {
    @Autowired
    private EntityStates entityStates;
    @ViewComponent
    private FileAttachmentFragment attachmentFragment;
    @ViewComponent
    private TypedDatePicker<Date> fcnDateField;
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        var fcn = getEditedEntity();
        attachmentFragment.setHostEntity(fcn);
        if (entityStates.isNew(fcn)) {
            fcnDateField.setValue(LocalDate.now());
        } else {
            createdByString.setText(fcn.getCreatedByString());
        }
    }

    @Subscribe("memoField")
    protected void onMemoFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixTextArea, ?> event) {
        event.getSource().setValue(((String) event.getValue()).trim());
    }
}