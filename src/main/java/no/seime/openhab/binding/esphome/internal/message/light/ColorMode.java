package no.seime.openhab.binding.esphome.internal.message.light;

import no.seime.openhab.binding.esphome.internal.message.LightMessageHandler;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ColorMode {
    /// No color mode configured (cannot be a supported mode, only active when light is off).
    UNKNOWN(),
    /// Only on/off control.
    ON_OFF(
            ColorCapability.ON_OFF
    ),
    /// Dimmable light.
    BRIGHTNESS(
            ColorCapability.ON_OFF,
            ColorCapability.BRIGHTNESS
    ),
    /// White output only (use only if the light also has another color mode such as RGB).
    WHITE(
            ColorCapability.ON_OFF,
            ColorCapability.BRIGHTNESS,
            ColorCapability.WHITE
    ),
    /// Controllable color temperature output.
    COLOR_TEMPERATURE(
            ColorCapability.ON_OFF,
            ColorCapability.BRIGHTNESS,
            ColorCapability.COLOR_TEMPERATURE
    ),
    /// Cold and warm white output with individually controllable brightness.
    COLD_WARM_WHITE(
            ColorCapability.ON_OFF,
            ColorCapability.BRIGHTNESS,
            ColorCapability.COLD_WARM_WHITE
    ),
    /// RGB color output.
    RGB(
            ColorCapability.ON_OFF,
            ColorCapability.BRIGHTNESS,
            ColorCapability.RGB
    ),
    /// RGB color output and a separate white output.
    RGB_WHITE(
            ColorCapability.ON_OFF,
            ColorCapability.BRIGHTNESS,
            ColorCapability.RGB,
            ColorCapability.WHITE
    ),
    /// RGB color output and a separate white output with controllable color temperature.
    RGB_COLOR_TEMPERATURE(
            ColorCapability.ON_OFF,
            ColorCapability.BRIGHTNESS,
            ColorCapability.RGB,
            ColorCapability.WHITE,
            ColorCapability.COLOR_TEMPERATURE
    ),
    /// RGB color output, and separate cold and warm white outputs.
    RGB_COLD_WARM_WHITE(
            ColorCapability.ON_OFF,
            ColorCapability.BRIGHTNESS,
            ColorCapability.RGB,
            ColorCapability.COLD_WARM_WHITE
    );

    private final int modeBitMask;
    private final SortedSet<ColorCapability> capabilities;

    private final static Map<Integer, ColorMode> maskToModeMap;

    static {
        maskToModeMap = Stream.of(values()).collect(Collectors.toUnmodifiableMap(
                mode -> mode.modeBitMask,
                mode -> mode
        ));
    }

    ColorMode(ColorCapability... capabilities) {
        int resultMask = 0;
        final TreeSet<ColorCapability> capsSet = new TreeSet<>(Comparator.comparing(ColorCapability::getBitMask));
        for (ColorCapability cap : capabilities) {
            resultMask |= cap.getBitMask();
            capsSet.add(cap);
        }
        this.modeBitMask = resultMask;
        this.capabilities = Collections.unmodifiableSortedSet(capsSet);
    }

    public int getBitMask() {
        return modeBitMask;
    }

    public SortedSet<ColorCapability> getCapabilities() {
        return capabilities;
    }

    public static ColorMode decodeFromBitMask(int bitMask) {
        ColorMode known = maskToModeMap.get(bitMask);
        return known != null ? known : UNKNOWN;
    }
}
