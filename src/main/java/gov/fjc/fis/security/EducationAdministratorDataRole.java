package gov.fjc.fis.security;

import gov.fjc.fis.entity.*;
import io.jmix.flowuidata.entity.UserSettingsItem;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

@RowLevelRole(name = "Education Admin Data", code = EducationAdministratorDataRole.CODE, description = "division constraints")
public interface EducationAdministratorDataRole extends AppropriationYearsDataRole {
    String CODE = "data-ed-admin";

    @JpqlRowLevelPolicy(
            entityClass = Division.class,
            where = "{E}.divisionCode in ('2','6')")
    void division();

    @JpqlRowLevelPolicy(
            entityClass = Branch.class,
            join = "{E}.division d",
            where = "d.divisionCode in ('2','6')")
    void branch();

    @JpqlRowLevelPolicy(
            entityClass = Group.class,
            join = "{E}.division d",
            where = "d.divisionCode in ('2','6')")
    void group();

    @JpqlRowLevelPolicy(
            entityClass = Activity.class,
            join = "{E}.division d",
            where = "d.divisionCode in ('2','6')")
    void activity();

    @JpqlRowLevelPolicy(
            entityClass = ActivityProjection.class,
            join = "{E}.activity a join a.division d",
            where = "d.divisionCode in ('2','6')")
    void activityProjection();

    @JpqlRowLevelPolicy(
            entityClass = ActivityReimbursement.class,
            join = "{E}.activity a join a.division d",
            where = "d.divisionCode in ('2','6')")
    void activityReimbursement();

    @JpqlRowLevelPolicy(
            entityClass = Obligation.class,
            join = "{E}.activity a join a.division d",
            where = "d.divisionCode in ('2','6')")
    void obligation();

    @JpqlRowLevelPolicy(
            entityClass = Invoice.class,
            join = "{E}.obligation.activity.division d",
            where = "d.divisionCode in ('2','6')")
    void invoice();

    @JpqlRowLevelPolicy(
            entityClass = FundControlNotice.class,
            join = "{E}.obligation.activity.division d",
            where = "d.divisionCode in ('2','6')")
    void fundControlNotice();

    @JpqlRowLevelPolicy(entityClass = UserSettingsItem.class, where = "{E}.createdBy = :current_user_username")
    void userSettingsItem();
}