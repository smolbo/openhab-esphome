package no.seime.openhab.binding.esphome.internal.message.light;

import static no.seime.openhab.binding.esphome.internal.message.light.ChannelUtils.defaultStateChannelBuilder;
import static no.seime.openhab.binding.esphome.internal.message.light.ChannelUtils.extendSystemStateChannelType;
import static no.seime.openhab.binding.esphome.internal.message.light.ColorTempUtil.formatKelvin;
import static no.seime.openhab.binding.esphome.internal.message.light.ColorTempUtil.miredToKelvin;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.ItemUtil;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;

import io.esphome.api.ListEntitiesLightResponse;

public abstract class LightChannelTypeProvider {
    private final Set<LightChannelType> supported;

    public LightChannelTypeProvider(LightChannelType... supported) {
        this.supported = Stream.of(supported).collect(Collectors.toUnmodifiableSet());
    }

    public final ChannelType provideChannelType(LightChannelDef channelType, ListEntitiesLightResponse rsp) {
        if (!supported.contains(channelType.getType())) {
            throw new IllegalArgumentException(
                    "Actual channel type " + channelType + " is not supported by " + getClass().getName());
        }
        return provideType(provideChannelTypeId(channelType, rsp), channelType, rsp);
    }

    protected abstract ChannelType provideType(ChannelTypeUID channelTypeUid, LightChannelDef channelType,
            ListEntitiesLightResponse rsp);

    public final ChannelTypeUID provideChannelTypeId(LightChannelDef channelType, ListEntitiesLightResponse rsp) {
        if (!supported.contains(channelType.getType())) {
            throw new IllegalArgumentException(
                    "Actual channel type " + channelType + " is not supported by " + getClass().getName());
        }
        return provideTypeId(channelType, rsp);
    }

    protected abstract ChannelTypeUID provideTypeId(LightChannelDef channelType, ListEntitiesLightResponse rsp);

    public static final LightChannelTypeProvider NOT_IMPLEMENTED = new LightChannelTypeProvider(
            LightChannelType.values()) {
        @Override
        protected ChannelType provideType(ChannelTypeUID channelTypeUid, LightChannelDef channelType,
                ListEntitiesLightResponse rsp) {
            throw new NotImplementedException("Channel type handling is not implemented for " + channelType);
        }

        @Override
        protected ChannelTypeUID provideTypeId(LightChannelDef channelType, ListEntitiesLightResponse rsp) {
            throw new NotImplementedException("Support for " + channelType + " is not implemented");
        }
    };

    public static final LightChannelTypeProvider ON_OFF = new LightChannelTypeProvider(LightChannelType.ON_OFF) {
        @Override
        protected ChannelType provideType(ChannelTypeUID channelTypeUid, LightChannelDef channelType,
                ListEntitiesLightResponse rsp) {
            return DefaultSystemChannelTypeProvider.SYSTEM_POWER;
        }

        @Override
        protected ChannelTypeUID provideTypeId(LightChannelDef channelType, ListEntitiesLightResponse rsp) {
            return DefaultSystemChannelTypeProvider.SYSTEM_POWER.getUID();
        }
    };

    public static final LightChannelTypeProvider COLOR_MODE = new LightChannelTypeProvider(LightChannelType.COLOR_MODE) {
        @Override
        protected ChannelType provideType(ChannelTypeUID channelTypeUid, LightChannelDef channelType,
                ListEntitiesLightResponse rsp) {
            final var modeOptions = rsp.getSupportedColorModesList().stream().map(ColorMode::decodeFromBitMask).sorted()
                    .map(mode -> new StateOption(mode.name(), mode.description())).toList();

            final var stateDescr = StateDescriptionFragmentBuilder.create().withReadOnly(true).withOptions(modeOptions)
                    .build();

            return defaultStateChannelBuilder(channelTypeUid, "Light mode", CoreItemFactory.STRING)
                    .withStateDescriptionFragment(stateDescr).build();
        }

        @Override
        protected ChannelTypeUID provideTypeId(LightChannelDef channelType, ListEntitiesLightResponse rsp) {
            final var modesSetSubtype = rsp.getSupportedColorModesList().stream().map(ColorMode::decodeFromBitMask)
                    .sorted().map(mode -> mode.name().toLowerCase()).collect(Collectors.joining("-"));
            return createStaticChannelTypeId(LightChannelDef.COLOR_MODE, modesSetSubtype);
        }
    };

    public static final LightChannelTypeProvider BRIGHTNESS = new LightChannelTypeProvider(
            LightChannelType.MASTER_BRIGHTNESS, LightChannelType.COLOR_BRIGHTNESS) {
        @Override
        protected ChannelType provideType(ChannelTypeUID channelTypeUid, LightChannelDef channelType,
                ListEntitiesLightResponse rsp) {
            return extendSystemStateChannelType(DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS, channelType)
                    .withStateDescriptionFragment(zeroToOneStateDescr().build()).build();
        }

        @Override
        protected ChannelTypeUID provideTypeId(LightChannelDef channelType, ListEntitiesLightResponse rsp) {
            return extendSystemChanelTypeUid(DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS, channelType);
        }
    };

    public static final LightChannelTypeProvider RGBW = new LightChannelTypeProvider(LightChannelType.RED,
            LightChannelType.GREEN, LightChannelType.BLUE, LightChannelType.WHITE) {
        @Override
        protected ChannelType provideType(ChannelTypeUID channelTypeUid, LightChannelDef channelType,
                ListEntitiesLightResponse rsp) {
            return extendSystemStateChannelType(DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS, channelType)
                    .withStateDescriptionFragment(zeroToOneStateDescr().build()).build();
        }

        @Override
        protected ChannelTypeUID provideTypeId(LightChannelDef channelType, ListEntitiesLightResponse rsp) {
            return extendSystemChanelTypeUid(DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS, channelType);
        }
    };

