package gov.fjc.fis.listener;

import gov.fjc.fis.entity.*;
import gov.fjc.fis.service.ActivityProjectionService;
import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import io.jmix.core.DataManager;

/**
 * Responds to create, update, and delete events for {@link ActivityProjection}
 * entities by maintaining projection audit history and recalculating activity
 * projection totals.
 *
 * <p>
 * When an {@link EntityChangedEvent} is received, this listener performs two
 * related operations:
 * </p>
 *
 * <ol>
 *     <li>Creates an {@link ActivityProjectionAudit} record that captures the
 *     previous and current projection values, the affected activity and object
 *     class, and the type of change (create, update, or delete).</li>
 *
 *     <li>Recalculates the total projected amount for the associated
 *     {@link Activity} and updates the activity's projected amount field to
 *     reflect the current sum of all related projections.</li>
 * </ol>
 *
 * <p>
 * By centralizing these responsibilities in an entity change listener, audit
 * history and projection totals remain synchronized regardless of whether
 * changes originate from Jmix UI screens, background processes, data imports,
 * REST endpoints, or other application services.
 * </p>
 *
 * <p>
 * This listener executes after an {@link ActivityProjection} entity has been
 * created, modified, or removed and ensures that both audit data and
 * aggregate projection values remain consistent throughout the application.
 * </p>
 *
 * @author Doug Mitchell
 * @version 2.2
 * @since 2.0
 */
@Component
public class ActivityProjectionEventListener {
    private final DataManager dataManager;
    private final ActivityProjectionService activityProjectionService;

    public ActivityProjectionEventListener(DataManager dataManager,
                                           ActivityProjectionService activityProjectionService) {
        this.dataManager = dataManager;
        this.activityProjectionService = activityProjectionService;
    }

    /**
     * Processes changes to an {@link ActivityProjection} entity by creating an
     * audit record and updating the projected total on the associated activity.
     *
     * <p>
     * For created and updated projections, the current projection is loaded and
     * compared with the previous persisted values supplied by the event. For
     * deleted projections, the previous entity references and values contained in
     * the event are used to construct the audit record.
     * </p>
     *
     * <p>
     * After the audit entry is saved, the total projected amount for the affected
     * {@link Activity} is recalculated using
     * {@link ActivityProjectionService#sumProjections(Activity)} and persisted to
     * the activity record.
     * </p>
     *
     * @param event the entity change event describing the projection that was
     *              created, updated, or deleted
     */
    @EventListener
    void onActivityProjectionChangeBeforeCommit(EntityChangedEvent<ActivityProjection> event) {
        // first, deal with audit record
        ActivityProjectionAudit audit = dataManager.create(ActivityProjectionAudit.class);
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            Id<Activity> activityId = event.getChanges().getOldReferenceId("activity");
            assert activityId != null;
            Activity activity = dataManager.load(activityId).one();
            audit.setActivity(activity);

            Id<ObjectClass> objectClassId = event.getChanges().getOldReferenceId("objectClass");
            assert objectClassId != null;
            ObjectClass objectClass = dataManager.load(objectClassId).one();
            audit.setObjectClass(objectClass);

            audit.setOldAmount(event.getChanges().getOldValue("amount"));
            audit.setNewAmount(null);
            audit.setChangeType(AuditChangeType.DELETED);
        } else {
            ActivityProjection projection = dataManager.load(event.getEntityId()).one();
            audit.setActivity(projection.getActivity());
            audit.setObjectClass(projection.getObjectClass());

            audit.setOldAmount(event.getChanges().getOldValue("amount"));
            audit.setNewAmount(projection.getAmount());
            if (event.getType() == EntityChangedEvent.Type.CREATED) {
                audit.setChangeType(AuditChangeType.CREATED);
            } else {
                audit.setChangeType(AuditChangeType.UPDATED);
            }
        }
        dataManager.saveWithoutReload(audit);

        // now, let's update the projection field on activity
        Activity activity;
        if (event.getType() != EntityChangedEvent.Type.DELETED) {
            Id<ActivityProjection> projectionId = event.getEntityId();
            ActivityProjection projection = dataManager.load(projectionId).one();
            activity = projection.getActivity();
        } else {
            Id<Activity> activityId = event.getChanges().getOldValue("activity");
            if (activityId == null) {
                throw new IllegalStateException("Cannot get Activity from deleted projection");
            }
            activity = dataManager.load(activityId).one();
        }

        activity.setProjectedAmount(activityProjectionService.sumProjections(activity));
        dataManager.saveWithoutReload(activity);
    }
}