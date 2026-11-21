package gov.fjc.fis.app;

import com.vaadin.flow.component.Unit;
import gov.fjc.fis.view.report.budgetrequestreport.BudgetRequestReportView;
import gov.fjc.fis.view.report.divisionobligationsreport.DivisionObligationsReportView;
import gov.fjc.fis.view.report.educationbranchreport.EducationBranchReportView;
import gov.fjc.fis.view.report.educationprogramsreport.EducationProgramsReportView;
import gov.fjc.fis.view.report.openobligationsreport.OpenObligationsReportView;
import gov.fjc.fis.view.report.opentravelobligationsreport.OpenTravelObligationsReportView;
import gov.fjc.fis.view.report.salaryprojectionsreport.SalaryProjectionsReportView;
import gov.fjc.fis.view.report.statusoffundsreport.StatusOfFundsReportView;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.UiComponentUtils;
import org.springframework.stereotype.Component;

/**
 * Routes report menu actions to their corresponding report parameter views.
 * <p>
 * Jmix main-menu items can only invoke application methods and cannot directly
 * open report parameter dialogs. This component serves as an intermediary
 * between the menu system and report views by opening the appropriate view in
 * a modal dialog window.
 * </p>
 * <p>
 * Each method launches a report-specific parameter view that allows the user
 * to configure report options before report generation and execution.
 * </p>
 *
 * @author Doug Mitchell
 * @since 2.1
 */
@Component("fis_ReportRouter")
public class ReportRouter {
    private final DialogWindows dialogWindows;

    public ReportRouter(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    public void openStatusOfFundsReport() {
        dialogWindows.view(UiComponentUtils.getCurrentView(), StatusOfFundsReportView.class).open().setWidth(20, Unit.EM);
    }

    public void openEducationProgramsReport() {
        dialogWindows.view(UiComponentUtils.getCurrentView(), EducationProgramsReportView.class).open();
    }

    public void openEducationBranchReport() {
        dialogWindows.view(UiComponentUtils.getCurrentView(), EducationBranchReportView.class).open();
    }

    public void openBudgetRequestReport() {
        dialogWindows.view(UiComponentUtils.getCurrentView(), BudgetRequestReportView.class).open().setWidth(20, Unit.EM);
    }

    public void openDivisionObligationsReport() {
        dialogWindows.view(UiComponentUtils.getCurrentView(), DivisionObligationsReportView.class).open();
    }

    public void openOpenObligationsReport() {
        dialogWindows.view(UiComponentUtils.getCurrentView(), OpenObligationsReportView.class).open();
    }

    public void openOpenTravelObligationsReport() {
        dialogWindows.view(UiComponentUtils.getCurrentView(), OpenTravelObligationsReportView.class).open();
    }

    public void openSalaryProjectionsReport() {
        dialogWindows.view(UiComponentUtils.getCurrentView(), SalaryProjectionsReportView.class).open();
    }
}