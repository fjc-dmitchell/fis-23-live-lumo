package gov.fjc.fis.view.documentaudit;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.DocumentAudit;
import gov.fjc.fis.event.FiscalYearChangeEvent;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.view.main.MainView;
import gov.fjc.fis.view.search.EntitySearchFragment;
import io.jmix.core.LoadContext;
import io.jmix.core.Sort;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Route(value = "document-audit", layout = MainView.class)
@ViewController(id = "fis_DocumentAudit.list")
@ViewDescriptor(path = "document-audit-list-view.xml")
@LookupComponent("documentAuditsDataGrid")
@DialogMode(width = "64em")
public class DocumentAuditListView extends StandardListView<DocumentAudit> {
    @Autowired
    private SessionData sessionData;
    @Autowired
    private AppropriationService appropriationService;

    @ViewComponent
    private DataGrid<DocumentAudit> documentAuditsDataGrid;
    @ViewComponent
    private CollectionLoader<DocumentAudit> documentAuditsDl;
    @ViewComponent
    private JmixButton showBfyBtn;

    private List<Appropriation> fiscalYears;

    @Subscribe("showDivisionAction")
    protected void onShowDivisionAction(final ActionPerformedEvent event) {
        if (event.getComponent().getId().isPresent()) {
            clearCustomSearchParameters();
            String btnId = event.getComponent().getId().get();
            String budgetOrg = switch (btnId) {
                case "showDiv1Btn" -> "JXXXXXF";
                case "showDiv2Btn" -> "JXXXXXA";
                case "showDiv3Btn" -> "JXXXXXD";
                case "showDiv4Btn" -> "JXXXXXC";
                case "showDiv5Btn" -> "JXXXXXB";
                case "showDiv9Btn" -> "JXXMAPP";
                default -> null;
            };

            if (budgetOrg != null) {
                documentAuditsDl.setParameter("budgetOrgFilterField", budgetOrg);
                performSearch();
            }
        }
    }

    @Subscribe("showStatusAction")
    protected void onShowStatusAction(final ActionPerformedEvent event) {
        if (event.getComponent().getId().isPresent()) {
            clearCustomSearchParameters();
            String btnId = event.getComponent().getId().get();
            String processStatus = switch (btnId) {
                case "showRejectBtn" -> "R";
                case "showInsertBtn" -> "I";
                case "showUpdateBtn" -> "U";
                default -> null;
            };

            if (processStatus != null) {
                documentAuditsDl.setParameter("processStatusFilterField", processStatus);
                performSearch();
            }
        }
    }

    @Subscribe(id = "showTodayBtn", subject = "clickListener")
    protected void onShowTodayBtnClick(final ClickEvent<JmixButton> event) {
        clearCustomSearchParameters();
        Date today = new Date();
        documentAuditsDl.setParameter("todayFilterField", today);
        performSearch();
    }

    @Subscribe(id = "showFcnBtn", subject = "clickListener")
    protected void onShowFcnBtnClick(final ClickEvent<JmixButton> event) {
        clearCustomSearchParameters();
        documentAuditsDl.setParameter("fcnFilterField", 0);
        performSearch();
    }


    private void clearCustomSearchParameters() {
        // remove query conditions from data loader
        Set<String> params = new HashSet<>(documentAuditsDl.getParameters().keySet());
        params.forEach(documentAuditsDl::removeParameter);

//        clearSearchFields();

//        customFilters.forEach((key, value) -> value.setValue(null));
    }

    @Subscribe(id = "showBfyBtn", subject = "clickListener")
    protected void onShowBfyBtnClick(final ClickEvent<JmixButton> event) {
        clearCustomSearchParameters();
        performSearch();
    }

