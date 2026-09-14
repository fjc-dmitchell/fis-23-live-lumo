package gov.fjc.fis.listener;

import gov.fjc.fis.entity.Activity;
import gov.fjc.fis.entity.FileAttachment;
import gov.fjc.fis.entity.Obligation;
import gov.fjc.fis.service.ObligationService;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * listen for changes to obligations and update total on activity and
 * reassign file attachments
 *
 * @author Doug Mitchell
 * @version 2.3
 * @since 2.1
 *
 */
@Component("fis_ObligationEventListener")
public class ObligationEventListener {
    private final UnconstrainedDataManager dataManager;
    private final ObligationService obligationService;

    public ObligationEventListener(DataManager dataManager, ObligationService obligationService) {
        this.dataManager = dataManager;
        this.obligationService = obligationService;
    }

    @EventListener
    public void onObligationChangedBeforeCommit(final EntityChangedEvent<Obligation> event) {
        recalculateActivityObligationSums(event);
        reassignFileAttachments(event);
    }

    /**
     * recalculate activity obligation sums on obligation insert, update, delete and
     * recalculate for old activity when activity changes (e.g., via JIFMS processing)
     *
     * @param event
     */
    void recalculateActivityObligationSums(EntityChangedEvent<Obligation> event) {
        Set<Id<Activity>> activityIds = new HashSet<>();

        if (event.getType() != EntityChangedEvent.Type.DELETED) {
            Id<Obligation> obligationId = event.getEntityId();
            Obligation obligation = dataManager.load(obligationId).one();
            activityIds.add(Id.of(obligation.getActivity()));

            // Reassigned to a different activity: the old one also needs recalculating.
            if (event.getChanges().isChanged("activity")) {
                Id<Activity> oldActivityId = event.getChanges().getOldValue("activity");
                if (oldActivityId != null) {
                    activityIds.add(oldActivityId);
                }
            }
        } else {
            Id<Activity> activityId = event.getChanges().getOldValue("activity");
            if (activityId == null) {
                throw new IllegalStateException("Cannot get Activity from deleted obligation");
            }
            activityIds.add(activityId);
        }

        for (Id<Activity> activityId : activityIds) {
            Activity activity = dataManager.load(activityId).one();
            activity.setObligatedAmount(obligationService.sumObligations(activity));
            dataManager.saveWithoutReload(activity);
        }
    }

    /**
     * if the activity has changed (e.g., via JIFMS processing), all file attachments for
     * the obligation, invoice, or FCN must be reassigned to the new activity.
     *
     * @param event
     */
    private void reassignFileAttachments(EntityChangedEvent<Obligation> event) {
        // can't reassign attachments unless this is an obligation update
        if (event.getType() != EntityChangedEvent.Type.UPDATED) {
            return;
        }

        // can't reassign attachments if activity didn't change
        if (!event.getChanges().isChanged("activity")) {
            return;
        }

        Id<Activity> oldActivityId = event.getChanges().getOldReferenceId("activity");
        if (oldActivityId == null) {
            // invalid condition - this cannot happen!
            return;
        }

        Id<Obligation> obligationId = event.getEntityId();

        List<FileAttachment> attachments = dataManager.load(FileAttachment.class)
                .query("select fa from fis_FileAttachment fa "
                        + "where fa.obligation.id = :obligationId and fa.activity.id = :oldActivityId")
                .parameter("obligationId", obligationId.getValue())
                .parameter("oldActivityId", oldActivityId.getValue())
                .list();

        if (attachments.isEmpty()) {
            return;
        }

        Activity newActivity = dataManager.load(obligationId)
                .fetchPlan(fp -> fp.add("activity"))
                .one()
                .getActivity();

        SaveContext saveContext = new SaveContext();
        for (FileAttachment attachment : attachments) {
            attachment.setActivity(newActivity);
            saveContext.saving(attachment);
        }
        dataManager.save(saveContext);
    }
}