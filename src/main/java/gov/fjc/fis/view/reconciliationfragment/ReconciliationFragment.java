package gov.fjc.fis.view.reconciliationfragment;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.dto.ReconciliationDto;
import gov.fjc.fis.service.ObligationService;
import io.jmix.core.LoadContext;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.pivottableflowui.component.PivotTable;
import io.jmix.pivottableflowui.export.PivotTableExcelExporter;
import io.jmix.pivottableflowui.export.PivotTableExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

@FragmentDescriptor("reconciliation-fragment.xml")
public class ReconciliationFragment extends Fragment<VerticalLayout> {
    @Autowired
    private ObligationService obligationService;

    @ViewComponent
    private CollectionLoader<ReconciliationDto> reconciliationDtoesDl;
    @ViewComponent
    private PivotTable<Object> pivotTable;

    Appropriation appropriation;
    ApplicationContext applicationContext;
    PivotTableExporter pivotTableExport;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setAppropriation(Appropriation appropriation) {
        this.appropriation = appropriation;
        reconciliationDtoesDl.load();
    }

//    @Subscribe(target = Target.HOST_CONTROLLER)
//    protected void onHostInit(final View.InitEvent event) {
//        PivotTableExcelExporter pivotTableExcelExporter = applicationContext.getBean(PivotTableExcelExporter.class);
//        pivotTableExport = applicationContext.getBean(PivotTableExporter.class, pivotTable, pivotTableExcelExporter);
//    }

    @Install(to = "reconciliationDtoesDl", target = Target.DATA_LOADER)
    private List<ReconciliationDto> reconciliationDtoesDlLoadDelegate(final LoadContext<ReconciliationDto> loadContext) {
        return obligationService.getReconciliationDto(appropriation);
    }

//    @Subscribe(id = "pivotExportBtn", subject = "clickListener")
//    public void onPivotExportBtnClick(final ClickEvent<JmixButton> event) {
//        pivotTableExport.exportTableToXls();
//    }

}