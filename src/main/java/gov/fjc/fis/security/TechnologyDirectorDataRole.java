package gov.fjc.fis.security;

import gov.fjc.fis.entity.*;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

@RowLevelRole(name = "ITO Director data", code = TechnologyDirectorDataRole.CODE, description = "division and branch constraints")
public interface TechnologyDirectorDataRole extends AppropriationYearsDataRole {
    String CODE = "data-ito-director";

    @JpqlRowLevelPolicy(
            entityClass = Division.class,
            where = "{E}.divisionCode in ('1','5')")
    void division();

    @JpqlRowLevelPolicy(
            entityClass = Branch.class,
            join = "{E}.division d",
            where = "{E}.branchCode in ('02','03') and d.divisionCode='1'")
    void branch();

    @JpqlRowLevelPolicy(
            entityClass = Activity.class,
            join = "left join {E}.branch b join {E}.division d",
            where = "(b.branchCode in ('02','03') and d.divisionCode='1') or d.divisionCode='5'")
    void activity();

    @JpqlRowLevelPolicy(
            entityClass = ActivityProjection.class,
            join = "{E}.activity a join a.division d left join a.branch b",
            where = "(b.branchCode in ('02','03') and d.divisionCode='1') or d.divisionCode='5'")
    void activityProjection();

    @JpqlRowLevelPolicy(
            entityClass = Obligation.class,
            join = "{E}.activity a join a.division d left join a.branch b",
            where = "(b.branchCode in ('02','03') and d.divisionCode='1') or d.divisionCode='5'")
    void obligation();
}