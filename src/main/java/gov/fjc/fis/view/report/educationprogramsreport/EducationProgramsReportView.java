package gov.fjc.fis.view.report.educationprogramsreport;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Branch;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.BranchService;
import gov.fjc.fis.service.DivisionService;
import gov.fjc.fis.service.report.EducationProgramsReportService;
import gov.fjc.fis.view.main.MainView;
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

import static gov.fjc.fis.FisUtilities.refreshField;

@Route(value = "education-programs-report-view", layout = MainView.class)
@ViewController("fis_EducationProgramsReportView")
@ViewDescriptor("education-programs-report-view.xml")
@DialogMode(closeOnEsc = true)
public class EducationProgramsReportView extends StandardView {
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
    @ViewComponent
    private CollectionLoader<Branch> branchesDl;

    /**
     * services
     */
    @Autowired
    private EducationProgramsReportService educationProgramsReportService;
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private BranchService branchService;

    /**
     * screen components
     */
    @ViewComponent
    private EntityComboBox<Division> divisionSelectorField;
    @ViewComponent
    private EntityComboBox<Appropriation> bfySelectorField;
    @ViewComponent
    private EntityComboBox<Branch> branchSelectorField;
    @ViewComponent
    private JmixButton executeBtn;

    /**
     * instance variables
     */
    private Appropriation appropriation;
    private Division division;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        appropriationsDl.load();
        bfySelectorField.setValue(appropriationService.getBfyEntryAppropriation(sessionData));
        appropriation = bfySelectorField.getValue();

        divisionsDl.load();
        divisionSelectorField.setValue(divisionService.getEducationDivision(appropriation));
        division = divisionSelectorField.getValue();
    }

    @Subscribe("bfySelectorField")
    public void onBfySelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {
        appropriation = event.getValue();
        divisionsDl.load();
        refreshField(divisionsDl, divisionSelectorField, Division::getDivisionCode);
        branchesDl.load();
        refreshField(branchesDl, branchSelectorField, Branch::getBranchCode);
        enableExecuteBtn();
    }

    @Subscribe("divisionSelectorField")
    public void onDivisionSelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Division>, Division> event) {
        division = event.getValue();
        branchesDl.load();
        refreshField(branchesDl, branchSelectorField, Branch::getBranchCode);
        enableExecuteBtn();
    }

    private void enableExecuteBtn() {
        executeBtn.setEnabled(division != null);
    }

    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    private List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return appropriationService.fetchReportFiscalYears(sessionData);
    }

    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    private List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.fetchEducationPlusObbaDivisions(bfySelectorField.getValue());
    }

    @Install(to = "branchesDl", target = Target.DATA_LOADER)
    private List<Branch> branchesDlLoadDelegate(final LoadContext<Branch> loadContext) {
        return branchService.fetchBranches(division);
    }

    @Install(to = "divisionSelectorField", subject = "itemLabelGenerator")
    private String divisionSelectorFieldItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }

    @Install(to = "branchSelectorField", subject = "itemLabelGenerator")
    private String branchSelectorFieldItemLabelGenerator(final Branch branch) {
        return branch.getTitleAndCode();
    }

    @Subscribe(id = "cancelBtn", subject = "clickListener")
    public void onCancelBtnClick(final ClickEvent<JmixButton> event) {
        closeWithDefaultAction();
    }

    @Subscribe(id = "executeBtn", subject = "clickListener")
    public void onExecuteBtnClick(final ClickEvent<JmixButton> event) {
        var branch = branchSelectorField.getValue();

        var reportData = educationProgramsReportService.generateReportData(division, branch);

        var fluentUiReportRunner = uiReportRunner.byReportCode("education-programs");

        fluentUiReportRunner.addParam("reportData", reportData)
                .withOutputType(ReportOutputType.XLSX)
                .withOutputNamePattern(reportData.getFileName())
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .runAndShow();

        closeWithDefaultAction();
    }
}