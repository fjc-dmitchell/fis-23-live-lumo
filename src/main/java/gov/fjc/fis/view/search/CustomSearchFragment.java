package gov.fjc.fis.view.search;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import gov.fjc.fis.entity.*;
import gov.fjc.fis.event.FiscalYearChangeEvent;
import gov.fjc.fis.event.SearchGridSelectedItemsEvent;
import gov.fjc.fis.service.*;
import io.jmix.core.LoadContext;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.impl.CollectionContainerImpl;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@FragmentDescriptor("custom-search-fragment.xml")
public class CustomSearchFragment extends Fragment<VerticalLayout> {
    @Autowired
    private SessionData sessionData;
    @Autowired
    private Fragments fragments;

    /**
     * data containers
     */
    @ViewComponent
    private CollectionContainer<Division> divisionsDc;
    @ViewComponent
    private CollectionContainer<FileAttachmentCategory> fileAttachmentCategoriesDc;

    /**
     * data loaders
     */
    @ViewComponent
    private CollectionLoader<Division> divisionsDl;
    @ViewComponent
    private CollectionLoader<Fund> fundsDl;
    @ViewComponent
    private CollectionLoader<ObjectCategory> categoriesDl;
    @ViewComponent
    private CollectionLoader<ObjectClass> objectClassesDl;
    @ViewComponent
    private CollectionLoader<Branch> branchesDl;
    @ViewComponent
    private CollectionLoader<Group> groupsDl;
    @ViewComponent
    private CollectionLoader<FileAttachmentCategory> fileAttachmentCategoriesDl;

    /**
     * services
     */
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private FundService fundService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private ObjectCategoryService categoryService;
    @Autowired
    private ObjectClassService objectClassService;
    @Autowired
    private BranchService branchService;
    @Autowired
    private GroupService groupService;

    /**
     * components
     */
    @ViewComponent("searchTabSheet.customSearchTab")
    private Tab searchTabSheetCustomSearchTab;
    @ViewComponent("searchTabSheet.subsetTab")
    private Tab searchTabSheetSubsetTab;
    @ViewComponent
    private VerticalLayout subFragmentSearchBox;
    @ViewComponent
    private JmixButton showBfyBtn;
    @ViewComponent
    private JmixButton customSearchBtn;
    @ViewComponent
    private TypedTextField<String> fiscalYearsField;
    @ViewComponent
    private JmixButton showDiv1Btn;
    @ViewComponent
    private JmixButton showDiv2Btn;
    @ViewComponent
    private JmixButton showDiv3Btn;
    @ViewComponent
    private JmixButton showDiv4Btn;
    @ViewComponent
    private JmixButton showDiv5Btn;
    @ViewComponent
    private JmixButton showDiv6Btn;
    @ViewComponent
    private JmixButton showDiv7Btn;
    @ViewComponent
    private JmixButton showDiv8Btn;
    @ViewComponent
    private JmixButton showDiv9Btn;
    @ViewComponent
    private JmixButton showGroupBtn;
    @ViewComponent
    private JmixButton showSubsetBtn;
    @ViewComponent
    private JmixButton showBranchBtn;
    @ViewComponent
    private JmixButton showActivityBtn;
    @ViewComponent
    private JmixTabSheet searchTabSheet;
    @ViewComponent
    private EntityComboBox<Fund> fundSearchField;
    @ViewComponent
    private EntityComboBox<Division> divisionSearchField;
    @ViewComponent
    private EntityComboBox<ObjectCategory> categorySearchField;
    @ViewComponent
    private EntityComboBox<ObjectClass> objectClassSearchField;
    @ViewComponent
    private EntityComboBox<Branch> branchSearchField;
    @ViewComponent
    private EntityComboBox<Group> groupSearchField;
    @ViewComponent
    private JmixMultiSelectComboBoxPicker<FileAttachmentCategory> fileCategorySearchField;
    @ViewComponent
    private HorizontalLayout divisionSearchButtons;
    @ViewComponent
    private HorizontalLayout divFundBox;

    /**
     * instance variables
     */
    private CollectionContainer<?> hostContainer;
    private CollectionLoader<?> hostLoader;
    private String hostEntityName;
    private String hostEntityQuery;
    private Class hostEntityClass;
    private String fundJoin;
    private String appropriationJoin;
    private String divisionJoin;
    private String activityJoin;
    private String categoryJoin;
    private String objectClassJoin;
    private String obligationJoin;
    private String branchJoin;
    private String groupJoin;
    private String fileCategoryJoin;
    private EntitySearchFragment subFragment;
    private List<Appropriation> fiscalYears;
    private List<Appropriation> searchYears;
    private String divisionCode;
    private String majorObjectClass;
    private boolean fjcFoundation;
    private Fund fjcFoundationFund;
    private int firstResult;
    private Integer tabIdx;
    private Map<String, Object> sessionSearchParams;
    private DataGrid<?> dataGrid;
    private List<Integer> subsetIds;
    private Group relatedGroup;
    private Branch relatedBranch;
    private Activity relatedActivity;
    private String subsetButtonId;
    private Map<JmixButton, String> buttonDivisionCodeMap;

