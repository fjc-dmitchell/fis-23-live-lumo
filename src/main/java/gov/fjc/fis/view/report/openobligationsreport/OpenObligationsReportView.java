package gov.fjc.fis.view.report.openobligationsreport;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.BranchService;
import gov.fjc.fis.service.DivisionService;
import gov.fjc.fis.service.report.OpenObligationsReportService;
import gov.fjc.fis.reportdata.OpenObligationsReportData;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static gov.fjc.fis.app.FrameworkUtils.refreshField;

/**
 * View controller for the Open Obligations Report.
 * <p>
 * This view allows the user to select a budget fiscal year, one or more
 * divisions, an optional branch, and an aging threshold used to identify open
 * obligations. Additional reporting options are available when the selected
 * appropriation is subject to the One Big Beautiful Bill Act (OBBBA).
 * </p>
 *
 * <p>
 * Available divisions and branches are automatically filtered based on the
 * user's selections. When executed, the view delegates report data generation
 * to {@link OpenObligationsReportService} and submits the resulting
 * {@link OpenObligationsReportData} object to the reporting engine for XLSX
 * generation and download.
 * </p>
 *
 * @see OpenObligationsReportService
 * @see OpenObligationsReportData
 * @author Doug Mitchell
 */
@Route(value = "open-obligations-report-view", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_OpenObligationsReportView")
@ViewDescriptor(path = "open-obligations-report-view.xml")
@DialogMode(closeOnEsc = true)
public class OpenObligationsReportView extends StandardView {

    /**
     * Default number of days used when determining whether an obligation should
     * be considered open.
     */
    private static final String DEFAULT_NUMBER_OF_DAYS = "15";

    @Autowired
    private SessionData sessionData;
    @Autowired
    private UiReportRunner uiReportRunner;

    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private BranchService branchService;
    @Autowired
    private OpenObligationsReportService openObligationsReportService;

    @ViewComponent
    private CollectionLoader<Appropriation> appropriationsDl;
    @ViewComponent
    private CollectionLoader<Division> divisionsDl;
    @ViewComponent
    private CollectionLoader<Branch> branchesDl;

    @ViewComponent
    private EntityComboBox<Appropriation> bfySelectorField;
    @ViewComponent
    private JmixCheckbox obbbaField;
    @ViewComponent
    private JmixMultiSelectComboBoxPicker<Division> divisionSelectorField;
    @ViewComponent
    private EntityComboBox<Branch> branchSelectorField;
    @ViewComponent
    private TypedTextField<Integer> numberOfDaysField;
    @ViewComponent
    private JmixButton executeBtn;

    /**
     * Initializes the view with the user's default budget fiscal year, default
     * division selection, and default aging threshold.
     *
     * @param event the view lifecycle event
     */
    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        appropriationsDl.load();
        bfySelectorField.setValue(appropriationService.getBfyEntryAppropriation(sessionData));
        divisionsDl.load();
        divisionSelectorField.setValue(divisionService.findEducationDivision(bfySelectorField.getValue()));
        numberOfDaysField.setValue(DEFAULT_NUMBER_OF_DAYS);
    }

    /**
     * Loads fiscal years available for report generation.
     *
     * @param loadContext the load context
     * @return available report fiscal years
     */
    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    protected List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return appropriationService.fetchReportFiscalYears(sessionData);
    }

    /**
     * Loads divisions eligible for inclusion in the report for the selected
     * fiscal year.
     *
     * @param loadContext the load context
     * @return available divisions
     */
    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    protected List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.fetchNonFoundationDivisions(bfySelectorField.getValue());
    }

    /**
     * Loads branches for the currently selected division when branch-level
     * filtering is available.
     *
     * @param loadContext the load context
     * @return available branches
     */
    @Install(to = "branchesDl", target = Target.DATA_LOADER)
    protected List<Branch> branchesDlLoadDelegate(final LoadContext<Branch> loadContext) {
        return branchService.fetchBranches(firstSelectedDivision());
    }

    /**
     * Generates the display label for division selection values.
     *
     * @param division the division being displayed
     * @return the division title and code
     */
    @Install(to = "divisionSelectorField", subject = "itemLabelGenerator")
    protected Object divisionSelectorFieldItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }

    /**
     * Generates the display label for branch selection values.
     *
     * @param branch the branch being displayed
     * @return the branch title and code
     */
    @Install(to = "branchSelectorField", subject = "itemLabelGenerator")
    protected Object branchSelectorFieldItemLabelGenerator(final Branch branch) {
        return branch.getTitleAndCode();
    }

    /**
     * Refreshes reporting options when the selected fiscal year changes.
     * <p>
     * OBBBA reporting availability, division selections, branch selection
     * behavior, and report execution eligibility are updated to reflect the
     * selected appropriation.
     * </p>
     *
     * @param event the value change event
     */
    @Subscribe("bfySelectorField")
    protected void onBfySelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {
        if (event.getValue() != null) {
            var obbbaApplicable = appropriationService.isOneBigBeautifulBillAct(event.getValue());
            obbbaField.setVisible(obbbaApplicable);
            obbbaField.setValue(false);
            branchSelectorField.setHelperText(obbbaApplicable
                    ? "enabled when selecting a single division without OBBBA"
                    : "enabled when selecting a single division");
        }
        refreshField(divisionsDl, divisionSelectorField, Division::getDivisionCode);
        updateExecuteBtn();
    }

    /**
     * Updates branch-selection availability and report execution eligibility when
     * the selected divisions change.
     *
     * @param event the value change event
     */
    @Subscribe("divisionSelectorField")
    public void onDivisionSelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixMultiSelectComboBoxPicker<Division>, Division> event) {
        updateExecuteBtn();
        updateBranchSelector();
    }

    /**
     * Updates branch-selection availability when the OBBBA reporting option
     * changes.
     *
     * @param event the value change event
     */
    @Subscribe("obbbaField")
    public void onObbbaFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {
        updateBranchSelector();
    }

    /**
     * Closes the dialog without generating the report.
     *
     * @param event the button click event
     */
    @Subscribe(id = "cancelBtn", subject = "clickListener")
    protected void onCancelBtnClick(final ClickEvent<JmixButton> event) {
        closeWithDefaultAction();
    }

    /**
     * Generates and displays the Open Obligations Report using the selected
     * reporting parameters.
     * <p>
     * Report data is assembled by {@link OpenObligationsReportService} and passed
     * to the reporting engine for XLSX generation. The report executes
     * asynchronously and is presented to the user when processing completes.
     * </p>
     *
     * @param event the button click event
     */
    @Subscribe(id = "executeBtn", subject = "clickListener")
    protected void onExecuteBtnClick(final ClickEvent<JmixButton> event) {
        var reportData = openObligationsReportService.generateReportData(
                bfySelectorField.getValue(),
                divisionSelectorField.getValue(),
                branchSelectorField.getValue(),
                parseNumberOfDays(),
                isObbbaSelected());

        uiReportRunner.byReportCode("open-obligations")
                .addParam("reportData", reportData)
                .withOutputType(ReportOutputType.XLSX)
                .withOutputNamePattern(reportData.getFileName())
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .inBackground(this)
                .runAndShow();

        closeWithDefaultAction();
    }

    /**
     * Enables or disables branch selection based on the current reporting
     * criteria.
     * <p>
     * Branch-level filtering is available only when exactly one division is
     * selected and OBBBA reporting is not enabled.
     * </p>
     */
    private void updateBranchSelector() {
        boolean branchSelectable = divisionSelectorField.getValue().size() == 1 && !isObbbaSelected();

        branchSelectorField.setEnabled(branchSelectable);
        if (branchSelectable) {
            refreshField(branchesDl, branchSelectorField, Branch::getBranchCode);
        } else {
            branchSelectorField.clear();
            branchesDl.load();
        }
    }

    /**
     * Updates the execute button state based on whether the required reporting
     * parameters have been provided.
     */
    private void updateExecuteBtn() {
        executeBtn.setEnabled(bfySelectorField.getValue() != null
                && !divisionSelectorField.getValue().isEmpty());
    }

    /**
     * The checkbox is only visible, and only ever set, for appropriations covered by the
     * One Big Beautiful Bill Act, so its value is the whole answer.
     */
    private boolean isObbbaSelected() {
        return Boolean.TRUE.equals(obbbaField.getValue());
    }

    private Division firstSelectedDivision() {
        return divisionSelectorField.getValue().stream().findFirst().orElse(null);
    }

    /**
     * @return the entered number of days, or {@code 0} (no end-date filter) when the field is
     * empty or not a valid number
     */
    private int parseNumberOfDays() {
        try {
            return Integer.parseInt(numberOfDaysField.getValue());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}