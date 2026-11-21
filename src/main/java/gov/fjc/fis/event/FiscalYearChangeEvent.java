package gov.fjc.fis.event;

import org.springframework.context.ApplicationEvent;

/**
 * Application event published when a user changes either the data entry
 * fiscal year or search fiscal year for the current session.
 *
 * <p>FIS maintains user-specific fiscal year settings that determine the
 * default fiscal year context used by searches, reports, views, and other
 * business processes. When a user changes one of these fiscal year values,
 * this event is published so that interested components can refresh their
 * state and react to the updated fiscal year selection.
 *
 * <p>The event enables loose coupling between the user interface and
 * application services by providing a centralized notification mechanism
 * whenever fiscal year preferences change during a session.
 *
 * <p>Typical consumers include reporting, search, and data retrieval
 * components whose behavior depends upon the user's currently selected
 * fiscal year.
 *
 * @author Doug Mitchell
 */
public class FiscalYearChangeEvent extends ApplicationEvent {
    private String name;

    public FiscalYearChangeEvent(Object source, String name) {
        super(source);
        this.name = name;
    }
}
