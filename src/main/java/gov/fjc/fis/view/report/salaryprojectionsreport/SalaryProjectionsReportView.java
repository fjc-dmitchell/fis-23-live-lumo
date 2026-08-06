package gov.fjc.fis.view.report.salaryprojectionsreport;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.service.PayPeriodService;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.Fund;
import gov.fjc.fis.entity.OutputType;
import gov.fjc.fis.entity.personnel.PayPeriod;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.DivisionService;
import gov.fjc.fis.service.FundService;
import gov.fjc.fis.service.report.SalaryProjectionsReportService;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.component.textfield.JmixNumberField;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static gov.fjc.fis.FisUtilities.getReportOutputType;

@Route(value = "salary-projections-report-view", layout = MainView.class)
@ViewController(id = "fis_SalaryProjectionsReportView")
@ViewDescriptor(path = "salary-projections-report-view.xml")
@DialogMode(closeOnEsc = true)
public class SalaryProjectionsReportView extends StandardView {

    @Autowired
    private UiReportRunner uiReportRunner;

    /**
     * services
     */
    @Autowired
    private FundService fundService;
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private PayPeriodService payPeriodService;
    @Autowired
    private SalaryProjectionsReportService salaryProjectionsReportService;

    /**
     * data containers and data loaders
     */
    @ViewComponent
    private CollectionLoader<Appropriation> appropriationsDl;
    @ViewComponent
    private CollectionContainer<Division> divisionsDc;
    @ViewComponent
    private CollectionLoader<Division> divisionsDl;
    @ViewComponent
    private CollectionLoader<PayPeriod> payPeriodsDl;

    /**
     * view components
     */
//    @ViewComponent
//    private TypedTextField<String> bfyField;
    @ViewComponent
    private EntityComboBox<Appropriation> bfySelectorField;
    @ViewComponent
    private EntityComboBox<Division> divisionSelectorField;
    @ViewComponent
    private JmixRadioButtonGroup<OutputType> outputType;
    @ViewComponent
    private EntityComboBox<PayPeriod> payPeriodField;
    @ViewComponent
    private JmixNumberField ficaRateField;
    @ViewComponent
    private JmixNumberField benefitsRateField;
    @ViewComponent
    private JmixNumberField bonusAmountField;
    @ViewComponent
    private JmixButton executeBtn;

    /**
     * instance variables
     */
    private Fund oneYearFund;
    private Appropriation appropriation;
    private PayPeriod currentPayPeriod;

    /**
     * constants - should be moved elsewhere
     */
    private final double FICA_RATE = 7.65;
    private final double BENEFIT_RATE = 30.1;

    @Subscribe
    public void onInit(final InitEvent event) {
        configurePercentageField(ficaRateField, FICA_RATE);
        configurePercentageField(benefitsRateField, BENEFIT_RATE);
        currentPayPeriod = payPeriodService.fetchCurrentPayPeriod();
    }

