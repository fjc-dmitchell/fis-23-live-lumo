package gov.fjc.fis.security;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Grants unrestricted access to all FIS resources.
 * <p>
 * This role is intended for system administrators who require complete
 * access to application functionality, data, views, menus, and security
 * resources. Users assigned this role bypass the entity, attribute, view,
 * menu, and specific permission restrictions applied by other security
 * roles.
 * </p>
 * <p>
 * The role provides full read, create, update, and delete access to all
 * entities and attributes within the application, as well as access to all
 * UI views, menu items, and protected resources.
 * </p>
 * <p>
 * Because this role provides unrestricted access across the entire FIS
 * application, it should be granted only to trusted administrators
 * responsible for application configuration, troubleshooting, maintenance,
 * and security administration.
 * </p>
 */
@ResourceRole(name = "Full Access", code = FullAccessRole.CODE)
public interface FullAccessRole {

    String CODE = "system-full-access";

    /**
     * Grants full access to all entities, entity attributes, views, menus,
     * and specifically protected resources within the application.
     */
    @EntityPolicy(entityName = "*", actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityName = "*", attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @ViewPolicy(viewIds = "*")
    @MenuPolicy(menuIds = "*")
    @SpecificPolicy(resources = "*")
    void fullAccess();
}