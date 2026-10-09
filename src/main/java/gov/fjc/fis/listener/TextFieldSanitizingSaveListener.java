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

@Component("fis_TextFieldSanitizingSaveListener")
public class TextFieldSanitizingSaveListener {

    private static final Logger log = LoggerFactory.getLogger(TextFieldSanitizingSaveListener.class);

    private final Metadata metadata;

    public TextFieldSanitizingSaveListener(Metadata metadata) {
        this.metadata = metadata;
    }

    @EventListener
//    @SuppressWarnings("rawtypes")
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