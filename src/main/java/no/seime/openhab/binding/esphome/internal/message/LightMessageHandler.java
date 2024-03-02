package no.seime.openhab.binding.esphome.internal.message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.core.thing.Channel;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.esphome.api.LightCommandRequest;
import io.esphome.api.LightStateResponse;
import io.esphome.api.ListEntitiesLightResponse;
import no.seime.openhab.binding.esphome.internal.comm.ProtocolAPIError;
import no.seime.openhab.binding.esphome.internal.handler.ESPHomeHandler;
import no.seime.openhab.binding.esphome.internal.message.light.Light;
import no.seime.openhab.binding.esphome.internal.message.light.LightChannel;
import no.seime.openhab.binding.esphome.internal.message.light.LightFactory;

public class LightMessageHandler extends AbstractMessageHandler<ListEntitiesLightResponse, LightStateResponse> {

    private static final Logger logger = LoggerFactory.getLogger(LightMessageHandler.class);
    private final LightFactory lightFactory;
    private final Map<Integer, Light> lightsByKey = new ConcurrentHashMap<>();

    public LightMessageHandler(ESPHomeHandler handler, LightFactory lightFactory) {
        super(handler);
        this.lightFactory = lightFactory;
    }

    @Override
    public void handleCommand(Channel channel, Command command, int key) throws ProtocolAPIError {
        // TODO must figure out the HA light component modes and how to map them to openhab
        logger.warn(
                "Unhandled command {} for channel {} - in fact the Light component isn't really implemented yet. Contribution needed",
                command, channel);
        // handler.sendMessage(LightCommandRequest.newBuilder().setKey(key).setState().build());

        getLight(key).ifPresent(light -> light.handleCommand(channel, command, this::sendMessage));
    }

    private void sendMessage(LightCommandRequest req) {
        try {
            logger.debug("esphome:light: Sending message {}", req);
            handler.sendMessage(req);
        } catch (ProtocolAPIError e) {
            logger.error("Failed to send climate command for key {} / {}", req.getKey(),
                    getLight(req.getKey()).map(Light::logId), e);
        }
    }

    public void buildChannels(ListEntitiesLightResponse rsp) {

        var light = lightFactory.createLight(handler.getThing().getUID(), rsp);
        lightsByKey.put(light.getKey(), light);
        light.getAllChannels().stream().map(LightChannel::getChannel).forEach(handler::addChannel);
    }

    public void handleState(LightStateResponse rsp) {
        // TODO must figure out the HA light component modes and how to map them to openhab
        // logger.warn(
        // "Unhandled state for esp light {} - in fact the Light component isn't really implemented yet. Contribution
        // needed",
        // rsp.getKey());
        // channel -> handler.updateState(channel.getUID(), HSBType.fromRGB(rsp.getRed(),rsp.getGreen(),rsp.getBlue()));

        getLight(rsp.getKey()).ifPresent(light -> light.handleState(rsp, handler::updateState));
    }

    private Optional<Light> getLight(int lightObjectKey) {
        var ret = Optional.ofNullable(lightsByKey.get(lightObjectKey));

        ret.ifPresentOrElse(light -> logger.trace("[esphome:light] Got light: {} / {}", lightObjectKey, light.logId()),
                () -> logger.warn("[esphome:light] light {} is unknown - not added (yet?)", lightObjectKey));

        return ret;
    }
}
