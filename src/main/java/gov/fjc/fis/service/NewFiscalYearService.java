package gov.fjc.fis.service;

import gov.fjc.fis.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service to create new budget fiscal year and associated entities with skeleton data.
 * Only non-Foundation divisions, branches, groups, and activities are included.
 * Activity criteria requested by Mark Hannan and Mary Greiner in April 1999.
 * Unlike FIS 1.0, no empty division allocations or activity projections are created
 * to ensure the new audit function includes the initial creation.
 * <p>
 * The whole rollover runs in a single transaction. Prior-year collections such as
 * categories, branches, groups, and activities are lazily loaded within that transaction.
 * <p>
 * Parent entities (appropriation, category, division) are saved individually without reload
 * because only their IDs are needed by children. Leaf entities are saved in batches.
 *
 * @author Doug Mitchell
 * @version 2.4
 * @since 2.1
 */
@Component("fis_NewFiscalYearService")
public class NewFiscalYearService {

    private static final Logger log = LoggerFactory.getLogger(NewFiscalYearService.class);

    private final DataManager dataManager;
    private final DivisionService divisionService;

    private final Fund oneYearFund;
    private final Fund twoYearFund;

    /**
     * Training projects are only carried forward when their activity number ends with this suffix.
     */
    private static final String GENERIC_TRAINING_SUFFIX = "00";

    public NewFiscalYearService(DataManager dataManager,
                                DivisionService divisionService,
                                FundService fundService) {
        this.dataManager = dataManager;
        this.divisionService = divisionService;
        this.oneYearFund = fundService.getAppropriationOneYearFund();
        this.twoYearFund = fundService.getAppropriationTwoYearFund();
    }

    /**
     * Creates new Appropriation and associated entities. If unsuccessful, rollback entire
     * transaction and throw exception to user interface.
     *
     * @param requestedFiscalYear String representing the budget fiscal year to create
     * @throws IllegalArgumentException if the requested year is not the next year in sequence
     */
    @Transactional
    public void createAppropriation(String requestedFiscalYear) {
        log.info("Begin transaction for New Fiscal Year: {}", requestedFiscalYear);

        Appropriation oldAppropriation = fetchMaxAppropriation();
        String expectedFiscalYear = getNextFiscalYear(oldAppropriation);
        if (!expectedFiscalYear.equals(requestedFiscalYear)) {
            throw new IllegalArgumentException(
                    "The requested fiscal year is not the next fiscal year in sequence: " + requestedFiscalYear
                            + " (expected: " + expectedFiscalYear + ")");
        }

        Appropriation newAppropriation = buildAppropriation(requestedFiscalYear);
        dataManager.saveWithoutReload(newAppropriation);
        log.info("Appropriation saved: {}", requestedFiscalYear);

        createObjectCategories(oldAppropriation, newAppropriation);
        createDivisions(oldAppropriation, newAppropriation);
        log.info("End transaction for New Fiscal Year: {}", requestedFiscalYear);
    }

    /**
     * Increment the fiscal year of the given appropriation by one.
     *
     * @param appropriation the most recent appropriation
     * @return String containing the fiscal year following the appropriation's year
     * @throws IllegalStateException if the appropriation's fiscal year is not numeric
     */
    private String getNextFiscalYear(Appropriation appropriation) {
        String budgetFiscalYear = appropriation.getBudgetFiscalYear();
        try {
            return String.valueOf(Integer.parseInt(budgetFiscalYear) + 1);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Cannot parse fiscal year: " + budgetFiscalYear, e);
        }
    }

    /**
     * Increment the most recent fiscal year by one.
     *
     * @return String containing the next available fiscal year
     */
    public String getNextFiscalYear() {
        return getNextFiscalYear(fetchMaxAppropriation());
    }

    /**
     * Fetches the most recent Appropriation in FIS.
     *
     * @return Appropriation entity
     * @throws IllegalStateException if Appropriation not found
     */
    private Appropriation fetchMaxAppropriation() {
        return dataManager.load(Appropriation.class)
                .query("SELECT a FROM fis_Appropriation a"
                        + " WHERE a.budgetFiscalYear = ("
                        + "SELECT MAX(e.budgetFiscalYear) FROM fis_Appropriation e)")
                .optional()
                .orElseThrow(() -> new IllegalStateException("No appropriations found"));
    }

    /**
     * Saves all entities in a single batch without reloading them.
     *
     * @param entities new entities to persist
     */
    private void saveAll(Collection<?> entities) {
        SaveContext saveContext = new SaveContext().setDiscardSaved(true);
        entities.forEach(saveContext::saving);
        dataManager.save(saveContext);
    }

    /**
     * Creates object categories for new fiscal year based on prior year.
     *
     * @param oldAppropriation prior year appropriation whose categories are copied
     * @param newAppropriation new appropriation that will own the copied categories
     */
    private void createObjectCategories(Appropriation oldAppropriation, Appropriation newAppropriation) {
        for (ObjectCategory oldCategory : oldAppropriation.getCategories()) {
            ObjectCategory newCategory = copyObjectCategory(oldCategory, newAppropriation);
            dataManager.saveWithoutReload(newCategory);
            log.info("Category saved: {}", newCategory.getTitleAndCode());

            createObjectClasses(oldCategory, newCategory);
        }
    }

