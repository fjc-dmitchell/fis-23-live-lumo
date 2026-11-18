package gov.fjc.fis.listener;

import gov.fjc.fis.entity.Activity;
import gov.fjc.fis.entity.ActivityReimbursement;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.service.ActivityReimbursementService;
import gov.fjc.fis.service.AppropriationService;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Responds to create, update, and delete events for
 * {@link ActivityReimbursement} entities by recalculating reimbursement
 * totals on the associated {@link Activity} and {@link Appropriation}.
 *
 * <p>
 * When an {@link EntityChangedEvent} is received, this listener identifies
 * the affected activity, recalculates its total reimbursed amount from all
 * related reimbursement records, and persists the updated aggregate value.
 * The listener then locates the corresponding appropriation and updates its
 * reimbursed amount using the aggregated reimbursement total for all
 * associated activities.
 * </p>
 *
 * <p>
 * Centralizing this logic in an entity-change listener ensures that
 * reimbursement totals remain accurate regardless of whether changes
 * originate from Jmix UI views, background jobs, data imports, REST
 * integrations, or application services.
 * </p>
 *
 * <p>
 * This listener helps maintain consistency between reimbursement detail
 * records and the summary financial fields stored on activity and
 * appropriation entities.
 * </p>
 *
 * @author Doug Mitchell
 * @version 2.3
 * @since 2.0
 */
@Component("fis_ActivityReimbursementEventListener")
public class ActivityReimbursementEventListener {
    private final DataManager dataManager;
    private final ActivityReimbursementService activityReimbursementService;
    private final AppropriationService appropriationService;

    public ActivityReimbursementEventListener(DataManager dataManager,
                                              AppropriationService appropriationService,
                                              ActivityReimbursementService activityReimbursementService) {
        this.dataManager = dataManager;
        this.appropriationService = appropriationService;
        this.activityReimbursementService = activityReimbursementService;
    }

    /**
     * Recalculates reimbursement totals when an
     * {@link ActivityReimbursement} is created, updated, or deleted.
     *
     * <p>
     * The listener determines the affected {@link Activity}, calculates the
     * current reimbursed amount using
     * {@link ActivityReimbursementService#sumReimbursements(Activity)}, and
     * updates the activity record. It then retrieves the related
     * {@link Appropriation} and updates its reimbursed amount using the
     * aggregated reimbursement total for that appropriation.
     * </p>
     *
     * <p>
     * For deleted reimbursement records, the associated activity is obtained
     * from the entity change information contained in the event.
     * </p>
     *
     * @param event the entity change event describing the reimbursement that
     *              was created, updated, or deleted
     */
    @EventListener
    public void onActivityReimbursementChangedBeforeCommit(final EntityChangedEvent<ActivityReimbursement> event) {
        Activity activity;
        if (event.getType() != EntityChangedEvent.Type.DELETED) {
            Id<ActivityReimbursement> reimbursementId = event.getEntityId();
            ActivityReimbursement reimbursement = dataManager.load(reimbursementId).one();
            activity = reimbursement.getActivity();
        } else {
            Id<Activity> activityId = event.getChanges().getOldValue("activity");
            if (activityId == null) {
                throw new IllegalStateException("Cannot get Activity from deleted reimbursement");
            }
            activity = dataManager.load(activityId).one();
        }

        activity.setReimbursedAmount(activityReimbursementService.sumReimbursements(activity));
        dataManager.saveWithoutReload(activity);

        Appropriation appropriation = appropriationService.getAppropriation(activity);
        appropriation.setReimbursedAmount(activityReimbursementService.sumReimbursements(appropriation));
        dataManager.saveWithoutReload(appropriation);
    }
}