package gov.fjc.fis.view.invoice;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.ViewController;

@Route(value = "foundation-invoices", layout = MainView.class)
@ViewController("fis_Invoice.foundation-list")
public class FoundationInvoiceListView extends InvoiceListView {
    public FoundationInvoiceListView() {
        super();
        super.setFjcFoundation();
    }

    @Override
    public String getPageTitle() {
        return "FJC Foundation: ".concat(super.getPageTitle());
    }
}
