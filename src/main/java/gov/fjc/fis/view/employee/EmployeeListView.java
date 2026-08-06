package gov.fjc.fis.view.employee;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.Employee;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.Sort;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Route(value = "employees", layout = MainView.class)
@ViewController("fis_Employee.list")
@ViewDescriptor("employee-list-view.xml")
@LookupComponent("employeesDataGrid")
@DialogMode(width = "64em")
public class EmployeeListView extends StandardListView<Employee> {
    @ViewComponent
    private CollectionLoader<Employee> employeesDl;

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
                employeesDl.setParameter("costOrgFilterField", budgetOrg);
                performSearch();
            }
        }
    }

    private void clearCustomSearchParameters() {
        // remove query conditions from data loader
        Set<String> params = new HashSet<>(employeesDl.getParameters().keySet());
        params.forEach(employeesDl::removeParameter);
    }

    @Subscribe(id = "showAll", subject = "clickListener")
    public void onShowAllClick(final ClickEvent<JmixButton> event) {
        clearCustomSearchParameters();
        performSearch();
    }

    private void performSearch() {
        List<Condition> customConditions = new ArrayList<>();

        String hostEntityQuery = "SELECT e FROM fis_Employee e";
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        employeesDl.setSort(sort);


        customConditions.add(JpqlCondition.create("e.jlCostOrgCd = :costOrgFilterField", null).skipNullOrEmpty());

        employeesDl.setQuery(hostEntityQuery);
        employeesDl.setCondition(LogicalCondition.and(customConditions.toArray(new Condition[0])));
        employeesDl.setFirstResult(0);
        employeesDl.load();
    }
}