    public static final LightChannelTypeProvider COLD_WARM_WHITE = new LightChannelTypeProvider(
            LightChannelType.COLD_WHITE, LightChannelType.WARM_WHITE) {
        @Override
        protected ChannelType provideType(ChannelTypeUID channelTypeUid, LightChannelDef channelType,
                ListEntitiesLightResponse rsp) {
            var miredsColorTemperature = switch (channelType) {
                case COLD_WHITE -> rsp.getMinMireds();
                case WARM_WHITE -> rsp.getMaxMireds();
                default -> throw new IllegalArgumentException("Unsupported channel type " + channelType);
            };
            float colorTemperature = miredToKelvin(miredsColorTemperature);
            String kelvinFormatted = formatKelvin(colorTemperature);
            return extendSystemStateChannelType(DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS, channelType,
                    kelvinFormatted + "k").withStateDescriptionFragment(zeroToOneStateDescr().build())
                    .withTag(channelType.name() + "-" + kelvinFormatted + "K").build();
        }

        @Override
        protected ChannelTypeUID provideTypeId(LightChannelDef channelType, ListEntitiesLightResponse rsp) {
            float miredsColorTemperature = switch (channelType) {
                case COLD_WHITE -> rsp.getMinMireds();
                case WARM_WHITE -> rsp.getMaxMireds();
                default -> throw new IllegalArgumentException("Unsupported channel type " + channelType);
            };
            String kelvinFormatted = formatKelvin(miredToKelvin(miredsColorTemperature));
            return extendSystemChanelTypeUid(DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS, channelType,
                    kelvinFormatted + "k");
        }
    };

    public static final LightChannelTypeProvider COLOR_TEMPERATURE = new LightChannelTypeProvider(
            LightChannelType.COLOR_TEMPERATURE) {
        @Override
        protected ChannelType provideType(ChannelTypeUID channelTypeUid, LightChannelDef channelType,
                ListEntitiesLightResponse rsp) {
            var kelvinSorted = Stream.of(rsp.getMaxMireds(), rsp.getMinMireds()).map(ColorTempUtil::miredToKelvin)
                    .sorted().toList();
            var minCT = kelvinSorted.get(0);
            var maxCT = kelvinSorted.get(1);
            var minMaxColorTemperature = StateDescriptionFragmentBuilder.create().withMinimum(BigDecimal.valueOf(minCT))
                    .withMaximum(BigDecimal.valueOf(maxCT)).withPattern("%.0f K").build();
            var ctSubtype = kelvinSorted.stream().map(ColorTempUtil::formatKelvin).collect(Collectors.joining("-"));
            return extendSystemStateChannelType(DefaultSystemChannelTypeProvider.SYSTEM_COLOR_TEMPERATURE_ABS,
                    channelType, ctSubtype).withTag("ColorTemperatureRange-" + ctSubtype)
                    .withStateDescriptionFragment(minMaxColorTemperature).build();
        }

        @Override
        protected ChannelTypeUID provideTypeId(LightChannelDef channelType, ListEntitiesLightResponse rsp) {
            var ctSubtype = Stream.of(rsp.getMaxMireds(), rsp.getMinMireds()).map(ColorTempUtil::miredToKelvin).sorted()
                    .map(ColorTempUtil::formatKelvin).collect(Collectors.joining("-"));
            return extendSystemChanelTypeUid(DefaultSystemChannelTypeProvider.SYSTEM_COLOR_TEMPERATURE_ABS, channelType,
                    ctSubtype);
        }
    };

    public static final LightChannelTypeProvider DURATION = new LightChannelTypeProvider(
            LightChannelType.TRANSITION_LENGTH, LightChannelType.FLASH_LENGTH) {

        @Override
        protected ChannelType provideType(ChannelTypeUID channelTypeUid, LightChannelDef channelType,
                ListEntitiesLightResponse rsp) {
            var stateDescr = StateDescriptionFragmentBuilder.create().withMinimum(BigDecimal.ZERO)
                    .withMaximum(BigDecimal.valueOf((long) Integer.MAX_VALUE * 2)).withStep(BigDecimal.ONE).build();
            return defaultStateChannelBuilder(channelTypeUid, channelType.name().toLowerCase(),
                    CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + "Time")
                    .withStateDescriptionFragment(stateDescr).withDescription("Length in ms").build();
        }

        @Override
        protected ChannelTypeUID provideTypeId(LightChannelDef channelType, ListEntitiesLightResponse rsp) {
            return createStaticChannelTypeId(channelType);
        }
    };

    private static ChannelTypeUID createStaticChannelTypeId(LightChannelDef type, @Nullable String subtype) {
        return ChannelUtils.createStaticChannelTypeId("light", type, subtype);
    }

    private static ChannelTypeUID createStaticChannelTypeId(LightChannelDef type) {
        return createStaticChannelTypeId(type, null);
    }

    private static ChannelTypeUID extendSystemChanelTypeUid(ChannelType base, LightChannelDef type,
            @Nullable String... subtypes) {
        return ChannelUtils.extendSystemChanelTypeUid(base.getUID(), type.name().toLowerCase(), subtypes);
    }

    private static StateDescriptionFragmentBuilder zeroToOneStateDescr() {
        return StateDescriptionFragmentBuilder.create().withMinimum(BigDecimal.ZERO).withMaximum(BigDecimal.ONE)
                .withReadOnly(false);
    }
}
