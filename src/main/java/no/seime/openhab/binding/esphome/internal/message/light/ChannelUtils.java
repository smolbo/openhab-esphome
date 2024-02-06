package no.seime.openhab.binding.esphome.internal.message.light;

import com.google.common.base.Strings;
import no.seime.openhab.binding.esphome.internal.BindingConstants;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.UID;
import org.openhab.core.thing.type.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChannelUtils {
    public static ChannelTypeUID extendSystemChanelTypeUid(ChannelTypeUID base, String extendType, String... subtypes) {
        return new ChannelTypeUID(base.getId()
                + UID.SEPARATOR + extendType
                + Stream.of(subtypes).collect(Collectors.joining(UID.SEPARATOR, UID.SEPARATOR, "")));
    }

    public static StateChannelTypeBuilder extendSystemStateChannelType(ChannelType base, LightChannelFactory.LightChannelType type, @Nullable String... subtypes) {
        var channelName = type.name().toLowerCase();

        return ChannelTypeBuilder.state(
                        extendSystemChanelTypeUid(base.getUID(), channelName, subtypes),
                        base.getLabel() + ", " + channelName,
                        Strings.nullToEmpty(base.getItemType())
                )
                .withAutoUpdatePolicy(AutoUpdatePolicy.VETO)
                .withTags(base.getTags())
                .withCategory(Strings.nullToEmpty(base.getCategory()))
                .withDescription(Strings.nullToEmpty(base.getDescription()) + ", " + channelName)
                .withCommandDescription(base.getCommandDescription());
    }

    public static ChannelTypeUID createStaticChannelTypeId(String moduleName, LightChannelFactory.LightChannelType type, @Nullable String subtype) {
        var typenameSb = new StringBuilder(BindingConstants.BINDING_ID)
                .append(UID.SEPARATOR)
                .append(moduleName)
                .append(UID.SEPARATOR)
                .append(type.name().toLowerCase());

        if (!Strings.isNullOrEmpty(subtype)) {
            typenameSb.append(UID.SEPARATOR).append(subtype);
        }

        return new ChannelTypeUID(typenameSb.toString());
    }

}
