package no.seime.openhab.binding.esphome.internal.message.light;

import io.esphome.api.ListEntitiesLightResponse;
import no.seime.openhab.binding.esphome.internal.handler.ESPChannelTypeProvider;
import org.openhab.core.thing.type.ChannelType;

public class LightChannelTypeFactory {
    private final ESPChannelTypeProvider channelTypesStorage;

    public LightChannelTypeFactory(ESPChannelTypeProvider channelTypesStorage) {
        this.channelTypesStorage = channelTypesStorage;
    }

    public ChannelType getChannelType(LightChannelDef type, ListEntitiesLightResponse rsp) {
        var typeId = type.getOhChanelTypeProvider().provideChannelTypeId(type, rsp);
        var existingChannelType = channelTypesStorage.getChannelType(typeId, null);

        if(existingChannelType != null) {
            return existingChannelType;
        }

        var ret = type.getOhChanelTypeProvider().provideChannelType(type, rsp);
        channelTypesStorage.putChannelType(ret);
        return ret;
    }
}
