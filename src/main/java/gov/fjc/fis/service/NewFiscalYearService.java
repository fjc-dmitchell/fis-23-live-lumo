package gov.fjc.fis.service;

import gov.fjc.fis.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to create new budget fiscal year and associated entities with skeleton data.
 * Only non-Foundation divisions, branches, groups, and activities are included.
 * Activity criteria requested by Mark Hannan and Mary Greiner in April 1999.
 * Unlike FIS 1.0, no empty division allocations or activity projections are created
 * to ensure the new audit function includes the initial creation.
 *
 * @author Doug Mitchell
 * @version 2.3
 * @since 2.1
 */
@Component("fis_NewFiscalYearService")
public class NewFiscalYearService {

    private static final Logger log = LoggerFactory.getLogger(NewFiscalYearService.class);

    private final DataManager dataManager;
    private final GroupService groupService;
    private final BranchService branchService;
    private final DivisionService divisionService;
    private final FundService fundService;

    public NewFiscalYearService(DataManager dataManager,
                                FundService fundService,
                                DivisionService divisionService,
                                BranchService branchService,
                                GroupService groupService) {
        this.dataManager = dataManager;
        this.fundService = fundService;
        this.divisionService = divisionService;
        this.branchService = branchService;
        this.groupService = groupService;
    }

    /**
     * Creates new Appropriation and associated entities. If unsuccessful, rollback entire
     * transaction and throw exception to user interface.
     *
     * @param nextFiscalYear String representing the budget fiscal year to create
     */
    @Transactional
    public void createAppropriation(String nextFiscalYear) {
        log.info("Begin transaction for New Fiscal Year: {}", nextFiscalYear);
        String expectedNextFiscalYear = getNextFiscalYear();
        if (!expectedNextFiscalYear.equals(nextFiscalYear)) {
            throw new IllegalArgumentException(
                    "The requested fiscal year is not the next fiscal year in sequence: " + nextFiscalYear
                            + " (expected: " + expectedNextFiscalYear + ")");
        }

        Appropriation oldAppropriation = fetchMaxAppropriation();

        Appropriation newAppropriation = dataManager.create(Appropriation.class);
        newAppropriation.setBudgetFiscalYear(nextFiscalYear);
        newAppropriation.setStatus(true);
        newAppropriation.setOneYearAmount(BigDecimal.ZERO);
        newAppropriation.setTwoYearAmount(BigDecimal.ZERO);
        newAppropriation.setOneYearAdjustment(BigDecimal.ZERO);
        newAppropriation.setTwoYearAdjustment(BigDecimal.ZERO);
        newAppropriation.setReimbursedAmount(BigDecimal.ZERO);
        newAppropriation = dataManager.save(newAppropriation);
        log.info("\tAppropriation saved: {}", newAppropriation.getBudgetFiscalYear());

        createCategories(oldAppropriation, newAppropriation);
        createDivisions(oldAppropriation, newAppropriation);
//        createPayPeriods(oldAppropriation, newAppropriation);
        log.info("End transaction for New Fiscal Year: {}", nextFiscalYear);
    }

