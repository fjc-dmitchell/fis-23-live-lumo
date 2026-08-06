package gov.fjc.fis.view.localityarea;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.LocalityArea;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;

@Route(value = "locality-areas/:id", layout = MainView.class)
@ViewController(id = "fis_LocalityArea.detail")
@ViewDescriptor(path = "locality-area-detail-view.xml")
@EditedEntityContainer("localityAreaDc")
public class LocalityAreaDetailView extends StandardDetailView<LocalityArea> {
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        createdByString.setText(getEditedEntity().getCreatedByString());
    }
}