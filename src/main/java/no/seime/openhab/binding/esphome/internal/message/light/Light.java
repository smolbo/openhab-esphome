package no.seime.openhab.binding.esphome.internal.message.light;

import io.esphome.api.LightCommandRequest;
import io.esphome.api.LightStateResponse;
import io.esphome.api.ListEntitiesLightResponse;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Light {

    private static final Logger log = LoggerFactory.getLogger(Light.class);

    public static final String LIGHT_CHANNEL_CONFIG_COLOR_MODE = "esphome.light.colorMode";
    private final Map<ChannelUID, LightChannel> allChannelsById;
    private final Map<LightChannelDef, LightChannel> channelsByType;

    private final Map<ColorMode, ColorModeChannels> channelsByMode;

    private final String objectId;
    private final int key;
    private final String objectUniqueId;

    private Light(ListEntitiesLightResponse rsp, Collection<LightChannel> channels) {
        objectId = rsp.getObjectId();
        key = rsp.getKey();
        objectUniqueId = rsp.getUniqueId();

        allChannelsById = channels.stream().collect(Collectors.toUnmodifiableMap(LightChannel::getUID, ch -> ch));
        channelsByType = channels.stream().collect(Collectors.toUnmodifiableMap(LightChannel::getType, ch -> ch));

        channelsByMode = rsp.getSupportedColorModesList().stream()
                .map(ColorMode::decodeFromBitMask)
                .map(mode ->
                        new ColorModeChannels(mode,
                                channels.stream()
                                        .filter(ch -> ch.isEnabled(mode))
                                        .collect(Collectors.toList())
                        )

                ).collect(Collectors.toUnmodifiableMap(
                        ColorModeChannels::getMode,
                        cmChannels -> cmChannels
                ));
    }

    public String getObjectId() {
        return objectId;
    }

    public int getKey() {
        return key;
    }

    public String getObjectUniqueId() {
        return objectUniqueId;
    }

    public Collection<LightChannel> getAllChannels() {
        return allChannelsById.values();
    }

    public Optional<LightChannel> getChannelByUid(ChannelUID uid) {
        return Optional.ofNullable(allChannelsById.get(uid));
    }

    public ColorModeChannels getForColorMode(ColorMode mode) {
        return channelsByMode.get(mode);
    }

    public void handleState(LightStateResponse rsp, BiConsumer<ChannelUID, State> stateUpdater) {
        var newMode = ColorMode.decodeFromBitMask(rsp.getColorMode());
        var modeString = new StringType(newMode.name());
        Optional.ofNullable(channelsByType.get(LightChannelDef.COLOR_MODE)).ifPresentOrElse(
                lightChannel -> {
                    stateUpdater.accept(lightChannel.getUID(), modeString);
                },
                () -> log.error("[esphome:light {} ]: channel {} not found", logId(), LightChannelDef.COLOR_MODE)
        );

        handleChannelState(stateUpdater, LightChannelDef.COLOR_TEMPERATURE, rsp.getColorTemperature());

        handleChannelState(stateUpdater, LightChannelDef.WHITE, rsp.getWhite());
        handleChannelState(stateUpdater, LightChannelDef.WARM_WHITE, rsp.getWarmWhite());
        handleChannelState(stateUpdater, LightChannelDef.COLD_WHITE, rsp.getColdWhite());

        handleChannelState(stateUpdater, LightChannelDef.RED, rsp.getRed());
        handleChannelState(stateUpdater, LightChannelDef.GREEN, rsp.getGreen());
        handleChannelState(stateUpdater, LightChannelDef.BLUE, rsp.getBlue());

        handleChannelState(stateUpdater, LightChannelDef.MASTER_BRIGHTNESS, rsp.getBrightness());
        handleChannelState(stateUpdater, LightChannelDef.COLOR_BRIGHTNESS, rsp.getColorBrightness());
        handleChannelState(stateUpdater, LightChannelDef.ON_OFF, rsp.getState() ? 0 : 1);
    }

    private void handleChannelState(BiConsumer<ChannelUID, State> stateUpdater, LightChannelDef type, float state) {
        Optional.ofNullable(channelsByType.get(type)).ifPresentOrElse(
                ch -> stateUpdater.accept(ch.getUID(), new DecimalType(BigDecimal.valueOf(state))),
                () -> log.error("{}: no channel found for type {}", logId(), type)
        );
    }

    public String logId() {
        return this.getKey() + "/" + this.objectUniqueId;
    }

    public void handleCommand(Channel channel, Command command, Consumer<LightCommandRequest> sender) {
        getChannelByUid(channel.getUID()).ifPresent(
                lightChannel -> {
                    var commandBuilder = LightCommandRequest.newBuilder()
                            .setKey(this.key);
                    lightChannel.handleCommand(commandBuilder, command);
                    sender.accept(commandBuilder.build());
                }
        );

    }

    private static class ColorModeChannels {
        private final ColorMode mode;
        private final Map<ChannelUID, LightChannel> channelsByUid;
        private final Map<LightChannelDef, LightChannel> channelsByType;

        public ColorModeChannels(ColorMode mode, Collection<LightChannel> channels) {
            this.mode = mode;
            channelsByUid = channels.stream().collect(Collectors.toUnmodifiableMap(LightChannel::getUID, ch -> ch));
            channelsByType = channels.stream().collect(Collectors.toUnmodifiableMap(LightChannel::getType, ch -> ch));
        }

        public ColorMode getMode() {
            return mode;
        }

        public Optional<LightChannel> get(ChannelUID uid) {
            return Optional.ofNullable(channelsByUid.get(uid));
        }

        public Optional<LightChannel> get(LightChannelDef type) {
            return Optional.ofNullable(channelsByType.get(type));
        }
    }

    public static class Builder {
        private final ListEntitiesLightResponse rsp;
        private final Map<LightChannelDef, LightChannel.Builder> channelBuilders = new HashMap<>();

        public Builder(ListEntitiesLightResponse rsp) {
            this.rsp = rsp;
        }

        public LightChannel.Builder addChannel(LightChannelDef espType) {
            return getChannelBuilder(espType);
        }

        public LightChannel.Builder getChannelBuilder(LightChannelDef espType) {
            return channelBuilders.computeIfAbsent(espType, k -> new LightChannel.Builder(rsp.getKey(), espType));
        }

        public Light build(ThingUID thingUID, LightChannelTypeFactory ohTypeFactory) {
            return new Light(
                    rsp,
                    channelBuilders.values().stream()
                            .map(chBuilder -> chBuilder.build(thingUID, ohTypeFactory, rsp))
                            .collect(Collectors.toList())
            );
        }
    }
}
