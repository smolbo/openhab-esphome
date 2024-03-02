package no.seime.openhab.binding.esphome.internal.message.light;

import static no.seime.openhab.binding.esphome.internal.util.OHTypeUtils.commandAsFloat;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.esphome.api.LightStateResponse;
import org.apache.directory.api.util.Strings;
import org.openhab.core.types.Command;

import io.esphome.api.LightCommandRequest;
import no.seime.openhab.binding.esphome.internal.util.OHTypeUtils;

public interface CommandProcessor<ValueType> {

    Optional<ValueType> handleCommand(LightCommandRequest.Builder builder, Command command);

    Optional<ValueType> getValue(Command command);

    Optional<ValueType> getState(LightStateResponse rsp);


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    class DefaultCommandProcessor<ValueType> implements CommandProcessor<ValueType> {
        private final BiConsumer<LightCommandRequest.Builder, ValueType> valueSetter;
        private final BiConsumer<LightCommandRequest.Builder, Boolean> hasValueMethod;
        private final Function<Command, Optional<ValueType>> commandTransform;

        private final Optional<Function<LightStateResponse, ValueType>> stateTransform;

        protected DefaultCommandProcessor(BiConsumer<LightCommandRequest.Builder, ValueType> valueSetter,
                                          BiConsumer<LightCommandRequest.Builder, Boolean> hasValueMethod,
                                          Function<Command, Optional<ValueType>> commandTransform,
                                          Optional<Function<LightStateResponse, ValueType>> stateTransform) {
            this.valueSetter = valueSetter;
            this.hasValueMethod = hasValueMethod;
            this.commandTransform = commandTransform;
            this.stateTransform = stateTransform;
        }

        protected DefaultCommandProcessor(BiConsumer<LightCommandRequest.Builder, ValueType> valueSetter,
                                          BiConsumer<LightCommandRequest.Builder, Boolean> hasValueMethod,
                                          Function<Command, Optional<ValueType>> commandTransform,
                                          Function<LightStateResponse, ValueType> stateTransform) {
            this(valueSetter, hasValueMethod, commandTransform, Optional.of(stateTransform));
        }

        protected DefaultCommandProcessor(BiConsumer<LightCommandRequest.Builder, ValueType> valueSetter,
                                          BiConsumer<LightCommandRequest.Builder, Boolean> hasValueMethod,
                                          Function<Command, Optional<ValueType>> commandTransform) {
            this(valueSetter, hasValueMethod, commandTransform, Optional.empty());
        }

        @Override
        public Optional<ValueType> handleCommand(LightCommandRequest.Builder builder, Command command) {
            getValue(command).ifPresent(valueType -> {
                valueSetter.accept(builder, valueType);
                hasValueMethod.accept(builder, true);
            });
            return getValue(command);
        }

        @Override
        public Optional<ValueType> getValue(Command command) {
            return commandTransform.apply(command);
        }

        @Override
        public Optional<ValueType> getState(LightStateResponse rsp) {
            return stateTransform.map(f -> f.apply(rsp));
        }


    }

    CommandProcessor<Integer> COLOR_MODE = new DefaultCommandProcessor<>(LightCommandRequest.Builder::setColorMode,
            LightCommandRequest.Builder::setHasColorMode,
            command -> ColorMode.parseSafe(command.toString()).map(ColorMode::getBitMask),
            LightStateResponse::getColorMode
            ) {
    };

    CommandProcessor<Boolean> ON_OFF = new DefaultCommandProcessor<>(LightCommandRequest.Builder::setState,
            LightCommandRequest.Builder::setHasState, OHTypeUtils::commandAsBoolean,
            LightStateResponse::getState) {
    };

    CommandProcessor<Float> MASTER_BRIGHTNESS = clampedFloatProc(LightCommandRequest.Builder::setBrightness,
            LightCommandRequest.Builder::setHasBrightness, LightStateResponse::getBrightness);

    CommandProcessor<Float> COLOR_BRIGHTNESS = clampedFloatProc(LightCommandRequest.Builder::setColorBrightness,
            LightCommandRequest.Builder::setHasColorBrightness, LightStateResponse::getColorBrightness);

    CommandProcessor<Float> RED = clampedFloatProc(LightCommandRequest.Builder::setRed,
            LightCommandRequest.Builder::setHasRgb, LightStateResponse::getRed);

    CommandProcessor<Float> GREEN = clampedFloatProc(LightCommandRequest.Builder::setGreen,
            LightCommandRequest.Builder::setHasRgb, LightStateResponse::getGreen);

    CommandProcessor<Float> BLUE = clampedFloatProc(LightCommandRequest.Builder::setBlue,
            LightCommandRequest.Builder::setHasRgb, LightStateResponse::getBlue);

    CommandProcessor<Float> WHITE = clampedFloatProc(LightCommandRequest.Builder::setWhite,
            LightCommandRequest.Builder::setHasWhite, LightStateResponse::getWhite);

    CommandProcessor<Float> COLD_WHITE = clampedFloatProc(LightCommandRequest.Builder::setColdWhite,
            LightCommandRequest.Builder::setHasColdWhite, LightStateResponse::getColdWhite);

    CommandProcessor<Float> WARM_WHITE = clampedFloatProc(LightCommandRequest.Builder::setWarmWhite,
            LightCommandRequest.Builder::setHasWarmWhite, LightStateResponse::getWarmWhite);

    CommandProcessor<Float> COLOR_TEMPERATURE = floatProc(LightCommandRequest.Builder::setColorTemperature,
            LightCommandRequest.Builder::setHasColorTemperature, LightStateResponse::getColorTemperature);

    CommandProcessor<Integer> TRANSITION_LENGTH = intProcNoState(LightCommandRequest.Builder::setTransitionLength,
            LightCommandRequest.Builder::setHasTransitionLength);

    CommandProcessor<Integer> FLASH_LENGTH = intProcNoState(LightCommandRequest.Builder::setFlashLength,
            LightCommandRequest.Builder::setHasFlashLength);

    CommandProcessor<String> EFFECT = new DefaultCommandProcessor<>(LightCommandRequest.Builder::setEffect,
            LightCommandRequest.Builder::setHasEffect,
            command -> Optional.of(command.toString()).filter(Strings::isNotEmpty), LightStateResponse::getEffect) {
    };

    private static CommandProcessor<Float> floatProc(BiConsumer<LightCommandRequest.Builder, Float> valueSetter,
            BiConsumer<LightCommandRequest.Builder, Boolean> hasValueMethod,
                                                     Function<LightStateResponse, Float> stateTransform) {
        return new DefaultCommandProcessor<>(valueSetter, hasValueMethod, OHTypeUtils::commandAsFloat, stateTransform) {
        };
    }

    private static CommandProcessor<Integer> intProcNoState(BiConsumer<LightCommandRequest.Builder, Integer> valueSetter,
                                                            BiConsumer<LightCommandRequest.Builder, Boolean> hasValueMethod) {
        return new DefaultCommandProcessor<>(valueSetter, hasValueMethod, OHTypeUtils::commandAsInt) {
        };
    }


    private static CommandProcessor<Float> clampedFloatProc(BiConsumer<LightCommandRequest.Builder, Float> valueSetter,
            BiConsumer<LightCommandRequest.Builder, Boolean> hasValueMethod,
                                                            Function<LightStateResponse, Float> stateTransform) {
        return new DefaultCommandProcessor<>(valueSetter, hasValueMethod,
                command -> commandAsFloat(command).map(CommandProcessor::clamp01), stateTransform) {
        };
    }

    private static float clamp01(float val) {
        return Math.max(0.0f, Math.min(1.0f, val));
    }
}
