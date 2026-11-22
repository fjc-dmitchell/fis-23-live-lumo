package gov.fjc.fis.security;

import io.jmix.flowuidata.entity.UserSettingsItem;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

/**
 * Row-level security role for finance users.
 * <p>
 * This role provides data-level security policies that apply to standard
 * finance users. Unlike specialized roles that restrict access to specific
 * divisions, branches, or financial data, this role is used to limit access
 * to user-specific application settings.
 * </p>
 * <p>
 * Users assigned this role may access only their own saved search criteria,
 * UI preferences, and other persisted user settings. This prevents users
 * from viewing or modifying settings created by other users.
 * </p>
 * @author Doug Mitchell
 */
@RowLevelRole(name = "FinanceUserDataRole", code = FinanceUserDataRole.CODE)
public interface FinanceUserDataRole {
    String CODE = "finance-user-data-role";

    /**
     * Restricts users to their own saved settings and search criteria.
     */
    @JpqlRowLevelPolicy(
            entityClass = UserSettingsItem.class,
            where = "{E}.createdBy = :current_user_username")
    void userSettingsItem();
}