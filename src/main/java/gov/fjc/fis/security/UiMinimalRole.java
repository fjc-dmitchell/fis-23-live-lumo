package gov.fjc.fis.security;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.UiMinimalPolicies;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Provides the minimum UI permissions required to access the FIS application.
 * <p>
 * This role serves as a foundational security role that is typically
 * inherited by other resource roles. It grants only the permissions
 * necessary to authenticate and access the application's main user
 * interface, while leaving business functions, menus, views, and data
 * access to more specialized roles.
 * </p>
 * <p>
 * By separating basic UI access from functional permissions, additional
 * roles can focus on granting capabilities specific to a user's
 * responsibilities without duplicating common login and navigation
 * permissions.
 * </p>
 */
@ResourceRole(name = "UI: minimal access", code = UiMinimalRole.CODE, scope = SecurityScope.UI)
public interface UiMinimalRole extends UiMinimalPolicies {

    String CODE = "ui-minimal";

    /**
     * Grants access to the application's main view.
     */
    @ViewPolicy(viewIds = "fis_MainView")
    void main();

    /**
     * Grants access to the login view an* permission to authenticate to the UI.
     */
    @ViewPolicy(viewIds = "fis_LoginView")
    @SpecificPolicy(resources = "ui.loginToUi")
    void login();
}
