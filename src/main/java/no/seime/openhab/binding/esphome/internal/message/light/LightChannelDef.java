package no.seime.openhab.binding.esphome.internal.message.light;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum LightChannelDef {
    ON_OFF(false, LightChannelType.ON_OFF, LightChannelGroup.RAW_STATE, CommandProcessor.ON_OFF, LightChannelTypeProvider.ON_OFF),
    MASTER_BRIGHTNESS(true, LightChannelType.MASTER_BRIGHTNESS, LightChannelGroup.RAW_STATE, CommandProcessor.MASTER_BRIGHTNESS,
            LightChannelTypeProvider.BRIGHTNESS),
    COLOR_MODE(false, LightChannelType.COLOR_MODE, LightChannelGroup.RAW_STATE, CommandProcessor.COLOR_MODE, LightChannelTypeProvider.COLOR_MODE),
    COLOR_BRIGHTNESS(true, LightChannelType.COLOR_BRIGHTNESS, LightChannelGroup.RAW_STATE, CommandProcessor.COLOR_BRIGHTNESS,
            LightChannelTypeProvider.BRIGHTNESS),
    RED(true, LightChannelType.RED, LightChannelGroup.RAW_STATE, CommandProcessor.RED, LightChannelTypeProvider.RGBW),
    GREEN(true, LightChannelType.GREEN, LightChannelGroup.RAW_STATE, CommandProcessor.GREEN, LightChannelTypeProvider.RGBW),
    BLUE(true, LightChannelType.BLUE, LightChannelGroup.RAW_STATE, CommandProcessor.BLUE, LightChannelTypeProvider.RGBW),
    WHITE(true, LightChannelType.WHITE, LightChannelGroup.RAW_STATE, CommandProcessor.WHITE, LightChannelTypeProvider.RGBW),
    COLOR_TEMPERATURE(true, LightChannelType.COLOR_TEMPERATURE, LightChannelGroup.RAW_STATE, CommandProcessor.COLOR_TEMPERATURE,
            LightChannelTypeProvider.COLOR_TEMPERATURE),
    COLD_WHITE(true, LightChannelType.COLD_WHITE, LightChannelGroup.RAW_STATE, CommandProcessor.COLD_WHITE,
            LightChannelTypeProvider.COLD_WARM_WHITE),
    WARM_WHITE(true, LightChannelType.WARM_WHITE, LightChannelGroup.RAW_STATE, CommandProcessor.WARM_WHITE,
            LightChannelTypeProvider.COLD_WARM_WHITE),
    TRANSITION_LENGTH(false, LightChannelType.TRANSITION_LENGTH, LightChannelGroup.RAW_CALL_PARAM, CommandProcessor.TRANSITION_LENGTH,
            LightChannelTypeProvider.DURATION),
    FLASH_LENGTH(false, LightChannelType.FLASH_LENGTH, LightChannelGroup.RAW_CALL_PARAM, CommandProcessor.FLASH_LENGTH,
            LightChannelTypeProvider.DURATION),
    EFFECT(false, LightChannelType.EFFECT, LightChannelGroup.RAW_STATE, CommandProcessor.EFFECT, LightChannelTypeProvider.NOT_IMPLEMENTED);

    private final boolean isModeDependant;
    private final LightChannelType type;
    private final LightChannelGroup group;

    private final CommandProcessor<?> commandProcessor;
    private final LightChannelTypeProvider ohChanelTypeProvider;

    LightChannelDef(boolean isModeDependant, LightChannelType type, LightChannelGroup group, CommandProcessor<?> valueSetter,
                    LightChannelTypeProvider ohChanelTypeProvider) {
        this.isModeDependant = isModeDependant;
        this.type = type;
        this.group = group;
        this.commandProcessor = valueSetter;
        this.ohChanelTypeProvider = ohChanelTypeProvider;
    }

    public boolean isModeDependant() {
        return isModeDependant;
    }

    public static Set<LightChannelDef> getModeIndependentChannels() {
        return modeIndependentChannels;
    }

    public CommandProcessor<?> getCommandProcessor() {
        return commandProcessor;
    }

    private static final Set<LightChannelDef> modeIndependentChannels = Stream.of(values())
            .filter(ch -> !ch.isModeDependant).collect(Collectors.toUnmodifiableSet());

    public LightChannelType getType() {
        return type;
    }

    public LightChannelGroup getGroup() {
        return group;
    }

    public LightChannelTypeProvider getOhChanelTypeProvider() {
        return ohChanelTypeProvider;
    }
}
