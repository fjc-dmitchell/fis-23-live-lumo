package gov.fjc.fis.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

/**
 * Identifies the type of entity to which a {@link FileAttachment}
 * is associated.
 *
 * <p>This enumeration is used by the file attachment framework to
 * determine the host entity for an attachment and to support display,
 * filtering, and business logic based on the attachment's source record.
 *
 * <p>File attachments may be associated with entities that implement
 * {@link FileAttachable}, including {@link Activity},
 * {@link Obligation}, {@link Invoice}, and
 * {@link FundControlNotice}.
 *
 * <p>Each enum value is persisted using a single-character identifier
 * through the Jmix {@link EnumClass} interface.
 *
 * @author Doug Mitchell
 */
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