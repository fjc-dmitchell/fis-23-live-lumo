package gov.fjc.fis.event;

import org.springframework.context.ApplicationEvent;

/**
 * Application event published when a user message is created, updated,
 * or deleted.
 *
 * <p>User messages are custom FIS announcements displayed on the
 * application landing page and are visible to all users. This event is
 * published whenever the set of available messages changes so that active
 * user sessions can immediately refresh their displayed messages.
 *
 * <p>The primary purpose of this event is to eliminate the need for users
 * to sign out and back in to see changes made to system-wide messages.
 * Components that display or cache user messages can subscribe to this
 * event and reload message data whenever a message is added, modified,
 * or removed.
 *
 * <p>By using an application event, FIS provides a centralized and loosely
 * coupled mechanism for synchronizing landing page message content across
 * all active user sessions and ensuring that users always see the current
 * set of announcements.
 *
 * @author Doug Mitchell
 */
public class UserMessageSavedEvent extends ApplicationEvent {
    private String name;

    public UserMessageSavedEvent(Object source, String name) {
        super(source);
        this.name = name;
    }
}
