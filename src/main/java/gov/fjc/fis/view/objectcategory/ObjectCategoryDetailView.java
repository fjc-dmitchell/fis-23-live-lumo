package gov.fjc.fis.view.objectcategory;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.entity.ObjectCategory;

import gov.fjc.fis.entity.DivisionAllocation;
import gov.fjc.fis.entity.ObjectClass;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.view.activityprojectionfragment.ActivityProjectionFragment;
import gov.fjc.fis.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "object-categories/:id", layout = MainView.class)
@ViewController("fis_ObjectCategory.detail")
@ViewDescriptor("object-category-detail-view.xml")
@EditedEntityContainer("categoryDc")
public class ObjectCategoryDetailView extends StandardDetailView<ObjectCategory> {
    @Autowired
    private SessionData sessionData;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private ReadOnlyViewsSupport readOnlyViewsSupport;
    @Autowired
    private AppropriationService appropriationService;
    @Autowired
    private Fragments fragments;

    @ViewComponent
    private EntityPicker<Appropriation> appropriationField;
    @ViewComponent
    private TypedTextField<String> majorObjectClassField;
    @ViewComponent
    private VerticalLayout tabBox;
    @ViewComponent
    private VerticalLayout projectionsBox;
    @ViewComponent
    private DataGrid<ObjectClass> budgetObjectClassesDataGrid;
    @ViewComponent
    private DataGrid<DivisionAllocation> allocationsGrid;
    @ViewComponent
    private Paragraph createdByString;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        var category = getEditedEntity();

        if (entityStates.isNew(category)) {
            var appropriation = appropriationService.getBfyEntryAppropriation(sessionData);
            category.setAppropriation(appropriation);
            appropriationField.setReadOnly(true);
            majorObjectClassField.focus();
        } else {
            var appropriation = category.getAppropriation();
            if ((!appropriation.getStatus())) {
                readOnlyViewsSupport.setViewReadOnly(this, true);
            } else {
                appropriationField.setReadOnly(true);
                majorObjectClassField.setReadOnly(true);
            }

            allocationsGrid.setEmptyStateText("There are no allocations for ".concat(category.getTitleAndCode()));
            budgetObjectClassesDataGrid.setEmptyStateText("There are no object classes for ".concat(category.getTitleAndCode()));

            ActivityProjectionFragment fragment = fragments.create(this, ActivityProjectionFragment.class);
            fragment.setEntity(category);
            projectionsBox.add(fragment);
            tabBox.setVisible(true);
        }
        createdByString.setText(category.getCreatedByString());
    }
}