package gov.fjc.fis.view.report.statusoffundsreport;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.OutputType;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.report.StatusOfFundsReportService;
import gov.fjc.fis.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static gov.fjc.fis.FisUtilities.getReportOutputType;

@Route(value = "status-of-funds-report-view", layout = MainView.class)
@ViewController("fis_StatusOfFundsReportView")
@ViewDescriptor("status-of-funds-report-view.xml")
@DialogMode(closeOnEsc = true)
public class StatusOfFundsReportView extends StandardView {
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
    private StatusOfFundsReportService statusOfFundsReportService;
    @Autowired
    private AppropriationService appropriationService;

    /**
     * view components
     */
    @ViewComponent
    private EntityComboBox<Appropriation> bfySelectorField;
    @ViewComponent
    private JmixRadioButtonGroup<OutputType> outputType;
    @ViewComponent
    private JmixButton executeBtn;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        appropriationsDl.load();
        bfySelectorField.setValue(appropriationService.getBfyEntryAppropriation(sessionData));
        outputType.setValue(OutputType.PDF);
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
        var reportOutputType = getReportOutputType(outputType.getValue());
        var scale = reportOutputType.equals(ReportOutputType.PDF) ? 0 : 2;

        var reportData = statusOfFundsReportService.generateReportData(appropriation, scale);

        var templateCode = appropriationService.isOneBigBeautifulBillAct(appropriation) ? "obbba" : "default";
        var reportCode = reportOutputType.equals(ReportOutputType.PDF) ? "status-of-funds-pdf" : "status-of-funds-excel";
        var fluentUiReportRunner = uiReportRunner.byReportCode(reportCode);

        fluentUiReportRunner.addParam("reportData", reportData)
                .withTemplateCode(templateCode)
                .withOutputType(reportOutputType)
                .withOutputNamePattern(reportData.getFileName())
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .runAndShow();

        closeWithDefaultAction();
    }
}