    /**
     * Creates object classes for new category based on old category.
     *
     * @param oldCategory prior year category whose object classes are copied
     * @param newCategory new category that will own the copied object classes
     */
    private void createObjectClasses(ObjectCategory oldCategory, ObjectCategory newCategory) {
        List<ObjectClass> newObjectClasses = oldCategory.getBudgetObjectClasses().stream()
                .map(oldObjectClass -> copyObjectClass(oldObjectClass, newCategory))
                .toList();
        saveAll(newObjectClasses);
        log.info("Saved {} object classes for category: {}",
                newObjectClasses.size(), newCategory.getTitleAndCode());
    }

    /**
     * Creates non-foundation divisions for new fiscal year based on prior year.
     *
     * @param oldAppropriation prior year appropriation whose divisions are copied
     * @param newAppropriation new appropriation that will own the copied divisions
     */
    private void createDivisions(Appropriation oldAppropriation, Appropriation newAppropriation) {
        for (Division oldDivision : divisionService.fetchNonFoundationDivisions(oldAppropriation)) {
            Division newDivision = copyDivision(oldDivision, newAppropriation);
            dataManager.saveWithoutReload(newDivision);
            log.info("Division saved: {}", newDivision.getTitleAndCode());

            Map<String, Branch> newBranchesByCode = createBranches(oldDivision, newDivision);
            Map<String, Group> newGroupsByCode = createGroups(oldDivision, newDivision);
            createActivities(oldDivision, newDivision, newBranchesByCode, newGroupsByCode);
        }
    }

    /**
     * Creates branches for new division based on old division.
     *
     * @param oldDivision prior year division whose branches are copied
     * @param newDivision new division that will own the copied branches
     * @return the new branches keyed by branch code
     */
    private Map<String, Branch> createBranches(Division oldDivision, Division newDivision) {
        List<Branch> newBranches = oldDivision.getBranches().stream()
                .map(oldBranch -> copyBranch(oldBranch, newDivision))
                .toList();
        saveAll(newBranches);
        log.info("Saved {} branches for division: {}", newBranches.size(), newDivision.getTitleAndCode());

        return newBranches.stream()
                .collect(Collectors.toMap(Branch::getBranchCode, Function.identity()));
    }

    /**
     * Creates groups for new division based on old division.
     *
     * @param oldDivision prior year division whose groups are copied
     * @param newDivision new division that will own the copied groups
     * @return the new groups keyed by group code
     */
    private Map<String, Group> createGroups(Division oldDivision, Division newDivision) {
        List<Group> newGroups = oldDivision.getGroups().stream()
                .map(oldGroup -> copyGroup(oldGroup, newDivision))
                .toList();
        saveAll(newGroups);
        log.info("Saved {} groups for division: {}", newGroups.size(), newDivision.getTitleAndCode());

        return newGroups.stream()
                .collect(Collectors.toMap(Group::getGroupCode, Function.identity()));
    }

    /**
     * Creates certain activities for new division based on those in old division and
     * associates each activity with matching branches and groups of the new division.
     * For generic training activities, city and state are included. No activities
     * set to two year fund. Based on criteria provided by Mark and Mary in 1999.
     *
     * @param oldDivision       prior year division whose activities are copied
     * @param newDivision       new division that will own the copied activities
     * @param newBranchesByCode branches of the new division keyed by branch code
     * @param newGroupsByCode   groups of the new division keyed by group code
     */
    private void createActivities(Division oldDivision,
                                  Division newDivision,
                                  Map<String, Branch> newBranchesByCode,
                                  Map<String, Group> newGroupsByCode) {
        List<Activity> newActivities =
                oldDivision.getActivities().stream()
                        .filter(this::shouldCopyActivity)
                        .map(oldActivity ->
                                copyActivity(
                                        oldActivity,
                                        newDivision,
                                        resolveFund(oldActivity.getFund()),
                                        newBranchesByCode,
                                        newGroupsByCode))
                        .toList();
        saveAll(newActivities);
        log.info("Saved {} activities for division: {}", newActivities.size(), newDivision.getTitleAndCode());
    }

    /**
     * Only copy non-training projects OR training projects ending in {@value #GENERIC_TRAINING_SUFFIX}.
     *
     * @param activity prior year activity
     * @return true if activity should be copied
     */
    private boolean shouldCopyActivity(Activity activity) {
        return !isTrainingProject(activity) || isGenericTrainingActivityForGroup(activity);
    }

    private boolean isGenericTrainingActivityForGroup(Activity activity) {
        String activityNumber = activity.getActivityNumber();
        Group group = activity.getGroup();

        if (activityNumber == null || group == null || group.getGroupCode() == null) {
            return false;
        }

        return activityNumber.endsWith(GENERIC_TRAINING_SUFFIX)
                && activityNumber.startsWith(group.getGroupCode());
    }

