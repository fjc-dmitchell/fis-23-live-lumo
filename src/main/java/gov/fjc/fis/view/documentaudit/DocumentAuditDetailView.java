package gov.fjc.fis.view.documentaudit;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.DocumentAudit;
import gov.fjc.fis.service.DocumentExceptionService;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "document-audits/:id", layout = MainView.class)
@ViewController(id = "fis_DocumentAudit.detail")
@ViewDescriptor(path = "document-audit-detail-view.xml")
@EditedEntityContainer("documentAuditDc")
public class DocumentAuditDetailView extends StandardDetailView<DocumentAudit> {
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private DocumentExceptionService documentExceptionService;
    @ViewComponent
    private TypedTextField<String> processIdString;
    @ViewComponent
    private TypedTextField<String> processStatusString;
    @ViewComponent
    private TypedTextField<String> bfyString;
    @ViewComponent
    private TypedTextField<String> obligationStatusString;
    @ViewComponent
    private JmixDetails activityBocChange;
    @ViewComponent
    private JmixButton createExceptionBtn;
    @ViewComponent
    private JmixButton removeExceptionBtn;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        setReadOnly(true);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        var audit = getEditedEntity();

        var processId = audit.getProcessId();
        processIdString.setValue(processId != null ? processId.toString() : "");

        var processStatus = audit.getProcessStatus();
        switch (processStatus) {
            case "I" -> processStatusString.setValue("I - Inserted new Obligation");
            case "U" -> processStatusString.setValue("U - Obligation was updated");
            case "R" -> processStatusString.setValue("R - Document was rejected");
            default -> processStatusString.setValue(processStatus.concat(" - unknown status"));
        }

        if (processStatus.equals("R")) {
            toggleExceptionButtons(!documentExceptionService.exceptionExists(audit));
        }

        obligationStatusString.setValue(audit.getObligationStatus() ? "Closed" : "Open");

        var bbfy = audit.getDocumentBbfy();
        var ebfy = audit.getDocumentEbfy();
        bfyString.setValue(ebfy.isBlank() ? bbfy : bbfy + "/" + ebfy);

        var previousProjectionActivity = audit.getPreviousActivityNumber();
        var budgetObjectClass = audit.getPreviousProjectionBoc();
        activityBocChange.setVisible(previousProjectionActivity != null || budgetObjectClass != null);
    }

    @Subscribe(id = "createExceptionBtn", subject = "clickListener")
    public void onCreateExceptionBtnClick(final ClickEvent<JmixButton> event) {
        var exception = documentExceptionService.createException(getEditedEntity());
        if (exception != null) {
            Html htmlContent = new Html("<p>A Document Exception has been created for <strong>"
                    .concat(exception.getDocumentNumber()).concat("</strong>.<br /><br />")
                    .concat("The document with the following attributes will not be processed again<br />")
                    .concat(exception.getInstanceName(metadataTools))
                    .concat("</p>"));
            dialogs.createMessageDialog()
                    .withHeader("Success")
                    .withContent(htmlContent)
                    .open();
        } else {
            dialogs.createMessageDialog()
                    .withHeader("Error")
                    .withText("An exception already exists for this document.")
                    .open();
        }
        toggleExceptionButtons(false);
    }

    @Subscribe(id = "removeExceptionBtn", subject = "clickListener")
    public void onRemoveExceptionBtnClick(final ClickEvent<JmixButton> event) {
        if (documentExceptionService.removeException(getEditedEntity())) {
            dialogs.createMessageDialog()
                    .withHeader("Success")
                    .withText("The Document Exception has been removed successfully")
                    .open();
        } else {
            dialogs.createMessageDialog()
                    .withHeader("Error")
                    .withText("The Document Exception did not exist and could not be deleted.")
                    .open();
        }
        toggleExceptionButtons(true);
    }

    private void toggleExceptionButtons(boolean showCreate) {
        createExceptionBtn.setVisible(showCreate);
        createExceptionBtn.setEnabled(showCreate);
        removeExceptionBtn.setVisible(!showCreate);
        removeExceptionBtn.setEnabled(!showCreate);
    }
}