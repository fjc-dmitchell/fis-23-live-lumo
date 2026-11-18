package gov.fjc.fis.listener;

import gov.fjc.fis.entity.FileAttachment;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responds to changes in {@link FileAttachment} entities and performs
 * related file storage maintenance operations.
 *
 * <p>
 * This listener ensures that files stored in Jmix {@link FileStorage}
 * remain synchronized with their corresponding database records. When a
 * {@link FileAttachment} entity is deleted, the listener removes the
 * associated file from the configured file storage to prevent orphaned
 * files from accumulating over time.
 * </p>
 *
 * <p>
 * If the referenced file cannot be found in storage when the entity is
 * deleted, an error is logged to aid in diagnosing data integrity or
 * storage synchronization issues.
 * </p>
 *
 * <p>
 * Centralizing file cleanup in an entity-change listener ensures that
 * storage maintenance occurs consistently regardless of whether file
 * attachment records are deleted through Jmix UI screens, application
 * services, integrations, or administrative processes.
 * </p>
 *
 * @author Doug Mitchell
 * @version 2.3
 * @since 2.0
 */
@Component("fis_FileAttachmentEventListener")
public class FileAttachmentEventListener {
    private final FileStorage fileStorage;
    private static final Logger log = LoggerFactory.getLogger(FileAttachmentEventListener.class);

    /**
     * Creates a listener responsible for maintaining file storage consistency
     * when attachment records are modified.
     *
     * @param fileStorage the Jmix file storage implementation used to locate
     *                    and remove stored files
     */
    public FileAttachmentEventListener(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    /**
     * Removes the physical file associated with a deleted
     * {@link FileAttachment} entity.
     *
     * <p>
     * When a file attachment record is deleted, the listener retrieves the
     * previous {@link FileRef} from the entity change event and attempts to
     * remove the corresponding file from Jmix file storage.
     * </p>
     *
     * <p>
     * If the referenced file no longer exists in storage, an error is logged
     * to indicate a potential inconsistency between the database and the
     * underlying file storage system.
     * </p>
     *
     * @param event the entity change event describing the attachment that was
     *              modified or deleted
     */
    @EventListener
    public void onFileAttachmentChangedBeforeCommit(final EntityChangedEvent<FileAttachment> event) {
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            FileRef fileRef = event.getChanges().getOldValue("fileReference");
            if (fileRef != null) {
                if (!fileStorage.fileExists(fileRef)) {
                    log.error("File {} reference was deleted but did does not exist on file system", fileRef);
                } else {
                    fileStorage.removeFile(fileRef);
                }
            }
        }
    }
}