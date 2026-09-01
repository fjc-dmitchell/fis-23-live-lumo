package gov.fjc.fis.view.invoice;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.Invoice;
import gov.fjc.fis.entity.Obligation;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.ObligationService;
import gov.fjc.fis.view.fileattachmentfragment.FileAttachmentFragment;
import gov.fjc.fis.view.main.MainView;
import gov.fjc.fis.view.obligation.ObligationLookupView;
import io.jmix.core.EntityStates;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;
import java.util.stream.Stream;

@Route(value = "invoices/:id", layout = MainView.class)
@ViewController(id = "fis_Invoice.detail")
@ViewDescriptor(path = "invoice-detail-view.xml")
@EditedEntityContainer("invoiceDc")
public class InvoiceDetailView extends StandardDetailView<Invoice> {
    @Autowired
    private ReadOnlyViewsSupport readOnlyViewsSupport;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private SessionData sessionData;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private Fragments fragments;

    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private ObligationService obligationService;

    @ViewComponent
    private TypedTextField<String> budgetFiscalYearField;
    @ViewComponent
    private EntityComboBox<Obligation> docIdSuggestionField;
    @ViewComponent
    private JmixFormLayout obligationInfoForm;
    @ViewComponent
    private TypedDatePicker<Date> invoiceDateField;
    @ViewComponent
    private TypedDatePicker<Date> paymentDateField;

    @ViewComponent
    private Paragraph attachmentNote;
    @ViewComponent
    private JmixDetails attachmentDetails;
    @ViewComponent
    private Paragraph createdByString;

    Boolean fjcFoundation = false;
    Appropriation entryBfy;
    @ViewComponent
    private TypedTextField<String> invoiceNumberField;

    public void setFjcFoundation(boolean fjcFoundation) {
        this.fjcFoundation = fjcFoundation;
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        var invoice = getEditedEntity();
//        attachmentFragment.setHostEntity(invoice);
        if (entityStates.isNew(invoice)) {
            entryBfy = appropriationService.getBfyEntryAppropriation(sessionData);
            if (entryBfy != null) {
                budgetFiscalYearField.setValue(entryBfy.getBudgetFiscalYear());
            }
            docIdSuggestionField.setVisible(true);
            docIdSuggestionField.focus();
            invoiceDateField.setValue(LocalDate.now());
            paymentDateField.setValue(LocalDate.now());


        } else {
            entryBfy = invoice.getObligation().getActivity().getDivision().getAppropriation();
            budgetFiscalYearField.setValue(entryBfy.getBudgetFiscalYear());
            createdByString.setText(invoice.getCreatedByString());
            obligationInfoForm.setVisible(true);

            FileAttachmentFragment fileAttachmentFragment = fragments.create(this, FileAttachmentFragment.class);
            attachmentDetails.add(fileAttachmentFragment);
            attachmentNote.setVisible(false);
            fileAttachmentFragment.setHostEntity(invoice);

            invoiceNumberField.setAutoselect(true);
            invoiceNumberField.focus();
            if (!entryBfy.getStatus()) {
                readOnlyViewsSupport.setViewReadOnly(this, true);
                fileAttachmentFragment.setReadOnly(true);
            }
        }
    }

    @Install(to = "docIdSuggestionField", subject = "itemLabelGenerator")
    protected Object docIdSuggestionFieldItemLabelGenerator(final Obligation obligation) {
        return obligation.getSuggestion();
    }

    @Install(to = "docIdSuggestionField", subject = "itemsFetchCallback")
    protected Stream<Obligation> docIdSuggestionFieldItemsFetchCallback(final Query<Obligation, String> query) {
        String enteredValue = query.getFilter().orElse("");
        return obligationService.getObligationSuggestion(entryBfy.getBudgetFiscalYear(), enteredValue, fjcFoundation)
                .stream().skip(query.getOffset()).limit(query.getLimit());
    }

    @Subscribe("docIdSuggestionField")
    protected void onDocIdSuggestionFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Obligation>, Obligation> event) {
        obligationInfoForm.setVisible(docIdSuggestionField.getValue() != null);
        if (docIdSuggestionField.getValue() != null) {
            invoiceNumberField.focus();
        } else {
            docIdSuggestionField.focus();
        }
    }

    @Subscribe("docIdSuggestionField.customLookup")
    protected void onDocIdSuggestionFieldCustomLookup(final ActionPerformedEvent event) {
        DialogWindow<ObligationLookupView> window = dialogWindows.lookup(this, Obligation.class)
                .withViewClass(ObligationLookupView.class)
                .withSelectHandler(obligations -> {
                    Obligation obligation = obligations.stream().findFirst().orElse(null);
                    docIdSuggestionField.setValue(obligation);
                })
                .build();
        window.getView().setAppropriation(entryBfy);
        window.getView().setFjcFoundation(fjcFoundation);
        window.setWidth("1300px");
        window.open();
    }
}