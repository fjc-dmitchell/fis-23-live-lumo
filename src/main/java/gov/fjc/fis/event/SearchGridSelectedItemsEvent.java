package gov.fjc.fis.event;

import io.jmix.flowui.component.grid.DataGrid;
import org.springframework.context.ApplicationEvent;

public class SearchGridSelectedItemsEvent extends ApplicationEvent {
    private final DataGrid<?> dataGrid;
    private final Integer selectionSize;

    public SearchGridSelectedItemsEvent(Object source, DataGrid<?> dataGrid, Integer selectionSize) {
        super(source);
        this.dataGrid = dataGrid;
        this.selectionSize = selectionSize;
    }

    public DataGrid<?> getDataGrid() {
        return dataGrid;
    }

    public Integer getSelectionSize() {
        return selectionSize;
    }
}