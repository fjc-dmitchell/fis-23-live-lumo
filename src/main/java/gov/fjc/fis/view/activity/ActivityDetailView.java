package gov.fjc.fis.view.activity;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.*;
import gov.fjc.fis.service.*;
import gov.fjc.fis.view.fileattachmentfragment.FileAttachmentFragment;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.EntityStates;
import io.jmix.core.LoadContext;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "activities/:id", layout = MainView.class)
@ViewController(id = "fis_Activity.detail")
@ViewDescriptor(path = "activity-detail-view.xml")
@EditedEntityContainer("activityDc")
public class ActivityDetailView extends StandardDetailView<Activity> {
    @Autowired
    private ReadOnlyViewsSupport readOnlyViewsSupport;
    @Autowired
    private SessionData sessionData;
    @Autowired
    private EntityStates entityStates;

    /**
     * services
     */
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private FundService fundService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private BranchService branchService;
    @Autowired
    private ActivityService activityService;

    /**
     * Data Loaders
     */
    @ViewComponent
    private CollectionLoader<Fund> fundsDl;
    @ViewComponent
    private CollectionLoader<Division> divisionsDl;
    @ViewComponent
    private CollectionLoader<Division> costOrgsDl;
    @ViewComponent
    private CollectionLoader<Group> groupsDl;
    @ViewComponent
    private CollectionLoader<Branch> branchesDl;

    /**
     * view components
     */
    @ViewComponent
    private CollectionPropertyContainer<ActivityProjection> projectionsDc;
    @ViewComponent
    private CollectionPropertyContainer<ActivityReimbursement> reimbursementsDc;
    @ViewComponent
    private CollectionPropertyContainer<Obligation> obligationsDc;
    @ViewComponent
    private DataGrid<ActivityProjection> projectionsDataGrid;
    @ViewComponent
    private DataGrid<ActivityReimbursement> reimbursementsDataGrid;
    @ViewComponent
    private DataGrid<Obligation> obligationsDataGrid;
    @ViewComponent
    private EntityComboBox<Division> divisionField;
    @ViewComponent
    private EntityComboBox<Fund> fundField;
    @ViewComponent
    private EntityComboBox<Group> groupField;
    @ViewComponent
    private EntityComboBox<Branch> branchField;
    @ViewComponent
    private JmixDetails trainingDetails;
    @ViewComponent
    private JmixCheckbox trainingProjectField;
    @ViewComponent
    private JmixCheckbox canceledField;
    @ViewComponent
    private TypedTextField<String> budgetFiscalYearField;
    @ViewComponent
    private Paragraph createdByString;
    @ViewComponent
    private FileAttachmentFragment attachmentFragment;
    @ViewComponent
    private JmixCheckbox genericProjectionField;
    @ViewComponent
    private VerticalLayout tabsheetBox;
    @ViewComponent
    private TypedTextField<String> activityNumberField;
    @ViewComponent
    private HorizontalLayout costOrgBox;
    @ViewComponent
    private EntityComboBox<Division> costOrgField;
    private Appropriation entryBfy;
    private Division division;
    private Division mandatoryDivision;
    private Boolean fjcFoundation = false;

