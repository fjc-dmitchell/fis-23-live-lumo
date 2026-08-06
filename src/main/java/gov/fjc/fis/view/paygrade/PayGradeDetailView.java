package gov.fjc.fis.view.paygrade;

import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.PayGrade;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.view.*;

@Route(value = "payGrades/:id", layout = MainView.class)
@ViewController(id = "fis_PayGrade.detail")
@ViewDescriptor(path = "pay-grade-detail-view.xml")
@EditedEntityContainer("payGradeDc")
public class PayGradeDetailView extends StandardDetailView<PayGrade> {
    @ViewComponent
    private DataGrid<Object> ratesDataGrid;
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    protected void onInit(final InitEvent event) {
        HeaderRow headerRow = ratesDataGrid.prependHeaderRow();

        HeaderRow.HeaderCell headerCell = headerRow.join(
                ratesDataGrid.getColumnByKey("minRtAnnual"),
                ratesDataGrid.getColumnByKey("maxRtAnnual"));
        Span bands = new Span("Base Pay");
        HorizontalLayout layout = new HorizontalLayout(bands);

        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        headerCell.setComponent(layout);

        headerCell = headerRow.join(
                ratesDataGrid.getColumnByKey("quartile1Range"),
                ratesDataGrid.getColumnByKey("quartile2Range"),
                ratesDataGrid.getColumnByKey("quartile3Range"),
                ratesDataGrid.getColumnByKey("quartile4Range")
        );

        Span ranges = new Span("Base Pay Quartile Ranges");
        layout = new HorizontalLayout(ranges);

        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        headerCell.setComponent(layout);
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        createdByString.setText(getEditedEntity().getCreatedByString());
    }

}