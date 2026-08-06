package gov.fjc.fis.view.appropriation;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Html;
import gov.fjc.fis.entity.*;

import gov.fjc.fis.event.NewAppropriationEvent;
import gov.fjc.fis.service.*;
import gov.fjc.fis.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.List;

@Route(value = "appropriations", layout = MainView.class)
@ViewController("fis_Appropriation.list")
@ViewDescriptor("appropriation-list-view.xml")
@LookupComponent("appropriationsDataGrid")
@DialogMode(width = "64em")
public class AppropriationListView extends StandardListView<Appropriation> {
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private UiEventPublisher uiEventPublisher;
    @ViewComponent
    private CollectionLoader<Appropriation> appropriationsDl;
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private NewFiscalYearService newFiscalYearService;
    @ViewComponent
    private JmixButton createBtn;
    @Autowired
    private Dialogs dialogs;

    String nextFiscalYear;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        createBtn.setEnabled(currentAuthentication.getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_system-full-access")));
        refreshCreateBtn();
    }

    @Install(to = "appropriationsDl", target = Target.DATA_LOADER)
    private List<Appropriation> appropriationsDlLoadDelegate(final LoadContext<Appropriation> loadContext) {
        return appropriationService.getAppropriations();
    }

    @Subscribe(id = "createBtn", subject = "clickListener")
    protected void onCreateBtnClick(final ClickEvent<JmixButton> event) {
        Html htmlContent = new Html("<p>Do you want to create a new Appropriation for "
                .concat(nextFiscalYear).concat(" with selected<br />")
                .concat("prior year Divisions, Branches, Groups, Activities, Object Classes,<br />")
                .concat("and Pay Periods?<br />")
                .concat("<br /><b>This action cannot be undone.</b>"));

        dialogs.createOptionDialog()
                .withHeader("Create Budget Fiscal Year ".concat(nextFiscalYear))
                .withContent(htmlContent)
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(e -> createAppropriation()),
                        new DialogAction(DialogAction.Type.NO)
                )
                .open();
    }

    private void createAppropriation() {
        newFiscalYearService.createAppropriation(nextFiscalYear);
        uiEventPublisher.publishEventForUsers(new NewAppropriationEvent(this, nextFiscalYear),null);
    }

    private void refreshCreateBtn() {
        nextFiscalYear = newFiscalYearService.getNextFiscalYear();
        createBtn.setText("Create ".concat(nextFiscalYear).concat(" Appropriation"));
    }

    @EventListener
    public void handleNewAppropriationEvent(NewAppropriationEvent event) {
        appropriationsDl.load();
        refreshCreateBtn();
    }
}