    /**
     * host view constants
     */
    private static final String DIVISION = "fis_Division";
    private static final String BRANCH = "fis_Branch";
    private static final String GROUP = "fis_Group";
    private static final String ACTIVITY = "fis_Activity";
    private static final String ACTIVITY_PROJECTION = "fis_ActivityProjection";
    private static final String ACTIVITY_REIMBURSEMENT = "fis_ActivityReimbursement";
    private static final String OBLIGATION = "fis_Obligation";
    private static final String INVOICE = "fis_Invoice";
    private static final String FCN = "fis_FundControlNotice";
    private static final String CATEGORY = "fis_ObjectCategory";
    private static final String OBJECTCLASS = "fis_ObjectClass";
    private static final String FILE_ATTACHMENT = "fis_FileAttachment";
//    private static final String PAY_PERIOD = "fis_PayPeriod";


    /**
     * The hostDataContainer property must be explicitly set by the host invoking the fragment.
     * The hostDataContainer must have a dataLoader
     *
     * @param hostDataContainer dataContainer of host view
     */
    public void setHostDataContainer(CollectionContainer<?> hostDataContainer) {
        hostContainer = hostDataContainer;
        hostLoader = (CollectionLoader<?>) ((CollectionContainerImpl<?>) hostDataContainer).getLoader();
        if (hostLoader == null) {
            throw new IllegalStateException("hostLoader is null in SearchFragment");
        }
        hostEntityName = hostDataContainer.getEntityMetaClass().getName();
    }

    /**
     * fjcFoundation is optional but is needed by most entities
     *
     * @param fjcFoundation limits search to Foundation entities
     */
    public void setFjcFoundation(boolean fjcFoundation) {
        this.fjcFoundation = fjcFoundation;
    }

    /**
     * optional, used by the subset search
     *
     * @param dataGrid
     */
    public void setDataGrid(DataGrid<?> dataGrid) {
        this.dataGrid = dataGrid;
        searchTabSheetSubsetTab.setVisible(true);
    }

    @Subscribe(target = Target.HOST_CONTROLLER)
    public void onHostInit(final View.InitEvent event) {
        buttonDivisionCodeMap = Map.of(
                showDiv1Btn, "1",
                showDiv2Btn, "2",
                showDiv3Btn, "3",
                showDiv4Btn, "4",
                showDiv5Btn, "5",
                showDiv6Btn, "6",
                showDiv7Btn, "7",
                showDiv8Btn, "8",
                showDiv9Btn, "9"
        );
    }

    @Subscribe(target = Target.HOST_CONTROLLER)
    protected void onHostAttach(final AttachEvent event) {
        if (hostContainer == null) {
            throw new IllegalStateException("hostContainer is null in SearchFragment");
        }
        fiscalYears = appropriationService.getBfyFilterField(sessionData);
        fjcFoundationFund = fundService.getFoundationFund();
        setShortcut();
    }

    @Subscribe(target = Target.HOST_CONTROLLER)
    protected void onHostReady(final View.ReadyEvent event) {
        configureHostEntity();
        if (fjcFoundation) {
            fundSearchField.setValue(fjcFoundationFund);
            fundSearchField.setReadOnly(true);
            // set visibility of division box?
        }
        restoreSearchParameters();
    }

