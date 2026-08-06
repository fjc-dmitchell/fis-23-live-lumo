package gov.fjc.fis.listener;

import gov.fjc.fis.entity.DocumentException;
import gov.fjc.fis.event.DocumentExceptionEvent;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.flowui.UiEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component("fis_DocumentExceptionEventListener")
public class DocumentExceptionEventListener {
    private final UiEventPublisher uiEventPublisher;

    public DocumentExceptionEventListener(UiEventPublisher uiEventPublisher) {
        this.uiEventPublisher = uiEventPublisher;
    }

    @EventListener
    public void onDocumentExceptionChangedBeforeCommit(final EntityChangedEvent<DocumentException> event) {
        DocumentExceptionEvent documentExceptionEvent = new DocumentExceptionEvent(this);
        uiEventPublisher.publishEventForCurrentUI(documentExceptionEvent);
    }
}