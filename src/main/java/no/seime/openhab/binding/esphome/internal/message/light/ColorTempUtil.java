package no.seime.openhab.binding.esphome.internal.message.light;

public class ColorTempUtil {
    private static final float MIRED_BASE = 1_000_000f; // K

    public static float miredToKelvin(float mired) {
        // https://en.wikipedia.org/wiki/Mired
        return MIRED_BASE / mired;
    }

    public static float kelvinToMired(float kelvin) {
        return MIRED_BASE / kelvin;
    }

    public static String formatKelvin(float colorTemperature) {
        return String.format("%.0f", colorTemperature);
    }
}
