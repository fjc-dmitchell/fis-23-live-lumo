package gov.fjc.fis.view.report.opentravelobligationsreport;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.DivisionService;
import gov.fjc.fis.service.report.OpenTravelObligationsReportService;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "open-travel-obligations-report-view", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_OpenTravelObligationsReportView")
@ViewDescriptor(path = "open-travel-obligations-report-view.xml")
public class OpenTravelObligationsReportView extends StandardView {
    @Autowired
    private SessionData sessionData;
    @Autowired
    private UiReportRunner uiReportRunner;

    /**
     * data loaders
     */
    @ViewComponent
    private CollectionLoader<Appropriation> appropriationsDl;
    @ViewComponent
    private CollectionLoader<Division> divisionsDl;

    /**
     * services
     */
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private OpenTravelObligationsReportService openTravelObligationsReportService;

    /**
     * view components
     */
    @ViewComponent
    private EntityComboBox<Appropriation> bfySelectorField;
    @ViewComponent
    private JmixCheckbox obbbaField;
    @ViewComponent
    private JmixMultiSelectComboBoxPicker<Division> divisionSelectorField;
    @ViewComponent
    private TypedDatePicker<LocalDate> endDateField;
    @ViewComponent
    private TypedDatePicker<LocalDate> beginDateField;
    @ViewComponent
    private JmixButton executeBtn;

    /**
     * instance variables
     */
    Appropriation appropriation;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        appropriationsDl.load();
        bfySelectorField.setValue(appropriationService.getBfyEntryAppropriation(sessionData));
        appropriation = bfySelectorField.getValue();
        obbbaField.setValue(true);
        divisionsDl.load();
        // set Education and Research as defaults
        divisionSelectorField.setValue(Set.of(
                divisionService.getEducationDivision(appropriation),
                divisionService.getResearchDivision(appropriation))
        );

        setDateFields(LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1));
    }

    private void setDateFields(LocalDate beginTravel) {
        var endTravel = beginTravel;
        endTravel = endTravel.withDayOfMonth(
                endTravel.getMonth().length(endTravel.isLeapYear()));
        endDateField.setMin(beginTravel);
        beginDateField.setValue(beginTravel);
        endDateField.setValue(endTravel);
    }

    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    protected List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return appropriationService.fetchReportFiscalYears(sessionData);
    }

    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    protected List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.getDivisions(appropriation, false);
    }

    @Subscribe("bfySelectorField")
    protected void onBfySelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {
        appropriation = event.getValue();
        obbbaField.setVisible(divisionService.fetchMandatoryDivision(appropriation) != null);
        divisionsDl.load();
        if (!divisionSelectorField.getValue().isEmpty()) {
            divisionSelectorField.setValue(
                    divisionsDl.getContainer().getItems().stream()
                            .filter(div -> divisionSelectorField.getValue().stream()
                                    .anyMatch(oldDiv -> oldDiv.getDivisionCode().equals(div.getDivisionCode())))
                            .collect(Collectors.toSet())
            );
        }
    }

    @Subscribe("divisionSelectorField")
    protected void onDivisionSelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixMultiSelectComboBoxPicker<Division>, Division> event) {
        enableExecuteBtn();
    }

    @Subscribe("beginDateField")
    protected void onBeginDateFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<TypedDatePicker<LocalDate>, LocalDate> event) {
        if (event.getValue() != null) {
            setDateFields(event.getValue());
        }
    }

    @Subscribe("allOpenField")
    protected void onAllOpenFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {
        if (event.getValue().equals(Boolean.TRUE)) {
            beginDateField.setEnabled(false);
            endDateField.setEnabled(false);
            beginDateField.setValue(null);
            endDateField.setValue(null);
        } else {
            beginDateField.setEnabled(true);
            endDateField.setEnabled(true);
            setDateFields(LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1));
        }
    }

    @Install(to = "divisionSelectorField", subject = "itemLabelGenerator")
    protected Object divisionSelectorFieldItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }

    @Subscribe(id = "cancelBtn", subject = "clickListener")
    protected void onCancelBtnClick(final ClickEvent<JmixButton> event) {
        closeWithDefaultAction();
    }

    private void enableExecuteBtn() {
        executeBtn.setEnabled(!divisionSelectorField.getValue().isEmpty());
    }

    @Subscribe(id = "executeBtn", subject = "clickListener")
    protected void onExecuteBtnClick(final ClickEvent<JmixButton> event) {
        var appropriation = bfySelectorField.getValue();
        var division = divisionSelectorField.getValue();
        var beginDate = beginDateField.getValue();
        var endDate = endDateField.getValue();
        var obbba = obbbaField.getValue();

        var reportData = openTravelObligationsReportService.generateReportData(
                appropriation, division, beginDate, endDate, obbba);

        var fluentUiReportRunner = uiReportRunner.byReportCode("open-travel-obligations");
        fluentUiReportRunner.addParam("reportData", reportData)
                .withOutputType(ReportOutputType.XLSX)
                .withOutputNamePattern(reportData.getFileName())
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .runAndShow();

        closeWithDefaultAction();
    }
}