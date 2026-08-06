package gov.fjc.fis.service;

import gov.fjc.fis.entity.*;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("fis_FileAttachmentService")
public class FileAttachmentService {
    private final DataManager dataManager;

    public FileAttachmentService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

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

    private List<FileAttachment> fetchFor(String field, FileAttachable entity) {
        return dataManager.load(FileAttachment.class)
                .query("SELECT a FROM fis_FileAttachment a"
                        + " WHERE a." + field + " = :ref")
                .parameter("ref", entity)
                .list();
    }
}