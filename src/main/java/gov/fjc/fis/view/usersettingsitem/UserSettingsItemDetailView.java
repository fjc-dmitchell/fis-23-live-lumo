package gov.fjc.fis.view.usersettingsitem;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.view.*;
//import io.jmix.flowui.xml.facet.SettingsFacetProvider;
import io.jmix.flowuidata.entity.UserSettingsItem;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "userSettingsItems/:id", layout = MainView.class)
@ViewController(id = "flowui_UserSettingsItem.detail")
@ViewDescriptor(path = "user-settings-item-detail-view.xml")
@EditedEntityContainer("userSettingsItemDc")
public class UserSettingsItemDetailView extends StandardDetailView<UserSettingsItem> {
}