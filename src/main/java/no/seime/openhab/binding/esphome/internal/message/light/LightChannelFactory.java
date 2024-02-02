package no.seime.openhab.binding.esphome.internal.message.light;

import no.seime.openhab.binding.esphome.internal.BindingConstants;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class LightChannelFactory {

    enum LightChannelType {
        ON_OFF,
        MASTER_BRIGHTNESS,
        COLOR_MODE,
        COLOR_BRIGHTNESS,
        RED,
        GREEN,
        BLUE,
        WHITE,
        COLOR_TEMPERATURE,
        COLD_WHITE,
        WARM_WHITE,
        TRANSITION_LENGTH,
        FLASH_LENGTH,
        EFFECT
    }

    private Map<LightChannelType, ChannelType> lightChannelTypes = new HashMap<>();

    LightChannelFactory() {
        lightChannelTypes.put(LightChannelType.ON_OFF, DefaultSystemChannelTypeProvider.SYSTEM_POWER);
        lightChannelTypes.put(LightChannelType.MASTER_BRIGHTNESS, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
        lightChannelTypes.put(LightChannelType.COLOR_MODE, null );//todo
        lightChannelTypes.put(LightChannelType.COLOR_BRIGHTNESS, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
        lightChannelTypes.put(LightChannelType.RED, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
        lightChannelTypes.put(LightChannelType.GREEN, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
        lightChannelTypes.put(LightChannelType.BLUE, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
        lightChannelTypes.put(LightChannelType.WHITE, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
        lightChannelTypes.put(LightChannelType.COLOR_TEMPERATURE, DefaultSystemChannelTypeProvider.SYSTEM_COLOR_TEMPERATURE_ABS);
        lightChannelTypes.put(LightChannelType.COLD_WHITE, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
        lightChannelTypes.put(LightChannelType.WARM_WHITE, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
        lightChannelTypes.put(LightChannelType.TRANSITION_LENGTH, null); //todo
        lightChannelTypes.put(LightChannelType.FLASH_LENGTH, DefaultSystemChannelTypeProvider.SYSTEM_POWER);
        lightChannelTypes.put(LightChannelType.EFFECT, DefaultSystemChannelTypeProvider.SYSTEM_POWER);
    }

    private ChannelTypeUID createTypeUid(LightChannelType type) {
        String str = BindingConstants.BINDING_ID + ChannelTypeUID.SEPARATOR + "light" + ChannelTypeUID.SEPARATOR + type.name().toLowerCase();
        return new ChannelTypeUID(str);
    }

//    public List<Channel> createChannels(ColorMode mode) {
//
//    }
//
//    public List<Channel> createChannels(ColorCapability capability) {
//        switch (capability) {
//            case RGB -> color()
//        }
//    }
//
//    private Channel color() {
//
//    }
}
