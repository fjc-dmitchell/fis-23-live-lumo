package gov.fjc.fis.view.report.spendingchartfragment;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.AppropriationType;
import gov.fjc.fis.entity.dto.ObjectCategoryDto;
import gov.fjc.fis.service.report.StatusOfFundsReportService;
import io.jmix.chartsflowui.component.Chart;
import io.jmix.chartsflowui.data.item.MapDataItem;
import io.jmix.chartsflowui.kit.component.model.DataSet;
import io.jmix.chartsflowui.kit.data.chart.ListChartItems;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@FragmentDescriptor("spending-chart-fragment.xml")
public class SpendingChartFragment extends Fragment<VerticalLayout> {

    @Autowired
    private StatusOfFundsReportService statusOfFundsReportService;

    @ViewComponent
    private CollectionContainer<ObjectCategoryDto> categorySofDc;
    @ViewComponent
    private CollectionLoader<ObjectCategoryDto> categorySofDl;
    @ViewComponent
    private CollectionContainer<ObjectCategoryDto> categorySpendDc;
    @ViewComponent
    private CollectionLoader<ObjectCategoryDto> categorySpendDl;

    @ViewComponent
    private Chart spendingChart;
    @ViewComponent
    private Chart allocationsChart;
    @ViewComponent
    private JmixComboBox<AppropriationType> appropriationType;

    Appropriation appropriation;

    public void setAppropriation(Appropriation appropriation) {
        this.appropriation = appropriation;
        categorySpendDl.load();
        categorySofDl.load();
        refreshAllocationsChart();
        refreshSpendingChart();
    }

    @Subscribe(target = Target.HOST_CONTROLLER)
    protected void onHostReady(final View.ReadyEvent event) {
        appropriationType.setValue(AppropriationType.COMBINED_YEAR_FUND);
        categorySofDl.load();
        categorySpendDl.load();
        refreshAllocationsChart();
        refreshSpendingChart();
    }

    @Subscribe(target = Target.HOST_CONTROLLER)
    protected void onHostBeforeShow(final View.BeforeShowEvent event) {
        Objects.requireNonNull(spendingChart.getTitle()).setSubtext("(Obligations plus outstanding Projections)");
    }

