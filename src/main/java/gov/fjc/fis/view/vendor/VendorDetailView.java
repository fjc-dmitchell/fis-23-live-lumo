package gov.fjc.fis.view.vendor;

import com.vaadin.flow.component.BlurNotifier;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Vendor;
import gov.fjc.fis.view.main.MainView;
import gov.fjc.fis.view.obligationfragment.ObligationFragment;
import io.jmix.core.EntityStates;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Stream;

@Route(value = "vendors/:id", layout = MainView.class)
@ViewController(id = "fis_Vendor.detail")
@ViewDescriptor(path = "vendor-detail-view.xml")
@EditedEntityContainer("vendorDc")
public class VendorDetailView extends StandardDetailView<Vendor> {
    @Autowired
    private EntityStates entityStates;
    @ViewComponent
    private Paragraph createdByString;
    @ViewComponent
    private ObligationFragment obligationFragment;
    @ViewComponent
    private JmixDetails addressDetails;
    @ViewComponent
    private TypedTextField<String> vendorCodeField;
    @ViewComponent
    private TypedTextField<String> addressCodeField;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        Vendor vendor = getEditedEntity();
        if (entityStates.isNew(vendor)) {
            vendor.setActive(true);
        } else {
            vendorCodeField.setReadOnly(true);
            addressCodeField.setReadOnly(true);
            addressDetails.setOpened(hasAddressInfo(vendor));
            createdByString.setText(vendor.getCreatedByString());
        }
        refreshObligations();
    }

    private boolean hasAddressInfo(Vendor vendor) {
        return Stream.of(vendor.getCity(),
                        vendor.getState(),
                        vendor.getAddress1(),
                        vendor.getAddress2(),
                        vendor.getZipCode())
                .anyMatch(s -> s != null && !s.isBlank());
    }

//    @Subscribe("memoField")
//    public void onMemoFieldBlur(final BlurNotifier.BlurEvent<JmixTextArea> event) {
//        JmixTextArea field = event.getSource();
//        String value = field.getValue();
//        if (value != null) {
//            String trimmed = value.trim();
//            if (!trimmed.equals(value)) {
//                field.setValue(trimmed);
//            }
//        }
//    }

    @Subscribe(id = "vendorDc", target = Target.DATA_CONTAINER)
    public void onVendorDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<Vendor> event) {
        if (Set.of("addressCode", "vendorCode").contains(event.getProperty())) {
            refreshObligations();
        }
    }

    private void refreshObligations() {
        obligationFragment.setEntity(getEditedEntity());
    }
}