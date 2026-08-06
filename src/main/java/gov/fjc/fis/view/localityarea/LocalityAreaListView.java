package gov.fjc.fis.view.localityarea;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.LocalityArea;
import gov.fjc.fis.service.LocalityAreaService;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.LoadContext;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Route(value = "locality-areas", layout = MainView.class)
@ViewController(id = "fis_LocalityArea.list")
@ViewDescriptor(path = "locality-area-list-view.xml")
@LookupComponent("localityAreasDataGrid")
@DialogMode(width = "64em")
public class LocalityAreaListView extends StandardListView<LocalityArea> {
    @Autowired
    private LocalityAreaService localityAreaService;
    @ViewComponent
    private CollectionLoader<LocalityArea> localityAreasDl;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        localityAreasDl.load();
    }

    @Install(to = "localityAreasDl", target = Target.DATA_LOADER)
    protected List<LocalityArea> localityAreasDlLoadDelegate(final LoadContext<LocalityArea> loadContext) {
        return localityAreaService.getCurrentLocalityAreas();
    }
}