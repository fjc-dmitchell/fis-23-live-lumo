package gov.fjc.fis.view.division;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.*;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.FundService;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.EntityStates;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "divisions/:id", layout = MainView.class)
@ViewController(id = "fis_Division.detail")
@ViewDescriptor(path = "division-detail-view.xml")
@EditedEntityContainer("divisionDc")
public class DivisionDetailView extends StandardDetailView<Division> {
    @Autowired
    private SessionData sessionData;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private ReadOnlyViewsSupport readOnlyViewsSupport;

    @Autowired
    private FundService fundService;
    @Autowired
    private AppropriationService appropriationService;

    @ViewComponent
    private CollectionPropertyContainer<DivisionAllocation> allocationsDc;
    @ViewComponent
    private CollectionLoader<Fund> fundsDl;
    @ViewComponent
    private EntityPicker<Appropriation> appropriationField;
    @ViewComponent
    private TypedTextField<String> divisionCodeField;
    @ViewComponent
    private EntityComboBox<Fund> fundsComboBox;
    @ViewComponent
    private JmixButton updateDivisionAllocations;
    @ViewComponent
    private TypedTextField<Object> titleField;
    @ViewComponent
    private Paragraph allocationWarning;
    @ViewComponent
    private Paragraph allocationRule;
    @ViewComponent
    private Paragraph createdByString;

    private boolean fjcFoundation;
    private BigDecimal computedOneYearAllocations;
    private BigDecimal computedTwoYearAllocations;

    public void setFjcFoundation(boolean fjcFoundation) {
        this.fjcFoundation = fjcFoundation;
        if (fjcFoundation) {
            var division = getEditedEntity();
            fundsDl.load();
            if (entityStates.isNew(division)) {
                fundsComboBox.setValue(fundService.getFoundationFund());
                fundsComboBox.setReadOnly(true);
            }
            fundsComboBox.setReadOnly(true);
        }
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        var division = getEditedEntity();
        fundsDl.load();

        if (entityStates.isNew(division)) {
            var appropriation = appropriationService.getBfyEntryAppropriation(sessionData);
//            var appropriation = (Appropriation) sessionData.getAttribute("bfyEntry");
            division.setAppropriation(appropriation);
            appropriationField.setReadOnly(true);
            if (!fjcFoundation) {
                fundsComboBox.setValue(fundService.getAppropriationOneYearFund());
            }
        } else {
            var appropriation = division.getAppropriation();
            if ((!appropriation.getStatus())) {
                readOnlyViewsSupport.setViewReadOnly(this, true);
                updateDivisionAllocations.setEnabled(false);
            } else {
                appropriationField.setReadOnly(true);
                divisionCodeField.setReadOnly(true);
                titleField.focus();
                titleField.setAutoselect(true);
            }
            if (division.getFund().equals(fundService.getFoundationFund())) {
                fundsComboBox.setReadOnly(true);
            }
            createdByString.setText(division.getCreatedByString());
        }
    }

    @Install(to = "fundsDl", target = Target.DATA_LOADER)
    protected List<Fund> fundsDlLoadDelegate(final LoadContext<Fund> loadContext) {
        return fundService.fetchFundSearchList(fjcFoundation);
    }

    @Subscribe(id = "allocationsDc", target = Target.DATA_CONTAINER)
    protected void onAllocationsDcCollectionChange(final CollectionContainer.CollectionChangeEvent<DivisionAllocation> event) {
        computeAllocations();
        allocationWarning();
    }

    @Subscribe("totalAmountField")
    protected void onTotalAmountFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<TypedTextField<BigDecimal>, BigDecimal> event) {
        computeAllocations();
        allocationWarning();
    }

    @Subscribe(id = "updateDivisionAllocations", subject = "clickListener")
    protected void onUpdateDivisionAllocationsClick(final ClickEvent<JmixButton> event) {
        var division = getEditedEntity();
        computeAllocations();
        division.setOneYearAmount(computedOneYearAllocations);
        division.setTwoYearAmount(computedTwoYearAllocations);
    }

    private void computeAllocations() {
        computedOneYearAllocations = BigDecimal.ZERO;
        computedTwoYearAllocations = BigDecimal.ZERO;
        for (var allocation : allocationsDc.getItems()) {
            computedOneYearAllocations = computedOneYearAllocations.add(allocation.getOneYearAmount());
            computedTwoYearAllocations = computedTwoYearAllocations.add(allocation.getTwoYearAmount());
        }
    }

    private void allocationWarning() {
        var division = getEditedEntity();

        // prior to 2014, boc allocations had only one year amounts
        if (appropriationService.isAppropriationBefore2014(division.getAppropriation())) {
            allocationWarning.setVisible(division.getTotalAmount().compareTo(computedOneYearAllocations) != 0);
            allocationRule.setVisible(true);
        } else {
            allocationWarning.setVisible((division.getOneYearAmount().compareTo(computedOneYearAllocations) != 0)
                    || (division.getTwoYearAmount().compareTo(computedTwoYearAllocations) != 0));
        }
    }
}