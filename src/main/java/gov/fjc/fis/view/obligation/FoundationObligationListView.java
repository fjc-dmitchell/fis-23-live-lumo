package gov.fjc.fis.view.obligation;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.ViewController;

@Route(value = "foundation-obligations", layout = MainView.class)
@ViewController("fis_Obligation.foundation-list")
public class FoundationObligationListView extends ObligationListView {
    public FoundationObligationListView() {
        super();
        super.setFjcFoundation();
    }

    @Override
    public String getPageTitle() {
        return "FJC Foundation: ".concat(super.getPageTitle());
    }
}