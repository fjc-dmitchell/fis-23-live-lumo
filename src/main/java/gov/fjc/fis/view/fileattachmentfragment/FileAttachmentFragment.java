package gov.fjc.fis.view.fileattachmentfragment;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gov.fjc.fis.entity.*;
import gov.fjc.fis.service.FileAttachmentService;
import io.jmix.core.*;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.upload.FileStorageUploadField;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.upload.TemporaryStorage;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@FragmentDescriptor("file-attachment-fragment.xml")
public class FileAttachmentFragment extends Fragment<VerticalLayout> {
    @Autowired
    private DataManager dataManager;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private FileAttachmentService fileAttachmentService;
    @Autowired
    private TemporaryStorage temporaryStorage;
    @Autowired
    private Downloader downloader;

    @ViewComponent
    private CollectionContainer<FileAttachment> attachmentsDc;
    @ViewComponent
    private CollectionLoader<FileAttachment> attachmentsDl;
    @ViewComponent
    private DataGrid<FileAttachment> attachmentsDataGrid;
    @ViewComponent
    private FileStorageUploadField fileAttachField;
    @ViewComponent
    private Paragraph unsavedMessage;
    @ViewComponent
    private VerticalLayout attachmentsBox;
    private FileAttachable hostEntity;

    private Activity activity;
    private Obligation obligation;
    private Invoice invoice;
    private FundControlNotice fundControlNotice;

    @ViewComponent
    private CollectionContainer<FileAttachmentCategory> fileAttachmentCategoriesDc;
    @ViewComponent
    private CollectionLoader<FileAttachmentCategory> fileAttachmentCategoriesDl;
    @ViewComponent
    private HorizontalLayout buttonsPanel;

    @Subscribe(target = Target.HOST_CONTROLLER)
    protected void onHostInit(final View.InitEvent event) {
        fileAttachmentCategoriesDl.load();
        attachmentsDataGrid.addComponentColumn(this::createDownloadButton);
        attachmentsDataGrid.addComponentColumn(this::createViewButton);
    }

    /* =========================================================
                       HOST ENTITY RESOLUTION
       ========================================================= */
    public void setHostEntity(FileAttachable hostEntity) {
        Objects.requireNonNull(hostEntity, "hostEntity cannot be null");

        this.hostEntity = hostEntity;
        resolveLinkedEntities(hostEntity);
    }

    private void resolveLinkedEntities(Object hostEntity) {
        switch (hostEntity) {
            case Activity act -> this.activity = act;
            case Obligation obl -> {
                this.obligation = obl;
                this.activity = obl.getActivity();
            }
            case Invoice inv -> {
                this.invoice = inv;
                this.obligation = inv.getObligation();
                this.activity = obligation.getActivity();
            }
            case FundControlNotice fcn -> {
                this.fundControlNotice = fcn;
                this.obligation = fcn.getObligation();
                this.activity = obligation.getActivity();
            }
            default -> throw new IllegalArgumentException("Unsupported host entity: " + hostEntity.getClass());
        }
    }

    private void applyEntityVisibilityRules() {
        if (entityStates.isNew(hostEntity)) {
            unsavedMessage.setVisible(true);
            attachmentsBox.setVisible(false);
            return;
        }

        // Hide attachedTo for invoice and FCN
        if (invoice != null || fundControlNotice != null) {
            var col = attachmentsDataGrid.getColumnByKey("attachedTo");
            if (col != null) {
                col.setVisible(false);
            }
        }
    }

    public void setReadOnly(boolean readOnly) {
        buttonsPanel.setEnabled(!readOnly);
        attachmentsDataGrid.setEnabled(!readOnly);
    }

    @Subscribe(target = Target.HOST_CONTROLLER)
    protected void onHostBeforeShow(final View.BeforeShowEvent event) {
        if (hostEntity == null) {
            throw new IllegalStateException("hostEntity is null in FileAttachmentFragment");
        }
        applyEntityVisibilityRules();
    }

    public void refresh() {
        if(hostEntity instanceof Obligation) {
            attachmentsDl.load();
        }
    }


    @Install(to = "attachmentsDl", target = Target.DATA_LOADER)
    private List<FileAttachment> attachmentsDlLoadDelegate(final LoadContext<FileAttachment> loadContext) {
        return fileAttachmentService.getFileAttachments(hostEntity);
    }

