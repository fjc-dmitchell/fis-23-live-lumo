package gov.fjc.fis.view.fund;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Fund;
import gov.fjc.fis.service.FundService;
import gov.fjc.fis.view.main.MainView;
import io.jmix.core.LoadContext;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Route(value = "funds", layout = MainView.class)
@ViewController(id = "fis_Fund.list")
@ViewDescriptor(path = "fund-list-view.xml")
@LookupComponent("fundsDataGrid")
@DialogMode(width = "64em")
public class FundListView extends StandardListView<Fund> {
    @Autowired
    private FundService fundService;

    @Install(to = "fundsDl", target = Target.DATA_LOADER)
    private List<Fund> fundsDlLoadDelegate(final LoadContext<Fund> loadContext) {
        return fundService.getFundList();
    }
}