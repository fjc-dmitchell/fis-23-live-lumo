package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.*;
import gov.fjc.fis.entity.dto.ActivityDto;
import gov.fjc.fis.entity.dto.ObligationDto;
import gov.fjc.fis.reportdata.DivisionObligationReportData;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.Sort;
import io.jmix.core.querycondition.PropertyCondition;
import org.springframework.stereotype.Component;

import java.util.List;

import static gov.fjc.fis.app.BigDecimalUtils.sum;

/**
 * Generates the data used by the division obligations report.
 * <p>
 * The report may be generated for an entire division or limited to a
 * particular branch. For each activity within the selected reporting scope,
 * this service creates an {@link ActivityDto} containing the activity's
 * identifying information, projections, obligations, and calculated
 * financial totals.
 * </p>
 * <p>
 * Obligation data is converted to {@link ObligationDto} instances containing
 * the information required by the report, including the obligated and
 * disbursed amounts. The resulting activity DTOs are added to a
 * {@link DivisionObligationReportData} object, along with a separate collection
 * containing only activities that have obligations.
 * </p>
 * @see gov.fjc.fis.reportdata.DivisionObligationReportData
 * @author Doug Mitchell
 */
@Component("fis_DivisionObligationsReportService")
public class DivisionObligationsReportService {

    private final DataManager dataManager;

    /**
     * Creates a division obligations report service.
     *
     * @param dataManager the data manager used to load entities and create DTO
     *                    instances
     */
    public DivisionObligationsReportService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Generates the data for a division or branch obligations report.
     * <p>
     * When a branch is supplied, the branch's division determines the
     * appropriation and reporting division. Otherwise, the supplied division
     * is used. Activities are loaded for the selected scope and converted to
     * DTOs containing their projections, obligations, disbursements, and
     * related report values.
     * </p>
     *
     * @param division the division for which report data is generated; used
     *                 when {@code branch} is {@code null}
     * @param branch   the branch for which report data is generated, or
     *                 {@code null} to include activities for the entire
     *                 division
     * @return the generated report data containing the reporting context,
     *         activity data, and activities having obligations
     */
    public DivisionObligationReportData generateReportData(Division division, Branch branch) {
        var reportDivision = branch == null ? division : branch.getDivision();
        var appropriation = reportDivision.getAppropriation();

        var activityDtos = toActivityDtos(getActivities(reportDivision, branch), appropriation);

        var reportData = new DivisionObligationReportData(appropriation, reportDivision, branch);
        reportData.setActivityDtos(activityDtos);
        reportData.setActivitiesWithObligations(
                activityDtos.stream().filter(ActivityDto::hasObligations).toList());
        return reportData;
    }

    /**
     * Converts activities to the DTOs used by the division obligations report.
     *
     * @param activities    the activities to convert
     * @param appropriation the appropriation that supplies report-level
     *                      fiscal-year information
     * @return an unmodifiable list of activity DTOs in the same encounter
     *         order as the supplied activities
     */
    private List<ActivityDto> toActivityDtos(List<Activity> activities, Appropriation appropriation) {
        return activities.stream()
                .map(activity -> toActivityDto(activity, appropriation))
                .toList();
    }

    /**
     * Loads the activities included in the requested reporting scope.
     * <p>
     * When a branch is supplied, only activities assigned to that branch are
     * loaded. Otherwise, all activities assigned to the division are loaded.
     * Activities are sorted by sort code and activity number. The fetch plan
     * also loads projections, obligations, invoices, object classes, and
     * object categories required while constructing the report DTOs.
     * </p>
     *
     * @param division the division whose activities are loaded when
     *                 {@code branch} is {@code null}
     * @param branch   the branch whose activities are loaded, or {@code null}
     *                 to load activities for the division
     * @return the activities in report order
     */
    private List<Activity> getActivities(Division division, Branch branch) {
        var condition = branch == null
                ? PropertyCondition.equal("division", division)
                : PropertyCondition.equal("branch", branch);

        return dataManager.load(Activity.class)
                .condition(condition)
                .sort(Sort.by("sortCode", "activityNumber"))
                .fetchPlan(plan -> plan
                        .addFetchPlan(FetchPlan.BASE)
                        .add("projections", FetchPlan.BASE)
                        .add("obligations", obligation -> obligation
                                .addFetchPlan(FetchPlan.BASE)
                                .add("invoices", FetchPlan.BASE)
                                .add("objectClass", objectClass -> objectClass
                                        .addFetchPlan(FetchPlan.BASE)
                                        .add("objectCategory", FetchPlan.BASE))))
                .list();
    }

    /**
     * Converts an activity to the DTO used by the division obligations report.
     * <p>
     * The resulting DTO contains the activity's identifying and descriptive
     * information, branch information when applicable, converted obligations,
     * and calculated totals for projected, obligated, and disbursed amounts.
     * </p>
     *
     * @param activity      the activity to convert
     * @param appropriation the appropriation that supplies the budget fiscal
     *                      year
     * @return a DTO containing the activity's report data
     */
    private ActivityDto toActivityDto(Activity activity, Appropriation appropriation) {
        var activityDto = dataManager.create(ActivityDto.class);
        activityDto.setId(activity.getId());
        activityDto.setTitle(activity.getTitle());
        activityDto.setActivityNumber(activity.getActivityNumber());
        activityDto.setMemo(activity.getMemo());
        activityDto.setBudgetFiscalYear(appropriation.getBudgetFiscalYear());
        activityDto.setFundCode(activity.getFund().getFundCode());
        if (activity.getBranch() != null) {
            activityDto.setBranchTitle(activity.getBranch().getTitle());
            activityDto.setBranchCode(activity.getBranch().getBranchCode());
        }

        var obligationDtos = activity.getObligations().stream()
                .map(this::toObligationDto)
                .toList();

        activityDto.setTotalProjected(sum(activity.getProjections(), ActivityProjection::getAmount));
        activityDto.setTotalObligated(sum(obligationDtos, ObligationDto::getAmount));
        activityDto.setTotalDisbursed(sum(obligationDtos, ObligationDto::getDisbursed));
        activityDto.setObligationDtos(obligationDtos);
        return activityDto;
    }

    /**
     * Converts an obligation to the DTO used by the division obligations
     * report.
     * <p>
     * For an open obligation, the disbursed amount is calculated as the sum
     * of its invoice amounts. For a closed obligation, the full obligation
     * amount is treated as disbursed.
     * </p>
     *
     * @param obligation the obligation to convert
     * @return a DTO containing the obligation's report data
     */
    private ObligationDto toObligationDto(Obligation obligation) {
        var obligationDto = dataManager.create(ObligationDto.class);
        obligationDto.setDocumentNumber(obligation.getDocumentNumber());
        obligationDto.setBudgetObjectClass(obligation.getObjectClass().getBudgetObjectClass());
        obligationDto.setVendor(obligation.getVendor());
        obligationDto.setAmount(obligation.getAmount());
        obligationDto.setStatus(obligation.getStatus()); // make expressive in Dto: isOpen/isClosed
        obligationDto.setDisbursed(obligation.getStatus()
                ? sum(obligation.getInvoices(), Invoice::getAmount)
                : obligation.getAmount());
        return obligationDto;
    }
}
