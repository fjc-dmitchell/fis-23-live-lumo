package gov.fjc.fis.view.bonusprojection;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.personnel.BonusProjection;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.DivisionService;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.EntityStates;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "bonus-projections/:id", layout = MainView.class)
@ViewController(id = "fis_BonusProjection.detail")
@ViewDescriptor(path = "bonus-projection-detail-view.xml")
@EditedEntityContainer("bonusProjectionDc")
public class BonusProjectionDetailView extends StandardDetailView<BonusProjection> {
    @Autowired
    private SessionData sessionData;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private ReadOnlyViewsSupport readOnlyViewsSupport;
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private DivisionService divisionService;
    @ViewComponent
    private CollectionLoader<Division> divisionsDl;
    @ViewComponent
    private TypedTextField<String> budgetFiscalYearField;
    @ViewComponent
    private EntityComboBox<Division> divisionsComboBox;
    @ViewComponent
    private TypedTextField<BigDecimal> projectionField;
    @ViewComponent
    private TypedTextField<BigDecimal> awardedField;
    @ViewComponent
    private JmixTextArea memoField;
    @ViewComponent
    private JmixCheckbox statusField;
    @ViewComponent
    private Paragraph createdByString;

    private Appropriation appropriation;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        var bonusProjection = getEditedEntity();
        if (entityStates.isNew(bonusProjection)) {
            appropriation = appropriationService.getBfyEntryAppropriation(sessionData);
            divisionsDl.load();
            statusField.setReadOnly(true);
        } else {
            appropriation = bonusProjection.getDivision().getAppropriation();
            if (!appropriation.getStatus()) {
                readOnlyViewsSupport.setViewReadOnly(this, true);
            }
            divisionsComboBox.setReadOnly(true);
        }
        budgetFiscalYearField.setValue(appropriation.getBudgetFiscalYear());
        createdByString.setText(bonusProjection.getCreatedByString());
    }

    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    private List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.getDivisions(appropriation, false);
    }

    @Install(to = "divisionsComboBox", subject = "itemLabelGenerator")
    private Object divisionsComboBoxItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }

    @Subscribe("divisionsComboBox")
    public void onDivisionsComboBoxComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Division>, Division> event) {
        if (divisionsComboBox.getValue() == null) {
            statusField.setValue(false);
            statusField.setReadOnly(true);
        } else {
            statusField.setReadOnly(false);
        }
    }

    @Subscribe("statusField")
    public void onStatusFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {
        setFieldsReadOnly(Boolean.TRUE.equals(event.getValue()));
    }

    private void setFieldsReadOnly(boolean readOnly) {
        if (entityStates.isNew(getEditedEntity()) && divisionsComboBox.getValue() != null) {
            divisionsComboBox.setReadOnly(readOnly);
        }
        projectionField.setReadOnly(readOnly);
        awardedField.setReadOnly(readOnly);
        memoField.setReadOnly(readOnly);
    }
}