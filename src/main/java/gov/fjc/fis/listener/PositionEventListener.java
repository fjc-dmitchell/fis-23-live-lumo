package gov.fjc.fis.listener;

import gov.fjc.fis.entity.personnel.Position;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component("fis_PositionEventListener")
public class PositionEventListener {
    private final Sequences sequences;

    public PositionEventListener(Sequences sequences) {
        this.sequences = sequences;
    }

    @EventListener
    public void onPositionSaving(final EntitySavingEvent<Position> event) {
        Position position = event.getEntity();

        // CRITICAL: Only generate a sequence if it's a brand new entity
//        if (event.isNewEntity()) {
//            Long number = sequences.createNextValue(Sequence.withName("position_number")
//                    .setStartValue(1000)
//                    .setIncrement(1));
//
//            position.setPositionNbr(String.format("NEW%05d", number));
//        }
    }
}