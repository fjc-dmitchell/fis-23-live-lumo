package gov.fjc.fis.security;

import gov.fjc.fis.entity.*;
import io.jmix.flowuidata.entity.UserSettingsItem;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

/**
 * Row-level security role for Education Administrators.
 * <p>
 * This role restricts users to education-related financial data and extends
 * {@link AppropriationYearsDataRole} to limit visibility to recent budget
 * fiscal years.
 * </p>
 * <p>
 * Users assigned this role may access activities, projections,
 * reimbursements, obligations, invoices, Fund Control Notices (FCNs), and
 * related reference data associated with the Education Division and
 * supporting organizational units. Access is limited to Divisions 2, 6,
 * and selected Division 9 records associated with Education cost
 * organizations.
 * </p>
 * <p>
 * The role also excludes personnel-related object classes and major object
 * classes beginning with {@code 1}, restricting users to non-personnel
 * financial activity. In addition, users may access only their own saved
 * search criteria and user settings.
 * </p>
 * <p>
 * This role is intended for Education Division financial management and
 * reporting activities while preventing access to data outside the user's
 * authorized organizational and budgetary scope.
 * </p>
 * @author Doug Mitchell
 */
@RowLevelRole(name = "Education Admin Data", code = EducationAdministratorDataRole.CODE, description = "division constraints")
public interface EducationAdministratorDataRole extends AppropriationYearsDataRole {
    String CODE = "data-ed-admin";

    /**
     * Restricts object categories to non-personnel major object classes.
     */
    @JpqlRowLevelPolicy(
            entityClass = ObjectCategory.class,
            where = "{E}.majorObjectClass not like '1%'")
    void objectCategory();

    /**
     * Restricts object classes to non-personnel major object classes.
     */
    @JpqlRowLevelPolicy(
            entityClass = ObjectClass.class,
            join = "join {E}.objectCategory sec_cat",
            where = "sec_cat.majorObjectClass not like '1%'")
    void objectClass();

    /**
     * Restricts access to authorized divisions.
     */
    @JpqlRowLevelPolicy(
            entityClass = Division.class,
            where = "{E}.divisionCode in ('2','6','9')")
    void division();

    /**
     * Restricts access to branches within authorized divisions.
     */
    @JpqlRowLevelPolicy(
            entityClass = Branch.class,
            join = "join {E}.division sec_d",
            where = "sec_d.divisionCode in ('2','6','9')")
    void branch();

    /**
     * Restricts access to groups within authorized divisions.
     */
    @JpqlRowLevelPolicy(
            entityClass = Group.class,
            join = "join {E}.division sec_d",
            where = "sec_d.divisionCode in ('2','6','9')")
    void group();

    /**
     * Restricts activities to authorized education-related divisions and
     * excludes activities associated with personnel object classes.
     */
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

    /**
     * Restricts activity projections to authorized education-related activities
     * and non-personnel object classes.
     */
    @JpqlRowLevelPolicy(
            entityClass = ActivityProjection.class,
            join = "join {E}.activity sec_a join sec_a.division sec_d left join sec_a.costOrg sec_co join {E}.objectClass sec_ob",
            where = "((sec_d.divisionCode in ('2','6')) or (sec_d.divisionCode = '9' and sec_co.divisionCode = '2')) " +
                    "and sec_ob.budgetObjectClass not like '1%'")
    void activityProjection();

    /**
     * Restricts activity reimbursements to authorized divisions and
     * non-personnel object classes.
     */
    @JpqlRowLevelPolicy(
            entityClass = ActivityReimbursement.class,
            join = "join {E}.activity sec_a join sec_a.division sec_d join {E}.objectClass sec_ob",
            where = "sec_d.divisionCode in ('2','6','9') and sec_ob.budgetObjectClass not like '1%'")
    void activityReimbursement();

    /**
     * Restricts obligations to authorized education-related activities and
     * non-personnel object classes.
     */
    @JpqlRowLevelPolicy(
            entityClass = Obligation.class,
            join = "join {E}.activity sec_a join sec_a.division sec_d left join sec_a.costOrg sec_co join {E}.objectClass sec_ob",
            where = "((sec_d.divisionCode in ('2','6')) or (sec_d.divisionCode = '9' and sec_co.divisionCode = '2')) " +
                    "and sec_ob.budgetObjectClass not like '1%'")
    void obligation();

    /**
     * Restricts invoices to those associated with authorized obligations.
     */
    @JpqlRowLevelPolicy(
            entityClass = Invoice.class,
            join = "join {E}.obligation.activity sec_a join sec_a.division sec_d left join sec_a.costOrg sec_co join {E}.obligation.objectClass sec_ob",
            where = "((sec_d.divisionCode in ('2','6')) or (sec_d.divisionCode = '9' and sec_co.divisionCode = '2')) " +
                    "and sec_ob.budgetObjectClass not like '1%'")
    void invoice();

    /**
     * Restricts Fund Control Notices to those associated with authorized
     * obligations.
     */
    @JpqlRowLevelPolicy(
            entityClass = FundControlNotice.class,
            join = "join {E}.obligation.activity sec_a join sec_a.division sec_d left join sec_a.costOrg sec_co join {E}.obligation.objectClass sec_ob",
            where = "((sec_d.divisionCode in ('2','6')) or (sec_d.divisionCode = '9' and sec_co.divisionCode = '2')) " +
                    "and sec_ob.budgetObjectClass not like '1%'")
    void fundControlNotice();

    /**
     * Restricts users to their own saved settings and search criteria.
     */
    @JpqlRowLevelPolicy(entityClass = UserSettingsItem.class, where = "{E}.createdBy = :current_user_username")
    void userSettingsItem();
}