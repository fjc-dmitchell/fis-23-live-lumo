package gov.fjc.fis.service;

import gov.fjc.fis.entity.UserMessage;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Service for retrieving user-facing announcements displayed on the
 * FIS landing page.
 * <p>
 * User messages are application-wide communications that can be published
 * by administrators to inform users of system maintenance, operational
 * notices, policy changes, scheduled outages, upcoming events, or other
 * important information. Published messages are visible to all FIS users.
 * </p>
 * <p>
 * This service returns only messages that have been marked as published
 * and orders them with the most recent announcements appearing first.
 * </p>
 *
 * @author Doug Mitchell
 * @since 2.2
 */
@Component("fis_UserMessageService")
public class UserMessageService {

    private final DataManager dataManager;

    public UserMessageService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Retrieves all published user messages.
     * <p>
     * Results are ordered by posting date in descending order so that
     * the most recently published announcements appear first on the
     * application landing page.
     * </p>
     *
     * @return the list of published user messages, ordered from newest
     *         to oldest
     */
    public List<UserMessage> fetchPublishedMessages() {
        return dataManager.load(UserMessage.class)
                .query("SELECT e FROM fis_UserMessage e"
                        + " WHERE e.published = TRUE"
                        + " ORDER BY e.postDate DESC")
                .list();
    }
}