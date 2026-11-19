package gov.fjc.fis.listener;

import gov.fjc.fis.entity.Activity;
import gov.fjc.fis.service.ActivityProjectionService;
import gov.fjc.fis.service.ActivityReimbursementService;
import gov.fjc.fis.service.ObligationService;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Entity event listener for {@link Activity} entities.
 * <p>
 * This listener ensures that calculated financial totals remain synchronized
 * whenever an existing activity is updated. Before the entity is persisted,
 * the listener recalculates and updates the obligated, projected, and reimbursed
 * amounts using the corresponding business services.
 * </p>
 * <p>
 * The recalculation is performed only for existing entities. New activities
 * are excluded because related obligations, projections, and reimbursements
 * may not yet exist at the time of creation.
 * </p>
 *
 * @see Activity
 * @see EntitySavingEvent
 * @author Doug Mitchell
 * @since 2.0
 */
@Component("fis_ActivityEventListener")
public class ActivityEventListener {
    private final ObligationService obligationService;
    private final ActivityReimbursementService reimbursementService;
    private final ActivityProjectionService projectionService;

    public ActivityEventListener(ObligationService obligationService,
                                 ActivityProjectionService projectionService,
                                 ActivityReimbursementService reimbursementService) {
        this.obligationService = obligationService;
        this.projectionService = projectionService;
        this.reimbursementService = reimbursementService;
    }

    /**
     * Handles the {@link EntitySavingEvent} for {@link Activity} entities.
     * <p>
     * Prior to saving an existing activity, this method recalculates aggregate
     * financial values and updates the entity to ensure data consistency.
     * The following totals are refreshed:
     * </p>
     * <ul>
     *     <li>Obligated amount</li>
     *     <li>Projected amount</li>
     *     <li>Reimbursed amount</li>
     * </ul>
     *
     * @param event the entity saving event containing the activity being persisted
     */
    @EventListener
    public void onActivitySaving(final EntitySavingEvent<Activity> event) {
        if (!event.isNewEntity()) {
            Activity activity = event.getEntity();
            activity.setObligatedAmount(obligationService.sumObligations(activity));
            activity.setProjectedAmount(projectionService.sumProjections(activity));
            activity.setReimbursedAmount(reimbursementService.sumReimbursements(activity));
        }
    }
}