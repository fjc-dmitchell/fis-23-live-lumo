package gov.fjc.fis.view.fundcontrolnotice;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.DocumentType;
import gov.fjc.fis.entity.FundControlNotice;
import gov.fjc.fis.entity.Obligation;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.ObligationService;
import gov.fjc.fis.view.fileattachmentfragment.FileAttachmentFragment;
import gov.fjc.fis.view.main.MainView;
import gov.fjc.fis.view.obligation.ObligationLookupView;
import io.jmix.core.EntityStates;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;
import java.util.stream.Stream;

@Route(value = "fundControlNotices/:id", layout = MainView.class)
@ViewController("fis_FundControlNotice.detail")
@ViewDescriptor("fund-control-notice-detail-view.xml")
@EditedEntityContainer("fundControlNoticeDc")
public class FundControlNoticeDetailView extends StandardDetailView<FundControlNotice> {
    @Autowired
    private ReadOnlyViewsSupport readOnlyViewsSupport;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private SessionData sessionData;
    @Autowired
    private EntityStates entityStates;

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
    private TypedDatePicker<Date> fcnDateField;
    @ViewComponent
    private TypedDatePicker<Date> travelStartDateField;
    @ViewComponent
    private TypedDatePicker<Date> travelEndDateField;
    @ViewComponent
    private FileAttachmentFragment attachmentFragment;
    @ViewComponent
    private Paragraph aoSyncStringField;
    @ViewComponent
    private Paragraph createdByString;

    Boolean fjcFoundation = false;
    Appropriation entryBfy;

    public void setFjcFoundation(Boolean fjcFoundation) {
        this.fjcFoundation = fjcFoundation;
    }

    // Todo: hide date fields if obligation is not a Travel Authorization

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        var fcn = getEditedEntity();
        attachmentFragment.setHostEntity(fcn);
        if (entityStates.isNew(fcn)) {
            entryBfy = appropriationService.getBfyEntryAppropriation(sessionData);
            if (entryBfy != null) {
                budgetFiscalYearField.setValue(entryBfy.getBudgetFiscalYear());
            }
            docIdSuggestionField.setVisible(true);
            docIdSuggestionField.focus();
            fcnDateField.setValue(LocalDate.now());
        } else {
            entryBfy = fcn.getObligation().getActivity().getDivision().getAppropriation();
            budgetFiscalYearField.setValue(entryBfy.getBudgetFiscalYear());
            aoSyncStringField.setText(fcn.getAoSyncString());
            createdByString.setText(fcn.getCreatedByString());
            obligationInfoForm.setVisible(true);
            fcnDateField.focus();
            if (!entryBfy.getStatus()) {
                readOnlyViewsSupport.setViewReadOnly(this, true);
                attachmentFragment.setReadOnly(true);
            }
        }
    }

//    @Supply(to = "docIdSuggestionField", subject = "renderer")
//    protected Renderer<Obligation> docIdSuggestionFieldRenderer() {
//        return new TextRenderer<>(Obligation::getSuggestion);
//    }

    @Install(to = "docIdSuggestionField", subject = "itemLabelGenerator")
    protected Object docIdSuggestionFieldItemLabelGenerator(final Obligation obligation) {
        return obligation.getSuggestion();
    }

    @Install(to = "docIdSuggestionField", subject = "itemsFetchCallback")
    protected Stream<Obligation> docIdSuggestionFieldItemsFetchCallback(final Query<Obligation, String> query) {
        String enteredValue = query.getFilter()
                .orElse("");
        return obligationService.getObligationSuggestion(entryBfy.getBudgetFiscalYear(), enteredValue, fjcFoundation)
                .stream().skip(query.getOffset()).limit(query.getLimit());
    }

    @Subscribe("docIdSuggestionField")
    protected void onDocIdSuggestionFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Obligation>, Obligation> event) {
        obligationInfoForm.setVisible(docIdSuggestionField.getValue() != null);
        if (docIdSuggestionField.getValue() != null) {
            fcnDateField.focus();
        } else {
            docIdSuggestionField.focus();
        }
        setTravelDataVisibility();
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

    private void setTravelDataVisibility() {
        var fcn = getEditedEntity();
        var obligation = fcn.getObligation();
        if (obligation != null) {
            var docType = obligation.getDocumentType();
            travelStartDateField.setVisible(docType.equals(DocumentType.TRAVEL_AUTHORIZATION));
            travelEndDateField.setVisible(docType.equals(DocumentType.TRAVEL_AUTHORIZATION));
        }
    }
}