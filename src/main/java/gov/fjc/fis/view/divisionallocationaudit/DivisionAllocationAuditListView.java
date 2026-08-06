package gov.fjc.fis.view.divisionallocationaudit;

import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.DivisionAllocationAudit;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.view.*;


@Route(value = "division-allocation-audits", layout = DefaultMainViewParent.class)
@ViewController(id = "fis_DivisionAllocationAudit.list")
@ViewDescriptor(path = "division-allocation-audit-list-view.xml")
@LookupComponent("divisionAllocationAuditsDataGrid")
@DialogMode(width = "64em")
public class DivisionAllocationAuditListView extends StandardListView<DivisionAllocationAudit> {
    @ViewComponent
    private DataGrid<DivisionAllocationAudit> divisionAllocationAuditsDataGrid;

    @Subscribe
    protected void onInit(final InitEvent event) {

        HeaderRow headerRow = divisionAllocationAuditsDataGrid.prependHeaderRow();

        HeaderRow.HeaderCell oneYearCell = headerRow.join(
                divisionAllocationAuditsDataGrid.getColumnByKey("oldOneYearAmount"),
                divisionAllocationAuditsDataGrid.getColumnByKey("newOneYearAmount")
        );
        Span oneYearSpan = new Span("One Year");
        HorizontalLayout oneYearLayout = new HorizontalLayout(oneYearSpan);
        oneYearLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        oneYearCell.setComponent(oneYearLayout);
        oneYearCell.setPartName("font-weight-bold");

        HeaderRow.HeaderCell twoYearCell = headerRow.join(
                divisionAllocationAuditsDataGrid.getColumnByKey("oldTwoYearAmount"),
                divisionAllocationAuditsDataGrid.getColumnByKey("newTwoYearAmount")
        );
        Span twoYearSpan = new Span("Two Year");
        HorizontalLayout twoYearLayout = new HorizontalLayout(twoYearSpan);
        twoYearLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        twoYearCell.setComponent(twoYearLayout);
        twoYearCell.setPartName("font-weight-bold");
    }
}