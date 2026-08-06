package gov.fjc.fis.listener;

import gov.fjc.fis.entity.*;
import gov.fjc.fis.service.ActivityProjectionService;
import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import io.jmix.core.DataManager;

/**
 * listen for changes to projections, create audit record (new in 2.1), and update total on activity
 *
 * @author Doug Mitchell
 * @version 2.2
 * @since 2.0
 *
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
        dataManager.save(audit);

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
        dataManager.save(activity);
    }
}