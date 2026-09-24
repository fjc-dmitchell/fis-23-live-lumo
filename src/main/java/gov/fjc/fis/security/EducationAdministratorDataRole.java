package gov.fjc.fis.security;

import gov.fjc.fis.entity.*;
import io.jmix.flowuidata.entity.UserSettingsItem;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

@RowLevelRole(name = "Education Admin Data", code = EducationAdministratorDataRole.CODE, description = "division constraints")
public interface EducationAdministratorDataRole extends AppropriationYearsDataRole {
    String CODE = "data-ed-admin";

    @JpqlRowLevelPolicy(
            entityClass = ObjectCategory.class,
            where = "{E}.majorObjectClass not like '1%'")
    void objectCategory();

    @JpqlRowLevelPolicy(
            entityClass = ObjectClass.class,
            join = "join {E}.objectCategory sec_cat",
            where = "sec_cat.majorObjectClass not like '1%'")
    void objectClass();

    @JpqlRowLevelPolicy(
            entityClass = Division.class,
            where = "{E}.divisionCode in ('2','6','9')")
    void division();

    @JpqlRowLevelPolicy(
            entityClass = Branch.class,
            join = "join {E}.division sec_d",
            where = "sec_d.divisionCode in ('2','6','9')")
    void branch();

    @JpqlRowLevelPolicy(
            entityClass = Group.class,
            join = "join {E}.division sec_d",
            where = "sec_d.divisionCode in ('2','6','9')")
    void group();

    @JpqlRowLevelPolicy(
            entityClass = Activity.class,
            join = "join {E}.division sec_d " +
                    "left join {E}.costOrg sec_co",
            where = "((sec_d.divisionCode in ('2','6')) " +
                    "or (sec_d.divisionCode = '9' and sec_co.divisionCode = '2')) " +
                    "and NOT EXISTS (" +
                    "       select sec_p " +
                    "       from fis_ActivityProjection sec_p " +
                    "       join sec_p.objectClass sec_ob " +
                    "       where sec_p.activity = e " +
                    "         and sec_ob.budgetObjectClass like '1%' " +
                    ")"
    )
    void activity();

    @JpqlRowLevelPolicy(
            entityClass = ActivityProjection.class,
            join = "join {E}.activity sec_a join sec_a.division sec_d left join sec_a.costOrg sec_co join {E}.objectClass sec_ob",
            where = "((sec_d.divisionCode in ('2','6')) or (sec_d.divisionCode = '9' and sec_co.divisionCode = '2')) " +
                    "and sec_ob.budgetObjectClass not like '1%'")
    void activityProjection();

    @JpqlRowLevelPolicy(
            entityClass = ActivityReimbursement.class,
            join = "join {E}.activity sec_a join sec_a.division sec_d join {E}.objectClass sec_ob",
            where = "sec_d.divisionCode in ('2','6','9') and sec_ob.budgetObjectClass not like '1%'")
    void activityReimbursement();

    @JpqlRowLevelPolicy(
            entityClass = Obligation.class,
            join = "join {E}.activity sec_a join sec_a.division sec_d left join sec_a.costOrg sec_co join {E}.objectClass sec_ob",
            where = "((sec_d.divisionCode in ('2','6')) or (sec_d.divisionCode = '9' and sec_co.divisionCode = '2')) " +
                    "and sec_ob.budgetObjectClass not like '1%'")
    void obligation();

    @JpqlRowLevelPolicy(
            entityClass = Invoice.class,
            join = "join {E}.obligation.activity sec_a join sec_a.division sec_d left join sec_a.costOrg sec_co join {E}.obligation.objectClass sec_ob",
            where = "((sec_d.divisionCode in ('2','6')) or (sec_d.divisionCode = '9' and sec_co.divisionCode = '2')) " +
                    "and sec_ob.budgetObjectClass not like '1%'")
    void invoice();

    @JpqlRowLevelPolicy(
            entityClass = FundControlNotice.class,
            join = "join {E}.obligation.activity sec_a join sec_a.division sec_d left join sec_a.costOrg sec_co join {E}.obligation.objectClass sec_ob",
            where = "((sec_d.divisionCode in ('2','6')) or (sec_d.divisionCode = '9' and sec_co.divisionCode = '2')) " +
                    "and sec_ob.budgetObjectClass not like '1%'")
    void fundControlNotice();

    @JpqlRowLevelPolicy(entityClass = UserSettingsItem.class, where = "{E}.createdBy = :current_user_username")
    void userSettingsItem();
}