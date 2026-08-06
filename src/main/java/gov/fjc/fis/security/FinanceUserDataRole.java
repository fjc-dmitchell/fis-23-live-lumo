package gov.fjc.fis.security;

import io.jmix.flowuidata.entity.UserSettingsItem;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

@RowLevelRole(name = "FinanceUserDataRole", code = FinanceUserDataRole.CODE)
public interface FinanceUserDataRole {
    String CODE = "finance-user-data-role";

    @JpqlRowLevelPolicy(
            entityClass = UserSettingsItem.class,
            where = "{E}.createdBy = :current_user_username")
    void userSettingsItem();
}