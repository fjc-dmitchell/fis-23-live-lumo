package gov.fjc.fis.view.documentexception;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.DocumentException;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;

@Route(value = "document-exceptions/:id", layout = MainView.class)
@ViewController(id = "fis_DocumentException.detail")
@ViewDescriptor(path = "document-exception-detail-view.xml")
@EditedEntityContainer("documentExceptionDc")
public class DocumentExceptionDetailView extends StandardDetailView<DocumentException> {
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        setReadOnly(true);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        createdByString.setText(getEditedEntity().getCreatedByString());
    }
}