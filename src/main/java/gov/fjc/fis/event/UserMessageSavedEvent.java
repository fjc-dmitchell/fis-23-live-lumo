package gov.fjc.fis.event;

import org.springframework.context.ApplicationEvent;

public class UserMessageSavedEvent extends ApplicationEvent {
    private String name;

    public UserMessageSavedEvent(Object source, String name) {
        super(source);
        this.name = name;
    }
}