    private void performSearch() {
        List<Condition> customConditions = new ArrayList<>();

        String hostEntityQuery = "SELECT e FROM fis_DocumentAudit e";
        Sort sort = Sort.by(Sort.Direction.DESC, "processDate");
        documentAuditsDl.setSort(sort);


        if (documentAuditsDl.getParameter("todayFilterField") != null) {
            customConditions.add(JpqlCondition.create("e.processDate = :todayFilterField", null).skipNullOrEmpty());
        } else {
            List<String> searchYears = fiscalYears.stream().map(Appropriation::getBudgetFiscalYear).toList();
            documentAuditsDl.setParameter("bfyFilterField", searchYears);// this is an issue when idList is edited and returned after fy change

            customConditions.add(JpqlCondition.create("e.documentBbfy in :bfyFilterField", null).skipNullOrEmpty());
            customConditions.add(JpqlCondition.create("e.documentBudgetOrg = :budgetOrgFilterField", null).skipNullOrEmpty());
            customConditions.add(JpqlCondition.create("e.processStatus = :processStatusFilterField", null).skipNullOrEmpty());
            customConditions.add(JpqlCondition.create("(e.documentAmount-coalesce(e.obligationAmount,e.documentAmount)) <> :fcnFilterField", null).skipNullOrEmpty());
        }

        documentAuditsDl.setQuery(hostEntityQuery);
        documentAuditsDl.setCondition(LogicalCondition.and(customConditions.toArray(new Condition[0])));
        documentAuditsDl.setFirstResult(0);
        documentAuditsDl.load();
//        saveSearchParameters();
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        setBfyBtnCaption();
    }

