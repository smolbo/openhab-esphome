package no.seime.openhab.binding.esphome.internal.message.light;

import org.openhab.core.thing.ThingUID;

import io.esphome.api.ListEntitiesLightResponse;
import no.seime.openhab.binding.esphome.internal.handler.ESPChannelTypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO:
 * 1. raw channels exposed, as reported by device and color mode supported (done)
 * 2. complex channels types exposed:
 * Switch -> [ON_OFF] // ON_OFF only capability
 * Dimmer -> [ON_OFF, BRIGHTNESS] // MASTER_BRIGHTNESS capability
 * ColorTemperature + Dimmer -> [ON_OFF, MASTER_BRIGHTNESS, COLOR_TEMPERATURE] // COLOR_TEMPERATURE capability
 * ColorTemperature + Dimmer -> [ON_OFF, MASTER_BRIGHTNESS, COLD_WHITE, WARM_WHITE] // COLD_WARM_WHITE capability
 * Color -> [ON_OFF, COLOR_BRIGHTNESS, R, G, B] // RGB capability (thimk about master/color brightness)
 * 3. Super complex:
 * HSBColor -> get xyY, transform xy to gamuts -> [RGB] or [RGBW(D65)] or [RGB WW CW] + brightness
 * where:
 * RGB - standard RGB gamut (normalize RGB) + brightness
 * RGBW - split into gamuts RGW, RBW, GBW, find actual gamut, normalize + brightness
 * RGB WW CW - TBD, probably even more gamuts (RGWw, GBCw, RBCwWw, GCwWw) + brightness
 * improve: add there actual relative intensities
 */

public class LightFactory {
    private static final Logger log = LoggerFactory.getLogger(LightFactory.class);

    private final LightChannelTypeFactory channelTypeFactory;

    public LightFactory(ESPChannelTypeProvider channelTypeProvider) {
        channelTypeFactory = new LightChannelTypeFactory(channelTypeProvider);
    }

    public Light createLight(ThingUID thingId, ListEntitiesLightResponse rsp) {
        var lightBuilder = new Light.Builder(rsp);

        log.debug("Building light {}", rsp.getName());

        for (var channelType : LightChannelDef.getModeIndependentChannels()) {
            log.debug("  Building light {} channel: {}", rsp.getName(), channelType);

            lightBuilder.getChannelBuilder(channelType);
        }

        for (ColorMode mode : rsp.getSupportedColorModesList().stream().map(ColorMode::decodeFromBitMask).toList()) {
            log.debug("    Building light {}, for mode {} capabilities {}", rsp.getName(), mode, mode.getCapabilities());

            for (var capability : mode.getCapabilities()) {
                for (var espChannelType : capability.getChannels()) {
                    log.debug("      Building light {} cap {} channel: {}", rsp.getName(), capability, espChannelType);
                    lightBuilder.getChannelBuilder(espChannelType).withSupportedMode(mode);
                }
            }
        }
        return lightBuilder.build(thingId, channelTypeFactory);
    }
}
