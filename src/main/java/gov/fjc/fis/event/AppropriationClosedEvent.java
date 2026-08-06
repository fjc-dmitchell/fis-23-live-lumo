package gov.fjc.fis.event;

import org.springframework.context.ApplicationEvent;

public class AppropriationClosedEvent extends ApplicationEvent {

    private String name;

    public AppropriationClosedEvent(Object source, String name) {
        super(source);
        this.name = name;
    }
}
