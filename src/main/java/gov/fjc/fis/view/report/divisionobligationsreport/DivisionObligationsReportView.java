package gov.fjc.fis.view.report.divisionobligationsreport;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.service.report.DivisionObligationsReportService;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.BranchService;
import gov.fjc.fis.service.DivisionService;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.component.combobox.EntityComboBox;
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
 * Provides the parameter-selection interface for the division obligations
 * report and runs the report using the selected values.
 * <p>
 * The view allows the user to select a budget fiscal year, division, and
 * optional branch. The available divisions are limited to non-foundation
 * divisions associated with the selected fiscal year, and the available
 * branches are limited to those belonging to the selected division.
 * </p>
 * <p>
 * When the report is executed, the selected division and optional branch are
 * passed to {@link DivisionObligationsReportService}. The generated report
 * data is then supplied to the {@code division-obligations} report, which runs
 * in the background and produces an Excel workbook.
 * </p>
 * @author Doug Mitchell
 */
@Route(value = "division-obligations-report-view", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_DivisionObligationsReportView")
@ViewDescriptor(path = "division-obligations-report-view.xml")
@DialogMode(closeOnEsc = true)
public class DivisionObligationsReportView extends StandardView {
    @Autowired
    private SessionData sessionData;
    @Autowired
    private UiReportRunner uiReportRunner;

    @ViewComponent
    private CollectionLoader<Appropriation> appropriationsDl;
    @ViewComponent
    private CollectionLoader<Division> divisionsDl;
    @ViewComponent
    private CollectionLoader<Branch> branchesDl;

    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private BranchService branchService;
    @Autowired
    private DivisionObligationsReportService divisionObligationsReportService;

    @ViewComponent
    private EntityComboBox<Appropriation> bfySelectorField;
    @ViewComponent
    private EntityComboBox<Division> divisionSelectorField;
    @ViewComponent
    private EntityComboBox<Branch> branchSelectorField;
    @ViewComponent
    private JmixButton executeBtn;

    /**
     * Initializes the report parameters before the view is displayed.
     * <p>
     * The available report fiscal years are loaded, and the user's current
     * budget fiscal year entry appropriation is selected as the initial value.
     * Selecting the initial appropriation triggers the loading of the
     * corresponding divisions.
     * </p>
     *
     * @param event the event published before the view is displayed
     */
    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        appropriationsDl.load();
        bfySelectorField.setValue(appropriationService.getBfyEntryAppropriation(sessionData));
    }

    /**
     * Refreshes the division selector when the selected budget fiscal year
     * changes.
     * <p>
     * The division options are reloaded for the selected appropriation, and
     * the current division is retained when a division having the same
     * division code remains available.
     * </p>
     *
     * @param event the value-change event containing the selected
     *              appropriation
     */
    @Subscribe("bfySelectorField")
    protected void onBfySelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {
        refreshField(divisionsDl, divisionSelectorField, Division::getDivisionCode);
    }

    /**
     * Updates the view when the selected division changes.
     * <p>
     * Report execution is enabled only when a division is selected. The branch
     * selector is also refreshed with the branches belonging to the selected
     * division, retaining the current branch when a branch having the same
     * branch code remains available.
     * </p>
     *
     * @param event the value-change event containing the selected division
     */
    @Subscribe("divisionSelectorField")
    protected void onDivisionSelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Division>, Division> event) {
        executeBtn.setEnabled(event.getValue() != null);
        refreshField(branchesDl, branchSelectorField, Branch::getBranchCode);
    }

    /**
     * Loads the appropriations available for report fiscal-year selection.
     *
     * @param loadContext the collection loader context
     * @return the appropriations representing the fiscal years available for
     *         reporting
     */
    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    protected List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return appropriationService.fetchReportFiscalYears(sessionData);
    }

    /**
     * Loads the non-foundation divisions associated with the selected
     * appropriation.
     *
     * @param loadContext the collection loader context
     * @return the non-foundation divisions available for the selected budget
     *         fiscal year
     */
    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    protected List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.fetchNonFoundationDivisions(bfySelectorField.getValue());
    }

    /**
     * Loads the branches associated with the selected division.
     *
     * @param loadContext the collection loader context
     * @return the branches available for the selected division
     */
    @Install(to = "branchesDl", target = Target.DATA_LOADER)
    protected List<Branch> branchesDlLoadDelegate(final LoadContext<Branch> loadContext) {
        return branchService.fetchBranches(divisionSelectorField.getValue());
    }

    /**
     * Returns the label displayed for a division in the division selector.
     *
     * @param division the division for which to generate a label
     * @return the division title and code
     */
    @Install(to = "divisionSelectorField", subject = "itemLabelGenerator")
    protected Object divisionSelectorFieldItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }

    /**
     * Returns the label displayed for a branch in the branch selector.
     *
     * @param branch the branch for which to generate a label
     * @return the branch title and code
     */
    @Install(to = "branchSelectorField", subject = "itemLabelGenerator")
    protected Object branchSelectorFieldItemLabelGenerator(final Branch branch) {
        return branch.getTitleAndCode();
    }

    /**
     * Closes the view without running the report.
     *
     * @param event the cancel button click event
     */
    @Subscribe(id = "cancelBtn", subject = "clickListener")
    protected void onCancelBtnClick(final ClickEvent<JmixButton> event) {
        closeWithDefaultAction();
    }

    /**
     * Generates the report data and runs the division obligations report.
     * <p>
     * The selected division and optional branch are used to generate the
     * report data. The report is produced as an Excel workbook, using the
     * filename supplied by the report data object. It runs in the background
     * without displaying the report framework's parameter dialog.
     * </p>
     * <p>
     * After report execution is initiated, the parameter-selection view is
     * closed.
     * </p>
     *
     * @param event the execute button click event
     */
    @Subscribe(id = "executeBtn", subject = "clickListener")
    protected void onExecuteBtnClick(final ClickEvent<JmixButton> event) {
        var division = divisionSelectorField.getValue();
        var branch = branchSelectorField.getValue();

        var reportData = divisionObligationsReportService.generateReportData(division, branch);

        var fluentUiReportRunner = uiReportRunner.byReportCode("division-obligations");
        fluentUiReportRunner.addParam("reportData", reportData)
                .withOutputType(ReportOutputType.XLSX)
                .withOutputNamePattern(reportData.getFileName())
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .inBackground(this)
                .runAndShow();

        closeWithDefaultAction();
    }
}