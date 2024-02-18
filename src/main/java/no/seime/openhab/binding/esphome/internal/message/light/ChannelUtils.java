package no.seime.openhab.binding.esphome.internal.message.light;

import com.google.common.base.Strings;
import no.seime.openhab.binding.esphome.internal.BindingConstants;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.UID;
import org.openhab.core.thing.type.*;

import javax.validation.constraints.NotNull;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChannelUtils {
    public static ChannelTypeUID extendSystemChanelTypeUid(ChannelTypeUID base, String extendType, @Nullable String ... subtypes) {
        var subtypesStr = subtypes != null ? Stream.of(subtypes).collect(Collectors.joining(UID.SEPARATOR, UID.SEPARATOR, "")) : "";
        return new ChannelTypeUID(BindingConstants.BINDING_ID,
                base.getId()
                + UID.SEPARATOR + extendType
                + subtypesStr);
    }

    public static StateChannelTypeBuilder extendSystemStateChannelType(ChannelType base, LightChannelDef type, @Nullable String... subtypes) {
        var channelName = type.name().toLowerCase();

        return defaultStateChannelBuilder(
                        extendSystemChanelTypeUid(base.getUID(), channelName, subtypes),
                        base.getLabel() + ", " + channelName,
                        Strings.nullToEmpty(base.getItemType())
                )
                .withTags(base.getTags())
                .withCategory(Strings.nullToEmpty(base.getCategory()))
                .withDescription(Strings.nullToEmpty(base.getDescription()) + ", " + channelName)
                .withCommandDescription(base.getCommandDescription());
    }

    public static ChannelTypeUID createStaticChannelTypeId(String moduleName, LightChannelDef type, @Nullable String subtype) {
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

    public static StateChannelTypeBuilder defaultStateChannelBuilder(ChannelTypeUID typeUid, @NotNull String channelName, @NotNull String itemType) {
        return ChannelTypeBuilder.state(typeUid, channelName, itemType)
                .withAutoUpdatePolicy(AutoUpdatePolicy.VETO);

    }
}
