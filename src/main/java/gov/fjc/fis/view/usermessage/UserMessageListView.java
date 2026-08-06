package gov.fjc.fis.view.usermessage;

import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.UserMessage;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.view.*;


@Route(value = "userMessages", layout = MainView.class)
@ViewController("fis_UserMessage.list")
@ViewDescriptor("user-message-list-view.xml")
@LookupComponent("userMessagesDataGrid")
@DialogMode(width = "64em")
public class UserMessageListView extends StandardListView<UserMessage> {
}