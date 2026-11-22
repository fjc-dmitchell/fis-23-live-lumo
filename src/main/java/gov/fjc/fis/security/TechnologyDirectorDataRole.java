package gov.fjc.fis.security;

import gov.fjc.fis.entity.*;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

/**
 * Row-level security role for the Technology Director.
 * <p>
 * This role restricts users to financial data associated with the authorized
 * technology organization. Access is limited to Division 1 and Branches 02
 * and 03, including their activities, activity projections, and obligations.
 * </p>
 * <p>
 * The role extends {@link AppropriationYearsDataRole} to further limit
 * visibility to appropriations within the permitted budget fiscal-year
 * lookback period.
 * </p>
 * <p>
 * This role is intended to support technology financial management and
 * reporting while preventing access to records outside the Technology
 * Director's authorized organizational and fiscal-year scope.
 * </p>
 * @author Doug Mitchell
 */
@RowLevelRole(name = "ITO Director data", code = TechnologyDirectorDataRole.CODE, description = "division and branch constraints")
public interface TechnologyDirectorDataRole extends AppropriationYearsDataRole {
    String CODE = "data-ito-director";

    /**
     * Restricts access to the authorized technology division.
     */
    @JpqlRowLevelPolicy(
            entityClass = Division.class,
            where = "{E}.divisionCode = '1'")
    void division();

    /**
     * Restricts access to the authorized branches within the technology division.
     */
    @JpqlRowLevelPolicy(
            entityClass = Branch.class,
            join = "join {E}.division sec_d",
            where = "{E}.branchCode in ('02','03') and sec_d.divisionCode='1'")
    void branch();

    /**
     * Restricts activities to the authorized technology division and branches.
     */
    @JpqlRowLevelPolicy(
            entityClass = Activity.class,
            join = "join {E}.branch sec_b join {E}.division sec_d",
            where = "sec_b.branchCode in ('02','03') and sec_d.divisionCode='1'")
    void activity();

    /**
     * Restricts activity projections to activities associated with the
     * authorized technology division and branches.
     */
    @JpqlRowLevelPolicy(
            entityClass = ActivityProjection.class,
            join = "join {E}.activity sec_a join sec_a.division sec_d left join sec_a.branch sec_b",
            where = "sec_b.branchCode in ('02','03') and sec_d.divisionCode='1'")
    void activityProjection();

    /**
     * Restricts obligations to activities associated with the authorized
     * technology division and branches.
     */
    @JpqlRowLevelPolicy(
            entityClass = Obligation.class,
            join = "join {E}.activity sec_a join sec_a.division sec_d left join sec_a.branch sec_b",
            where = "sec_b.branchCode in ('02','03') and sec_d.divisionCode='1'")
    void obligation();
}