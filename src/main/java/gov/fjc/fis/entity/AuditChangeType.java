package gov.fjc.fis.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


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