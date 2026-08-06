package gov.fjc.fis.service;

import gov.fjc.fis.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("fis_DivisionAllocationService")
public class DivisionAllocationService {

    private final DataManager dataManager;

    public DivisionAllocationService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * use when setting allocations during division edit?
     *
     * @param division
     * @return
     */
    public List<ObjectCategory> getAvailableCategories(Division division) {
        var appropriation = division == null ? null : division.getAppropriation();

        return dataManager.load(ObjectCategory.class)
                .query("SELECT c FROM fis_ObjectCategory c"
                        + " WHERE c.appropriation = :appropriation "
                        + " AND c NOT IN (SELECT a.objectCategory FROM fis_DivisionAllocation a WHERE a.division = :division)"
                        + " ORDER BY c.majorObjectClass")
                .parameter("appropriation", appropriation)
                .parameter("division", division)
                .list();
    }

    public List<DivisionAllocation> getCategoryAllocations(ObjectCategory category) {
        return dataManager.load(DivisionAllocation.class)
                .query("SELECT a FROM fis_DivisionAllocation a"
                        + " INNER JOIN fis_Division dv ON dv=a.division"
                        + " WHERE a.objectCategory = :category"
                        + " ORDER BY dv.divisionCode")
                .parameter("category", category)
                .list();
    }

    /**
     * Returns division allocations for the appropriation.
     *
     * @param appropriation
     * @param funds
     * @return list of KV entities
     */
    public List<KeyValueEntity> fetchAllocations(Appropriation appropriation, List<Fund> funds) {
        return dataManager.loadValues(
                        "SELECT cat.majorObjectClass, div.divisionCode, alloc.oneYearAmount, alloc.twoYearAmount"
                                + " FROM fis_DivisionAllocation alloc"
                                + " INNER JOIN fis_Division div ON div = alloc.division"
                                + " INNER JOIN fis_ObjectCategory cat ON cat = alloc.objectCategory"
                                + " WHERE div.appropriation = :appropriation"
                                + " AND div.fund IN :funds")
                .properties("moc", "divcode", "oneyearamount", "twoyearamount")
                .parameter("appropriation", appropriation)
                .parameter("funds", funds)
                .list();
    }

    public List<KeyValueEntity> sumDivisionAllocations(List<Division> divisions) {
        return dataManager.loadValues(
                        "SELECT dv, dv.title, COALESCE(SUM(alloc.oneYearAmount),0), COALESCE(SUM(alloc.twoYearAmount),0)"
                                + " FROM fis_Division dv"
                                + " LEFT JOIN fis_DivisionAllocation alloc ON dv=alloc.division"
                                + " WHERE dv IN :divisions"
                                + " GROUP BY dv.id, dv.divisionCode, dv.title"
                                + " ORDER BY dv.divisionCode")
                .parameter("divisions", divisions)
                .properties("division", "title", "oneYearAllocations", "twoYearAllocations")
                .list();
    }
}