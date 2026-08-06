package gov.fjc.fis.service;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.Group;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("fis_GroupService")
public class GroupService {

    private final DataManager dataManager;

    public GroupService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public List<Group> fetchGroupSearchList(List<Appropriation> fiscalYears, String divCode) {
        var descendingYears = fiscalYears.stream()
                .sorted(Comparator.comparing(Appropriation::getBudgetFiscalYear).reversed())
                .toList();
        List<Group> groupList = new ArrayList<>();
        Set<String> groupCodes = null;

        for (Appropriation year : descendingYears) {
            List<Group> groupsInBfyList;
            if(groupCodes == null) {
                // done for safety for first iteration since null set
                // behavior is not defined by JPQL specification
                groupsInBfyList = dataManager.load(Group.class)
                        .query("SELECT e FROM fis_Group e"
                                + " WHERE e.division.appropriation = :year"
                                + " AND e.division.divisionCode = :divCode")
                        .parameter("year", year)
                        .parameter("divCode", divCode)
                        .list();
            } else {
                groupsInBfyList = dataManager.load(Group.class)
                        .query("SELECT e FROM fis_Group e"
                                + " WHERE e.division.appropriation = :year"
                                + " AND e.division.divisionCode = :divCode"
                                + " AND e.groupCode NOT IN :groupCodes")
                        .parameter("year", year)
                        .parameter("divCode", divCode)
                        .parameter("groupCodes", groupCodes)
                        .list();
            }
            groupList.addAll(groupsInBfyList);
            groupCodes = groupList.stream().map(Group::getGroupCode).collect(Collectors.toSet());
        }
        return groupList.stream().sorted(Comparator.comparing(Group::getGroupCode)).toList();
    }

    public boolean groupsExist(Division division) {
        return !dataManager.load(Group.class)
                .query("SELECT e FROM fis_Group e WHERE e.division = :division")
                .parameter("division", division)
                .maxResults(1)
                .list()
                .isEmpty();
    }

    public List<Group> fetchGroups(Division division) {
        return dataManager.load(Group.class)
                .query("SELECT e FROM fis_Group e"
                        + " WHERE e.division = :division"
                        + " ORDER BY e.groupCode")
                .parameter("division", division)
                .list();
    }

    public Group getGroupByActivity(Division division, String activityNumber) {
        String groupCode = activityNumber.length() >= 2
                ? activityNumber.substring(0, 2)
                : activityNumber;
        return dataManager.load(Group.class)
                .query("SELECT g FROM fis_Group g"
                        + " WHERE g.groupCode = :groupCode"
                        + " AND g.division = :division")
                .parameter("groupCode", groupCode)
                .parameter("division", division)
                .optional()
                .orElse(null);
    }
}