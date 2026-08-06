package gov.fjc.fis.listener;

import gov.fjc.fis.entity.Activity;
import gov.fjc.fis.service.ActivityProjectionService;
import gov.fjc.fis.service.ActivityReimbursementService;
import gov.fjc.fis.service.ObligationService;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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

    @EventListener
    public void onActivitySaving(final EntitySavingEvent<Activity> event) {
        // for safety, ensure obligated, projected, and reimbursed amounts are correct on activity update
        if (!event.isNewEntity()) {
            Activity activity = event.getEntity();
            activity.setObligatedAmount(obligationService.sumObligations(activity));
            activity.setProjectedAmount(projectionService.sumProjections(activity));
            activity.setReimbursedAmount(reimbursementService.sumReimbursements(activity));
        }
    }
}