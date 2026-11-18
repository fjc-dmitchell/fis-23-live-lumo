package gov.fjc.fis.listener;

import gov.fjc.fis.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Responds to create, update, and delete events for
 * {@link DivisionAllocation} entities by creating corresponding
 * {@link DivisionAllocationAudit} records.
 *
 * <p>
 * When a {@link EntityChangedEvent} is received, this listener captures
 * relevant allocation information and records an audit entry containing
 * the affected {@link Division}, {@link ObjectCategory}, allocation
 * amounts, and the type of change that occurred.
 * </p>
 *
 * <p>
 * For newly created and deleted allocations, the audit record captures
 * the complete state transition. For updates, the listener records the
 * previous and current values of modified allocation amounts and tracks
 * which allocation attributes were changed.
 * </p>
 *
 * <p>
 * Centralizing audit creation in an entity-change listener ensures that
 * allocation history is recorded consistently regardless of whether
 * changes originate from Jmix UI views, background processes, data
 * imports, REST integrations, or application services.
 * </p>
 *
 * <p>
 * This listener provides a historical record of allocation changes to
 * support financial reconciliation, reporting, and auditing requirements.
 * </p>
 *
 * @author Doug Mitchell
 * @version 2.3
 * @since 2.1
 */
@Component("fis_DivisionAllocationEventListener")
public class DivisionAllocationEventListener {
    private final DataManager dataManager;

    /**
     * Creates a listener responsible for auditing division allocation changes.
     *
     * @param dataManager the Jmix data manager used to load related entities
     *                    and persist audit records
     */
    public DivisionAllocationEventListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Creates an audit record when a {@link DivisionAllocation} is created,
     * updated, or deleted.
     *
     * <p>
     * For created and updated allocations, the current entity is loaded and
     * compared with the previous values supplied by the event. For deleted
     * allocations, entity references and amount values are obtained from the
     * event's change information.
     * </p>
     *
     * <p>
     * The resulting {@link DivisionAllocationAudit} record includes the
     * affected division and object category, previous and current allocation
     * amounts, the type of change performed, and, for update operations, the
     * specific allocation attributes that were modified.
     * </p>
     *
     * @param event the entity change event describing the allocation that
     *              was created, updated, or deleted
     */
    @EventListener
    public void onDivisionAllocationChangedBeforeCommit(final EntityChangedEvent<DivisionAllocation> event) {
        DivisionAllocationAudit audit = dataManager.create(DivisionAllocationAudit.class);
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            Id<Division> divisionId = event.getChanges().getOldReferenceId("division");
            assert divisionId != null;
            Division division = dataManager.load(divisionId).one();
            audit.setDivision(division);
            Id<ObjectCategory> categoryId = event.getChanges().getOldReferenceId("category");
            assert categoryId != null;
            ObjectCategory category = dataManager.load(categoryId).one();
            audit.setObjectCategory(category);
            audit.setOldOneYearAmount(event.getChanges().getOldValue("oneYearAmount"));
            audit.setNewOneYearAmount(null);
            audit.setOldTwoYearAmount(event.getChanges().getOldValue("twoYearAmount"));
            audit.setNewTwoYearAmount(null);

            audit.setChangeType(AuditChangeType.DELETED);
        } else {
            DivisionAllocation allocation = dataManager.load(event.getEntityId()).one();
            audit.setDivision(allocation.getDivision());
            audit.setObjectCategory(allocation.getObjectCategory());

            if (event.getChanges().isChanged("oneYearAmount")) {
                audit.setOldOneYearAmount(event.getChanges().getOldValue("oneYearAmount"));
                audit.setNewOneYearAmount(allocation.getOneYearAmount());
            }
            if (event.getChanges().isChanged("twoYearAmount")) {
                audit.setOldTwoYearAmount(event.getChanges().getOldValue("twoYearAmount"));
                audit.setNewTwoYearAmount(allocation.getTwoYearAmount());
            }

            if (event.getType() == EntityChangedEvent.Type.CREATED) {
                audit.setChangeType(AuditChangeType.CREATED);
            } else {
                audit.setChangeType(AuditChangeType.UPDATED);
                var changes = event.getChanges().getAttributes().stream()
                        .filter(change -> change.equals("oneYearAmount") || change.equals("twoYearAmount"))
                        .toList().toString();
                audit.setUpdatedAttributes(changes);
            }
        }
        dataManager.saveWithoutReload(audit);
    }
}