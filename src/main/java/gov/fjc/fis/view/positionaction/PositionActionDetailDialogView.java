package gov.fjc.fis.view.positionaction;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.service.PayPeriodService;
import gov.fjc.fis.entity.personnel.ActionCode;
import gov.fjc.fis.entity.personnel.PayPeriod;
import gov.fjc.fis.entity.personnel.PositionAction;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.EntityStates;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.textfield.JmixBigDecimalField;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Route(value = "position-action-dialog/:id", layout = MainView.class)
@ViewController(id = "fis_PositionAction_dialog.detail")
@ViewDescriptor(path = "position-action-detail-dialog-view.xml")
@EditedEntityContainer("positionActionDc")
@DialogMode(width = "40em")
public class PositionActionDetailDialogView extends StandardDetailView<PositionAction> {
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private PayPeriodService payPeriodService;

    @ViewComponent
    private TypedDatePicker<Date> effectiveDateField;
    @ViewComponent
    private JmixBigDecimalField stdHoursField;
    @ViewComponent
    private JmixBigDecimalField hourlyRtField;
    @ViewComponent
    private JmixBigDecimalField totalPayField;
    @ViewComponent
    private JmixBigDecimalField annualRtField;

    @ViewComponent
    private Paragraph createdByString;

    PayPeriod currentPayPeriod;

    private static final BigDecimal HOURS_PER_YEAR = BigDecimal.valueOf(2080);
    private static final BigDecimal WEEKS_PER_YEAR = BigDecimal.valueOf(52);

    private boolean adjustingRate = true;

    @Subscribe
    public void onInit(final InitEvent event) {
        currentPayPeriod = payPeriodService.fetchCurrentPayPeriod();

        hourlyRtField.addValueChangeListener(this::onHourlyRateChanged);
        totalPayField.addValueChangeListener(this::onTotalPayChanged);
        stdHoursField.addValueChangeListener(this::onStandardHoursChanged);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        var positionAction = getEditedEntity();
        var position = positionAction.getPosition();

        if (entityStates.isNew(positionAction)) {
            effectiveDateField.setValue(LocalDate.now());
            stdHoursField.setValue(position.getStdHours());
            hourlyRtField.setValue(position.getHourlyRt());
            totalPayField.setValue(position.getTotalPay());
        }
        createdByString.setText(positionAction.getCreatedByString());
        adjustingRate = true;
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        // these should be added after data has been loaded to avoid dirty context
        adjustingRate = false;
    }

    @Install(to = "payPeriodsDl", target = Target.DATA_LOADER)
    private List<PayPeriod> payPeriodsDlLoadDelegate(final LoadContext<PayPeriod> loadContext) {
        return payPeriodService.getPayPeriods(currentPayPeriod);
    }

    @Install(to = "actionCodesDl", target = Target.DATA_LOADER)
    private List<ActionCode> actionCodesDlLoadDelegate(final LoadContext<ActionCode> loadContext) {
        return payPeriodService.getActionCodes();
    }

    @Subscribe("actionField")
    public void onActionFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<ActionCode>, ActionCode> event) {
        var actionCode = event.getValue();
        if(actionCode != null && actionCode.getNatureOfActionCode().startsWith("3")) {
           totalPayField.setValue(BigDecimal.ZERO);
        }
    }

    private void onHourlyRateChanged(AbstractField.ComponentValueChangeEvent<BigDecimalField, BigDecimal> event) {
        if (adjustingRate) {
            return;
        }
        final BigDecimal hourlyRt = event.getValue();
        if (hourlyRt == null) {
            return;
        }

        adjustingRate = true;
        try {
            totalPayField.setValue(hourlyRt.multiply(HOURS_PER_YEAR));
            annualRtField.setValue(recomputeAnnualRateField(stdHoursField.getValue(), hourlyRt));
        } finally {
            adjustingRate = false;
        }
    }

    private void onTotalPayChanged(AbstractField.ComponentValueChangeEvent<BigDecimalField, BigDecimal> event) {
        if (adjustingRate) {
            return;
        }
        final BigDecimal totalPay = event.getValue();
        if (totalPay == null) {
            return;
        }

        adjustingRate = true;
        try {
            hourlyRtField.setValue(totalPay.divide(HOURS_PER_YEAR, 2, RoundingMode.HALF_UP));
            annualRtField.setValue(recomputeAnnualRateField(stdHoursField.getValue(), hourlyRtField.getValue()));
        } finally {
            adjustingRate = false;
        }
    }

    private BigDecimal recomputeAnnualRateField(BigDecimal stdHours, BigDecimal hourlyRt) {
        if (hourlyRt == null) {
            return BigDecimal.ZERO;
        } else {
            return hourlyRt.multiply(stdHours).multiply(WEEKS_PER_YEAR);
        }
    }

    private void onStandardHoursChanged(AbstractField.ComponentValueChangeEvent<BigDecimalField, BigDecimal> event) {
        if (adjustingRate) {
            return;
        }
        final BigDecimal standardHours = event.getValue();
        if (standardHours == null) {
            return;
        }
        adjustingRate = true;
        try {
            BigDecimal hourlyRt = hourlyRtField.getValue();
            if (hourlyRt != null) {
                annualRtField.setValue(standardHours.multiply(WEEKS_PER_YEAR).multiply(hourlyRt));
            }
        } finally {
            adjustingRate = false;
        }
    }
}