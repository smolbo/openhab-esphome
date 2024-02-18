package no.seime.openhab.binding.esphome.internal.message.light;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum LightChannelDef {
    ON_OFF(false,
            LightChannelGroup.RAW_STATE,
            CommandProcessor.ON_OFF,
            LightChannelTypeProvider.ON_OFF),
    MASTER_BRIGHTNESS(false,
            LightChannelGroup.RAW_STATE,
            CommandProcessor.MASTER_BRIGHTNESS,
            LightChannelTypeProvider.BRIGHTNESS),
    COLOR_MODE(false,
            LightChannelGroup.RAW_STATE,
            CommandProcessor.COLOR_MODE,
            LightChannelTypeProvider.COLOR_MODE),
    COLOR_BRIGHTNESS(true,
            LightChannelGroup.RAW_STATE,
            CommandProcessor.COLOR_BRIGHTNESS,
            LightChannelTypeProvider.BRIGHTNESS),
    RED(true, LightChannelGroup.RAW_STATE,
            CommandProcessor.RED,
            LightChannelTypeProvider.RGBW),
    GREEN(true, LightChannelGroup.RAW_STATE,
            CommandProcessor.GREEN,
            LightChannelTypeProvider.RGBW),
    BLUE(true,
            LightChannelGroup.RAW_STATE,
            CommandProcessor.BLUE,
            LightChannelTypeProvider.RGBW),
    WHITE(true,
            LightChannelGroup.RAW_STATE,
            CommandProcessor.WHITE,
            LightChannelTypeProvider.RGBW),
    COLOR_TEMPERATURE(true,
            LightChannelGroup.RAW_STATE,
            CommandProcessor.COLOR_TEMPERATURE,
            LightChannelTypeProvider.COLOR_TEMPERATURE),
    COLD_WHITE(true,
            LightChannelGroup.RAW_STATE,
            CommandProcessor.COLD_WHITE,
            LightChannelTypeProvider.COLD_WARM_WHITE),
    WARM_WHITE(true,
            LightChannelGroup.RAW_STATE,
            CommandProcessor.WARM_WHITE,
            LightChannelTypeProvider.COLD_WARM_WHITE),
    TRANSITION_LENGTH(false,
            LightChannelGroup.RAW_CALL_PARAM,
            CommandProcessor.TRANSITION_LENGTH,
            LightChannelTypeProvider.DURATION),
    FLASH_LENGTH(false,
            LightChannelGroup.RAW_CALL_PARAM,
            CommandProcessor.FLASH_LENGTH,
            LightChannelTypeProvider.DURATION),
    EFFECT(false,
            LightChannelGroup.RAW_STATE,
            CommandProcessor.EFFECT,
            LightChannelTypeProvider.NOT_IMPLEMENTED);

    private final boolean isModeDependant;
    private final LightChannelGroup group;

    private final CommandProcessor<?> commandProcessor;
    private final LightChannelTypeProvider ohChanelTypeProvider;

    LightChannelDef(
            boolean isModeDependant,
            LightChannelGroup group,
            CommandProcessor<?> valueSetter,
            LightChannelTypeProvider ohChanelTypeProvider
    ) {
        this.isModeDependant = isModeDependant;
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
            .filter(ch -> !ch.isModeDependant)
            .collect(Collectors.toUnmodifiableSet());

    public LightChannelGroup getGroup() {
        return group;
    }

    public LightChannelTypeProvider getOhChanelTypeProvider() {
        return ohChanelTypeProvider;
    }
}
