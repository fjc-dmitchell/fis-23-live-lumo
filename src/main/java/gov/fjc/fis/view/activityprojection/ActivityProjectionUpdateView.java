package gov.fjc.fis.view.activityprojection;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.ActivityProjection;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;

import java.math.BigDecimal;

@Route(value = "activity-projection-update/:id", layout = MainView.class)
@ViewController(id = "fis_ActivityProjectionUpdate.detail")
@ViewDescriptor(path = "activity-projection-update-view.xml")
@EditedEntityContainer("activityProjectionDc")
public class ActivityProjectionUpdateView extends StandardDetailView<ActivityProjection> {
    @ViewComponent
    private TypedTextField<BigDecimal> priorProjectionField;
    @ViewComponent
    private TypedTextField<BigDecimal> adjustmentField;
    @ViewComponent
    private TypedTextField<BigDecimal> newProjectionField;

    private BigDecimal adjustmentAmount;

    public void setAdjustment(BigDecimal adjustmentAmount) {
        this.adjustmentAmount = adjustmentAmount;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        var projection = getEditedEntity();
        var priorProjection = projection.getAmount();
        var newProjection = priorProjection.add(adjustmentAmount).max(BigDecimal.ZERO);

        priorProjectionField.setValue(priorProjection.toString());
        adjustmentField.setValue(adjustmentAmount.toString());
        newProjectionField.setValue(newProjection.toString());

        newProjectionField.setAutoselect(true);
    }

    @Subscribe(id = "closeButton", subject = "clickListener")
    public void onCloseButtonClick(final ClickEvent<JmixButton> event) {
        this.close(StandardOutcome.DISCARD);
    }
}