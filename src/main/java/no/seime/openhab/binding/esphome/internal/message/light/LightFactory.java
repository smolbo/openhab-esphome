package no.seime.openhab.binding.esphome.internal.message.light;

import io.esphome.api.ListEntitiesLightResponse;
import no.seime.openhab.binding.esphome.internal.handler.ESPChannelTypeProvider;
import org.openhab.core.thing.ThingUID;

/**
 * TODO:
 * 1. raw channels exposed, as reported by device and color mode supported (done)
 * 2. complex channels types exposed:
 *       Switch -> [ON_OFF]                                                                  // ON_OFF only capability
 *       Dimmer -> [ON_OFF, BRIGHTNESS]                                                      // MASTER_BRIGHTNESS capability
 *       ColorTemperature + Dimmer -> [ON_OFF, MASTER_BRIGHTNESS, COLOR_TEMPERATURE]         // COLOR_TEMPERATURE capability
 *       ColorTemperature + Dimmer -> [ON_OFF, MASTER_BRIGHTNESS, COLD_WHITE, WARM_WHITE]    // COLD_WARM_WHITE capability
 *       Color -> [ON_OFF, COLOR_BRIGHTNESS, R, G, B]                                        // RGB capability (thimk about master/color brightness)
 * 3. Super complex:
 *      HSBColor -> get xyY, transform xy to gamuts -> [RGB] or [RGBW(D65)] or [RGB WW CW] + brightness
 *        where:
 *           RGB - standard RGB gamut (normalize RGB) + brightness
 *           RGBW - split into gamuts RGW, RBW, GBW, find actual gamut, normalize + brightness
 *           RGB WW CW - TBD, probably even more gamuts (RGWw, GBCw, RBCwWw, GCwWw) + brightness
 *             improve: add there actual relative intensities
 */


public class LightFactory {

    private final LightChannelTypeFactory channelTypeFactory;

    public LightFactory(ESPChannelTypeProvider channelTypeProvider) {
        channelTypeFactory = new LightChannelTypeFactory(channelTypeProvider);
    }

    public Light createLight(ThingUID thingId, ListEntitiesLightResponse rsp) {
        var lightBuilder = new Light.Builder(rsp);

        for (var channelType : LightChannelDef.getModeIndependentChannels()) {
            lightBuilder.getChannelBuilder(channelType);
        }

        for (ColorMode mode : rsp.getSupportedColorModesList().stream().map(ColorMode::decodeFromBitMask).toList()) {
            for (var capability : mode.getCapabilities()) {
                for (var espChannelType : capability.getChannels()) {

                    lightBuilder.addChannel(espChannelType).withSupportedMode(mode);

                }
            }
        }
        return lightBuilder.build(thingId, channelTypeFactory);
    }
}
