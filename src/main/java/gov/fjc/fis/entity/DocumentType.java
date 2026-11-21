package gov.fjc.fis.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

/**
 * Identifies the type of document received from the JIFMS interface.
 *
 * <p>Document types categorize imported documents and determine how they
 * are processed within FIS. Each type represents a specific business
 * document supported by the document import and obligation creation
 * process.
 *
 * <p>Currently, the JIFMS interface supports two document types:
 * miscellaneous obligations and travel authorizations. Imported
 * {@link Document} records are validated and processed according to the
 * rules applicable to their document type before being converted into
 * FIS obligations.
 *
 * <p>Each enum value is persisted using the JIFMS document type code
 * through the Jmix {@link EnumClass} interface.
 *
 * @author Doug Mitchell
 */
public enum DocumentType implements EnumClass<String> {

    MISCELLANEOUS_OBLIGATION("MOJ"),
    TRAVEL_AUTHORIZATION("TAJ");

    private final String id;

    DocumentType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static DocumentType fromId(String id) {
        for (DocumentType at : DocumentType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}