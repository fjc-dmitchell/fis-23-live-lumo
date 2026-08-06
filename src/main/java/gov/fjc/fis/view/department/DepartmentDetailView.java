package gov.fjc.fis.view.department;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.Department;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;

@Route(value = "departments/:id", layout = MainView.class)
@ViewController(id = "fis_Department.detail")
@ViewDescriptor(path = "department-detail-view.xml")
@EditedEntityContainer("departmentDc")
public class DepartmentDetailView extends StandardDetailView<Department> {
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        createdByString.setText(getEditedEntity().getCreatedByString());
    }
}