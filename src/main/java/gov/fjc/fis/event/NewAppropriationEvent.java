package gov.fjc.fis.event;

import org.springframework.context.ApplicationEvent;

public class NewAppropriationEvent extends ApplicationEvent {
    private String name;

    public NewAppropriationEvent(Object source, String name) {
        super(source);
        this.name = name;
    }
}
