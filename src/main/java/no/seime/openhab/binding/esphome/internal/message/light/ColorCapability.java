package no.seime.openhab.binding.esphome.internal.message.light;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ColorCapability {
    ON_OFF(1, LightChannelDef.ON_OFF),
    BRIGHTNESS(1 << 1, LightChannelDef.MASTER_BRIGHTNESS),
    WHITE(1 << 2, LightChannelDef.WHITE),
    COLOR_TEMPERATURE(1 << 3, LightChannelDef.COLOR_TEMPERATURE),
    COLD_WARM_WHITE(1 << 4, LightChannelDef.COLD_WHITE, LightChannelDef.WARM_WHITE),
    RGB(1 << 5, LightChannelDef.COLOR_BRIGHTNESS, LightChannelDef.RED, LightChannelDef.GREEN, LightChannelDef.BLUE);

    private final int colorCapabilityBitMask;
    private final Set<LightChannelDef> channels;

    ColorCapability(int bitMask, LightChannelDef... capabilityChannels) {
        this.colorCapabilityBitMask = bitMask;
        this.channels = Stream.of(capabilityChannels).collect(Collectors.toUnmodifiableSet());
    }

    public int getBitMask() {
        return colorCapabilityBitMask;
    }

    public Set<LightChannelDef> getChannels() {
        return channels;
    }
}
