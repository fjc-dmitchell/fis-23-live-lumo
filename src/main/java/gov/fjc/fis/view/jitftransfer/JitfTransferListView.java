package gov.fjc.fis.view.jitftransfer;

import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.JitfTransfer;
import gov.fjc.fis.service.AppropriationService;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "jitf-transfers", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_JitfTransfer.list")
@ViewDescriptor(path = "jitf-transfer-list-view.xml")
@LookupComponent("jitfTransfersDataGrid")
@DialogMode(width = "64em")
public class JitfTransferListView extends StandardListView<JitfTransfer> {
    @Autowired
    private AppropriationService appropriationService;
    @ViewComponent
    private JmixButton removeBtn;

    @Subscribe("jitfTransfersDataGrid")
    protected void onJitfTransfersDataGridSelection(final SelectionEvent<DataGrid<JitfTransfer>, JitfTransfer> event) {
        event.getFirstSelectedItem().ifPresentOrElse(
                item -> removeBtn.setEnabled(appropriationService.isAppropriationOpen(item)),
                () -> removeBtn.setEnabled(false)
        );
    }
}