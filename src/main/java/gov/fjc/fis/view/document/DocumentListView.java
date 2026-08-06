package gov.fjc.fis.view.document;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gov.fjc.fis.entity.Document;

import gov.fjc.fis.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.SupportsLabelPosition;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;

@Route(value = "documents", layout = MainView.class)
@ViewController("fis_Document.list")
@ViewDescriptor("document-list-view.xml")
@LookupComponent("documentsDataGrid")
@DialogMode(width = "64em")
public class DocumentListView extends StandardListView<Document> {
    @ViewComponent
    private VerticalLayout searchvbox;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        for (var component : UiComponentUtils.getComponents(searchvbox)) {
            if (component instanceof PropertyFilter<?>) {
                setFilterProperties((PropertyFilter<?>) component);
            }
        }
    }

    private void setFilterProperties(PropertyFilter<?> filter) {
        filter.setAutoApply(true);
        filter.setLabelPosition(SupportsLabelPosition.LabelPosition.TOP);
    }

    @Subscribe(id = "clearSearchBtn", subject = "clickListener")
    protected void onClearSearchBtnClick(final ClickEvent<JmixButton> event) {
        for (var component : UiComponentUtils.getComponents(searchvbox)) {
            if (component instanceof PropertyFilter<?>) {
                ((PropertyFilter<?>) component).clear();
                ((PropertyFilter<?>) component).getQueryCondition().setParameterValue(null);
                ((PropertyFilter<?>) component).apply();
            }
        }
    }
}