    /**
     * Increment the most recent fiscal year by one
     *
     * @return String containing the next available fiscal year
     */
    public String getNextFiscalYear() {
        String maxBudgetFiscalYear = fetchMaxAppropriation().getBudgetFiscalYear();
        try {
            return String.valueOf(Integer.parseInt(maxBudgetFiscalYear) + 1);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Cannot parse fiscal year: " + maxBudgetFiscalYear, e);
        }
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
     * Creates budget object class categories for new fiscal year based on prior year.
     *
     * @param oldAppropriation
     * @param newAppropriation
     */
    private void createCategories(Appropriation oldAppropriation, Appropriation newAppropriation) {
        for (ObjectCategory oldCategory : oldAppropriation.getCategories()) {
            ObjectCategory newCategory = dataManager.create(ObjectCategory.class);
            newCategory.setAppropriation(newAppropriation);
            newCategory.setMajorObjectClass(oldCategory.getMajorObjectClass());
            newCategory.setTitle(oldCategory.getTitle());
            dataManager.saveWithoutReload(newCategory);
            log.info("\t\tCategory saved: {}", newCategory.getTitleAndCode());

            createObjectClasses(oldCategory, newCategory);
        }
    }

    /**
     * Creates object classes for new category based on old category.
     *
     * @param oldCategory
     * @param newCategory
     */
    private void createObjectClasses(ObjectCategory oldCategory, ObjectCategory newCategory) {
        SaveContext saveContext = new SaveContext().setDiscardSaved(true);
        for (ObjectClass oldObjectClass : oldCategory.getBudgetObjectClasses()) {
            ObjectClass newObjectClass = dataManager.create(ObjectClass.class);
            newObjectClass.setObjectCategory(newCategory);
            newObjectClass.setBudgetObjectClass(oldObjectClass.getBudgetObjectClass());
            newObjectClass.setTitle(oldObjectClass.getTitle());
            saveContext.saving(newObjectClass);
            log.info("\t\t\t\tObject class created: {}", newObjectClass.getTitleAndCode());
        }
        dataManager.save(saveContext);
        log.info("\t\t\tObject classes saved for category: {}", newCategory.getTitleAndCode());
    }

    /**
     * Creates non-foundation divisions for new fiscal year based on prior year.
     *
     * @param oldAppropriation
     * @param newAppropriation
     */
    private void createDivisions(Appropriation oldAppropriation, Appropriation newAppropriation) {
        for (Division oldDivision : divisionService.fetchNonFoundationDivisions(oldAppropriation)) {
            Division newDivision = dataManager.create(Division.class);
            newDivision.setAppropriation(newAppropriation);
            newDivision.setFund(oldDivision.getFund());
            newDivision.setDivisionCode(oldDivision.getDivisionCode());
            newDivision.setTitle(oldDivision.getTitle());
            newDivision.setShortTitle(oldDivision.getShortTitle());
            newDivision.setBudgetOrg(oldDivision.getBudgetOrg());
            newDivision.setOneYearAmount(BigDecimal.ZERO);
            newDivision.setTwoYearAmount(BigDecimal.ZERO);
            dataManager.saveWithoutReload(newDivision);
            log.info("\t\tDivision saved: {}", newDivision.getTitleAndCode());

            createBranches(oldDivision, newDivision);
            createGroups(oldDivision, newDivision);
            createActivities(oldDivision, newDivision);
        }
    }

    /**
     * Creates branches for new division based on old division
     *
     * @param oldDivision
     * @param newDivision
     */
    private void createBranches(Division oldDivision, Division newDivision) {
        SaveContext saveContext = new SaveContext().setDiscardSaved(true);
        for (Branch oldBranch : oldDivision.getBranches()) {
            Branch newBranch = dataManager.create(Branch.class);
            newBranch.setDivision(newDivision);
            newBranch.setBranchCode(oldBranch.getBranchCode());
            newBranch.setTitle(oldBranch.getTitle());
            newBranch.setSortCode(oldBranch.getSortCode());
            saveContext.saving(newBranch);
            log.info("\t\t\t\tBranch created: {}", newBranch.getTitleAndCode());
        }
        dataManager.save(saveContext);
        log.info("\t\t\tBranches saved for division: {}", newDivision.getTitleAndCode());
    }

    /**
     * Creates groups for new division based on old division
     *
     * @param oldDivision
     * @param newDivision
     */
    private void createGroups(Division oldDivision, Division newDivision) {
        SaveContext saveContext = new SaveContext().setDiscardSaved(true);
        for (Group oldGroup : oldDivision.getGroups()) {
            Group newGroup = dataManager.create(Group.class);
            newGroup.setDivision(newDivision);
            newGroup.setGroupCode(oldGroup.getGroupCode());
            newGroup.setTitle(oldGroup.getTitle());
            newGroup.setSortCode(oldGroup.getSortCode());
            saveContext.saving(newGroup);
            log.info("\t\t\t\tGroup created: {}", newGroup.getTitleAndCode());
        }
        dataManager.save(saveContext);
        log.info("\t\t\tGroups saved for division: {}", newDivision.getTitleAndCode());
    }

    /**
     * Creates certain activities for new division based on those in old division and
     * associates each activity with matching branches and groups of the new division.
     * For generic training activities, city and state are included. No activities
     * set to two year fund. Based on criteria provided by Mark and Mary in 1999.
     *
     * @param oldDivision
     * @param newDivision
     */
    private void createActivities(Division oldDivision, Division newDivision) {
        Fund oneYearFund = fundService.getAppropriationOneYearFund();
        Fund twoYearFund = fundService.getAppropriationTwoYearFund();
        List<Branch> newBranches = branchService.fetchBranches(newDivision);
        List<Group> newGroups = groupService.fetchGroups(newDivision);

        Map<String, Branch> newBranchesByCode = newBranches.stream()
                .collect(Collectors.toMap(Branch::getBranchCode, b -> b));
        Map<String, Group> newGroupsByCode = newGroups.stream()
                .collect(Collectors.toMap(Group::getGroupCode, g -> g));

        SaveContext saveContext = new SaveContext().setDiscardSaved(true);
        for (Activity oldActivity : oldDivision.getActivities()) {
            if (!shouldCopyActivity(oldActivity)) {
                continue;
            }
            Activity newActivity = dataManager.create(Activity.class);
            newActivity.setDivision(newDivision);
            newActivity.setFund(oldActivity.getFund().equals(twoYearFund) ? oneYearFund : oldActivity.getFund());
            newActivity.setActivityNumber(oldActivity.getActivityNumber());
            newActivity.setTitle(oldActivity.getTitle());
            newActivity.setSortCode(oldActivity.getSortCode());
            newActivity.setGenericProjection(oldActivity.getGenericProjection());
            newActivity.setTrainingProject(oldActivity.getTrainingProject());

            if (oldActivity.getTrainingProject()) {
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

            saveContext.saving(newActivity);
            log.info("\t\t\t\tActivity created: {}", newActivity.getTitleAndCode());
        }
        dataManager.save(saveContext);
        log.info("\t\t\tActivities saved for division: {}", newDivision.getTitleAndCode());
    }

    /**
     * only copy non-training projects OR training projects ending in 00
     *
     * @param activity
     * @return true if activity should be copied
     */
    private boolean shouldCopyActivity(Activity activity) {
        return !activity.getTrainingProject()
                || activity.getActivityNumber().endsWith("00");
    }

//    private void createPayPeriods(Appropriation oldAppropriation, Appropriation newAppropriation) {
//        int oldYear = Integer.parseInt(oldAppropriation.getBudgetFiscalYear());
//        updatePriorYearPeriods(oldYear, newAppropriation);
//        LocalDate newStartDate = findNextStartDate(oldYear);
//        createNewYearPeriods(newAppropriation, newStartDate);
//    }
//
//    private LocalDate findNextStartDate(int oldYear) {
//        LocalDate lastStartDate = dataManager.loadValue(
//                        "SELECT p.startDate FROM fis_PayPeriod p"
//                                + " WHERE p.payYear = :oldYear"
//                                + " AND p.payPeriod IN (26, 27)"
//                                + " ORDER BY p.startDate DESC",
//                        LocalDate.class)
//                .parameter("oldYear", oldYear)
//                .maxResults(1)
//                .optional()
//                .orElseThrow(() -> new IllegalStateException(
//                        "No pay period 26 or 27 found for year " + oldYear));
//
//        if (lastStartDate.getDayOfWeek() != DayOfWeek.MONDAY) {
//            throw new IllegalStateException(
//                    "Pay period for year " + oldYear + " does not start on a Monday: " + lastStartDate);
//        }
//
//        return lastStartDate.plusWeeks(2);
//    }
//
//    private void updatePriorYearPeriods(int priorYear, Appropriation newAppropriation) {
//        List<PayPeriod> payPeriods = dataManager.load(PayPeriod.class)
//                .query("SELECT p FROM fis_PayPeriod p"
//                        + " WHERE p.payYear = :priorYear"
//                        + " AND EXTRACT(MONTH FROM p.startDate) IN (10, 11, 12)")
//                .parameter("priorYear", priorYear)
//                .list();
//
//        SaveContext saveContext = new SaveContext().setDiscardSaved(true);
//        for (PayPeriod payPeriod : payPeriods) {
//            payPeriod.setAppropriation(newAppropriation);
//            saveContext.saving(payPeriod);
//            log.info("\t\t\tPay period updated: {}", payPeriod.getStartDate());
//        }
//        dataManager.save(saveContext);
//        log.info("\t\tPay period updates saved for prior year: {}", priorYear);
//    }
//
//    private void createNewYearPeriods(Appropriation appropriation, LocalDate startDate) {
//        int fiscalYear = Integer.parseInt(appropriation.getBudgetFiscalYear());
//
//        LocalDate periodStart = startDate;
//        LocalDate periodEnd = periodStart.plusDays(13);
//
//        SaveContext saveContext = new SaveContext().setDiscardSaved(true);
//        int periodNumber = 1;
//
//        while (periodEnd.getYear() == fiscalYear) {
//            PayPeriod payPeriod = dataManager.create(PayPeriod.class);
//            payPeriod.setPayYear(fiscalYear);
//            payPeriod.setPayPeriod(periodNumber++);
//            payPeriod.setStartDate(periodStart);
//
//            if (periodEnd.getMonthValue() < 10) {
//                payPeriod.setAppropriation(appropriation);
//            }
//
//            saveContext.saving(payPeriod);
//            log.info("\t\t\tPay period created: {}", periodStart);
//
//            periodStart = periodStart.plusWeeks(2);
//            periodEnd = periodEnd.plusWeeks(2);
//        }
//
//        dataManager.save(saveContext);
//        log.info("\t\tPay periods saved for year: {}", fiscalYear);
//    }
}