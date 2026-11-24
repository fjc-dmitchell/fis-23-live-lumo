package gov.fjc.fis.service;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Division;
import gov.fjc.fis.entity.Group;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provides operations for retrieving and locating groups within FIS divisions.
 * <p>
 * Groups are defined within a division and provide a way to organize related
 * activities. Because divisions and their groups may be defined for multiple
 * fiscal years, this service also supports retrieving the most recent group
 * associated with each group code across a collection of appropriations.
 *
 * @author Doug Mitchell
 */
@Component("fis_GroupService")
public class GroupService {

    private static final int GROUP_CODE_LENGTH = 2;

    private final DataManager dataManager;

    public GroupService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Returns the groups available for a division code across the specified
     * fiscal years.
     * <p>
     * If the same group code exists in more than one fiscal year, only the
     * group from the most recent fiscal year is returned. The resulting list
     * is ordered by group code.
     *
     * @param fiscalYears the appropriations representing the fiscal years to search
     * @param divCode     the code of the division whose groups are requested
     * @return the most recent group for each group code, or an empty list if
     * no fiscal years are provided or no matching groups exist
     */
    public List<Group> fetchGroupSearchList(Collection<Appropriation> fiscalYears, String divCode) {
        if (fiscalYears.isEmpty()) {
            return List.of();
        }

        List<Group> groupsNewestYearFirst = dataManager.load(Group.class)
                .query("SELECT e FROM fis_Group e"
                        + " WHERE e.division.appropriation IN :years"
                        + " AND e.division.divisionCode = :divCode"
                        + " ORDER BY e.division.appropriation.budgetFiscalYear DESC")
                .parameter("years", fiscalYears)
                .parameter("divCode", divCode)
                .list();

        Map<String, Group> latestGroupByCode = new TreeMap<>();
        for (Group group : groupsNewestYearFirst) {
            latestGroupByCode.putIfAbsent(group.getGroupCode(), group);
        }
        return List.copyOf(latestGroupByCode.values());
    }

    /**
     * Determines whether any groups are defined for the specified division.
     *
     * @param division the division to examine
     * @return {@code true} if at least one group is defined for the division;
     * otherwise {@code false}
     */
    public boolean groupsExist(Division division) {
        return dataManager.load(Group.class)
                .query("SELECT e FROM fis_Group e WHERE e.division = :division")
                .parameter("division", division)
                .maxResults(1)
                .optional()
                .isPresent();
    }

    /**
     * Returns all groups defined for the specified division.
     *
     * @param division the division whose groups are requested
     * @return the division's groups ordered by group code, or an empty list if
     * no groups are defined
     */
    public List<Group> fetchGroups(Division division) {
        return dataManager.load(Group.class)
                .query("SELECT e FROM fis_Group e"
                        + " WHERE e.division = :division"
                        + " ORDER BY e.groupCode")
                .parameter("division", division)
                .list();
    }

    /**
     * Finds the group to which an activity belongs.
     * <p>
     * The group code is derived from the leading
     * {@value #GROUP_CODE_LENGTH} characters of the activity number.
     *
     * @param division       the division containing the activity and group
     * @param activityNumber the activity number from which to derive the group code
     * @return an {@link Optional} containing the matching group, or an empty
     * {@code Optional} if no matching group exists
     */
    public Optional<Group> fetchGroupByActivity(Division division, String activityNumber) {
        return dataManager.load(Group.class)
                .query("SELECT e FROM fis_Group e"
                        + " WHERE e.groupCode = :groupCode"
                        + " AND e.division = :division")
                .parameter("groupCode", toGroupCode(activityNumber))
                .parameter("division", division)
                .optional();
    }

    /**
     * Derives a group code from the leading characters of an activity number.
     *
     * @param activityNumber the activity number from which to derive the group code
     * @return the derived group code
     */
    private static String toGroupCode(String activityNumber) {
        return activityNumber.substring(0, Math.min(GROUP_CODE_LENGTH, activityNumber.length()));
    }
}