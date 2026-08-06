package gov.fjc.fis.view.position;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.Position;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.Sort;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Route(value = "positions", layout = MainView.class)
@ViewController("fis_Position.list")
@ViewDescriptor("position-list-view.xml")
@LookupComponent("positionsDataGrid")
@DialogMode(width = "64em")
public class PositionListView extends StandardListView<Position> {
    @ViewComponent
    private CollectionLoader<Position> positionsDl;
    @Autowired
    private FetchPlans fetchPlans;

    @Subscribe
    public void onInit(final InitEvent event) {
        positionsDl.setFetchPlan(fetchPlans.builder(Position.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("actions")
                .build());
//        performSearch();
    }

    @Subscribe("showDivisionAction")
    public void onShowDivisionAction(final ActionPerformedEvent event) {
        if (event.getComponent().getId().isPresent()) {
            clearCustomSearchParameters();
            String btnId = event.getComponent().getId().get();
            String budgetOrg = switch (btnId) {
                case "showDiv1Btn" -> "JXXXXXF";
                case "showDiv2Btn" -> "JXXXXXA";
                case "showDiv3Btn" -> "JXXXXXD";
                case "showDiv4Btn" -> "JXXXXXC";
                case "showDiv5Btn" -> "JXXXXXB";
                default -> null;
            };

            if (budgetOrg != null) {
                positionsDl.setParameter("costOrgFilterField", budgetOrg);
                performSearch();
            }
        }
    }

    private void clearCustomSearchParameters() {
        // remove query conditions from data loader
        Set<String> params = new HashSet<>(positionsDl.getParameters().keySet());
        params.forEach(positionsDl::removeParameter);
    }

    @Subscribe(id = "showAll", subject = "clickListener")
    public void onShowAllClick(final ClickEvent<JmixButton> event) {
        clearCustomSearchParameters();
        performSearch();
    }

    @Subscribe(id = "showActions", subject = "clickListener")
    public void onShowActionsClick(final ClickEvent<JmixButton> event) {
        clearCustomSearchParameters();
        positionsDl.setParameter("actionsSize", 0);
        performSearch();
    }

    private void performSearch() {
        List<Condition> customConditions = new ArrayList<>();

        String hostEntityQuery = "SELECT e FROM fis_Position e";
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        positionsDl.setSort(sort);

        customConditions.add(JpqlCondition.create("e.jlCostOrgCd = :costOrgFilterField", null).skipNullOrEmpty());
        customConditions.add(JpqlCondition.create("SIZE(e.actions) > :actionsSize", null).skipNullOrEmpty());

        positionsDl.setQuery(hostEntityQuery);
        positionsDl.setCondition(LogicalCondition.and(customConditions.toArray(new Condition[0])));
        positionsDl.setFirstResult(0);
        positionsDl.load();
    }
}