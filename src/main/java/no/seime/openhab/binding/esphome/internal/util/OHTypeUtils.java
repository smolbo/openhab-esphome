package no.seime.openhab.binding.esphome.internal.util;

import java.util.Optional;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class OHTypeUtils {
    public static Optional<Boolean> commandAsBoolean(Command command) {
        if (!(command instanceof State state)) {
            return Optional.empty();
        }
        return Optional.ofNullable(state.as(OnOffType.class)).map(it -> it == OnOffType.ON);
    }

    public static Optional<Float> commandAsFloat(Command command) {
        if (!(command instanceof State state)) {
            return Optional.empty();
        }
        return Optional.ofNullable(state.as(DecimalType.class)).map(DecimalType::floatValue);
    }

    public static Optional<Integer> commandAsInt(Command command) {
        if (!(command instanceof State state)) {
            return Optional.empty();
        }
        return Optional.ofNullable(state.as(DecimalType.class)).map(DecimalType::intValue);
    }
}
