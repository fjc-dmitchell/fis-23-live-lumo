package gov.fjc.fis.view.group;

import com.vaadin.flow.component.html.Paragraph;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.Group;

import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.DivisionService;
import gov.fjc.fis.view.activityfragment.ActivityFragment;
import gov.fjc.fis.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "groups/:id", layout = MainView.class)
@ViewController("fis_Group.detail")
@ViewDescriptor("group-detail-view.xml")
@EditedEntityContainer("groupDc")
public class GroupDetailView extends StandardDetailView<Group> {
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
    @Autowired
    private Fragments fragments;

    @ViewComponent
    private CollectionLoader<Division> divisionsDl;
    @ViewComponent
    private TypedTextField<String> budgetFiscalYearField;
    @ViewComponent
    private EntityComboBox<Division> divisionsComboBox;
    @ViewComponent
    private TypedTextField<String> groupCodeField;
    @ViewComponent
    private JmixDetails activityDetails;
    @ViewComponent
    private Paragraph createdByString;

    Appropriation appropriation;
    boolean fjcFoundation;

    public void setFjcFoundation(boolean fjcFoundation) {
        this.fjcFoundation = fjcFoundation;
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        var group = getEditedEntity();
        if (entityStates.isNew(group)) {
            appropriation = appropriationService.getBfyEntryAppropriation(sessionData);
            divisionsDl.load();
            budgetFiscalYearField.setValue(appropriation.getBudgetFiscalYear());
            divisionsComboBox.focus();
        } else {
            appropriation = group.getDivision().getAppropriation();
            budgetFiscalYearField.setValue(appropriation.getBudgetFiscalYear());
            divisionsComboBox.setValue(group.getDivision());
            activityDetails.setVisible(true);
            if (!appropriation.getStatus()) {
                readOnlyViewsSupport.setViewReadOnly(this, true);
            } else {
                divisionsComboBox.setReadOnly(true);
                groupCodeField.setReadOnly(true);
            }
        }
        createdByString.setText(group.getCreatedByString());

        ActivityFragment fragment = fragments.create(this, ActivityFragment.class);
        fragment.setEntity(group);
        activityDetails.add(fragment);
    }

    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    protected List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.getDivisions(appropriation, fjcFoundation);
    }

    @Install(to = "divisionsComboBox", subject = "itemLabelGenerator")
    protected Object divisionsComboBoxItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }
}