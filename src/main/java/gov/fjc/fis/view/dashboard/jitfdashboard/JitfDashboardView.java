package gov.fjc.fis.view.dashboard.jitfdashboard;


import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.dto.JitfTransferDto;
import gov.fjc.fis.service.JitfTransferService;
import io.jmix.core.LoadContext;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "jitf-dashboard", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_JitfDashboardView")
@ViewDescriptor(path = "jitf-dashboard-view.xml")
public class JitfDashboardView extends StandardView {
    @Autowired
    private JitfTransferService jitfTransferService;
    @ViewComponent
    private CollectionLoader<JitfTransferDto> jitfTransferDtoesDl;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        jitfTransferDtoesDl.load();
    }

    @Install(to = "jitfTransferDtoesDl", target = Target.DATA_LOADER)
    protected List<JitfTransferDto> jitfTransferDtoesDlLoadDelegate(final LoadContext<JitfTransferDto> loadContext) {
        return jitfTransferService.generateReport();
    }
}