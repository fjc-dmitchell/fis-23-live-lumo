package gov.fjc.fis.view.donor;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Donor;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;

@Route(value = "donors/:id", layout = MainView.class)
@ViewController("fis_Donor.detail")
@ViewDescriptor("donor-detail-view.xml")
@EditedEntityContainer("donorDc")
public class DonorDetailView extends StandardDetailView<Donor> {
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        createdByString.setText(getEditedEntity().getCreatedByString());
    }
}