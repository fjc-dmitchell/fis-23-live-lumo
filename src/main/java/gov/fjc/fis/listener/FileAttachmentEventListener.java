package gov.fjc.fis.listener;

import gov.fjc.fis.entity.FileAttachment;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("fis_FileAttachmentEventListener")
public class FileAttachmentEventListener {
    private final FileStorage fileStorage;
    private static final Logger log = LoggerFactory.getLogger(FileAttachmentEventListener.class);

    public FileAttachmentEventListener(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

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