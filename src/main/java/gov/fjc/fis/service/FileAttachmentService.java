package gov.fjc.fis.service;

import gov.fjc.fis.entity.*;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Provides operations for retrieving file attachments associated with
 * {@link FileAttachable} entities.
 * <p>
 * The service determines the appropriate {@link FileAttachment} relationship
 * from the concrete type of the supplied entity. Supported entity types include
 * {@link Activity}, {@link Obligation}, {@link Invoice}, and
 * {@link FundControlNotice}.
 * <p>
 * Entities without a defined file attachment mapping are not supported and
 * result in an exception when attachment retrieval is attempted.
 */
@Component("fis_FileAttachmentService")
public class FileAttachmentService {
    private final DataManager dataManager;

    public FileAttachmentService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Returns the file attachments associated with the specified entity.
     * <p>
     * The concrete type of the entity determines which
     * {@link FileAttachment} relationship is queried.
     *
     * @param entity the entity whose file attachments are requested
     * @return the associated file attachments, or an empty list if the entity
     *         has no attachments
     * @throws IllegalArgumentException if the entity type does not have a
     *                                  defined file attachment mapping
     */
    public List<FileAttachment> getFileAttachments(FileAttachable entity) {
        return switch (entity) {
            case Activity a -> fetchFor("activity", a);
            case Obligation o -> fetchFor("obligation", o);
            case Invoice i -> fetchFor("invoice", i);
            case FundControlNotice f -> fetchFor("fundControlNotice", f);
            default -> throw new IllegalArgumentException(
                    "No FileAttachment mapping for type: " + entity.getClass().getSimpleName());
        };
    }

    /**
     * Returns file attachments whose specified association references the
     * supplied entity.
     *
     * @param field the name of the {@link FileAttachment} association to query
     * @param entity the entity referenced by the association
     * @return the matching file attachments, or an empty list if none exist
     */
    private List<FileAttachment> fetchFor(String field, FileAttachable entity) {
        return dataManager.load(FileAttachment.class)
                .query("SELECT e FROM fis_FileAttachment e"
                        + " WHERE e." + field + " = :ref")
                .parameter("ref", entity)
                .list();
    }
}