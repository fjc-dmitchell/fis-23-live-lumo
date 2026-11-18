package gov.fjc.fis.app;

import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.model.CollectionLoader;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FIS utility methods for working with Jmix and Vaadin UI components.
 * <p>
 * This class provides reusable helper methods that address common challenges
 * encountered when developing user interfaces with Jmix Flow UI and Vaadin
 * components. The utilities are designed to reduce boilerplate code, improve
 * consistency across views, and simplify interactions between UI components,
 * data containers, and data loaders.
 * </p>
 *
 * <p>
 * A primary focus of these utilities is preserving component state during
 * data reload operations. In Jmix applications, reloading data through
 * {@link CollectionLoader} instances often results in new entity instances
 * being created, which can cause selections in Vaadin components to be lost
 * when matching is based on object identity. The methods in this class help
 * maintain user selections by re-associating UI component values with the
 * corresponding reloaded entities using stable business keys.
 * </p>
 *
 * <p>
 * These utilities are intended for use in Jmix views, dialogs, fragments,
 * and other UI controllers where component state must be synchronized with
 * dynamically refreshed data.
 * </p>
 *
 * @author Doug Mitchell
 * @since 2.1
 */
public final class FrameworkUtils {

    private FrameworkUtils() {
    }

    /**
     * Reloads the data for an {@link EntityComboBox} and restores the currently
     * selected value by matching entities using the provided key extractor.
     * <p>
     * This method is useful in Jmix views when a {@link CollectionLoader} reloads
     * entities, causing previously selected entity instances to become detached
     * from the newly loaded instances. Rather than relying on object identity,
     * the selected item is re-associated using a stable business key.
     * </p>
     * <p>
     * If the current selection cannot be found in the reloaded collection, the
     * field value is cleared.
     * </p>
     *
     * @param loader       the collection loader used to refresh the available entities
     * @param field        the combo box whose selected value should be preserved
     * @param keyExtractor function that returns a unique key used to match
     *                     the old entity instance with its corresponding
     *                     reloaded instance
     * @param <T>          the entity type displayed by the combo box
     */
    public static <T> void refreshField(CollectionLoader<T> loader,
                                        EntityComboBox<T> field,
                                        Function<T, ?> keyExtractor) {
        loader.load();
        field.setValue(
                Optional.ofNullable(field.getValue())
                        .map(keyExtractor)
                        .flatMap(key -> loader.getContainer().getItems().stream()
                                .filter(item -> keyExtractor.apply(item).equals(key))
                                .findFirst())
                        .orElse(null)
        );
    }

    /**
     * Reloads the data for a multi-select field and restores all currently
     * selected values by matching entities using the provided key extractor.
     * <p>
     * This method is intended for Jmix components whose value is a
     * {@code Set<T>} (for example, multi-select entity picker or select
     * components). After the associated {@link CollectionLoader} reloads its
     * entities, the method replaces the selected items with the corresponding
     * instances from the newly loaded collection.
     * </p>
     * <p>
     * Entity matching is performed using the supplied business key rather than
     * object identity, allowing selections to survive reloads that create new
     * entity instances.
     * </p>
     * <p>
     * If the field contains no selected values, the method returns immediately
     * after reloading the data.
     * </p>
     *
     * @param loader       the collection loader used to refresh the available entities
     * @param field        the field containing a set of selected entities
     * @param keyExtractor function that returns a unique key used to match
     *                     previously selected entities with their reloaded
     *                     counterparts
     * @param <T>          the entity type contained in the field value
     * @param <K>          the key type returned by the key extractor
     */
    public static <T, K> void refreshField(CollectionLoader<T> loader,
                                           HasValue<?, Set<T>> field,
                                           Function<T, K> keyExtractor) {

        loader.load();

        Set<T> currentValues = field.getValue();

        if (currentValues == null || currentValues.isEmpty()) {
            return;
        }

        Set<K> selectedKeys = currentValues.stream()
                .map(keyExtractor)
                .collect(Collectors.toSet());

        Set<T> refreshedValues = loader.getContainer().getItems().stream()
                .filter(item -> selectedKeys.contains(keyExtractor.apply(item)))
                .collect(Collectors.toSet());

        field.setValue(refreshedValues);
    }
}
