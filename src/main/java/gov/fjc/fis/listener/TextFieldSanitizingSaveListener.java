package gov.fjc.fis.listener;

import gov.fjc.fis.app.AsciiSanitizer;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Jmix entity-save event listener that sanitizes string-valued entity
 * properties before they are persisted.
 * <p>
 * When an {@link EntitySavingEvent} is received, this listener uses Jmix
 * metadata to inspect the entity's properties. Each property whose Java type
 * is {@link String} is passed to {@link AsciiSanitizer#sanitize(String)}.
 * If sanitization changes the value, the sanitized value is assigned back to
 * the entity before the save operation completes.
 * </p>
 * <p>
 * This listener provides centralized sanitization for text entered through
 * Jmix and Vaadin UI components, as well as values assigned through other
 * application workflows. Applying sanitization at the entity-saving boundary
 * helps ensure that string data is handled consistently regardless of where
 * the value originated.
 * </p>
 * <p>
 * Changes made during sanitization are logged at the {@code INFO} level,
 * including the property name, original value, and sanitized value.
 * </p>
 *
 * @author Doug Mitchell
 * @since 2.3
 */
@Component("fis_TextFieldSanitizingSaveListener")
public class TextFieldSanitizingSaveListener {

    private static final Logger log = LoggerFactory.getLogger(TextFieldSanitizingSaveListener.class);

    private final Metadata metadata;

    public TextFieldSanitizingSaveListener(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Sanitizes all non-null string properties of an entity before it is saved.
     * <p>
     * Properties are identified through the entity's Jmix metadata. When a
     * sanitized value differs from its original value, the updated value is
     * assigned to the entity and the change is logged.
     * </p>
     *
     * @param event the entity-saving event containing the entity to sanitize
     */
    @EventListener
    public void onEntitySaving(EntitySavingEvent<?> event) {
        Object entity = event.getEntity();
        MetaClass metaClass = metadata.getClass(entity);
        for (MetaProperty property : metaClass.getProperties()) {
            if (property.getJavaType() == String.class) {
                String value = EntityValues.getValue(entity, property.getName());
                if (value != null) {
                    String cleaned = AsciiSanitizer.sanitize(value);
                    if (!cleaned.equals(value)) {
                        log.info("{} value {} sanitized as {}", property.getName(), value, cleaned);
                        EntityValues.setValue(entity, property.getName(), cleaned);
                    }
                }
            }
        }
    }
}