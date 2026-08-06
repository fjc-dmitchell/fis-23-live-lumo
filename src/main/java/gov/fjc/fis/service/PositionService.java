package gov.fjc.fis.service;

import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.personnel.Position;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("fis_PositionService")
public class PositionService {
    private final DataManager dataManager;

    public PositionService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public Map<String, String> getEmployeeStatusItems() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("A", "Active");
        map.put("I", "Inactive");
        map.put("V", "Vacant"); // unique to FIS
        return map;
    }

    public Map<String, String> getRegTempItems() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("R", "Regular");
        map.put("T", "Temporary");
        return map;
    }

    public Map<String, String> getFullPartTimeItems() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("F", "Full-Time");
        map.put("T", "Part-Time");
        return map;
    }

    public Map<String, String> getWorkScheduleItems() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("F", "Full Time");
        map.put("T", "Part Time");
        map.put("I", "Intermittent");
        return map;
    }

    public Map<String, String> getEmployeeTypeItems() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("E", "Excepted"); // Excep Hrly in Peoplesoft
        map.put("H", "Hourly");
        return map;
    }

    // BELOW FROM OLD FIS
    public BigDecimal calculateHourlyRate(BigDecimal totalPay) {
        if (totalPay == null) {
            return BigDecimal.ZERO;
        } else {
            return totalPay.divide(new BigDecimal(2080), 2, RoundingMode.HALF_UP);
        }
    }

    public List<Position> getPositions(Division division) {
        String budgetOrg = division == null ? null : division.getBudgetOrg();
        return dataManager.load(Position.class)
                .query("SELECT e FROM fis_Position e"
                        + " WHERE (:divisionNull = TRUE OR e.jlCostOrgCd=:budgetOrg)")
                .parameter("divisionNull", division == null)
                .parameter("budgetOrg", budgetOrg)
                .list();
    }


//    public Boolean isHourlyEmployee(Position position) {
//        if (position == null || position.getEmplType() == null) {
//            return false;
//        } else {
//            return position.getEmplType().equalsIgnoreCase("H");
//        }
//    }
}