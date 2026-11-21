package gov.fjc.fis.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

/**
 * Identifies the type of change recorded by an audit entity.
 *
 * <p>This enumeration is used by the {@link ActivityProjectionAudit} and
 * {@link DivisionAllocationAudit} entities to indicate the operation that
 * caused an audit record to be created.
 *
 * <p>Audit records are generated whenever an activity projection or division
 * allocation is created, modified, or deleted. The change type provides a
 * concise indication of the action performed and allows audit reports and
 * users to distinguish between newly created records, updates to existing
 * records, and records that have been removed.
 *
 * <p>Each value is persisted using a single-character identifier through the
 * Jmix {@link EnumClass} interface.
 *
 * @author Doug Mitchell
 */
public enum AuditChangeType implements EnumClass<String> {

    CREATED("I"),
    UPDATED("U"),
    DELETED("D");

    private final String id;

    AuditChangeType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static AuditChangeType fromId(String id) {
        for (AuditChangeType at : AuditChangeType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}