package gov.fjc.fis.event;

import org.springframework.context.ApplicationEvent;

public class DocumentExceptionEvent extends ApplicationEvent {

    public DocumentExceptionEvent(Object source) {
        super(source);
    }
}
