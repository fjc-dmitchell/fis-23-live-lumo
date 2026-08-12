package gov.fjc.fis.view.userroleslist;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.User;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentRepository;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "user-roles-list-view", layout = MainView.class)
@ViewController(id = "fis_UserRolesListView")
@ViewDescriptor(path = "user-roles-list-view.xml")
public class UserRolesListView extends StandardView {

    @ViewComponent
    private CollectionLoader<User> usersDl;

    @ViewComponent
    private ComboBox<String> resourceRoleFilter;

    @ViewComponent
    private ComboBox<String> rowLevelRoleFilter;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Autowired
    private ResourceRoleRepository resourceRoleRepository;

    @Autowired
    private RowLevelRoleRepository rowLevelRoleRepository;

    @Subscribe
    public void onInit(InitEvent event) {
        usersDl.setParameter("resourceRoleCode", null);
        usersDl.setParameter("rowLevelRoleCode", null);

        usersDl.load();

        initRoleFilter(resourceRoleFilter, resourceRoleRepository.getAllRoles(), ResourceRole::getCode, ResourceRole::getName);
        initRoleFilter(rowLevelRoleFilter, rowLevelRoleRepository.getAllRoles(), RowLevelRole::getCode, RowLevelRole::getName);
    }

    private <T> void initRoleFilter(ComboBox<String> comboBox, Collection<T> roles,
                                    java.util.function.Function<T, String> codeExtractor,
                                    java.util.function.Function<T, String> nameExtractor) {
        List<String> codes = roles.stream()
                .sorted(Comparator.comparing(nameExtractor))
                .map(codeExtractor)
                .toList();

        var namesByCode = roles.stream()
                .collect(Collectors.toMap(codeExtractor, nameExtractor));

        comboBox.setItems(codes);
        comboBox.setItemLabelGenerator(namesByCode::get);
    }

    @Subscribe("resourceRoleFilter")
    public void onResourceRoleFilterComponentValueChange(AbstractField.ComponentValueChangeEvent<ComboBox<String>, String> event) {
        applyFilters();
    }

    @Subscribe("rowLevelRoleFilter")
    public void onRowLevelRoleFilterComponentValueChange(AbstractField.ComponentValueChangeEvent<ComboBox<String>, String> event) {
        applyFilters();
    }

    @Subscribe("clearFiltersButton")
    public void onClearFiltersButtonClick(final com.vaadin.flow.component.ClickEvent<com.vaadin.flow.component.button.Button> event) {
        resourceRoleFilter.clear();
        rowLevelRoleFilter.clear();
    }

    @Subscribe(id = "refreshButton", subject = "clickListener")
    public void onRefreshButtonClick(final ClickEvent<JmixButton> event) {
        usersDl.load();
    }


    private void applyFilters() {
        usersDl.setParameter("resourceRoleCode", resourceRoleFilter.getValue());
        usersDl.setParameter("rowLevelRoleCode", rowLevelRoleFilter.getValue());
        usersDl.load();
    }

    @Supply(to = "usersDataGrid.resourceRoles", subject = "renderer")
    private Renderer<User> resourceRolesRenderer() {
        return new TextRenderer<>(user ->
                formatRoleNames(user.getUsername(), RoleAssignmentRoleType.RESOURCE));
    }

    @Supply(to = "usersDataGrid.rowLevelRoles", subject = "renderer")
    private Renderer<User> rowLevelRolesRenderer() {
        return new TextRenderer<>(user ->
                formatRoleNames(user.getUsername(), RoleAssignmentRoleType.ROW_LEVEL));
    }

    private String formatRoleNames(String username, String roleType) {
        Collection<RoleAssignment> assignments = roleAssignmentRepository.getAssignmentsByUsername(username);

        return assignments.stream()
                .filter(a -> roleType.equals(a.getRoleType()))
                .map(a -> resolveRoleName(a.getRoleCode(), roleType))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(", "));
    }

    private String resolveRoleName(String code, String roleType) {
        try {
            return RoleAssignmentRoleType.RESOURCE.equals(roleType)
                    ? resourceRoleRepository.getRoleByCode(code).getName()
                    : rowLevelRoleRepository.getRoleByCode(code).getName();
        } catch (RuntimeException e) {
            return code;
        }
    }
}
