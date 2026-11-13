package gov.fjc.fis.app;

import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.model.CollectionLoader;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FIS utility methods for working with Jmix and Vaadin components.
 */
public class FrameworkUtils {

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
