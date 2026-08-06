package gov.fjc.fis.view.positionaction;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.service.PayPeriodService;
import gov.fjc.fis.entity.personnel.PositionAction;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Route(value = "positionActions", layout = MainView.class)
@ViewController("fis_PositionAction.list")
@ViewDescriptor("position-action-list-view.xml")
@LookupComponent("positionActionsDataGrid")
@DialogMode(width = "64em")
public class PositionActionListView extends StandardListView<PositionAction> {
    @ViewComponent
    private CollectionLoader<PositionAction> positionActionsDl;
    @Autowired
    private PayPeriodService payPeriodService;
    @ViewComponent
    private JmixButton showPriorAction;

    private LocalDate payPeriodStartDate;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy");
        var currentPayPeriod = payPeriodService.fetchCurrentPayPeriod();
        if (currentPayPeriod != null) {
            payPeriodStartDate = currentPayPeriod.getStartDate();
            showPriorAction.setVisible(true);
            showPriorAction.setText("Actions before " + payPeriodStartDate.format(formatter));
        }
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
                positionActionsDl.setParameter("costOrgFilterField", budgetOrg);
                performSearch();
            }
        }
    }

    private void clearCustomSearchParameters() {
        // remove query conditions from data loader
        Set<String> params = new HashSet<>(positionActionsDl.getParameters().keySet());
        params.forEach(positionActionsDl::removeParameter);
    }

    @Subscribe(id = "showAll", subject = "clickListener")
    public void onShowAllClick(final ClickEvent<JmixButton> event) {
        clearCustomSearchParameters();
        performSearch();
    }

    @Subscribe(id = "showPriorAction", subject = "clickListener")
    public void onShowPriorActionClick(final ClickEvent<JmixButton> event) {
        clearCustomSearchParameters();
        if (payPeriodStartDate != null) {
            positionActionsDl.setParameter("effectiveDate", payPeriodStartDate);
            performSearch();
        }
    }

    private void performSearch() {
        List<Condition> customConditions = new ArrayList<>();

        String hostEntityQuery = "SELECT e FROM fis_PositionAction e";
//        Sort sort = Sort.by(Sort.Direction.ASC, "name");
//        positionActionsDl.setSort(sort);


        customConditions.add(JpqlCondition.create("e.position.jlCostOrgCd = :costOrgFilterField", null).skipNullOrEmpty());
        customConditions.add(JpqlCondition.create("e.effectiveDate < :effectiveDate", null).skipNullOrEmpty());

        positionActionsDl.setQuery(hostEntityQuery);
        positionActionsDl.setCondition(LogicalCondition.and(customConditions.toArray(new Condition[0])));
        positionActionsDl.setFirstResult(0);
        positionActionsDl.load();
    }

}