    private boolean isTrainingProject(Activity activity) {
        return Boolean.TRUE.equals(activity.getTrainingProject());
    }

    /**
     * ensure no new activities are assigned two-year-fund
     *
     * @param oldFund the old fund assigned to the activity, incl. JITF or FJC Foundation
     * @return the new funds, never the two-year-fund
     */
    private Fund resolveFund(Fund oldFund) {
        return twoYearFund.equals(oldFund) ? oneYearFund : oldFund;
    }

    // --------------------------------------------------------------------------------
    // Create FIS entities for new fiscal year
    // --------------------------------------------------------------------------------

    private Appropriation buildAppropriation(String fiscalYear) {
        Appropriation appropriation = dataManager.create(Appropriation.class);
        appropriation.setBudgetFiscalYear(fiscalYear);
        appropriation.setStatus(true);
        appropriation.setOneYearAmount(BigDecimal.ZERO);
        appropriation.setTwoYearAmount(BigDecimal.ZERO);
        appropriation.setOneYearAdjustment(BigDecimal.ZERO);
        appropriation.setTwoYearAdjustment(BigDecimal.ZERO);
        appropriation.setReimbursedAmount(BigDecimal.ZERO);
        return appropriation;
    }

    private ObjectCategory copyObjectCategory(ObjectCategory oldCategory, Appropriation newAppropriation) {
        ObjectCategory newCategory = dataManager.create(ObjectCategory.class);
        newCategory.setAppropriation(newAppropriation);
        newCategory.setMajorObjectClass(oldCategory.getMajorObjectClass());
        newCategory.setTitle(oldCategory.getTitle());
        return newCategory;
    }

    private ObjectClass copyObjectClass(ObjectClass oldObjectClass, ObjectCategory newCategory) {
        ObjectClass newObjectClass = dataManager.create(ObjectClass.class);
        newObjectClass.setObjectCategory(newCategory);
        newObjectClass.setBudgetObjectClass(oldObjectClass.getBudgetObjectClass());
        newObjectClass.setTitle(oldObjectClass.getTitle());
        return newObjectClass;
    }

    private Division copyDivision(Division oldDivision, Appropriation newAppropriation) {
        Division newDivision = dataManager.create(Division.class);
        newDivision.setAppropriation(newAppropriation);
        newDivision.setFund(oldDivision.getFund());
        newDivision.setDivisionCode(oldDivision.getDivisionCode());
        newDivision.setTitle(oldDivision.getTitle());
        newDivision.setShortTitle(oldDivision.getShortTitle());
        newDivision.setBudgetOrg(oldDivision.getBudgetOrg());
        newDivision.setOneYearAmount(BigDecimal.ZERO);
        newDivision.setTwoYearAmount(BigDecimal.ZERO);
        return newDivision;
    }

    private Branch copyBranch(Branch oldBranch, Division newDivision) {
        Branch newBranch = dataManager.create(Branch.class);
        newBranch.setDivision(newDivision);
        newBranch.setBranchCode(oldBranch.getBranchCode());
        newBranch.setTitle(oldBranch.getTitle());
        newBranch.setSortCode(oldBranch.getSortCode());
        return newBranch;
    }

    private Group copyGroup(Group oldGroup, Division newDivision) {
        Group newGroup = dataManager.create(Group.class);
        newGroup.setDivision(newDivision);
        newGroup.setGroupCode(oldGroup.getGroupCode());
        newGroup.setTitle(oldGroup.getTitle());
        newGroup.setSortCode(oldGroup.getSortCode());
        return newGroup;
    }

    private Activity copyActivity(Activity oldActivity,
                                  Division newDivision,
                                  Fund newFund,
                                  Map<String, Branch> newBranchesByCode,
                                  Map<String, Group> newGroupsByCode) {
        Activity newActivity = dataManager.create(Activity.class);
        newActivity.setDivision(newDivision);
        newActivity.setFund(newFund);
        newActivity.setActivityNumber(oldActivity.getActivityNumber());
        newActivity.setTitle(oldActivity.getTitle());
        newActivity.setSortCode(oldActivity.getSortCode());
        newActivity.setGenericProjection(oldActivity.getGenericProjection());
        newActivity.setTrainingProject(oldActivity.getTrainingProject());

        if (isTrainingProject(oldActivity)) {
            newActivity.setShortTitle(oldActivity.getShortTitle());
            newActivity.setCity(oldActivity.getCity());
            newActivity.setState(oldActivity.getState());
            newActivity.setProgramDirector(oldActivity.getProgramDirector());
        }

        if (oldActivity.getBranch() != null) {
            newActivity.setBranch(newBranchesByCode.get(oldActivity.getBranch().getBranchCode()));
        }

        if (oldActivity.getGroup() != null) {
            newActivity.setGroup(newGroupsByCode.get(oldActivity.getGroup().getGroupCode()));
        }

        return newActivity;
    }
}