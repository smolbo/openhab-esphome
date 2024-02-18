package no.seime.openhab.binding.esphome.internal.message.light;

import io.esphome.api.LightCommandRequest;
import no.seime.openhab.binding.esphome.internal.util.OHTypeUtils;
import org.apache.directory.api.util.Strings;
import org.openhab.core.types.Command;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static no.seime.openhab.binding.esphome.internal.util.OHTypeUtils.commandAsFloat;

public interface CommandProcessor<ValueType> {

    void handleCommand(LightCommandRequest.Builder builder, Command command);

    Optional<ValueType> getValue(Command command);

    class DefaultCommandProcessor<ValueType> implements CommandProcessor<ValueType>{
        private final BiConsumer<LightCommandRequest.Builder, ValueType> valueSetter;
        private final BiConsumer<LightCommandRequest.Builder, Boolean> hasValueMethod;
        private final Function<Command, Optional<ValueType>> commandTransform;

        protected DefaultCommandProcessor(
                BiConsumer<LightCommandRequest.Builder, ValueType> valueSetter,
                BiConsumer<LightCommandRequest.Builder, Boolean> hasValueMethod,
                Function<Command, Optional<ValueType>> commandTransform
        ) {
            this.valueSetter = valueSetter;
            this.hasValueMethod = hasValueMethod;
            this.commandTransform = commandTransform;
        }

        @Override
        public void handleCommand(LightCommandRequest.Builder builder, Command command) {
            getValue(command).ifPresent(valueType -> {
                        valueSetter.accept(builder, valueType);
                        hasValueMethod.accept(builder, true);
                    }
            );
        }

        @Override
        public Optional<ValueType> getValue(Command command) {
            return commandTransform.apply(command);
        }
    }

    CommandProcessor<Integer> COLOR_MODE = new DefaultCommandProcessor<>(
            LightCommandRequest.Builder::setColorMode,
            LightCommandRequest.Builder::setHasColorMode,
            command -> ColorMode.parseSafe(command.toString()).map(ColorMode::getBitMask)
    ) {
    };

    CommandProcessor<Boolean> ON_OFF = new DefaultCommandProcessor<>(
            LightCommandRequest.Builder::setState,
            LightCommandRequest.Builder::setHasState,
            OHTypeUtils::commandAsBoolean
    ) {
    };

    CommandProcessor<Float> MASTER_BRIGHTNESS = clampedFloatProc(
            LightCommandRequest.Builder::setBrightness,
            LightCommandRequest.Builder::setHasBrightness
    );

    CommandProcessor<Float> COLOR_BRIGHTNESS = clampedFloatProc(
            LightCommandRequest.Builder::setColorBrightness,
            LightCommandRequest.Builder::setHasColorBrightness
    );

    CommandProcessor<Float> RED = clampedFloatProc(
            LightCommandRequest.Builder::setRed,
            LightCommandRequest.Builder::setHasRgb
    );

    CommandProcessor<Float> GREEN = clampedFloatProc(
            LightCommandRequest.Builder::setGreen,
            LightCommandRequest.Builder::setHasRgb
    );

    CommandProcessor<Float> BLUE = clampedFloatProc(
            LightCommandRequest.Builder::setBlue,
            LightCommandRequest.Builder::setHasRgb
    );

    CommandProcessor<Float> WHITE = clampedFloatProc(
            LightCommandRequest.Builder::setWhite,
            LightCommandRequest.Builder::setHasWhite
    );

    CommandProcessor<Float> COLD_WHITE = clampedFloatProc(
            LightCommandRequest.Builder::setColdWhite,
            LightCommandRequest.Builder::setHasColdWhite
    );

    CommandProcessor<Float> WARM_WHITE = clampedFloatProc(
            LightCommandRequest.Builder::setWarmWhite,
            LightCommandRequest.Builder::setHasWarmWhite
    );

    CommandProcessor<Float> COLOR_TEMPERATURE = floatProc(
            LightCommandRequest.Builder::setColorTemperature,
            LightCommandRequest.Builder::setHasColorTemperature
    );

    CommandProcessor<Integer> TRANSITION_LENGTH = intProc(
            LightCommandRequest.Builder::setTransitionLength,
            LightCommandRequest.Builder::setHasTransitionLength
    );


    CommandProcessor<Integer> FLASH_LENGTH = intProc(
            LightCommandRequest.Builder::setFlashLength,
            LightCommandRequest.Builder::setHasFlashLength
    );

    CommandProcessor<String> EFFECT = new DefaultCommandProcessor<>(
            LightCommandRequest.Builder::setEffect,
            LightCommandRequest.Builder::setHasEffect,
            command -> Optional.of(command.toString()).filter(Strings::isNotEmpty)
    ) {
    };



    private static CommandProcessor<Float> floatProc(BiConsumer<LightCommandRequest.Builder, Float> valueSetter, BiConsumer<LightCommandRequest.Builder, Boolean> hasValueMethod) {
        return new DefaultCommandProcessor<>(valueSetter, hasValueMethod, OHTypeUtils::commandAsFloat) {
        };
    }

    private static CommandProcessor<Integer> intProc(BiConsumer<LightCommandRequest.Builder, Integer> valueSetter, BiConsumer<LightCommandRequest.Builder, Boolean> hasValueMethod) {
        return new DefaultCommandProcessor<>(valueSetter, hasValueMethod, OHTypeUtils::commandAsInt) {
        };
    }

    private static CommandProcessor<Float> clampedFloatProc(BiConsumer<LightCommandRequest.Builder, Float> valueSetter, BiConsumer<LightCommandRequest.Builder, Boolean> hasValueMethod) {
        return new DefaultCommandProcessor<>(valueSetter, hasValueMethod, command -> commandAsFloat(command).map(CommandProcessor::clamp01)) {
        };
    }

    private static float clamp01(float val) {
        return Math.max(0.0f, Math.min(1.0f, val));
    }
}
