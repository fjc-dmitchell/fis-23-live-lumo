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
 * Responds to create, update, and delete events for
 * {@link Obligation} entities by maintaining related activity totals and
 * synchronizing associated file attachments.
 *
 * <p>
 * When an {@link EntityChangedEvent} is received, this listener performs
 * two primary business functions:
 * </p>
 *
 * <ol>
 *     <li>Recalculates the obligated amount for affected
 *     {@link Activity} entities to ensure aggregate obligation totals
 *     remain accurate.</li>
 *
 *     <li>Reassigns related {@link FileAttachment} records when an
 *     obligation is moved from one activity to another, ensuring
 *     attachment relationships remain consistent.</li>
 * </ol>
 *
 * <p>
 * The listener handles obligation creation, modification, deletion, and
 * activity reassignment scenarios. By centralizing this logic in an
 * entity-change listener, financial summary data and attachment
 * relationships remain synchronized regardless of whether changes
 * originate from Jmix UI views, background processes, external
 * integrations, or automated data imports such as JIFMS processing.
 * </p>
 *
 * @author Doug Mitchell
 * @version 2.3
 * @since 2.1
 */
@Component("fis_ObligationEventListener")
public class ObligationEventListener {
    private final UnconstrainedDataManager dataManager;
    private final ObligationService obligationService;

    /**
     * Creates a listener responsible for maintaining obligation-related
     * activity totals and file attachment associations.
     *
     * @param dataManager the data manager used to load and persist entities
     * @param obligationService the service used to calculate obligation
     *                          aggregates for activities
     */
    public ObligationEventListener(DataManager dataManager, ObligationService obligationService) {
        this.dataManager = dataManager;
        this.obligationService = obligationService;
    }

    /**
     * Processes changes to an {@link Obligation} entity.
     *
     * <p>
     * The listener recalculates affected activity obligation totals and, when
     * necessary, updates related file attachment associations to reflect
     * activity reassignment.
     * </p>
     *
     * @param event the entity change event describing the obligation that
     *              was created, updated, or deleted
     */
    @EventListener
    public void onObligationChangedBeforeCommit(final EntityChangedEvent<Obligation> event) {
        recalculateActivityObligationSums(event);
        reassignFileAttachments(event);
    }

    /**
     * Recalculates obligated amount totals for all activities affected by an
     * obligation change.
     *
     * <p>
     * For created and updated obligations, the associated activity is
     * recalculated using the current set of obligations. If the obligation
     * was reassigned to a different activity, both the original and new
     * activities are recalculated to ensure aggregate values remain accurate.
     * </p>
     *
     * <p>
     * For deleted obligations, the obligated amount is recalculated for the
     * activity previously associated with the obligation.
     * </p>
     *
     * @param event the obligation entity change event
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
     * Reassigns file attachments when an obligation is moved to a different
     * activity.
     *
     * <p>
     * During certain update operations, such as external data synchronization
     * or JIFMS processing, an obligation may be reassigned from one activity
     * to another. When this occurs, any {@link FileAttachment} records that
     * reference both the obligation and the previous activity are updated to
     * reference the new activity.
     * </p>
     *
     * <p>
     * Processing occurs only for update events where the obligation's
     * activity association has changed.
     * </p>
     *
     * @param event the obligation entity change event
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