    private void configureHostEntity() {
        hostEntityQuery = "SELECT e FROM ".concat(hostEntityName).concat(" e");
        switch (hostEntityName) {
            case ACTIVITY:
                hostEntityClass = Activity.class;
                hostLoader.setFetchPlan("activity-search-fetch-plan");
                hostEntityQuery += " ORDER BY e.division.appropriation.budgetFiscalYear, e.division.divisionCode, e.activityNumber";
                fundJoin = "JOIN {E}.fund f";
                appropriationJoin = "JOIN {E}.division dv JOIN dv.appropriation app";
                divisionJoin = "JOIN {E}.division dv";
                categoryJoin = "JOIN {E}.projections p JOIN p.objectClass obj JOIN obj.objectCategory cat";
                objectClassJoin = "JOIN {E}.projections p JOIN p.objectClass obj";
                branchJoin = "JOIN {E}.branch bch";
                groupJoin = "JOIN {E}.group grp";
                configureSubFragment(ActivitySearchFragment.class, "activitiesDc", "activitiesDl");
                showActivityBtn.setText("Show Generic Activity");
                break;
            case ACTIVITY_PROJECTION:
                appropriationJoin = "JOIN {E}.activity act JOIN act.division dv JOIN dv.appropriation app";
                divisionJoin = "JOIN {E}.activity act JOIN act.division dv";
                break;
            case ACTIVITY_REIMBURSEMENT:
                appropriationJoin = "JOIN {E}.activity act JOIN act.division dv JOIN dv.appropriation app";
                divisionJoin = "JOIN {E}.activity act JOIN act.division dv";
                break;
            case OBLIGATION:
                hostEntityClass = Obligation.class;
                hostLoader.setFetchPlan("obligation-search-fetch-plan");
                hostEntityQuery += " ORDER BY e.activity.division.appropriation.budgetFiscalYear, e.activity.division.divisionCode, e.documentNumber, e.objectClass.budgetObjectClass";
                fundJoin = "JOIN {E}.activity act JOIN act.fund f";
                appropriationJoin = "JOIN {E}.activity act JOIN act.division dv JOIN dv.appropriation app";
                divisionJoin = "JOIN {E}.activity act JOIN act.division dv";
                activityJoin = "JOIN {E}.activity act";
                categoryJoin = "JOIN {E}.objectClass obj JOIN obj.objectCategory cat";
                objectClassJoin = "JOIN {E}.objectClass obj";
                branchJoin = "JOIN {E}.activity act JOIN act.branch bch";
                groupJoin = "JOIN {E}.activity act JOIN act.group grp";
                configureSubFragment(ObligationSearchFragment.class, "obligationsDc", "obligationsDl");
                break;
            case INVOICE:
                hostLoader.setFetchPlan("invoice-search-fetch-plan");
                hostEntityQuery += " ORDER BY e.obligation.activity.division.appropriation.budgetFiscalYear, e.obligation.activity.division.divisionCode, e.obligation.documentNumber, e.obligation.objectClass.budgetObjectClass, e.invoiceNumber";
                fundJoin = "JOIN {E}.obligation obl JOIN obl.activity act JOIN act.fund f";
                appropriationJoin = "JOIN {E}.obligation obl JOIN obl.activity act JOIN act.division dv JOIN dv.appropriation app";
                divisionJoin = "JOIN {E}.obligation obl JOIN obl.activity act JOIN act.division dv";
                categoryJoin = "JOIN {E}.obligation obl JOIN obl.objectClass obj JOIN obj.objectCategory cat";
                objectClassJoin = "JOIN {E}.obligation obl JOIN obl.objectClass obj";
                obligationJoin = "JOIN {E}.obligation obl";
                activityJoin = "JOIN {E}.obligation obl JOIN obl.activity act";
                branchJoin = "JOIN {E}.obligation obl JOIN obl.activity act JOIN act.branch bch";
                groupJoin = "JOIN {E}.obligation obl JOIN obl.activity act JOIN act.group grp";
                configureSubFragment(InvoiceSearchFragment.class, "invoicesDc", "invoicesDl");
                break;
            case FCN:
                hostLoader.setFetchPlan("fundControlNotice-search-fetch-plan");
                hostEntityQuery += " ORDER BY e.obligation.activity.division.appropriation.budgetFiscalYear, e.obligation.activity.division.divisionCode, e.obligation.documentNumber, e.obligation.objectClass.budgetObjectClass, e.fcnDate";
                obligationJoin = "JOIN {E}.obligation obl";
                objectClassJoin = obligationJoin.concat(" JOIN obl.objectClass obj");
                categoryJoin = objectClassJoin.concat(" JOIN obj.objectCategory cat");
                activityJoin = obligationJoin.concat(" JOIN obl.activity act");
                fundJoin = activityJoin.concat(" JOIN act.fund f");
                divisionJoin = activityJoin.concat(" JOIN act.division dv");
                appropriationJoin = divisionJoin.concat(" JOIN dv.appropriation app");
                branchJoin = obligationJoin.concat(" JOIN obl.activity act JOIN act.branch bch");
                groupJoin = obligationJoin.concat(" JOIN obl.activity act JOIN act.group grp");
                configureSubFragment(FcnSearchFragment.class, "fundControlNoticesDc", "fundControlNoticesDl");
                break;
            case DIVISION:
//                hostEntityQuery += " ORDER BY e.appropriation.budgetFiscalYear, e.divisionCode";
                appropriationJoin = "JOIN {E}.appropriation app";
                hostEntityQuery = "SELECT dv FROM fis_Division dv";
//                hostEntityQuery = "SELECT e FROM  e ORDER BY e.appropriation.budgetFiscalYear, e.divisionCode";
                fundJoin = "JOIN dv.fund f";
                break;
            case BRANCH:
                hostEntityQuery += " ORDER BY e.division.appropriation.budgetFiscalYear, e.division.divisionCode, e.branchCode";
                fundJoin = "JOIN {E}.division.fund f";
                appropriationJoin = "JOIN {E}.division dv JOIN dv.appropriation app";
                divisionJoin = "JOIN {E}.division d";
                break;
            case GROUP:
                hostEntityQuery += " ORDER BY e.division.appropriation.budgetFiscalYear, e.division.divisionCode, e.groupCode";
                fundJoin = "JOIN {E}.division.fund f";
                appropriationJoin = "JOIN {E}.division dv JOIN dv.appropriation app";
                divisionJoin = "JOIN {E}.division d";
                break;
            case CATEGORY:
                hostEntityQuery += " ORDER BY e.appropriation.budgetFiscalYear, e.majorObjectClass";
                fundJoin = null;
                appropriationJoin = "JOIN {E}.appropriation app";
//                hostEntityQuery = "SELECT cat FROM fis_Category cat ORDER BY cat.appropriation.budgetFiscalYear, cat.masterObjectClass";
                divisionSearchButtons.setVisible(false);
                break;
            case OBJECTCLASS:
                hostLoader.setFetchPlan("objectClass-search-fetch-plan");
                hostEntityQuery += " ORDER BY e.objectCategory.appropriation.budgetFiscalYear, e.objectCategory.majorObjectClass, e.budgetObjectClass";
                fundJoin = null;
                appropriationJoin = "JOIN {E}.objectCategory cat JOIN cat.appropriation app";
                categoryJoin = "JOIN {E}.objectCategory cat";
//                hostEntityQuery = "SELECT o FROM fis_ObjectClass o";
//                hostEntityQuery += " JOIN o.category d";
//                hostEntityQuery += " ORDER BY d.appropriation.budgetFiscalYear, d.masterObjectClass, o.budgetObjectClass";
                divisionSearchButtons.setVisible(false);
//                searchTabSheetCustomSearchTab.setVisible(true);
                divFundBox.setVisible(false);
//                bocBox.setVisible(true);
                break;
//            case PAY_PERIOD:
////                hostEntityClass = PayPeriod.class;
//                hostEntityQuery += " ORDER BY e.appropriation.budgetFiscalYear DESC, e.payPeriod ASC";
//                fundJoin = null;
//                appropriationJoin = "JOIN {E}.appropriation app";
//                divisionSearchButtons.setVisible(false);
//                break;
            case FILE_ATTACHMENT:
                hostEntityClass = FileAttachment.class;
                fundJoin = "JOIN {E}.activity.fund f";
                appropriationJoin = "JOIN {E}.activity.division dv JOIN dv.appropriation app";
                divisionJoin = "JOIN {E}.activity.division dv";
                activityJoin = "JOIN {E}.activity act";
                branchJoin = "JOIN {E}.activity act JOIN act.branch bch";
                groupJoin = "JOIN {E}.activity act JOIN act.group grp";
                fileCategoryJoin = "JOIN {E}.category fcat";
                configureSubFragment(FileAttachmentSearchFragment.class, "fileAttachmentsDc", "fileAttachmentsDl");
                break;
            default:
                throw new IllegalStateException(hostEntityName.concat(" has not been configured in CustomSearchFragment"));
        }
    }

