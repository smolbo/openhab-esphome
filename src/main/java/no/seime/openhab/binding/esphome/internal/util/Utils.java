package no.seime.openhab.binding.esphome.internal.util;

public class Utils {
    public static String safeIdFromName(String objectName) {
        return objectName.replaceAll("[\\s:,.!@#$%^&*()/\\\\<>].*", "_").toLowerCase();
    }
}
