package gov.fjc.fis.listener;

import gov.fjc.fis.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * listen for changes to allocations and create audit record
 *
 * @author Doug Mitchell
 * @version 2.2
 * @since 2.1
 */
@Component("fis_DivisionAllocationEventListener")
public class DivisionAllocationEventListener {
    private final DataManager dataManager;

    public DivisionAllocationEventListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

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
        dataManager.save(audit);
    }
}