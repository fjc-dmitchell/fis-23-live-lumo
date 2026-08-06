package gov.fjc.fis.view.fund;

import com.vaadin.flow.component.BlurNotifier;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import gov.fjc.fis.entity.Fund;
import gov.fjc.fis.view.main.MainView;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.view.*;

@Route(value = "funds/:id", layout = MainView.class)
@ViewController(id = "fis_Fund.detail")
@ViewDescriptor(path = "fund-detail-view.xml")
@EditedEntityContainer("fundDc")
public class FundDetailView extends StandardDetailView<Fund> {
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        createdByString.setText(getEditedEntity().getCreatedByString());
    }

//    @Subscribe("memoField")
//    public void onMemoFieldBlur(final BlurNotifier.BlurEvent<JmixTextArea> event) {
//        JmixTextArea field = event.getSource();
//        String value = field.getValue();
//        if (value != null) {
//            String trimmed = value.trim();
//            if (!trimmed.equals(value)) {
//                field.setValue(trimmed);
//            }
//        }
//    }
}