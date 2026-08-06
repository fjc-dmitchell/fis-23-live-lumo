package gov.fjc.fis.view.fundcontrolnotice;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.ViewController;

@Route(value = "foundation-fundControlNotices", layout = MainView.class)
@ViewController("fis_FundControlNotice.foundation-list")
public class FoundationFundControlNoticeListView extends FundControlNoticeListView {

    public FoundationFundControlNoticeListView() {
        super();
        super.setFjcFoundation();
    }

    @Override
    public String getPageTitle() {
        return "FJC Foundation: ".concat(super.getPageTitle());
    }
}
