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
import java.util.Set;

import static gov.fjc.fis.app.FrameworkUtils.refreshField;

@Route(value = "open-obligations-report-view", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_OpenObligationsReportView")
@ViewDescriptor(path = "open-obligations-report-view.xml")
@DialogMode(closeOnEsc = true)
public class OpenObligationsReportView extends StandardView {
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
    private AppropriationService appropriationService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private BranchService branchService;
    @Autowired
    private OpenObligationsReportService openObligationsReportService;

    /**
     * screen components
     */
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

    Appropriation appropriation;
    Set<Division> divisions;
    Branch branch;


    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        appropriationsDl.load();
        bfySelectorField.setValue(appropriationService.getBfyEntryAppropriation(sessionData));
        appropriation = bfySelectorField.getValue();
        divisionsDl.load();
        divisionSelectorField.setValue(divisionService.getEducationDivision(appropriation));
        divisions = divisionSelectorField.getValue();
        numberOfDaysField.setValue("15"); // ask Nanticha whether this should be dropdown
    }

    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    protected List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return appropriationService.fetchReportFiscalYears(sessionData);
    }

    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    protected List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.getDivisions(appropriation, false);
    }

    @Install(to = "branchesDl", target = Target.DATA_LOADER)
    protected List<Branch> branchesDlLoadDelegate(final LoadContext<Branch> loadContext) {
        var division = divisions.isEmpty() ? null : divisions.iterator().next();
        return branchService.fetchBranches(division);
    }

    @Install(to = "divisionSelectorField", subject = "itemLabelGenerator")
    protected Object divisionSelectorFieldItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }

    @Install(to = "branchSelectorField", subject = "itemLabelGenerator")
    protected Object branchSelectorFieldItemLabelGenerator(final Branch branch) {
        return branch.getTitleAndCode();
    }

    @Subscribe("obbbaField")
    public void onObbbaFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {
        updateBranchSelector();
    }

    @Subscribe("bfySelectorField")
    protected void onBfySelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {
        appropriation = event.getValue();
        var obbbaEnabled = appropriationService.isOneBigBeautifulBillAct(appropriation);
        obbbaField.setVisible(obbbaEnabled);
        obbbaField.setValue(false);
        branchSelectorField.setHelperText(
                obbbaEnabled ? "enabled when selecting a single division without OBBBA" : "enabled when selecting a single division");

        refreshField(divisionsDl, divisionSelectorField, Division::getDivisionCode);
    }


    @Subscribe("divisionSelectorField")
    public void onDivisionSelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixMultiSelectComboBoxPicker<Division>, Division> event) {
        divisions = event.getSource().getSelectedItems();
        enableExecuteBtn();
        updateBranchSelector();
    }

    private void updateBranchSelector() {
        boolean hasSingleDivision = divisions.size() == 1;
        boolean isObbbaEnabled = isObbbaApplicable();

        branchSelectorField.setEnabled(hasSingleDivision && !isObbbaEnabled);
        if (hasSingleDivision && !obbbaField.getValue()) {
            refreshField(branchesDl, branchSelectorField, Branch::getBranchCode);
        } else {
            branchSelectorField.clear();
            branchesDl.load();
        }
    }

    private boolean isObbbaApplicable() {
        return appropriationService.isOneBigBeautifulBillAct(appropriation)
                && Boolean.TRUE.equals(obbbaField.getValue());
    }

    @Subscribe("branchSelectorField")
    protected void onBranchSelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Branch>, Branch> event) {
        branch = event.getValue();
    }

    private void enableExecuteBtn() {
        executeBtn.setEnabled(!divisions.isEmpty());
    }

    @Subscribe(id = "cancelBtn", subject = "clickListener")
    protected void onCancelBtnClick(final ClickEvent<JmixButton> event) {
        closeWithDefaultAction();
    }

    @Subscribe(id = "executeBtn", subject = "clickListener")
    protected void onExecuteBtnClick(final ClickEvent<JmixButton> event) {
        var appropriation = bfySelectorField.getValue();
        var divisions = divisionSelectorField.getValue();
        var branch = branchSelectorField.getValue();
        var obbba = obbbaField.getValue();

        int numberOfDays;
        try {
            numberOfDays = Integer.parseInt(numberOfDaysField.getValue());
        } catch (NumberFormatException e) {
            numberOfDays = 0;
        }

        var reportData = openObligationsReportService
                .generateReportData(appropriation, divisions, branch, numberOfDays, obbba);

        var fluentUiReportRunner = uiReportRunner.byReportCode("open-obligations");
        fluentUiReportRunner.addParam("reportData", reportData)
                .withOutputType(ReportOutputType.XLSX)
                .withOutputNamePattern(reportData.getFileName())
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .inBackground(this)
                .runAndShow();

        closeWithDefaultAction();
    }
}