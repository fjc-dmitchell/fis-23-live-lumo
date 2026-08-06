package gov.fjc.fis.view.document;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Document;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "documents/:id", layout = MainView.class)
@ViewController(id = "fis_Document.detail")
@ViewDescriptor(path = "document-detail-view.xml")
@EditedEntityContainer("documentDc")
public class DocumentDetailView extends StandardDetailView<Document> {
    @Autowired
    private ReadOnlyViewsSupport readOnlyViewsSupport;
    @ViewComponent
    private TypedTextField<Object> docTypeDescField;
    @ViewComponent
    private VerticalLayout travelBox;

    @Subscribe
    protected void onInit(final InitEvent event) {
        readOnlyViewsSupport.setViewReadOnly(this, true);
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
       var document = getEditedEntity();
       var docType = document.getDocumentType();
       if(docType.startsWith("MO")) {
           docTypeDescField.setValue(docType.concat(" (Miscellaneous Obligation)"));
       } else if (docType.startsWith("TA")) {
           docTypeDescField.setValue(docType.concat(" (Travel Authorization"));
           travelBox.setVisible(true);
       } else
           docTypeDescField.setValue(docType);
    }
}