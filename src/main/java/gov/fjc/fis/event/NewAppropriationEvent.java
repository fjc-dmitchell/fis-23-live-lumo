package gov.fjc.fis.event;

import org.springframework.context.ApplicationEvent;

/**
 * Application event published when a new appropriation fiscal year is
 * created within FIS.
 *
 * <p>This event notifies active user sessions that a new appropriation
 * fiscal year is available for use. Subscribers may respond by refreshing
 * cached fiscal year data, reloading selection lists, or updating other
 * fiscal year-dependent application state.
 *
 * <p>The primary purpose of this event is to ensure that users immediately
 * have access to newly created fiscal years without requiring a new login
 * or application restart. When the event is received, user interface
 * components can update the list of available fiscal years presented in
 * data entry and related screens.
 *
 * <p>By using an application event, FIS provides a centralized and loosely
 * coupled mechanism for notifying all interested components that the
 * appropriation structure has changed and that fiscal year selections
 * should be refreshed.
 *
 * @author Doug Mitchell
 */
public class NewAppropriationEvent extends ApplicationEvent {
    private String name;

    public NewAppropriationEvent(Object source, String name) {
        super(source);
        this.name = name;
    }
}
