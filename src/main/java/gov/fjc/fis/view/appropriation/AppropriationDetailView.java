package gov.fjc.fis.view.appropriation;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.AppropriationAdjustment;
import gov.fjc.fis.event.AppropriationClosedEvent;
import gov.fjc.fis.view.main.MainView;
import gov.fjc.fis.view.report.divisionbalancefragment.DivisionBalanceFragment;
import gov.fjc.fis.view.report.spendingchartfragment.SpendingChartFragment;
import gov.fjc.fis.view.report.statusoffundsfragment.StatusOfFundsFragment;
import io.jmix.core.EntityStates;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.action.list.CreateAction;
import io.jmix.flowui.action.list.EditAction;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Route(value = "appropriations/:id", layout = MainView.class)
@ViewController(id = "fis_Appropriation.detail")
@ViewDescriptor(path = "appropriation-detail-view.xml")
@EditedEntityContainer("appropriationDc")
public class AppropriationDetailView extends StandardDetailView<Appropriation> {
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private UiEventPublisher uiEventPublisher;

    @ViewComponent
    private StatusOfFundsFragment sofFragment;
//    @ViewComponent
//    private SpendingChartFragment spendFragment;
    @ViewComponent
    private DivisionBalanceFragment balanceFragment;

    @ViewComponent
    private Paragraph createdByString;
    @ViewComponent
    private TypedTextField<String> budgetFiscalYearField;
    @ViewComponent
    private TypedTextField<Object> oneYearAmountField;
    @ViewComponent
    private TypedTextField<Object> twoYearAmountField;
//    @ViewComponent
//    private HorizontalLayout adjustmentsBox;
    @ViewComponent
    private JmixComboBox<Boolean> statusBox;
    @ViewComponent("adjustmentsDataGrid.create")
    private CreateAction<AppropriationAdjustment> adjustmentsDataGridCreate;
    @ViewComponent("adjustmentsDataGrid.edit")
    private EditAction<AppropriationAdjustment> adjustmentsDataGridEdit;
    @ViewComponent("adjustmentsDataGrid.remove")
    private RemoveAction<AppropriationAdjustment> adjustmentsDataGridRemove;


    @Subscribe
    protected void onInit(final InitEvent event) {
        Map<Boolean, String> map = new LinkedHashMap<>();
        map.put(true, "Open");
        map.put(false, "Closed");
        ComponentUtils.setItemsMap(statusBox, map);
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        Appropriation appropriation = getEditedEntity();
        createdByString.setText(appropriation.getCreatedByString());
        sofFragment.setAppropriation(appropriation);
//        spendFragment.setAppropriation(appropriation);
        balanceFragment.setAppropriation(appropriation);
        setEditable(statusBox.getValue());
        budgetFiscalYearField.setReadOnly(!entityStates.isNew(appropriation));
        calculateAdjustments();
    }

    @Subscribe(id = "adjustmentsDc", target = Target.DATA_CONTAINER)
    protected void onAdjustmentsDcCollectionChange(final CollectionContainer.CollectionChangeEvent<AppropriationAdjustment> event) {
        calculateAdjustments();
    }

    @Subscribe("statusBox")
    protected void onStatusBoxComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixComboBox<Boolean>, Boolean> event) {
        setEditable(statusBox.getValue());
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onPostSave(final DataContext.PostSaveEvent event) {
        uiEventPublisher.publishEventForUsers(
                new AppropriationClosedEvent(this, "appropriationClosed"), null);
    }

    private void calculateAdjustments() {
        var appropriation = getEditedEntity();
        var adjustments = appropriation.getAdjustments();

        if (adjustments != null) {
            var oneYearAdjustment = adjustments.stream()
                    .map(AppropriationAdjustment::getOneYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            var twoYearAdjustment = adjustments.stream()
                    .map(AppropriationAdjustment::getTwoYearAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            // don't update fields unless there is a change to existing values,
            // otherwise user will be prompted to save appropriation
            if (appropriation.getOneYearAdjustment().compareTo(oneYearAdjustment) != 0) {
                appropriation.setOneYearAdjustment(oneYearAdjustment);
            }
            if (appropriation.getTwoYearAdjustment().compareTo(twoYearAdjustment) != 0) {
                appropriation.setTwoYearAdjustment(twoYearAdjustment);
            }
        }

//        adjustmentsBox.setVisible((appropriation.getOneYearAdjustment().compareTo(BigDecimal.ZERO) != 0)
//                || (appropriation.getTwoYearAdjustment().compareTo(BigDecimal.ZERO) != 0));
    }

    private void setEditable(boolean statusOpen) {
        oneYearAmountField.setReadOnly(!statusOpen);
        twoYearAmountField.setReadOnly(!statusOpen);
        adjustmentsDataGridCreate.setEnabled(statusOpen);
        adjustmentsDataGridEdit.setEnabled(statusOpen);
        adjustmentsDataGridRemove.setEnabled(statusOpen);
    }
}