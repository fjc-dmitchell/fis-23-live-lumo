package gov.fjc.fis.view.report.budgetrequestreport;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.report.BudgetRequestReportService;
import gov.fjc.fis.view.main.MainView;

import com.vaadin.flow.router.Route;
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

@Route(value = "budget-request-report-view", layout = MainView.class)
@ViewController("fis_BudgetRequestReportView")
@ViewDescriptor("budget-request-report-view.xml")
@DialogMode(closeOnEsc = true)
public class BudgetRequestReportView extends StandardView {
    @Autowired
    private SessionData sessionData;
    @Autowired
    private UiReportRunner uiReportRunner;

    /**
     * data loaders
     */
    @ViewComponent
    private CollectionLoader<Appropriation> appropriationsDl;

    /**
     * services
     */
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private BudgetRequestReportService budgetRequestReportService;

    /**
     * view components
     */
    @ViewComponent
    private EntityComboBox<Appropriation> bfySelectorField;
    @ViewComponent
    private JmixButton executeBtn;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        appropriationsDl.load();
        bfySelectorField.setValue(appropriationService.getBfyEntryAppropriation(sessionData));
    }

    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    private List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return appropriationService.fetchReportFiscalYears(sessionData);
    }

    @Subscribe("bfySelectorField")
    public void onBfySelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {
        executeBtn.setEnabled(bfySelectorField.getValue() != null);
    }

    @Subscribe(id = "cancelBtn", subject = "clickListener")
    public void onCancelBtnClick(final ClickEvent<JmixButton> event) {
        closeWithDefaultAction();
    }

    @Subscribe(id = "executeBtn", subject = "clickListener")
    public void onExecuteBtnClick(final ClickEvent<JmixButton> event) {
        var appropriation = bfySelectorField.getValue();

        var reportData = budgetRequestReportService.generateReportData(appropriation);

        var fluentUiReportRunner = uiReportRunner.byReportCode("budget-request-workbook");

        fluentUiReportRunner.addParam("reportData", reportData)
                .withOutputType(ReportOutputType.XLSX)
                .withOutputNamePattern(reportData.getFileName())
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .runAndShow();

        closeWithDefaultAction();
    }
}