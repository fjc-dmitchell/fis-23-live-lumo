package gov.fjc.fis.view.appropriationadjustment;

import com.vaadin.flow.component.html.Paragraph;
import gov.fjc.fis.entity.AppropriationAdjustment;

import gov.fjc.fis.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Route(value = "appropriationAdjustments/:id", layout = MainView.class)
@ViewController("fis_AppropriationAdjustment.detail")
@ViewDescriptor("appropriation-adjustment-detail-view.xml")
@EditedEntityContainer("appropriationAdjustmentDc")
public class AppropriationAdjustmentDetailView extends StandardDetailView<AppropriationAdjustment> {
    @Autowired
    private EntityStates entityStates;
    @ViewComponent
    private TypedDatePicker<LocalDate> adjustmentDateField;
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            adjustmentDateField.setValue(LocalDate.now());
        }
        createdByString.setText(getEditedEntity().getCreatedByString());
    }
}