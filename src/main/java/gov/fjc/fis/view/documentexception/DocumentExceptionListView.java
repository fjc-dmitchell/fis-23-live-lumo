package gov.fjc.fis.view.documentexception;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.DocumentException;
import gov.fjc.fis.event.DocumentExceptionEvent;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.context.event.EventListener;


@Route(value = "document-exceptions", layout = MainView.class)
@ViewController(id = "fis_DocumentException.list")
@ViewDescriptor(path = "document-exception-list-view.xml")
@LookupComponent("documentExceptionsDataGrid")
@DialogMode(width = "64em")
public class DocumentExceptionListView extends StandardListView<DocumentException> {
    @ViewComponent
    private CollectionLoader<DocumentException> documentExceptionsDl;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        System.out.println("--- inside onBeforeShow: DocumentExceptionListView");
    }

    @EventListener
    public void handleDocumentExceptionEvent(DocumentExceptionEvent event) {
        documentExceptionsDl.load();
    }
}