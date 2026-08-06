package gov.fjc.fis.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum FileAttachmentEntityType implements EnumClass<String> {

    ACTIVITY("A"),
    OBLIGATION("O"),
    INVOICE("I"),
    FCN("F"),
    UNKNOWN("U");

    private final String id;

    FileAttachmentEntityType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static FileAttachmentEntityType fromId(String id) {
        for (FileAttachmentEntityType at : FileAttachmentEntityType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}