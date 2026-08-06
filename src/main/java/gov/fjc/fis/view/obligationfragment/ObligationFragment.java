package gov.fjc.fis.view.obligationfragment;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gov.fjc.fis.entity.*;
import gov.fjc.fis.service.ObligationService;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@FragmentDescriptor("obligation-fragment.xml")
public class ObligationFragment extends Fragment<VerticalLayout> {
    @Autowired
    private ObligationService obligationService;
    @ViewComponent
    private CollectionLoader<Obligation> obligationsDl;
    @ViewComponent
    private DataGrid<Obligation> obligationsDataGrid;
    private Object entity;
    private List<Appropriation> appropriations;
    private String vendorCode;
    private String addressCode;


    public void setEntity(Object entity) {
        this.entity = entity;
        obligationsDl.load();
    }

//    public void setVendorEntityFields(Object entity, List<Appropriation> appropriations, String vendorCode, String addressCode) {
//        this.entity = entity;
//        this.appropriations = appropriations;
//        this.vendorCode = vendorCode;
//        this.addressCode = addressCode;
//
//        obligationsDataGrid.setEmptyStateText("No obligations match the Vendor Code and Address Code for the selected fiscal years");
//        obligationsDl.load();
//    }

    @Install(to = "obligationsDl", target = Target.DATA_LOADER)
    protected List<Obligation> obligationsDlLoadDelegate(final LoadContext<Obligation> loadContext) {
        List<Obligation> obligations = new ArrayList<>();
        switch (entity.getClass().getSimpleName()) {
            case "Group" -> obligations = obligationService.getObligations((Group) entity);
            case "Branch" -> obligations = obligationService.getObligations((Branch) entity);
            case "Vendor" -> obligations = obligationService.getObligations((Vendor) entity);
        }
        return obligations;
    }
}