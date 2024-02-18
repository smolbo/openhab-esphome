package no.seime.openhab.binding.esphome.internal.message.light;

import io.esphome.api.LightCommandRequest;
import io.esphome.api.ListEntitiesLightResponse;
import no.seime.openhab.binding.esphome.internal.BindingConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.UID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static no.seime.openhab.binding.esphome.internal.util.Utils.safeIdFromName;

public class LightChannel {
    private final Channel channel;

    private final Set<ColorMode> enabledInColorModes;
    private final LightChannelDef type;
    private final ChannelTypeUID typeUid;

    private LightChannel(Channel channel, LightChannelDef type, ChannelTypeUID typeUid, Set<ColorMode> colorModes) {
        this.channel = channel;
        this.type = type;
        this.typeUid = typeUid;
        this.enabledInColorModes = colorModes;
    }

    public boolean isEnabled(ColorMode mode) {
        return enabledInColorModes.isEmpty() || enabledInColorModes.contains(mode);
    }

    public ChannelUID getUID() {
        return channel.getUID();
    }

    public Channel getChannel() {
        return channel;
    }

    public LightChannelDef getType() {
        return type;
    }

    public ChannelTypeUID getTypeUid() {
        return typeUid;
    }

    public LightChannelGroup getGroup() {
        return type.getGroup();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightChannel that = (LightChannel) o;
        return Objects.equals(channel, that.channel) && type == that.type && Objects.equals(typeUid, that.typeUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, type, typeUid);
    }

    @Override
    public String toString() {
        return "LightChannelWrapper{" +
                "channel=" + channel +
                ", type=" + type +
                ", typeUid=" + typeUid +
                '}';
    }

    public void handleCommand(LightCommandRequest.Builder commandBuilder, Command command) {
        type.getCommandProcessor().handleCommand(commandBuilder, command);
    }

    public static class Builder {
        private final LightChannelDef espChannelType;
        private final Configuration channelConfig = new Configuration();

        private final Set<ColorMode> modeList = new HashSet<>();

        public Builder(int lightObjectKey, LightChannelDef espChannelType) {
            this.espChannelType = espChannelType;
            channelConfig.put(BindingConstants.COMMAND_KEY, lightObjectKey);
            channelConfig.put(BindingConstants.COMMAND_FIELD, espChannelType);
            channelConfig.put(BindingConstants.COMMAND_CLASS, "Light");
        }

        public Builder withSupportedMode(ColorMode mode) {
            modeList.add(mode);
            return this;
        }

        LightChannel build(ThingUID thingUID, LightChannelTypeFactory ohTypeFactory, ListEntitiesLightResponse rsp) {
            var ohChannelType = ohTypeFactory.getChannelType(espChannelType, rsp);

            if (!modeList.isEmpty()) {
                channelConfig.put(
                        Light.LIGHT_CHANNEL_CONFIG_COLOR_MODE,
                        modeList.stream()
                                .map(cm -> cm.name().toLowerCase())
                                .collect(Collectors.joining(", "))
                );
            }

            String channelId = "light" +
                    UID.SEPARATOR +
                    safeIdFromName(rsp.getObjectId()) +
                    UID.SEPARATOR +
                    espChannelType.getGroup().name().toLowerCase() +
                    ChannelUID.CHANNEL_GROUP_SEPARATOR +
                    espChannelType.name().toLowerCase();

            var channelInstance = ChannelBuilder.create(
                            new ChannelUID(
                                    thingUID,
                                    channelId
                            )
                    )
                    .withLabel(rsp.getName() + " " + espChannelType.name().toLowerCase())
                    .withKind(ChannelKind.STATE)
                    .withType(ohChannelType.getUID())
                    .withAcceptedItemType(ohChannelType.getItemType())
                    .withConfiguration(channelConfig)
                    .build();

            return new LightChannel(channelInstance, espChannelType, ohChannelType.getUID(), modeList);
        }
    }
}
