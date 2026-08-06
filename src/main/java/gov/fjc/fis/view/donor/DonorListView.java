package gov.fjc.fis.view.donor;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Donor;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;
import org.springframework.lang.NonNull;

@Route(value = "donors", layout = MainView.class)
@ViewController("fis_Donor.list")
@ViewDescriptor("donor-list-view.xml")
@LookupComponent("donorsDataGrid")
@DialogMode(width = "64em")
public class DonorListView extends StandardListView<Donor> {
    @Override
    @NonNull
    public String getPageTitle() {
        return "FJC Foundation: ".concat(super.getPageTitle());
    }
}