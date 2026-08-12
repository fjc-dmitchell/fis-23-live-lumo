package gov.fjc.fis.view.dashboard.reconciliationdashboard;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.event.FiscalYearChangeEvent;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.report.ReconciliationReportService;
import gov.fjc.fis.view.main.MainView;
import gov.fjc.fis.view.reconciliationfragment.ReconciliationFragment;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

@Route(value = "reconciliation-dashboard", layout = MainView.class)
@ViewController(id = "fis_ReconciliationDashboardView")
@ViewDescriptor(path = "reconciliation-dashboard-view.xml")
public class ReconciliationDashboardView extends StandardView {
    @Autowired
    private SessionData sessionData;
    @ViewComponent
    private CollectionLoader<Appropriation> appropriationsDl;
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private ReconciliationReportService reconciliationReportService;
    @Autowired
    private Downloader downloader;
    @ViewComponent
    private EntityComboBox<Appropriation> appropriationsComboBox;
    Appropriation appropriation;

    @ViewComponent
    private ReconciliationFragment reconFragment;

    @Subscribe
    protected void onInit(final InitEvent event) {
        reconFragment.setApplicationContext(getApplicationContext());
        appropriationRefresh();
        appropriationsDl.load();
    }

    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    protected List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return appropriationService.getAppropriations();
    }

    @Subscribe("appropriationsComboBox")
    protected void onAppropriationsComboBoxComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {
        if (event.getValue() == null) {
            appropriationsComboBox.setValue(event.getOldValue());
        }
        appropriation = appropriationsComboBox.getValue();
        reconFragment.setAppropriation(appropriation);
    }

    private void appropriationRefresh() {
        Appropriation bfyEntry = appropriationService.getBfyEntryAppropriation(sessionData);
        if (bfyEntry != appropriation) {
            appropriation = bfyEntry;
            appropriationsComboBox.setValue(appropriation);
            //setTitles()
        }
    }

    @Async
    @EventListener
    public void handleAsyncEvent(FiscalYearChangeEvent event) {
        appropriationRefresh();
    }

    @Subscribe(id = "downloadButton", subject = "clickListener")
    public void onDownloadButtonClick(final ClickEvent<JmixButton> event) {
        byte[] content = reconciliationReportService.generateReportBytes(appropriation);
        String fileName = reconciliationReportService.getFilename(appropriation);
        downloader.download(content, fileName);
    }
}