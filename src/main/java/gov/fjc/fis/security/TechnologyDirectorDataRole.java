package gov.fjc.fis.security;

import gov.fjc.fis.entity.*;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

@RowLevelRole(name = "ITO Director data", code = TechnologyDirectorDataRole.CODE, description = "division and branch constraints")
public interface TechnologyDirectorDataRole extends AppropriationYearsDataRole {
    String CODE = "data-ito-director";

    @JpqlRowLevelPolicy(
            entityClass = Division.class,
            where = "{E}.divisionCode = '1'")
    void division();

    @JpqlRowLevelPolicy(
            entityClass = Branch.class,
            join = "join {E}.division sec_d",
            where = "{E}.branchCode in ('02','03') and sec_d.divisionCode='1'")
    void branch();

    @JpqlRowLevelPolicy(
            entityClass = Activity.class,
            join = "join {E}.branch sec_b join {E}.division sec_d",
            where = "sec_b.branchCode in ('02','03') and sec_d.divisionCode='1'")
    void activity();

    @JpqlRowLevelPolicy(
            entityClass = ActivityProjection.class,
            join = "join {E}.activity sec_a join a.division sec_d left join a.branch sec_b",
            where = "sec_b.branchCode in ('02','03') and sec_d.divisionCode='1'")
    void activityProjection();

    @JpqlRowLevelPolicy(
            entityClass = Obligation.class,
            join = "join {E}.activity sec_a join sec_a.division sec_d left join sec_a.branch sec_b",
            where = "sec_b.branchCode in ('02','03') and sec_d.divisionCode='1'")
    void obligation();
}