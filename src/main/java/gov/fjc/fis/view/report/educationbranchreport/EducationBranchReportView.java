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
import java.util.Optional;

@Route(value = "education-branch-report-view", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_EducationBranchReportView")
@ViewDescriptor(path = "education-branch-report-view.xml")
public class EducationBranchReportView extends StandardView {
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
    private CollectionLoader<Branch> branchesDl;

    /**
     * services
     */
    @Autowired
    private EducationBranchReportService educationBranchReportService;
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
    private EntityComboBox<Appropriation> bfySelectorField;
    @ViewComponent
    private EntityComboBox<Branch> branchSelectorField;
    @ViewComponent
    private JmixButton executeBtn;

    /**
     * instance variables
     */
    private Division division;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        appropriationsDl.load();
        bfySelectorField.setValue(appropriationService.getBfyEntryAppropriation(sessionData));
    }

    //    @Subscribe("bfySelectorField")
//    public void onBfySelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {
//        division = divisionService.getEducationDivision(bfySelectorField.getValue());
//        branchesDl.load();
//        if (branchSelectorField.getValue() != null) {
//            branchSelectorField.setValue(
//                    branchesDl.getContainer().getItems().stream()
//                            .filter(bch -> bch.getBranchCode().equals(branchSelectorField.getValue().getBranchCode()))
//                            .findFirst()
//                            .orElse(null)
//            );
//        }
//        enableExecuteBtn();
//    }
    @Subscribe("bfySelectorField")
    public void onBfySelectorFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {

        division = divisionService.getEducationDivision(event.getValue());
        branchesDl.load();

        Optional.ofNullable(branchSelectorField.getValue())
                .map(Branch::getBranchCode)
                .ifPresent(code ->
                        branchSelectorField.setValue(
                                branchesDl.getContainer().getItems().stream()
                                        .filter(bch -> code.equals(bch.getBranchCode()))
                                        .findFirst()
                                        .orElse(null)
                        )
                );

        enableExecuteBtn();
    }

    @Subscribe("branchSelectorField")
    public void onBranchSelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Branch>, Branch> event) {
        enableExecuteBtn();
    }

    private void enableExecuteBtn() {
        executeBtn.setEnabled(branchSelectorField.getValue() != null);
    }

    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    private List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return appropriationService.fetchReportFiscalYears(sessionData);
    }

    @Install(to = "branchesDl", target = Target.DATA_LOADER)
    private List<Branch> branchesDlLoadDelegate(final LoadContext<Branch> loadContext) {
        return branchService.fetchBranches(division);
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

        var reportData = educationBranchReportService.generateReportData(branch);

        var fluentUiReportRunner = uiReportRunner.byReportCode("education-branch-report");

        fluentUiReportRunner.addParam("reportData", reportData)
                .withOutputType(ReportOutputType.XLSX)
                .withOutputNamePattern(reportData.getFileName())
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .runAndShow();

        closeWithDefaultAction();
    }


}