package gov.fjc.fis.view.department;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.Department;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;


@Route(value = "departments", layout = MainView.class)
@ViewController(id = "fis_Department.list")
@ViewDescriptor(path = "department-list-view.xml")
@LookupComponent("departmentsDataGrid")
@DialogMode(width = "64em")
public class DepartmentListView extends StandardListView<Department> {
}