    private void configurePercentageField(JmixNumberField field, double defaultValue) {
        field.setValue(defaultValue);

        field.setSuffixComponent(new Span("%"));

        field.addValidator(value -> {
            if (value == null) {
                return;
            }
            if (value < 0 || value > 100) {
                throw new ValidationException("Percentage must be between 0 and 100");
            }
        });

        field.addBlurListener(e -> {
            Double value = field.getValue();
            if (value != null) {
                // Round to two decimal places
                double rounded = Math.round(value * 100.0) / 100.0;
                field.setValue(rounded);
            }
        });
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        appropriation = appropriationService.getCurrentBudgetFiscalYear();
        appropriationsDl.load();
        oneYearFund = fundService.getAppropriationOneYearFund();

        if (appropriation != null) {
//            bfyField.setValue(appropriation.getBudgetFiscalYear());
            bfySelectorField.setValue(appropriation);
            divisionsDl.load();
            divisionSelectorField.setValue(divisionsDc.getItems().getFirst());
            payPeriodsDl.load();
            payPeriodField.setValue(currentPayPeriod);
        }
        outputType.setValue(OutputType.PDF);
    }

    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    private List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return payPeriodService.getAppropriations(appropriation);
    }

    @Subscribe("bfySelectorField")
    public void onBfySelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Appropriation>, Appropriation> event) {
        appropriation = event.getValue();
        divisionsDl.load();
        payPeriodsDl.load();
    }


    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    private List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        // include only one year fund divisions
        return divisionService.getDivisions(appropriation, false)
                .stream().filter(division -> division.getFund().equals(oneYearFund)).toList();
    }

    @Install(to = "payPeriodsDl", target = Target.DATA_LOADER)
    private List<PayPeriod> payPeriodsDlLoadDelegate(final LoadContext<PayPeriod> loadContext) {
        List<PayPeriod> payPeriods;
        var thisPayPeriod = payPeriodService.fetchCurrentPayPeriod();
        payPeriods = payPeriodService.getPayPeriods(appropriation, currentPayPeriod);
        if(payPeriods.contains(thisPayPeriod)) {
            payPeriodField.setValue(currentPayPeriod);
        } else {
            payPeriodField.setValue(payPeriods.getFirst());
        }
//        if (thisPayPeriod.getAppropriation().equals(appropriation)) {
//            payPeriods = payPeriodService.getPayPeriods(thisPayPeriod);
//            payPeriodField.setValue(thisPayPeriod);
//        } else {
//            payPeriods = payPeriodService.getPayPeriods(appropriation);
//            payPeriodField.setValue(payPeriods.getFirst());
//        }
        return payPeriods;
    }

    @Subscribe("divisionSelectorField")
    public void onDivisionSelectorFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Division>, Division> event) {
        bonusAmountField.setValue(payPeriodService.getBonusProjections(event.getValue()));
    }

    @Install(to = "divisionSelectorField", subject = "itemLabelGenerator")
    private Object divisionSelectorFieldItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }

    @Subscribe("payPeriodField")
    public void onPayPeriodFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<PayPeriod>, PayPeriod> event) {
        executeBtn.setEnabled(event.getValue() != null);
    }

    @Subscribe(id = "cancelBtn", subject = "clickListener")
    public void onCancelBtnClick(final ClickEvent<JmixButton> event) {
        closeWithDefaultAction();
    }

    @Subscribe(id = "executeBtn", subject = "clickListener")
    public void onExecuteBtnClick(final ClickEvent<JmixButton> event) {
        var division = divisionSelectorField.getValue();
        var startingPayPeriod = payPeriodField.getValue();

        var ficaRate = ficaRateField.getValue();
        var ficaRatePct = ficaRate == null ? BigDecimal.ZERO : BigDecimal.valueOf(ficaRate / 100);

        var benefitsRate = benefitsRateField.getValue();
        var benefitsRatePct = benefitsRate == null ? BigDecimal.ZERO : BigDecimal.valueOf(benefitsRate / 100);

        var reportOutputType = getReportOutputType(outputType.getValue());
        var scale = reportOutputType.equals(ReportOutputType.PDF) ? 0 : 2;

        BigDecimal bonusProjection = BigDecimal.valueOf(bonusAmountField.getValue());

        var reportData = salaryProjectionsReportService.generateReportData(
                division, startingPayPeriod, benefitsRatePct, ficaRatePct, bonusProjection, scale);

//        var reportCode = reportOutputType.equals(ReportOutputType.PDF) ? "status-of-funds-pdf" : "status-of-funds-excel";
        var reportCode = "salary-projections";
        var fluentUiReportRunner = uiReportRunner.byReportCode(reportCode);

        fluentUiReportRunner.addParam("reportData", reportData)
                .withOutputType(reportOutputType)
                .withOutputNamePattern(reportData.getFileName())
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .runAndShow();

        closeWithDefaultAction();
    }
}