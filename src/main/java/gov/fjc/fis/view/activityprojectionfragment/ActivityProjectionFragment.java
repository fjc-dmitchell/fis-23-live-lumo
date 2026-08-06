package gov.fjc.fis.view.activityprojectionfragment;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gov.fjc.fis.entity.ActivityProjection;
import gov.fjc.fis.entity.ObjectCategory;
import gov.fjc.fis.entity.ObjectClass;
import gov.fjc.fis.service.ActivityProjectionService;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@FragmentDescriptor("activity-projection-fragment.xml")
public class ActivityProjectionFragment extends Fragment<VerticalLayout> {
    @ViewComponent
    private CollectionLoader<ActivityProjection> activityProjectionsDl;
    @ViewComponent
    private DataGrid<ActivityProjection> activityProjectionsDataGrid;
    @Autowired
    private ActivityProjectionService activityProjectionService;

    private Object hostEntity;

    public void setEntity(Object entity) {
        this.hostEntity = entity;
    }

    @Subscribe(target = Target.HOST_CONTROLLER)
    protected void onHostReady(final View.ReadyEvent event) {
        activityProjectionsDl.load();
    }

    @Install(to = "activityProjectionsDl", target = Target.DATA_LOADER)
    protected List<ActivityProjection> activityProjectionsDlLoadDelegate(final LoadContext<ActivityProjection> loadContext) {
        List<ActivityProjection> projections = new ArrayList<>();
        switch (hostEntity.getClass().getSimpleName()) {
            case "Category":
                var category = (ObjectCategory) hostEntity;
                projections = activityProjectionService.getActivitiesByProjectionMoc((ObjectCategory) hostEntity);
                activityProjectionsDataGrid.setEmptyStateText("There are no open projections for ".concat(category.getTitleAndCode()));
                break;
            case "ObjectClass":
                var objectClass = (ObjectClass) hostEntity;
                projections = activityProjectionService.getActivitiesByProjectionBoc((ObjectClass) hostEntity);
                activityProjectionsDataGrid.setEmptyStateText("There are no open projections for ".concat(objectClass.getTitleAndCode()));
                break;
        }
        return projections;
    }
}