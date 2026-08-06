package gov.fjc.fis.security;

import gov.fjc.fis.entity.Appropriation;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

@RowLevelRole(name = "AppropriationYearsData", code = AppropriationYearsDataRole.CODE)
public interface AppropriationYearsDataRole {
    String CODE = "data-appropriation-years";

    @JpqlRowLevelPolicy(
            entityClass = Appropriation.class,
            where = "{E}.budgetFiscalYear > '2017'")
    void appropriation();
}