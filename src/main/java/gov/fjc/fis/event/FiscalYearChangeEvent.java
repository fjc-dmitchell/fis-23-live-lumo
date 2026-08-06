package gov.fjc.fis.event;

import org.springframework.context.ApplicationEvent;

public class FiscalYearChangeEvent extends ApplicationEvent {
    private String name;

    public FiscalYearChangeEvent(Object source, String name) {
        super(source);
        this.name = name;
    }
}
