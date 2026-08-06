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
 * listen for changes to reimbursements and update totals on activity and appropriation
 *
 * @author Doug Mitchell
 * @version 2.2
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
        dataManager.save(activity);

        Appropriation appropriation = appropriationService.getAppropriation(activity);
        appropriation.setReimbursedAmount(activityReimbursementService.sumReimbursements(appropriation));
        dataManager.save(appropriation);
    }
}