    @Subscribe
    protected void onInit(final InitEvent event) {

        fiscalYears = appropriationService.getBfyFilterField(sessionData);
        performSearch();

        // add border to certain cells in existing header
        HeaderRow originalHeaderRow = documentAuditsDataGrid.getHeaderRows().getFirst();
        originalHeaderRow.getCell(documentAuditsDataGrid.getColumnByKey("fcnAmount")).setPartName("border-right");
        originalHeaderRow.getCell(documentAuditsDataGrid.getColumnByKey("documentLastModifiedBy")).setPartName("border-right");
        originalHeaderRow.getCell(documentAuditsDataGrid.getColumnByKey("obligationTravelEndDate")).setPartName("border-right");
        originalHeaderRow.getCell(documentAuditsDataGrid.getColumnByKey("currentProjectionAmountAfter")).setPartName("border-right");


//        HeaderRow.HeaderCell fcnAmountCell = originalHeaderRow.getCell(documentAuditsDataGrid.getColumnByKey("fcnAmount"));
//        fcnAmountCell.setPartName();


        HeaderRow headerRow = documentAuditsDataGrid.prependHeaderRow();

        HeaderRow.HeaderCell processedCell = headerRow.join(
                documentAuditsDataGrid.getColumnByKey("processDate"),
                documentAuditsDataGrid.getColumnByKey("processStatus"),
                documentAuditsDataGrid.getColumnByKey("loggedChanges"),
                documentAuditsDataGrid.getColumnByKey("fcnAmount")
        );
        Span statusSpan = new Span("Process Status");
        HorizontalLayout statusLayout = new HorizontalLayout(statusSpan);
        statusLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        processedCell.setComponent(statusLayout);
        processedCell.setPartName("audit-header");
        processedCell.setPartName("audit-header font-weight-bold");

        HeaderRow.HeaderCell documentCell = headerRow.join(
                documentAuditsDataGrid.getColumnByKey("documentBbfy"),
                documentAuditsDataGrid.getColumnByKey("documentEbfy"),
                documentAuditsDataGrid.getColumnByKey("documentFundCode"),
                documentAuditsDataGrid.getColumnByKey("documentBudgetOrg"),
                documentAuditsDataGrid.getColumnByKey("documentDocumentNumber"),
                documentAuditsDataGrid.getColumnByKey("documentBudgetObjectClass"),
                documentAuditsDataGrid.getColumnByKey("documentDocumentCreationDate"),
                documentAuditsDataGrid.getColumnByKey("documentTitle"),
                documentAuditsDataGrid.getColumnByKey("documentProject"),
                documentAuditsDataGrid.getColumnByKey("documentAmount"),
                documentAuditsDataGrid.getColumnByKey("documentTravelStartDate"),
                documentAuditsDataGrid.getColumnByKey("documentTravelEndDate"),
                documentAuditsDataGrid.getColumnByKey("documentLastModifiedBy")
        );
        Span documentSpan = new Span("processed JIFMS Document");
        HorizontalLayout documentLayout = new HorizontalLayout(documentSpan);
        documentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        documentCell.setComponent(documentLayout);
        documentCell.setPartName("font-weight-bold");


        HeaderRow.HeaderCell obligationCell = headerRow.join(
                documentAuditsDataGrid.getColumnByKey("obligationDocumentNumber"),
                documentAuditsDataGrid.getColumnByKey("obligationActivityNumber"),
                documentAuditsDataGrid.getColumnByKey("obligationBudgetObjectClass"),
                documentAuditsDataGrid.getColumnByKey("obligationAmount"),
                documentAuditsDataGrid.getColumnByKey("obligationVendor"),
                documentAuditsDataGrid.getColumnByKey("obligationTravelStartDate"),
                documentAuditsDataGrid.getColumnByKey("obligationTravelEndDate")
        );
        Span obligationSpan = new Span("Updated fields on existing FIS Obligation");
        HorizontalLayout obligationLayout = new HorizontalLayout(obligationSpan);
        obligationLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        obligationCell.setComponent(obligationLayout);
        obligationCell.setPartName("audit-header");


        HeaderRow.HeaderCell currentActivityCell = headerRow.join(
                documentAuditsDataGrid.getColumnByKey("currentActivityNumber"),
                documentAuditsDataGrid.getColumnByKey("currentProjectionBoc"),
                documentAuditsDataGrid.getColumnByKey("currentProjectionAmountBefore"),
                documentAuditsDataGrid.getColumnByKey("currentProjectionAmountAfter")
        );
        Span currentActivitySpan = new Span("Projection change (unchanged Activity and BOC)");
        HorizontalLayout currentActivityLayout = new HorizontalLayout(currentActivitySpan);
        currentActivityLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        currentActivityCell.setComponent(currentActivityLayout);
        currentActivityCell.setPartName("font-weight-bold");

        HeaderRow.HeaderCell previousActivityCell = headerRow.join(
                documentAuditsDataGrid.getColumnByKey("previousActivityNumber"),
                documentAuditsDataGrid.getColumnByKey("previousProjectionBoc"),
                documentAuditsDataGrid.getColumnByKey("previousProjectionAmountBefore"),
                documentAuditsDataGrid.getColumnByKey("previousProjectionAmountAfter")
        );
        Span previousActivitySpan = new Span("Projection change (changed Activity or BOC)");
        HorizontalLayout previousActivityLayout = new HorizontalLayout(previousActivitySpan);
        previousActivityLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        previousActivityCell.setComponent(previousActivityLayout);
        previousActivityCell.setPartName("audit-header");

    }

    // processing status columns
    @Install(to = "documentAuditsDataGrid.processDate", subject = "partNameGenerator")
    protected String documentAuditsDataGridProcessDatePartNameGenerator(final DocumentAudit documentAudit) {
        return "audit";
    }

    @Install(to = "documentAuditsDataGrid.processStatus", subject = "partNameGenerator")
    protected String documentAuditsDataGridProcessStatusPartNameGenerator(final DocumentAudit documentAudit) {
        return "audit";
    }

    @Install(to = "documentAuditsDataGrid.loggedChanges", subject = "partNameGenerator")
    protected String documentAuditsDataGridLoggedChangesPartNameGenerator(final DocumentAudit documentAudit) {
        return "audit";
    }

    @Install(to = "documentAuditsDataGrid.fcnAmount", subject = "partNameGenerator")
    protected String documentAuditsDataGridFcnAmountPartNameGenerator(final DocumentAudit documentAudit) {
        return "audit border-right";
    }

    // JIFMS document columns
    @Install(to = "documentAuditsDataGrid.documentLastModifiedBy", subject = "partNameGenerator")
    protected String documentAuditsDataGridDocumentLastModifiedByPartNameGenerator(final DocumentAudit documentAudit) {
        return "border-right";
    }