    /* =========================================================
                       GRID COLUMN BUTTONS
       ========================================================= */
    private Button createDownloadButton(FileAttachment attachment) {
        Button button = uiComponents.create(Button.class);
        button.setText("Download");
        button.setIcon(VaadinIcon.DOWNLOAD_ALT.create());
        button.addThemeName("tertiary-inline");
        button.addClickListener(click -> download(attachment));
        return button;
    }

    private Button createViewButton(FileAttachment attachment) {
        Button button = uiComponents.create(Button.class);
        button.setText("View");
        button.setIcon(VaadinIcon.ARROW_FORWARD.create());
        button.addThemeName("tertiary-inline");
        button.addClickListener(click -> view(attachment));
        return button;
    }

    /* =========================================================
                             UPLOAD HANDLING
       ========================================================= */
//    @Subscribe("fileAttachField")
//    protected void onFileAttachFieldFileUploadSucceeded(final FileUploadSucceededEvent<FileStorageUploadField> event) {
//        Receiver receiver = event.getReceiver();
//        if (!(receiver instanceof FileTemporaryStorageBuffer buffer)) {
//            return;
//        }
//
//        if (buffer.getFileData() == null) {
//            throw new IllegalStateException("File data is missing after upload");
//        }
//
//        UUID fileId = buffer.getFileData().getFileInfo().getId();
//
//        FileRef fileRef = temporaryStorage.putFileIntoStorage(fileId, event.getFileName());
//
//        FileAttachment attachment = buildNewAttachment(fileRef, event.getContentLength());
//        dataManager.save(attachment); // otherwise, we could end up with orphaned files
//        attachmentsDc.getMutableItems().add(attachment);
//
//        fileAttachField.clear();
//    }

    @Subscribe("fileAttachField")
    protected void onFileAttachFieldFileUploadSucceeded(
            final FileUploadSucceededEvent<FileStorageUploadField, TemporaryStorage.FileInfo> event) {

        TemporaryStorage.FileInfo fileInfo = event.getData();
        if (fileInfo == null) {
            throw new IllegalStateException("File data is missing after upload");
        }

        UUID fileId = fileInfo.getId();

        FileRef fileRef = temporaryStorage.putFileIntoStorage(fileId, event.getFileName());

        FileAttachment attachment = buildNewAttachment(fileRef, event.getContentLength());
        dataManager.saveWithoutReload(attachment); // otherwise, we could end up with orphaned files
        attachmentsDc.getMutableItems().add(attachment);

        fileAttachField.clear();
    }

    private FileAttachment buildNewAttachment(FileRef fileRef, Long length) {
        FileAttachment fileAttachment = dataManager.create(FileAttachment.class);

        fileAttachment.setFileReference(fileRef);
        fileAttachment.setContentLength(length);

        fileAttachment.setActivity(activity);
        fileAttachment.setObligation(obligation);
        fileAttachment.setInvoice(invoice);
        fileAttachment.setFundControlNotice(fundControlNotice);

        fileAttachment.setCategory(determineCategory());
        return fileAttachment;
    }

    private FileAttachmentCategory determineCategory() {
        if (fundControlNotice != null) {
            return findCategory("fcn", "fund control notice");
        }
        if (invoice != null) {
            return findCategory("invoice");
        }
        if (obligation != null) {
            if (obligation.getDocumentType() == DocumentType.MISCELLANEOUS_OBLIGATION) {
                return findCategory("purchase order");
            }
            if (obligation.getDocumentType() == DocumentType.TRAVEL_AUTHORIZATION) {
                return findCategory("travel authorization");
            }
        }
        return null;
    }

    private FileAttachmentCategory findCategory(String... names) {
        return fileAttachmentCategoriesDc.getItems().stream()
                .filter(cat -> Arrays.stream(names)
                        .anyMatch(n -> n.equalsIgnoreCase(cat.getTitle())))
                .findFirst().orElse(null);
    }

    /* =========================================================
                        VIEW / DOWNLOAD ACTIONS
       ========================================================= */
    private void download(FileAttachment attachment) {
        handleDownload(attachment, false);
    }

    private void view(FileAttachment attachment) {
        handleDownload(attachment, true);
    }

    private void handleDownload(FileAttachment attachment, boolean inline) {
        downloader.setShowNewWindow(inline);
        downloader.download(attachment.getFileReference());
    }
}