    private void configureSubFragment(Class<? extends EntitySearchFragment> fragmentClass,
                                      String dataContainerId,
                                      String dataLoaderId) {
        this.getFragmentData().registerContainer(dataContainerId, hostContainer);
        this.getFragmentData().registerLoader(dataLoaderId, hostLoader);

        subFragment = fragments.create(this, fragmentClass);
//        subFragment = (EntitySearchFragment) fragments.create(this, fragmentClass);
        subFragment.addCategoryObjectClass(categorySearchField, objectClassSearchField);
        subFragment.addBranchGroup(branchSearchField, groupSearchField);
        subFragment.addFileCategory(fileCategorySearchField);

        subFragmentSearchBox.add(subFragment);
        subFragmentSearchBox.setVisible(true);
        searchTabSheetCustomSearchTab.setVisible(true);
    }

    private void setBfyBtnCaption() {
        String bfyList = fiscalYears.stream()
                .map(Appropriation::getBudgetFiscalYear)
                .collect(Collectors.joining(", "));

        String caption = (fiscalYears.size() == 1)
                ? "Show all for " + bfyList
                : "Show Search BFYs";

        showBfyBtn.setText(caption);
        fiscalYearsField.setValue(bfyList);
    }

    @Subscribe(id = "divisionsDc", target = Target.DATA_CONTAINER)
    public void onDivisionsDcCollectionChange(final CollectionContainer.CollectionChangeEvent<Division> event) {
        Set<String> divisionCodes = divisionsDc.getItems().stream()
                .map(Division::getDivisionCode)
                .collect(Collectors.toSet());

        buttonDivisionCodeMap.forEach((btn, code) ->
                btn.setVisible(divisionCodes.contains(code)));
    }

    @Install(to = "fundsDl", target = Target.DATA_LOADER)
    protected List<Fund> fundsDlLoadDelegate(final LoadContext<Fund> loadContext) {
        return fundService.fetchFundSearchList(fjcFoundation);
    }

    @Install(to = "divisionsDl", target = Target.DATA_LOADER)
    protected List<Division> divisionsDlLoadDelegate(final LoadContext<Division> loadContext) {
        return divisionService.fetchDivisionSearchList(fiscalYears, fjcFoundation);
    }

    @Install(to = "categoriesDl", target = Target.DATA_LOADER)
    protected List<ObjectCategory> categoriesDlLoadDelegate(final LoadContext<ObjectCategory> loadContext) {
        return categoryService.fetchCategorySearchList(fiscalYears);
    }

    @Install(to = "objectClassesDl", target = Target.DATA_LOADER)
    protected List<ObjectClass> objectClassesDlLoadDelegate(final LoadContext<ObjectClass> loadContext) {
        return objectClassService.fetchObjectClassSearchList(fiscalYears, majorObjectClass, false);
    }

    @Install(to = "branchesDl", target = Target.DATA_LOADER)
    protected List<Branch> branchesDlLoadDelegate(final LoadContext<Branch> loadContext) {
        return branchService.fetchBranchSearchList(fiscalYears, divisionCode);
    }

    @Install(to = "groupsDl", target = Target.DATA_LOADER)
    protected List<Group> groupsDlLoadDelegate(final LoadContext<Group> loadContext) {
        return groupService.fetchGroupSearchList(fiscalYears, divisionCode);
    }

    @Install(to = "divisionSearchField", subject = "itemLabelGenerator")
    protected Object divisionSearchFieldItemLabelGenerator(final Division division) {
        return division.getTitleAndCode();
    }

    @Install(to = "categorySearchField", subject = "itemLabelGenerator")
    protected Object categorySearchFieldItemLabelGenerator(final ObjectCategory category) {
        return category.getTitleAndCode();
    }

    @Install(to = "objectClassSearchField", subject = "itemLabelGenerator")
    protected Object objectClassSearchFieldItemLabelGenerator(final ObjectClass objectClass) {
        return objectClass.getTitleAndCode();
    }

    @Install(to = "branchSearchField", subject = "itemLabelGenerator")
    protected Object branchSearchFieldItemLabelGenerator(final Branch branch) {
        return branch.getTitleAndCode();
    }

    @Install(to = "groupSearchField", subject = "itemLabelGenerator")
    protected Object groupSearchFieldItemLabelGenerator(final Group group) {
        return group.getTitleAndCode();
    }