    @Subscribe("appropriationType")
    protected void onAppropriationTypeComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixComboBox<AppropriationType>, AppropriationType> event) {
        refreshAllocationsChart();
        refreshSpendingChart();
    }

    @Install(to = "categorySofDl", target = Target.DATA_LOADER)
    protected List<ObjectCategoryDto> categorySofDlLoadDelegate(final LoadContext<ObjectCategoryDto> loadContext) {
        return statusOfFundsReportService.getStatusOfFundsCategoryData(appropriation, 0, false);
    }

    @Install(to = "categorySpendDl", target = Target.DATA_LOADER)
    protected List<ObjectCategoryDto> categorySpendDlLoadDelegate(final LoadContext<ObjectCategoryDto> loadContext) {
        return statusOfFundsReportService.getStatusOfFundsCategoryDataWithUnspent(appropriation, 0);
    }

    private void refreshAllocationsChart() {
        if (appropriation != null && appropriationType.getValue() != null) {
            ListChartItems<MapDataItem> allocations = new ListChartItems<>();
            List<ObjectCategoryDto> categories;
            switch (appropriationType.getValue()) {
                case ONE_YEAR_FUND:
                    categories = categorySofDc.getItems().stream().filter(categoryDto -> categoryDto.getTotalOneYearAllocations().signum() != 0).toList();
                    for (var cat : categories) {
                        allocations.addItem(new MapDataItem(Map.of("category", cat.getTitleAndCode(), "value", cat.getTotalOneYearAllocations())));
                    }
                    Objects.requireNonNull(allocationsChart.getTitle()).setText(appropriation.getBudgetFiscalYear().concat(" One Year Allocations"));
                    break;
                case TWO_YEAR_FUND:
                    categories = categorySofDc.getItems().stream().filter(categoryDto -> categoryDto.getTotalTwoYearAllocations().signum() != 0).toList();
                    for (var cat : categories) {
                        allocations.addItem(new MapDataItem(Map.of("category", cat.getTitleAndCode(), "value", cat.getTotalTwoYearAllocations())));
                    }
                    Objects.requireNonNull(allocationsChart.getTitle()).setText(appropriation.getBudgetFiscalYear().concat(" Two Year Allocations"));
                    break;
                case OBBBA_APPROPRIATION:
                    categories = categorySofDc.getItems().stream().filter(categoryDto -> categoryDto.getMandatoryAllocated().signum() != 0).toList();
                    for (var cat : categories) {
                        allocations.addItem(new MapDataItem(Map.of("category", cat.getTitleAndCode(), "value", cat.getMandatoryAllocated())));
                    }
                    Objects.requireNonNull(allocationsChart.getTitle()).setText(appropriation.getBudgetFiscalYear().concat(" JXXMAPP Mandatory Fund Allocations"));
                    break;
                default:
                    categories = categorySofDc.getItems().stream().filter(categoryDto -> categoryDto.getTotalAllocations().signum() != 0).toList();
                    for (var cat : categories) {
                        allocations.addItem(new MapDataItem(Map.of("category", cat.getTitleAndCode(), "value", cat.getTotalAllocations())));
                    }
                    Objects.requireNonNull(allocationsChart.getTitle()).setText(appropriation.getBudgetFiscalYear().concat(" Combined Year Allocations"));
                    break;
            }
            allocationsChart.setDataSet(new DataSet().withSource(new DataSet.Source<MapDataItem>().withDataProvider(allocations).withCategoryField("category").withValueField("value")));
        }
    }

    private void refreshSpendingChart() {
        if (appropriation != null && appropriationType.getValue() != null) {
            ListChartItems<MapDataItem> spending = new ListChartItems<>();
            List<ObjectCategoryDto> categories;
            switch (appropriationType.getValue()) {
                case ONE_YEAR_FUND:
                    categories = categorySpendDc.getItems().stream().filter(categoryDto -> categoryDto.getTotalOneYearObligations().add(categoryDto.getTotalOneYearProjections()).signum() != 0).toList();
                    for (var cat : categories) {
                        spending.addItem(new MapDataItem(Map.of("category", cat.getTitleAndCode(), "value", cat.getTotalOneYearObligations().add(cat.getTotalOneYearProjections()))));
                    }
                    Objects.requireNonNull(spendingChart.getTitle()).setText(appropriation.getBudgetFiscalYear().concat(" One Year Spending"));
                    break;
                case TWO_YEAR_FUND:
                    categories = categorySpendDc.getItems().stream().filter(categoryDto -> categoryDto.getTotalTwoYearObligations().add(categoryDto.getTotalTwoYearProjections()).signum() != 0).toList();
                    for (var cat : categories) {
                        spending.addItem(new MapDataItem(Map.of("category", cat.getTitleAndCode(), "value", cat.getTotalTwoYearObligations().add(cat.getTotalTwoYearProjections()))));
                    }
                    spendingChart.getTitle().setText(appropriation.getBudgetFiscalYear().concat(" Two Year Spending"));
                    break;
                case OBBBA_APPROPRIATION:
                    categories = categorySpendDc.getItems().stream().filter(categoryDto -> categoryDto.getMandatoryObligated().add(categoryDto.getMandatoryProjected()).signum() != 0).toList();
                    for (var cat : categories) {
                        spending.addItem(new MapDataItem(Map.of("category", cat.getTitleAndCode(), "value", cat.getMandatoryObligated().add(cat.getMandatoryProjected()))));
                    }
                    Objects.requireNonNull(spendingChart.getTitle()).setText(appropriation.getBudgetFiscalYear().concat(" JXXMAPP Mandatory Fund Spending (does not include unspent funds)"));
                    break;
                default:
                    categories = categorySpendDc.getItems().stream().filter(categoryDto -> categoryDto.getTotalObligations().add(categoryDto.getTotalProjections()).signum() != 0).toList();
                    for (var cat : categories) {
                        spending.addItem(new MapDataItem(Map.of("category", cat.getTitleAndCode(), "value", cat.getTotalObligations().add(cat.getTotalProjections()))));
                    }
                    Objects.requireNonNull(spendingChart.getTitle()).setText(appropriation.getBudgetFiscalYear().concat(" Combined Year Spending"));
                    break;
            }
            spendingChart.setDataSet(new DataSet().withSource(new DataSet.Source<MapDataItem>().withDataProvider(spending).withCategoryField("category").withValueField("value")));
        }
    }
}