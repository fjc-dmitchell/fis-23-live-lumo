package gov.fjc.fis.view.report.educationbranchreport;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.BranchService;
import gov.fjc.fis.service.DivisionService;
import gov.fjc.fis.service.report.EducationBranchReportService;
import gov.fjc.fis.reportdata.EducationBranchReportData;
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
 * View controller for the Education Branch Report.
 * <p>
 * This view allows the user to select a budget fiscal year, education
 * division, and branch before generating the report. Available divisions and
 * branches are automatically filtered based on the user's selections.
 * </p>
 *
 * <p>
 * When executed, the view delegates report data generation to
 * {@link EducationBranchReportService}, then submits the resulting
 * {@link EducationBranchReportData} object to the reporting engine for PDF
 * generation and display.
 * </p>
 *
 * @see EducationBranchReportService
 * @see EducationBranchReportData
 * @author Doug Mitchell
 */
@Route(value = "education-branch-report-view", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_EducationBranchReportView")
@ViewDescriptor(path = "education-branch-report-view.xml")
@DialogMode(closeOnEsc = true)
public class EducationBranchReportView extends StandardView {
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
    private EducationBranchReportService educationBranchReportService;
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private BranchService branchService;

    @ViewComponent
    private EntityComboBox<Appropriation> bfySelectorField;
    @ViewComponent
    private EntityComboBox<Division> divisionSelectorField;
    @ViewComponent
    private EntityComboBox<Branch> branchSelectorField;
    @ViewComponent
    private JmixButton executeBtn;

    /**
     * Initializes the view with the user's default budget fiscal year and
     * corresponding education division.
     *
     * @param event the view lifecycle event
     */
    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        appropriationsDl.load();
        var appropriation = appropriationService.getBfyEntryAppropriation(sessionData);
        bfySelectorField.setValue(appropriation);

        divisionsDl.load();
        divisionSelectorField.setValue(divisionService.findEducationDivision(appropriation));
    }

    /**
     * Refreshes available divisions and branches when the selected fiscal year
     * changes and updates the execute button state.
     *
     * @param event the value change event
     */
    @Subscribe("bfySelectorField")
    public void onBfySelectorFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {
        divisionsDl.load();
        refreshField(divisionsDl, divisionSelectorField, Division::getDivisionCode);
        branchesDl.load();
        refreshField(branchesDl, branchSelectorField, Branch::getBranchCode);
        enableExecuteBtn();
    }

    /**
     * Refreshes the list of available branches when the selected division changes
     * and updates the execute button state.
     *
     * @param event the value change event
     */
    @Subscribe("divisionSelectorField")
    public void onDivisionSelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Division>, Division> event) {
        branchesDl.load();
        refreshField(branchesDl, branchSelectorField, Branch::getBranchCode);
        enableExecuteBtn();
    }

    /**
     * Updates the execute button state when the selected branch changes.
     *
     * @param event the value change event
     */
    @Subscribe("branchSelectorField")
    public void onBranchSelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Branch>, Branch> event) {
        enableExecuteBtn();
    }

    /**
     * Enables report execution when a branch has been selected.
     */
    private void enableExecuteBtn() {
        executeBtn.setEnabled(branchSelectorField.getValue() != null);
    }

    /**
     * Loads fiscal years available for report generation.
     *
     * @param loadContext the load context
     * @return available report fiscal years
     */
    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    private List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return appropriationService.fetchReportFiscalYears(sessionData);
    }

    /**
     * Loads education and OBBBA divisions for the selected fiscal year.
     *
     * @param loadContext the load context
     * @return matching divisions
     */
    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    private List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.fetchEducationAndObbbaDivisions(bfySelectorField.getValue());
    }

    /**
     * Loads branches for the selected division.
     *
     * @param loadContext the load context
     * @return branches belonging to the selected division
     */
    @Install(to = "branchesDl", target = Target.DATA_LOADER)
    private List<Branch> branchesDlLoadDelegate(final LoadContext<Branch> loadContext) {
        return branchService.fetchBranches(divisionSelectorField.getValue());
    }

    /**
     * Generates the display label for division selection values.
     *
     * @param division the division being displayed
     * @return the division title and code
     */
    @Install(to = "divisionSelectorField", subject = "itemLabelGenerator")
    private Object divisionSelectorFieldItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }

    /**
     * Generates the display label for branch selection values.
     *
     * @param branch the branch being displayed
     * @return the branch title and code
     */
    @Install(to = "branchSelectorField", subject = "itemLabelGenerator")
    private String branchSelectorFieldItemLabelGenerator(final Branch branch) {
        return branch.getTitleAndCode();
    }

    /**
     * Closes the dialog without generating the report.
     *
     * @param event the button click event
     */
    @Subscribe(id = "cancelBtn", subject = "clickListener")
    public void onCancelBtnClick(final ClickEvent<JmixButton> event) {
        closeWithDefaultAction();
    }

    /**
     * Generates and displays the Education Branch Report for the selected branch.
     * <p>
     * Report data is assembled by the
     * {@link EducationBranchReportService} and passed to the reporting engine for
     * PDF generation. The report is executed asynchronously and displayed to the
     * user upon completion.
     * </p>
     *
     * @param event the button click event
     */
    @Subscribe(id = "executeBtn", subject = "clickListener")
    public void onExecuteBtnClick(final ClickEvent<JmixButton> event) {
        var reportData = educationBranchReportService.generateReportData(branchSelectorField.getValue());

        uiReportRunner.byReportCode("education-branch-report")
                .addParam("reportData", reportData)
                .withOutputType(ReportOutputType.PDF)
                .withOutputNamePattern(reportData.getFileName())
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .inBackground(this)
                .runAndShow();

        closeWithDefaultAction();
    }
}