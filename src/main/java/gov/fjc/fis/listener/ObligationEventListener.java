package gov.fjc.fis.listener;

import gov.fjc.fis.entity.Activity;
import gov.fjc.fis.entity.Obligation;
import gov.fjc.fis.service.ObligationService;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * listen for changes to obligations and update total on activity
 *
 * @author Doug Mitchell
 * @version 2.2
 * @since 2.1
 *
 */
@Component("fis_ObligationEventListener")
public class ObligationEventListener {
    private final DataManager dataManager;
    private final ObligationService obligationService;

    public ObligationEventListener(DataManager dataManager, ObligationService obligationService) {
        this.dataManager = dataManager;
        this.obligationService = obligationService;
    }

    @EventListener
    public void onObligationChangedBeforeCommit(final EntityChangedEvent<Obligation> event) {
        Activity activity;
        if (event.getType() != EntityChangedEvent.Type.DELETED) {
            Id<Obligation> obligationId = event.getEntityId();
            Obligation obligation = dataManager.load(obligationId).one();
            activity = obligation.getActivity();
        } else {
            Id<Activity> activityId = event.getChanges().getOldValue("activity");
            if (activityId == null) {
                throw new IllegalStateException("Cannot get Activity from deleted obligation");
            }
            activity = dataManager.load(activityId).one();
        }

        activity.setObligatedAmount(obligationService.sumObligations(activity));
        dataManager.save(activity);
    }
}