    public void setFjcFoundation(Boolean fjcFoundation) {
        this.fjcFoundation = fjcFoundation;
        if (fjcFoundation) {
            var activity = getEditedEntity();
//            fundsDl.load();
            divisionsDl.load();
//            if (entityStates.isNew(activity)) {
//                fundField.setValue(fundService.getFoundationFund());
//                fundField.setReadOnly(true);
//            }
//            fundField.setReadOnly(true);
        }
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        var activity = getEditedEntity();

        attachmentFragment.setHostEntity(activity);
        if (entityStates.isNew(activity)) {
            entryBfy = appropriationService.getBfyEntryAppropriation(sessionData);
            mandatoryDivision = divisionService.fetchMandatoryDivision(entryBfy);
            if (entryBfy != null) {
                budgetFiscalYearField.setValue(entryBfy.getBudgetFiscalYear());
            }
            divisionsDl.load();
            divisionField.focus();
            tabsheetBox.setVisible(false);

            activityNumberField.addValidator(activityNumber -> {
                if (activityService.activityNumberExists(division, activityNumber)) {
                    assert activityNumber != null;
                    throw new ValidationException(activityNumber.concat(" already exists for ").concat(division.getTitle()));
                }
            });
        } else {
            entryBfy = activity.getDivision().getAppropriation();
            mandatoryDivision = divisionService.fetchMandatoryDivision(entryBfy);
            division = activity.getDivision();
            setCostOrg(division.equals(mandatoryDivision));
            budgetFiscalYearField.setValue(entryBfy.getBudgetFiscalYear());
            divisionsDl.load();
//            divisionField.setValue(activity.getDivision());
            divisionField.setReadOnly(true);
            if (!entryBfy.getStatus()) {
                readOnlyViewsSupport.setViewReadOnly(this, true);
                attachmentFragment.setReadOnly(true);
            }
            canceledField.setVisible(trainingProjectField.getValue());
//            groupField.setVisible(false);
//            branchField.setVisible(false);
            createdByString.setText(activity.getCreatedByString());
//            costOrgBox.setVisible(division.equals(divisionService.getMandatoryDivision(entryBfy)));
        }
        costOrgsDl.load();
    }

    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    protected List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.getDivisions(entryBfy, fjcFoundation);
    }

    @Install(to = "costOrgsDl", target = Target.DATA_LOADER)
    protected List<Division> costOrgsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.getCostOrgDivisions(entryBfy);
    }

    @Install(to = "fundsDl", target = Target.DATA_LOADER)
    protected List<Fund> fundsDlLoadDelegate(final LoadContext<Fund> loadContext) {
        return fundService.getDivisionFundList(division);
    }

    @Install(to = "groupsDl", target = Target.DATA_LOADER)
    protected List<Group> groupsDlLoadDelegate(final LoadContext<Group> loadContext) {
        return groupService.fetchGroups(divisionField.getValue());
    }

    @Install(to = "branchesDl", target = Target.DATA_LOADER)
    protected List<Branch> branchesDlLoadDelegate(final LoadContext<Branch> loadContext) {
        return branchService.fetchBranches(divisionField.getValue());
    }

    @Install(to = "divisionField", subject = "itemLabelGenerator")
    protected String divisionFieldItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }

    @Install(to = "costOrgField", subject = "itemLabelGenerator")
    protected String costOrgFieldItemLabelGenerator(final Division division) {
        return division.getTitleAndBudgetOrg();
    }

    @Install(to = "fundField", subject = "itemLabelGenerator")
    protected String fundFieldItemLabelGenerator(final Fund fund) {
        return fund.getTitleAndCode();
    }

    @Install(to = "groupField", subject = "itemLabelGenerator")
    protected String groupFieldItemLabelGenerator(final Group group) {
        return group.getTitleAndCode();
    }

    @Install(to = "branchField", subject = "itemLabelGenerator")
    protected String branchFieldItemLabelGenerator(final Branch branch) {
        return branch.getTitleAndCode();
    }

    private void setCostOrg(boolean mandatory) {
        costOrgBox.setVisible(mandatory);
        costOrgField.setRequired(mandatory);
        if(!mandatory) {
            costOrgField.setValue(null);
        }
    }

    @Subscribe("divisionField")
    protected void onDivisionFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Division>, Division> event) {
        if (divisionField.isEmpty()) {
            division = event.getOldValue();
            divisionField.setValue(division);
        } else {
            division = event.getValue();
        }


        setCostOrg(division.equals(mandatoryDivision));

        if (activityService.activityNumberExists(division, activityNumberField.getValue())) {
            activityNumberField.focus();
            activityNumberField.setValue("");
        }

        if (entityStates.isNew(getEditedEntity())) {
            fundsDl.load();
            fundField.setValue(division.getFund());
            groupField.setValue(null);
            groupsDl.load();
            branchField.setValue(null);
            branchesDl.load();
        }
        branchField.setVisible(branchService.branchesExist(divisionField.getValue()));
        groupField.setVisible(groupService.groupsExist(divisionField.getValue()));
        tabsheetBox.setVisible(division != null);
    }

    @Subscribe("activityNumberField")
    protected void onActivityNumberFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<TypedTextField<?>, ?> event) {
        if (entityStates.isNew(getEditedEntity()) && !activityNumberField.isEmpty()) {
            groupField.setValue(groupService.getGroupByActivity(division, activityNumberField.getValue()));
        }
    }

    @Subscribe("trainingProjectField")
    protected void onTrainingProjectFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixCheckbox, ?> event) {
        trainingDetails.setOpened(Boolean.TRUE.equals(trainingProjectField.getValue()));
        trainingDetails.setEnabled(Boolean.TRUE.equals(trainingProjectField.getValue()));
        trainingDetails.setVisible(Boolean.TRUE.equals(trainingProjectField.getValue()));
        canceledField.setVisible(Boolean.TRUE.equals(trainingProjectField.getValue()));
    }

    @Subscribe(id = "projectionsDc", target = Target.DATA_CONTAINER)
    protected void onProjectionsDcCollectionChange(final CollectionContainer.CollectionChangeEvent<ActivityProjection> event) {
        projectionsDataGrid.setAggregatable(projectionsDc.getItems().size() > 1);
        genericProjectionField.setReadOnly(!projectionsDc.getItems().isEmpty());
        divisionField.setReadOnly(!projectionsDc.getItems().isEmpty() || !reimbursementsDc.getItems().isEmpty());
    }

    @Subscribe(id = "reimbursementsDc", target = Target.DATA_CONTAINER)
    protected void onReimbursementsDcCollectionChange(final CollectionContainer.CollectionChangeEvent<ActivityReimbursement> event) {
        reimbursementsDataGrid.setAggregatable(reimbursementsDc.getItems().size() > 1);
        divisionField.setReadOnly(!projectionsDc.getItems().isEmpty() || !reimbursementsDc.getItems().isEmpty());
    }

    @Subscribe(id = "obligationsDc", target = Target.DATA_CONTAINER)
    protected void onObligationsDcCollectionChange(final CollectionContainer.CollectionChangeEvent<Obligation> event) {
        obligationsDataGrid.setAggregatable(obligationsDc.getItems().size() > 1);
    }
}