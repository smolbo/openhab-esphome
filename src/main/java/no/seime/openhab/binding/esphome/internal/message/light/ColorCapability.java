package no.seime.openhab.binding.esphome.internal.message.light;

import no.seime.openhab.binding.esphome.internal.message.LightMessageHandler;

import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public enum ColorCapability {
    ON_OFF(1),
    BRIGHTNESS(1 << 1),
    WHITE(1 << 2),
    COLOR_TEMPERATURE(1 << 3),
    COLD_WARM_WHITE(1 << 4),
    RGB(1 << 5);
    private final int colorCapabilityBitMask;

    ColorCapability(int bitMask) {
        this.colorCapabilityBitMask = bitMask;
    }

    public int getBitMask() {
        return colorCapabilityBitMask;
    }

    public static SortedSet<ColorCapability> decodeToCapabilities(int bitMask) {
        TreeSet<ColorCapability> caps = new TreeSet<>(Comparator.comparing(ColorCapability::getBitMask));
        for (ColorCapability cap : values()) {
            if ((cap.getBitMask() & bitMask) != 0) {
                caps.add(cap);
            }
        }
        return Collections.unmodifiableSortedSet(caps);
    }
}
