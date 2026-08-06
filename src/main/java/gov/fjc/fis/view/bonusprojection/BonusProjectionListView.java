package gov.fjc.fis.view.bonusprojection;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.personnel.BonusProjection;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;


@Route(value = "bonusProjections", layout = MainView.class)
@ViewController("fis_BonusProjection.list")
@ViewDescriptor("bonus-projection-list-view.xml")
@LookupComponent("bonusProjectionsDataGrid")
@DialogMode(width = "64em")
public class BonusProjectionListView extends StandardListView<BonusProjection> {
}