    @Subscribe("divisionSearchField")
    protected void onDivisionSearchFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Division>, Division> event) {
        divisionCode = Optional.ofNullable(event.getValue())
                .map(Division::getDivisionCode)
                .orElse(null);

        refreshField(branchesDl, branchSearchField, Branch::getBranchCode);
        refreshField(groupsDl, groupSearchField, Group::getGroupCode);
    }

    @Subscribe("categorySearchField")
    protected void onCategorySearchFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<EntityComboBox<ObjectCategory>, ObjectCategory> event) {

        var category = event.getValue();

        if (category == null) {
            majorObjectClass = null;
            objectClassesDl.load();
            return;
        }

        majorObjectClass = category.getMajorObjectClass();

        var selectedObjectClass = objectClassSearchField.getValue();
        if (selectedObjectClass != null &&
                !majorObjectClass.equals(selectedObjectClass.getObjectCategory().getMajorObjectClass())) {
            objectClassSearchField.setValue(null);
        }

        objectClassesDl.load();
    }

    @Subscribe("showDivisionAction")
    public void onShowDivisionAction(final ActionPerformedEvent event) {
        buttonDivisionCodeMap.entrySet().stream()
                .filter(entry -> entry.getKey() == event.getComponent())
                .map(Map.Entry::getValue)
                .findFirst()
                .ifPresent(code -> {
                    clearCustomSearchParameters();
                    divisionCode = code;
                    hostLoader.setParameter("divCodeFilterField", divisionCode);
                    performSearch();
                });
    }

    @Subscribe(id = "showBfyBtn", subject = "clickListener")
    protected void onShowBfyBtnClick(final ClickEvent<JmixButton> event) {
        divisionCode = null;
        clearCustomSearchParameters();
        sessionSearchParams.clear(); // is this correct? probably
        performSearch();
    }

    @Subscribe("searchTabSheet")
    protected void onSearchTabSheetSelectedChange(final JmixTabSheet.SelectedChangeEvent event) {
        tabIdx = event.getSource().getSelectedIndex();
        if (dataGrid != null) {
            dataGrid.setMultiSelect(tabIdx.equals(2));
        }
        setShortcut();
    }

    private void setShortcut() {
        if (tabIdx == null) {
            tabIdx = 0;
        }
        switch (tabIdx) {
            case 0 -> showBfyBtn.addClickShortcut(Key.ENTER)
                    .resetFocusOnActiveElement();
            case 1 -> customSearchBtn.addClickShortcut(Key.ENTER)
                    .resetFocusOnActiveElement();
        }
    }

    private void setSubsetLoaderParameters(String btnId) {
        clearCustomSearchParameters();
        removeSubsetLoaderParameters();

        switch (btnId) {
            case "showSubsetBtn":
                hostLoader.setParameter("idList", subsetIds);
                break;
            case "showGroupBtn":
                hostLoader.setParameter("relatedGroup", relatedGroup);
                break;
            case "showBranchBtn":
                hostLoader.setParameter("relatedBranch", relatedBranch);
                break;
            case "showActivityBtn":
                switch (hostEntityName) {
                    case ACTIVITY:
                        hostLoader.removeParameter("relatedActivity");
                        hostLoader.setParameter("genericActivityNumber", relatedActivity.getGenericActivityNumber());
                        hostLoader.setParameter("relatedActivityDivision", relatedActivity.getDivision());
                        break;
                    case OBLIGATION:
                    case INVOICE:
                    case FCN:
                    case FILE_ATTACHMENT:
                        hostLoader.setParameter("relatedActivity", relatedActivity);
                        break;
                }
                break;
        }
        performSearch();
        dataGrid.deselectAll();
    }

    @Subscribe("showSubsetAction")
    protected void onShowSubsetAction(final ActionPerformedEvent event) {
        FragmentUtils.getComponentId(event.getComponent()).ifPresent(componentId -> {
            subsetButtonId = componentId;
            if (subsetButtonId.equals("showSubsetBtn")) {
                var selectedItems = dataGrid.getSelectedItems();
                subsetIds = null;
                switch (hostEntityName) {
                    case ACTIVITY -> subsetIds = selectedItems.stream()
                            .map(item -> (Activity) item)
                            .map(Activity::getId)
                            .toList();
                    case OBLIGATION -> subsetIds = selectedItems.stream()
                            .map(item -> (Obligation) item)
                            .map(Obligation::getId)
                            .toList();
                    case INVOICE -> subsetIds = selectedItems.stream()
                            .map(item -> (Invoice) item)
                            .map(Invoice::getId)
                            .toList();
                    case FCN -> subsetIds = selectedItems.stream()
                            .map(item -> (FundControlNotice) item)
                            .map(FundControlNotice::getId)
                            .toList();
                    case FILE_ATTACHMENT -> subsetIds = selectedItems.stream()
                            .map(item -> (FileAttachment) item)
                            .map(FileAttachment::getId)
                            .toList();
                }
            }
            setSubsetLoaderParameters(subsetButtonId);
        });
    }

    @Subscribe(id = "customSearchBtn", subject = "clickListener")
    protected void onCustomSearchBtnClick(final ClickEvent<JmixButton> event) {
//        hostLoader.setParameter("bfyFilterField", searchYears);
        setLoaderParameters();
        performSearch();
    }

    private void removeSubsetLoaderParameters() {
        hostLoader.removeParameter("idList");
        hostLoader.removeParameter("relatedGroup");
        hostLoader.removeParameter("relatedBranch");
        hostLoader.removeParameter("relatedActivity");
        hostLoader.removeParameter("relatedActivityDivision");
        hostLoader.removeParameter("genericActivityNumber");
    }

    private void setLoaderParameters() {
        removeSubsetLoaderParameters();

        setOrRemoveParameter("fundFilterField", fundSearchField.getValue(), v -> v);
        setOrRemoveParameter("divCodeFilterField", divisionSearchField.getValue(), Division::getDivisionCode);
        setOrRemoveParameter("mocFilterField", categorySearchField.getValue(), ObjectCategory::getMajorObjectClass);
        setOrRemoveParameter("bocFilterField", objectClassSearchField.getValue(), ObjectClass::getBudgetObjectClass);
        setOrRemoveParameter("branchCodeFilterField", branchSearchField.getValue(), Branch::getBranchCode);
        setOrRemoveParameter("groupCodeFilterField", groupSearchField.getValue(), Group::getGroupCode);

        // Kept separate — uses isEmpty() rather than a null check
        if (!fileCategorySearchField.getValue().isEmpty()) {
            hostLoader.setParameter("fileCategoryFilterField", fileCategorySearchField.getValue());
        } else {
            hostLoader.removeParameter("fileCategoryFilterField");
        }
    }

    private <T> void setOrRemoveParameter(String key, T value, Function<T, Object> extractor) {
        if (value != null) {
            hostLoader.setParameter(key, extractor.apply(value));
        } else {
            hostLoader.removeParameter(key);
        }
    }

    @Subscribe(id = "clearSearchBtn", subject = "clickListener")
    protected void onClearSearchBtnClick(final ClickEvent<JmixButton> event) {
        clearSearchFields();
    }

    private void clearSearchFields() {
        divisionSearchField.setValue(null);
        categorySearchField.setValue(null);
        objectClassSearchField.setValue(null);
        fileCategorySearchField.setValue(Set.of());
        if (!fjcFoundation) {
            fundSearchField.setValue(null);
        }
        if (subFragment != null) {
            subFragment.clearPropertyFilters();
        }
    }

    private void clearCustomSearchParameters() {
        // remove query conditions from data loader
        Set<String> params = new HashSet<>(hostLoader.getParameters().keySet());
        params.forEach(hostLoader::removeParameter);

        clearSearchFields();

//        customFilters.forEach((key, value) -> value.setValue(null));
    }

    private void saveSearchParameters() {

        if (sessionSearchParams == null) {
            sessionSearchParams = new HashMap<>();
        }

        tabIdx = tabIdx == null ? 0 : tabIdx;

        sessionSearchParams.put("tab", tabIdx.toString());

        sessionSearchParams.put("bfyFilterField", fiscalYears);
        sessionSearchParams.put("quick_divisionCode", divisionCode);

//        sessionSearchParams.put("bfyFilterField", fiscalYears);
        sessionSearchParams.put("custom_fund", fundSearchField.getValue());
        sessionSearchParams.put("custom_division", divisionSearchField.getValue());
        sessionSearchParams.put("custom_category", categorySearchField.getValue());
        sessionSearchParams.put("custom_objectClass", objectClassSearchField.getValue());
        sessionSearchParams.put("custom_branch", branchSearchField.getValue());
        sessionSearchParams.put("custom_group", groupSearchField.getValue());
//        sessionSearchParams.put("custom_fileCategory", fileCategorySearchField.getValue());


        // Extract selected category IDs (Integer)
        Set<Integer> ids = fileCategorySearchField.getValue()
                .stream()
                .map(FileAttachmentCategory::getId)
                .collect(Collectors.toSet());

        // Serialize
        String serialized = ids.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

        sessionSearchParams.put("custom_fileCategory", serialized);


        sessionSearchParams.put("subset_idList", subsetIds);
        sessionSearchParams.put("related_group", relatedGroup);
        sessionSearchParams.put("related_branch", relatedBranch);
        sessionSearchParams.put("related_activity", relatedActivity);
        sessionSearchParams.put("subset_button_id", subsetButtonId);

        if (subFragment != null) {
            List<PropertyFilter<?>> propertyFilters = subFragment.getPropertyFilters();
            for (PropertyFilter<?> filter : propertyFilters) {
                var name = filter.getProperty();
                if (name != null) {
                    String value = filter.getValue() != null ? filter.getValue().toString() : null;
                    Object filterValue = filter.getValue();
                    sessionSearchParams.put(name, filterValue);
                    if (filter.isOperationEditable()) {
                        sessionSearchParams.put(name.concat("_op"), filter.getOperation());
                    }
                }
            }
        }

        sessionData.setAttribute(hostEntityName.concat(".searchParams"), sessionSearchParams);
    }

    private void loadEntityComboBoxes() {
        setBfyBtnCaption();
        fundsDl.load();
        divisionsDl.load();
        categoriesDl.load();
        objectClassesDl.load();
        branchesDl.load();
        groupsDl.load();
        fileAttachmentCategoriesDl.load();
    }



    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> castToMap(Object obj) {
        return (Map<K, V>) obj;
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> castToList(Object obj) {
        return (List<T>) obj;
    }

    private void restoreSearchParameters() {
        // get entity search parameters
//        sessionSearchParams = (Map<String, Object>) sessionData.getAttribute(hostEntityName.concat(".searchParams"));
        sessionSearchParams = castToMap(sessionData.getAttribute(hostEntityName.concat(".searchParams")));
        fiscalYears = appropriationService.getBfyFilterField(sessionData);

        if (sessionSearchParams == null) {
            loadEntityComboBoxes();
            searchYears = fiscalYears;
        } else {
//            searchYears = (List<Appropriation>) sessionSearchParams.get("bfyFilterField");
            searchYears = castToList(sessionSearchParams.get("bfyFilterField"));
            loadEntityComboBoxes();

            var tabParam = sessionSearchParams.get("tab");
            if (tabParam != null) {
                var tabIdx = Integer.parseInt((String) tabParam);
                searchTabSheet.setSelectedIndex(tabIdx);

                // quick search
                if (tabIdx == 0) {
                    divisionCode = (String) sessionSearchParams.get("quick_divisionCode");
                    if (divisionCode != null) {
                        hostLoader.setParameter("divCodeFilterField", divisionCode);
                    }
                }

                // custom search
                if (tabIdx == 1) {
                    sessionSearchParams.remove("quick_divisionCode");// shouldn't be necessary

                    divisionSearchField.setValue((Division) sessionSearchParams.get("custom_division"));
                    fundSearchField.setValue((Fund) sessionSearchParams.get("custom_fund"));
                    categorySearchField.setValue((ObjectCategory) sessionSearchParams.get("custom_category"));
                    objectClassSearchField.setValue((ObjectClass) sessionSearchParams.get("custom_objectClass"));
                    groupSearchField.setValue((Group) sessionSearchParams.get("custom_group"));
                    branchSearchField.setValue((Branch) sessionSearchParams.get("custom_branch"));
//                    fileCategorySearchField.setValue((FileAttachmentCategory) sessionSearchParams.get("custom_fileCategory"));

                    String fileCategoryIds = (String) sessionSearchParams.get("custom_fileCategory");

                    if (!(fileCategoryIds == null || fileCategoryIds.isBlank())) {
                        // Convert comma-separated IDs to List<Integer>
                        List<Integer> ids = Arrays.stream(fileCategoryIds.split(","))
                                .map(Integer::valueOf)
                                .toList();

                        // Find matching entities in the existing container
                        List<FileAttachmentCategory> selected = fileAttachmentCategoriesDc.getItems()
                                .stream()
                                .filter(c -> ids.contains(c.getId()))
                                .toList();

                        fileCategorySearchField.setValue(new HashSet<>(selected));
                    }

                    setLoaderParameters();

                    if (subFragment != null) {
                        subFragment.setPropertyFilters(sessionSearchParams);
                    }
                }

                if (tabIdx == 2) {
//                    subsetIds = (List<Integer>) sessionSearchParams.get("subset_idList");
                    subsetIds = castToList(sessionSearchParams.get("subset_idList"));
                    relatedGroup = (Group) sessionSearchParams.get("related_group");
                    relatedBranch = (Branch) sessionSearchParams.get("related_branch");
                    relatedActivity = (Activity) sessionSearchParams.get("related_activity");
                    subsetButtonId = (String) sessionSearchParams.get("subset_button_id");

                    setSubsetLoaderParameters(subsetButtonId);
                }
            }
        }

        performSearch();

        // changeFiscalYears() ?
        if (!searchYears.equals(fiscalYears)) {
            changeFiscalYears();
            searchYears = fiscalYears;
            loadEntityComboBoxes();
        }
    }

    private void performSearch() {
        List<Condition> customConditions = new ArrayList<>();
        List<Condition> subFragmentConditions;

        if (tabIdx != null && tabIdx.equals(2)) {
            customConditions.add(JpqlCondition.create("e.id in :idList", null).skipNullOrEmpty());
            customConditions.add(JpqlCondition.create("grp = :relatedGroup", groupJoin).skipNullOrEmpty());
            customConditions.add(JpqlCondition.create("bch = :relatedBranch", branchJoin).skipNullOrEmpty());
            if (hostEntityName.equals(ACTIVITY)) {
                customConditions.add(JpqlCondition.create("dv = :relatedActivityDivision", divisionJoin).skipNullOrEmpty());
                customConditions.add(JpqlCondition.create("e.activityNumber like :genericActivityNumber", null).skipNullOrEmpty());
            }
            Set<String> allowed = Set.of(OBLIGATION, INVOICE, FCN, FILE_ATTACHMENT);

            if (allowed.contains(hostEntityName)) {
                customConditions.add(
                        JpqlCondition.create("act = :relatedActivity", activityJoin)
                                .skipNullOrEmpty()
                );
            }
        } else {

            if (fundJoin != null) {
                if (hostLoader.getParameter("fundFilterField") != null) {
                    customConditions.add(JpqlCondition.create("f = :fundFilterField", fundJoin));
                } else {
                    if (fjcFoundation) {
                        customConditions.add(JpqlCondition.createWithParameters("f = :foundationFund", fundJoin, Map.of("foundationFund", fjcFoundationFund)));
                    } else {
                        customConditions.add(JpqlCondition.createWithParameters("f <> :foundationFund", fundJoin, Map.of("foundationFund", fjcFoundationFund)));
                    }
                }
            }
            hostLoader.setParameter("bfyFilterField", searchYears);// this is an issue when idList is edited and returned after fy change

//        customConditions.add(JpqlCondition.createWithParameters("app in :bfyFilterField", appropriationJoin, Map.of("bfyFilterField", fiscalYears)));
            customConditions.add(JpqlCondition.create("app in :bfyFilterField", appropriationJoin).skipNullOrEmpty());
            customConditions.add(JpqlCondition.create("dv.divisionCode = :divCodeFilterField", divisionJoin).skipNullOrEmpty());
            customConditions.add(JpqlCondition.create("cat.majorObjectClass = :mocFilterField", categoryJoin).skipNullOrEmpty());
            customConditions.add(JpqlCondition.create("obj.budgetObjectClass = :bocFilterField", objectClassJoin).skipNullOrEmpty());
            customConditions.add(JpqlCondition.create("bch.branchCode = :branchCodeFilterField", branchJoin).skipNullOrEmpty());
            customConditions.add(JpqlCondition.create("grp.groupCode = :groupCodeFilterField", groupJoin).skipNullOrEmpty());
            customConditions.add(JpqlCondition.create("fcat in :fileCategoryFilterField", fileCategoryJoin).skipNullOrEmpty());
//            customConditions.add(JpqlCondition.create("e.id in :idList", null).skipNullOrEmpty());
            if (hostEntityName.equals(ACTIVITY_PROJECTION)) {
                customConditions.add(JpqlCondition.create("e.amount <> 0", null).skipNullOrEmpty());
            }
            if (subFragment != null) {
                subFragmentConditions = subFragment.getPropertyFilterConditions();
                customConditions.addAll(subFragmentConditions);
            }
        }

//        ((EntitySearchFragment) subFragment).applyPropertyFilters();
        hostLoader.setQuery(hostEntityQuery);
        hostLoader.setCondition(LogicalCondition.and(customConditions.toArray(new Condition[0])));
        hostLoader.setFirstResult(firstResult);
        hostLoader.load();
        saveSearchParameters();
    }

    /**
     * Changes the Show BFY button caption after a Fiscal Year change event
     *
     * @param event custom event
     */
    @EventListener
    public void handleFiscalYearChangeEvent(FiscalYearChangeEvent event) {
        changeFiscalYears();
        searchYears = fiscalYears;
    }

    /**
     * if datagrid match, sets related items and button visibility
     *
     * @param event custom event that publishes grid and selection size
     */
    @EventListener
    public void handleSearchGridSelectedItemsEvent(SearchGridSelectedItemsEvent event) {
        if (!event.getDataGrid().equals(dataGrid)) {
            return;
        }
        var size = event.getSelectionSize();

        relatedGroup = null;
        relatedBranch = null;
        relatedActivity = null;

        if (size == 1) {
            dataGrid.getSelectedItems().stream()
                    .findFirst()
                    .ifPresent(this::resolveRelated);
        }

        showGroupBtn.setEnabled(size == 1 && relatedGroup != null);
        showBranchBtn.setEnabled(size == 1 && relatedBranch != null);
        showActivityBtn.setEnabled(size == 1 && relatedActivity != null);

        showSubsetBtn.setEnabled(size > 0);
        showSubsetBtn.setText("Show Subset (" + size + ")");
    }

    private void resolveRelated(Object item) {
        switch (hostEntityName) {
            case ACTIVITY -> {
                var a = (Activity) item;
                relatedGroup = a.getGroup();
                relatedBranch = a.getBranch();
                relatedActivity = a;
            }
            case OBLIGATION -> {
                var a = ((Obligation) item).getActivity();
                relatedGroup = a.getGroup();
                relatedBranch = a.getBranch();
                relatedActivity = a;
            }
            case INVOICE -> {
                var a = ((Invoice) item).getObligation().getActivity();
                relatedGroup = a.getGroup();
                relatedBranch = a.getBranch();
                relatedActivity = a;
            }
            case FCN -> {
                var a = ((FundControlNotice) item).getObligation().getActivity();
                relatedGroup = a.getGroup();
                relatedBranch = a.getBranch();
                relatedActivity = a;
            }
            case FILE_ATTACHMENT -> {
                var a = ((FileAttachment) item).getActivity();
                relatedGroup = a.getGroup();
                relatedBranch = a.getBranch();
                relatedActivity = a;
            }
        }
    }

    private void changeFiscalYears() {
        fiscalYears = appropriationService.getBfyFilterField(sessionData);
        setBfyBtnCaption();
        refreshField(divisionsDl, divisionSearchField, Division::getDivisionCode);
        refreshField(categoriesDl, categorySearchField, ObjectCategory::getMajorObjectClass);
        refreshField(objectClassesDl, objectClassSearchField, ObjectClass::getBudgetObjectClass);
        refreshField(branchesDl, branchSearchField, Branch::getBranchCode);
        refreshField(groupsDl, groupSearchField, Group::getGroupCode);
    }

    // refactored to fisutility
    private <T> void refreshField(CollectionLoader<T> loader,
                                  EntityComboBox<T> field,
                                  Function<T, ?> keyExtractor) {
        loader.load();
//        Optional.ofNullable(field.getValue())
//                .map(keyExtractor)
//                .flatMap(key -> loader.getContainer().getItems().stream()
//                        .filter(item -> keyExtractor.apply(item).equals(key))
//                        .findFirst())
//                .ifPresentOrElse(field::setValue, () -> field.setValue(null));
        field.setValue(
                Optional.ofNullable(field.getValue())
                        .map(keyExtractor)
                        .flatMap(key -> loader.getContainer().getItems().stream()
                                .filter(item -> keyExtractor.apply(item).equals(key))
                                .findFirst())
                        .orElse(null)
        );
    }
}