    // FIS obligation columns
    @Install(to = "documentAuditsDataGrid.obligationDocumentNumber", subject = "partNameGenerator")
    protected String documentAuditsDataGridObligationDocumentNumberPartNameGenerator(final DocumentAudit documentAudit) {
        return "audit";
    }

    @Install(to = "documentAuditsDataGrid.obligationActivityNumber", subject = "partNameGenerator")
    protected String documentAuditsDataGridObligationActivityNumberPartNameGenerator(final DocumentAudit documentAudit) {
        return "audit";
    }

    @Install(to = "documentAuditsDataGrid.obligationBudgetObjectClass", subject = "partNameGenerator")
    protected String documentAuditsDataGridObligationBudgetObjectClassPartNameGenerator(final DocumentAudit documentAudit) {
        return "audit";
    }

    @Install(to = "documentAuditsDataGrid.obligationAmount", subject = "partNameGenerator")
    protected String documentAuditsDataGridObligationAmountPartNameGenerator(final DocumentAudit documentAudit) {
        return "audit";
    }

    @Install(to = "documentAuditsDataGrid.obligationVendor", subject = "partNameGenerator")
    protected String documentAuditsDataGridObligationVendorPartNameGenerator(final DocumentAudit documentAudit) {
        return "audit";
    }

    @Install(to = "documentAuditsDataGrid.obligationTravelStartDate", subject = "partNameGenerator")
    protected String documentAuditsDataGridObligationTravelStartDatePartNameGenerator(final DocumentAudit documentAudit) {
        return "audit";
    }

    @Install(to = "documentAuditsDataGrid.obligationTravelEndDate", subject = "partNameGenerator")
    protected String documentAuditsDataGridObligationTravelEndDatePartNameGenerator(final DocumentAudit documentAudit) {
        return "audit border-right";
    }

    // projection for current activity
    @Install(to = "documentAuditsDataGrid.currentProjectionAmountAfter", subject = "partNameGenerator")
    protected String documentAuditsDataGridCurrentProjectionAmountAfterPartNameGenerator(final DocumentAudit documentAudit) {
        return "border-right";
    }

    // projection for prior activity
    @Install(to = "documentAuditsDataGrid.previousActivityNumber", subject = "partNameGenerator")
    protected String documentAuditsDataGridPreviousActivityNumberPartNameGenerator(final DocumentAudit documentAudit) {
        return "audit";
    }

    @Install(to = "documentAuditsDataGrid.previousProjectionBoc", subject = "partNameGenerator")
    protected String documentAuditsDataGridPreviousProjectionBocPartNameGenerator(final DocumentAudit documentAudit) {
        return "audit";
    }

    @Install(to = "documentAuditsDataGrid.previousProjectionAmountBefore", subject = "partNameGenerator")
    protected String documentAuditsDataGridPreviousProjectionAmountBeforePartNameGenerator(final DocumentAudit documentAudit) {
        return "audit";
    }

    @Install(to = "documentAuditsDataGrid.previousProjectionAmountAfter", subject = "partNameGenerator")
    protected String documentAuditsDataGridPreviousProjectionAmountAfterPartNameGenerator(final DocumentAudit documentAudit) {
        return "audit";
    }

    /**
     * Changes the Show BFY button caption after a Fiscal Year change event
     *
     * @param event custom event
     */
    @EventListener
    public void handleFiscalYearChangeEvent(FiscalYearChangeEvent event) {
        fiscalYears = appropriationService.getBfyFilterField(sessionData);
        setBfyBtnCaption();
    }

    private void setBfyBtnCaption() {
        if (fiscalYears.size() == 1) {
            showBfyBtn.setText("Show all for " + fiscalYears.getFirst().getBudgetFiscalYear());
        } else {
            showBfyBtn.setText("Show Search BFYs");
        }
//        fiscalYearsField.setValue(fiscalYears.stream().map(Appropriation::getBudgetFiscalYear).collect(Collectors.joining(", ")));
    }
}