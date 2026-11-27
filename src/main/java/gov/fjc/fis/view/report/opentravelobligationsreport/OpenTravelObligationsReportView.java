package gov.fjc.fis.view.report.opentravelobligationsreport;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.reportdata.OpenTravelObligationsReportData;
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
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gov.fjc.fis.app.FrameworkUtils.refreshField;

/**
 * Provides the parameter-entry view for the open travel obligations report.
 *
 * <p>The view allows the user to select a budget fiscal year, one or more divisions, an optional
 * travel-end-date range, and whether qualifying OBBBA obligations should be included. It supplies
 * default values when opened, refreshes appropriation-dependent selections, and delegates report
 * data creation to {@link OpenTravelObligationsReportService}.</p>
 *
 * <p>The completed report is generated as an XLSX file in the background and displayed without an
 * additional parameters dialog.</p>
 *
 * @author Doug Mitchell
 * @see OpenTravelObligationsReportService
 * @see OpenTravelObligationsReportData
 */
@Route(value = "open-travel-obligations-report-view", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_OpenTravelObligationsReportView")
@ViewDescriptor(path = "open-travel-obligations-report-view.xml")
@DialogMode(closeOnEsc = true)
public class OpenTravelObligationsReportView extends StandardView {
    @Autowired
    private SessionData sessionData;
    @Autowired
    private UiReportRunner uiReportRunner;

    @ViewComponent
    private CollectionLoader<Appropriation> appropriationsDl;
    @ViewComponent
    private CollectionLoader<Division> divisionsDl;

    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private OpenTravelObligationsReportService openTravelObligationsReportService;

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
     * Initializes the report parameters before the view is displayed.
     *
     * <p>The available fiscal years are loaded and the session's default report appropriation is
     * selected. OBBBA inclusion is enabled, Education and Research are selected when available,
     * and the travel-end-date range is initialized to the current month.</p>
     *
     * @param event the event fired before the view is displayed
     */
    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        appropriationsDl.load();
        bfySelectorField.setValue(appropriationService.getBfyEntryAppropriation(sessionData));
        var appropriation = bfySelectorField.getValue();
        obbbaField.setValue(true);
        divisionsDl.load();
        // set Education and Research as defaults
        var divisionSet = Stream.of(
                        divisionService.findEducationDivision(appropriation),
                        divisionService.findResearchDivision(appropriation))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        divisionSelectorField.setValue(divisionSet);

        setDefaultDateRange();
    }

    /**
     * Loads the appropriations available to the current session for report selection.
     *
     * @param loadContext the Jmix load context for the appropriations data loader
     * @return the available report fiscal-year appropriations
     */
    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    protected List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return appropriationService.fetchReportFiscalYears(sessionData);
    }

    /**
     * Loads the selectable non-foundation divisions for the selected appropriation.
     *
     * @param loadContext the Jmix load context for the divisions data loader
     * @return the non-foundation divisions associated with the selected appropriation
     */
    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    protected List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.fetchNonFoundationDivisions(bfySelectorField.getValue());
    }

    /**
     * Refreshes appropriation-dependent controls when the selected budget fiscal year changes.
     *
     * <p>The OBBBA option is displayed only when the selected appropriation has an OBBBA division.
     * The available divisions are then reloaded while matching selections are retained by division
     * code.</p>
     *
     * @param event the budget fiscal year value-change event
     */
    @Subscribe("bfySelectorField")
    protected void onBfySelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {
        obbbaField.setVisible(divisionService.findObbbaDivision(event.getValue()) != null);
        refreshField(divisionsDl, divisionSelectorField, Division::getDivisionCode);
    }

    /**
     * Updates the availability of the execute button when the division selection changes.
     *
     * @param event the division selection value-change event
     */
    @Subscribe("divisionSelectorField")
    protected void onDivisionSelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixMultiSelectComboBoxPicker<Division>, Division> event) {
        enableExecuteBtn();
    }

    /**
     * Updates the ending date when the beginning date changes.
     *
     * <p>When a beginning date is selected, the ending date is set to the final day of the same
     * month and cannot precede the beginning date.</p>
     *
     * @param event the beginning-date value-change event
     */
    @Subscribe("beginDateField")
    protected void onBeginDateFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<TypedDatePicker<LocalDate>, LocalDate> event) {
        if (event.getValue() != null) {
            updateEndDate(event.getValue());
        }
    }

    /**
     * Enables or disables travel-end-date filtering when the all-open option changes.
     *
     * <p>Selecting all open obligations disables and clears both date fields. Clearing the option
     * enables the fields and restores the current month as the default range.</p>
     *
     * @param event the all-open checkbox value-change event
     */
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
            setDefaultDateRange();
        }
    }

    /**
     * Returns the label displayed for a division in the division selector.
     *
     * @param division the division for which a label is required
     * @return the division's combined title and code
     */
    @Install(to = "divisionSelectorField", subject = "itemLabelGenerator")
    protected Object divisionSelectorFieldItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }

    /**
     * Closes the report parameter view without generating a report.
     *
     * @param event the cancel-button click event
     */
    @Subscribe(id = "cancelBtn", subject = "clickListener")
    protected void onCancelBtnClick(final ClickEvent<JmixButton> event) {
        closeWithDefaultAction();
    }

    /**
     * Generates and displays the open travel obligations report using the selected parameters.
     *
     * <p>The report data is generated by {@link OpenTravelObligationsReportService} and supplied to
     * the {@code open-travel-obligations} report. The report runs in the background as an XLSX file,
     * uses the generated report file name, and bypasses the standard parameters dialog.</p>
     *
     * @param event the execute-button click event
     */
    @Subscribe(id = "executeBtn", subject = "clickListener")
    protected void onExecuteBtnClick(final ClickEvent<JmixButton> event) {
        var reportData = openTravelObligationsReportService.generateReportData(
                bfySelectorField.getValue(),
                divisionSelectorField.getValue(),
                beginDateField.getValue(),
                endDateField.getValue(),
                obbbaField.getValue());

        uiReportRunner.byReportCode("open-travel-obligations")
                .addParam("reportData", reportData)
                .withOutputType(ReportOutputType.XLSX)
                .withOutputNamePattern(reportData.getFileName())
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .inBackground(this)
                .runAndShow();

        closeWithDefaultAction();
    }

    /**
     * Enables the execute button when at least one division is selected.
     */
    private void enableExecuteBtn() {
        executeBtn.setEnabled(!divisionSelectorField.getValue().isEmpty());
    }

    /**
     * Returns the first day of the current month.
     *
     * @return the first day of the current month
     */
    private static LocalDate firstOfCurrentMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * Resets the report date range to the first and last days of the current month.
     */
    private void setDefaultDateRange() {
        var begin = firstOfCurrentMonth();
        beginDateField.setValue(begin);
        updateEndDate(begin);
    }

    /**
     * Sets the ending date to the final day of the beginning date's month.
     *
     * <p>The beginning date is also assigned as the minimum selectable ending date.</p>
     *
     * @param beginTravel the beginning travel end date from which to derive the ending date
     */
    private void updateEndDate(LocalDate beginTravel) {
        endDateField.setMin(beginTravel);
        endDateField.setValue(YearMonth.from(beginTravel).atEndOfMonth());
    }
}