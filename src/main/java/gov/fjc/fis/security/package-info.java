/**
 * Security roles and access control definitions for FIS.
 * <p>
 * This package contains the resource roles and data roles that define
 * authorization policies within the application. These roles determine which
 * screens, views, menus, actions, entities, attributes, and operations are
 * available to authenticated users.
 * <p>
 * Resource roles control access to application features and user interface
 * components, while data roles define permissions for viewing, creating,
 * modifying, and deleting domain data. Together, these roles implement the
 * application's role-based access control (RBAC) model.
 * <p>
 * Classes in this package are typically declared using Jmix security
 * annotations and are discovered automatically by the framework during
 * application startup.
 * <p>
 * The roles defined in this package provide the security foundation for FIS,
 * ensuring that users can access only the functionality and data required to
 * perform their assigned responsibilities.
 *
 * @author Doug Mitchell
 */
package gov.fjc.fis.security;