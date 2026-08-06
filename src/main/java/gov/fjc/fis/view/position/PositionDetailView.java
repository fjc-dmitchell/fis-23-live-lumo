package gov.fjc.fis.view.position;

import com.vaadin.flow.component.AbstractField;
import io.jmix.flowui.component.SupportsTypedValue.TypedValueChangeEvent;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.Position;
import gov.fjc.fis.service.DivisionService;
import gov.fjc.fis.service.PositionService;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.EntityStates;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Route(value = "positions/:id", layout = MainView.class)
@ViewController(id = "fis_Position.detail")
@ViewDescriptor(path = "position-detail-view.xml")
@EditedEntityContainer("positionDc")
public class PositionDetailView extends StandardDetailView<Position> {
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private Sequences sequences;
    @Autowired
    private PositionService positionService;
    @Autowired
    private DivisionService divisionService;
    @ViewComponent
    private TypedTextField<String> positionNbrField;
    @ViewComponent
    private TypedTextField<String> nameField;
    @ViewComponent
    private JmixComboBox<String> divisionComboBox;
    @ViewComponent
    private JmixComboBox<String> statusComboBox;
    @ViewComponent
    private JmixComboBox<String> regTempComboBox;
    @ViewComponent
    private JmixComboBox<String> gvtWorkSchedComboBox;
    @ViewComponent
    private TypedTextField<BigDecimal> stdHoursField;
    @ViewComponent
    private Paragraph createdByString;
    @ViewComponent
    private TypedTextField<BigDecimal> totalPayField;
    @ViewComponent
    private TypedTextField<BigDecimal> hourlyRtField;
    @ViewComponent
    private TypedTextField<BigDecimal> annualRtField;
    @ViewComponent
    private TypedDatePicker<Date> expirationField;

    private static final String VAC_PREFIX = "(VAC)";
    private static final String INACTIVE_PREFIX = "(INACTIVE)";

    private static final BigDecimal HOURS_PER_YEAR = BigDecimal.valueOf(2080);
    private static final BigDecimal WEEKS_PER_YEAR = BigDecimal.valueOf(52);

    private boolean adjustingRate = true;

    @Subscribe
    public void onInit(final InitEvent event) {
        ComponentUtils.setItemsMap(divisionComboBox, divisionService.getPositionDivisions());
        ComponentUtils.setItemsMap(statusComboBox, positionService.getEmployeeStatusItems());
        ComponentUtils.setItemsMap(regTempComboBox, positionService.getRegTempItems());
        ComponentUtils.setItemsMap(gvtWorkSchedComboBox, positionService.getWorkScheduleItems());

        hourlyRtField.addTypedValueChangeListener(this::onHourlyRateChanged);
        totalPayField.addTypedValueChangeListener(this::onTotalPayChanged);
        stdHoursField.addTypedValueChangeListener(this::onStandardHoursChanged);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        // these should be added after data has been loaded to avoid dirty context
        adjustingRate = false;
    }

    private void onHourlyRateChanged(
            final TypedValueChangeEvent<TypedTextField<BigDecimal>, BigDecimal> event) {
        if (adjustingRate) {
            return;
        }
        final BigDecimal hourlyRt = event.getValue();
        if (hourlyRt == null) {
            return;
        }

        adjustingRate = true;
        try {
            totalPayField.setTypedValue(hourlyRt.multiply(HOURS_PER_YEAR));
            annualRtField.setTypedValue(recomputeAnnualRateField(stdHoursField.getTypedValue(), hourlyRt));
        } finally {
            adjustingRate = false;
        }
    }

    private void onTotalPayChanged(
            final TypedValueChangeEvent<TypedTextField<BigDecimal>, BigDecimal> event) {
        if (adjustingRate) {
            return;
        }
        final BigDecimal totalPay = event.getValue();
        if (totalPay == null) {
            return;
        }

        adjustingRate = true;
        try {
            hourlyRtField.setTypedValue(totalPay.divide(HOURS_PER_YEAR, 2, RoundingMode.HALF_UP));
            annualRtField.setTypedValue(recomputeAnnualRateField(stdHoursField.getTypedValue(), hourlyRtField.getTypedValue()));
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

    private void onStandardHoursChanged(final TypedValueChangeEvent<TypedTextField<BigDecimal>, BigDecimal> event) {
        if (adjustingRate) {
            return;
        }
        final BigDecimal standardHours = event.getValue();
        if (standardHours == null) {
            return;
        }
        adjustingRate = true;
        try {
            BigDecimal hourlyRt = hourlyRtField.getTypedValue();
            if (hourlyRt != null) {
                annualRtField.setTypedValue(standardHours.multiply(WEEKS_PER_YEAR).multiply(hourlyRt));
            }
        } finally {
            adjustingRate = false;
        }
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        var position = getEditedEntity();
        if (entityStates.isNew(position)) {
            positionNbrField.setValue("NEW");
            positionNbrField.setReadOnly(true);
            statusComboBox.setValue("A");
            regTempComboBox.setValue("R");
            gvtWorkSchedComboBox.setValue("F");
            stdHoursField.setValue("40");
        }
        createdByString.setText(position.getCreatedByString());
        adjustingRate = true;
    }

    @Subscribe("statusComboBox")
    public void onStatusComboBoxComponentValueChange(
            AbstractField.ComponentValueChangeEvent<JmixComboBox<String>, String> event) {

        String status = event.getValue();
        if (status == null) {
            return;
        }

        String baseName = stripKnownPrefix(nameField.getTypedValue(), VAC_PREFIX, INACTIVE_PREFIX);
        if (baseName == null) {
            baseName = "";
        }
        baseName = baseName.trim();

        String newName = switch (status) {
            case "V" -> baseName.isEmpty() ? VAC_PREFIX : VAC_PREFIX + " " + baseName;
            case "I" -> baseName.isEmpty() ? INACTIVE_PREFIX : INACTIVE_PREFIX + " " + baseName;
            case "A" -> baseName;
            default -> nameField.getTypedValue();
        };

        if (newName != null) {
            nameField.setValue(newName.trim());
        }
    }

    @Subscribe("regTempComboBox")
    public void onRegTempComboBoxComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixComboBox<String>, String> event) {
        var regTemp = event.getValue();
        if (regTemp != null) {
            expirationField.setReadOnly(!regTemp.equals("T"));
            if (!regTemp.equals("T") && expirationField.getValue() != null) {
                expirationField.setValue(null);
            }
        }
    }

    private String stripKnownPrefix(String value, String... prefixes) {
        if (value == null) {
            return null;
        }
        for (String prefix : prefixes) {
            if (value.startsWith(prefix)) {
                return value.substring(prefix.length());
            }
        }
        return value;
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onPreSave(final DataContext.PreSaveEvent event) {
        Position position = getEditedEntity();

        if (!entityStates.isNew(position)) {
            return;
        }

        Long number = sequences.createNextValue(Sequence.withName("position_number")
                .setStartValue(1000)
                .setIncrement(1));
        position.setPositionNbr(String.format("NEW%05d", number));
    }
}