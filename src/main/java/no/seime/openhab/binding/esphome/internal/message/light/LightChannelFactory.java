package no.seime.openhab.binding.esphome.internal.message.light;

import com.google.common.base.Strings;
import no.seime.openhab.binding.esphome.internal.BindingConstants;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.UID;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.seime.openhab.binding.esphome.internal.message.light.ChannelUtils.extendSystemChanelTypeUid;
import static no.seime.openhab.binding.esphome.internal.message.light.ChannelUtils.extendSystemStateChannelType;

/**
 * TODO:
 * 1. raw channels exposed, as reported by device and color mode supported
 * 2. complex channels types exposed:
 *       Switch -> [ON_OFF]                                                                  // ON_OFF only capability
 *       Dimmer -> [ON_OFF, BRIGHTNESS]                                                      // MASTER_BRIGHTNESS capability
 *       ColorTemperature + Dimmer -> [ON_OFF, MASTER_BRIGHTNESS, COLOR_TEMPERATURE]         // COLOR_TEMPERATURE capability
 *       ColorTemperature + Dimmer -> [ON_OFF, MASTER_BRIGHTNESS, COLD_WHITE, WARM_WHITE]    // COLD_WARM_WHITE capability
 *       Color -> [ON_OFF, COLOR_BRIGHTNESS, R, G, B]                                        // RGB capability (thimk about master/color brightness)
 */


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

    private final Map<LightChannelType, ChannelType> lightChannelTypes = new HashMap<>();

    LightChannelFactory() {
        lightChannelTypes.put(LightChannelType.ON_OFF, DefaultSystemChannelTypeProvider.SYSTEM_POWER);
        lightChannelTypes.put(LightChannelType.MASTER_BRIGHTNESS, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
//        lightChannelTypes.put(LightChannelType.COLOR_MODE, null); //todo - enum
        lightChannelTypes.put(LightChannelType.COLOR_BRIGHTNESS, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
//        lightChannelTypes.put(LightChannelType.RED, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
//        lightChannelTypes.put(LightChannelType.GREEN, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
//        lightChannelTypes.put(LightChannelType.BLUE, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
//        lightChannelTypes.put(LightChannelType.WHITE, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
//        lightChannelTypes.put(LightChannelType.COLOR_TEMPERATURE, DefaultSystemChannelTypeProvider.SYSTEM_COLOR_TEMPERATURE_ABS);
//        lightChannelTypes.put(LightChannelType.COLD_WHITE, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
//        lightChannelTypes.put(LightChannelType.WARM_WHITE, DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS);
        lightChannelTypes.put(LightChannelType.TRANSITION_LENGTH, null); //todo - Duration
        lightChannelTypes.put(LightChannelType.FLASH_LENGTH, null); // todo - Duration
        lightChannelTypes.put(LightChannelType.EFFECT, null); // todo - extract from descriptor
    }


    private ChannelTypeUID createStaticChannelTypeId(LightChannelType type, @Nullable String subtype) {
        return ChannelUtils.createStaticChannelTypeId("light", type, subtype);
    }

    private ChannelTypeUID createStaticChannelTypeId(LightChannelType type) {
        return createStaticChannelTypeId(type, null);
    }

    private ChannelType createModeChannelType(Collection<ColorMode> supportedModes) {
        final var modesSetSubtype = supportedModes.stream().sorted().map(mode -> mode.name().toLowerCase()).collect(Collectors.joining("-"));
        final var modeOptions = supportedModes.stream().sorted().map(mode -> new StateOption(mode.name(), mode.description())).toList();

        final var stateDescr = StateDescriptionFragmentBuilder.create()
                .withReadOnly(true)
                .withOptions(modeOptions)
                .build();

        return ChannelTypeBuilder.state(
                        createStaticChannelTypeId(LightChannelType.COLOR_MODE, modesSetSubtype),
                        "Light mode",
                        CoreItemFactory.STRING
                )
                .withStateDescriptionFragment(stateDescr)
                .withAutoUpdatePolicy(AutoUpdatePolicy.VETO)
                .build();
    }

    private ChannelType createRgbColorChannelType(LightChannelType colorChannel) {
        switch (colorChannel) {
            case RED, GREEN, BLUE, WHITE -> {
                return extendSystemStateChannelType(DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS, colorChannel)
                        .withStateDescriptionFragment(zeroToOneStateDescr().build())
                        .build();
            }
            default ->
                    throw new IllegalArgumentException("Channel type " + colorChannel + " is not supported by RGB + White channel type creator");
        }
    }

    private StateDescriptionFragmentBuilder zeroToOneStateDescr() {
        return StateDescriptionFragmentBuilder.create()
                .withMinimum(BigDecimal.ZERO)
                .withMaximum(BigDecimal.ONE)
                .withReadOnly(false);
    }

    private ChannelType createCWWhiteChannelType(LightChannelType coldWarmWhite, float miredsColorTemperature) {
        switch (coldWarmWhite) {
            case COLD_WHITE, WARM_WHITE -> {
                float colorTemperature = miredToKelvin(miredsColorTemperature);
                String kelvinFormatted = formatKelvin(colorTemperature);
                return extendSystemStateChannelType(DefaultSystemChannelTypeProvider.SYSTEM_BRIGHTNESS, coldWarmWhite, kelvinFormatted + "k")
                        .withStateDescriptionFragment(zeroToOneStateDescr().build())
                        .withTag(coldWarmWhite.name() + "-" + kelvinFormatted + "K")
                        .build();
            }
            default ->
                    throw new IllegalArgumentException("Channel type " + coldWarmWhite + " is not supported by Cold/Warm white channel type creator");
        }
    }

    private ChannelType createCTChannelType(float mired1, float mired2) {
        var kelvinSorted = Stream.of(mired1, mired2).map(LightChannelFactory::miredToKelvin).sorted().toList();
        var minCT = kelvinSorted.get(0);
        var maxCT = kelvinSorted.get(1);
        var minMaxColorTemperature = StateDescriptionFragmentBuilder.create()
                .withMinimum(BigDecimal.valueOf(minCT))
                .withMaximum(BigDecimal.valueOf(maxCT))
                .withPattern("%.0f K")
                .build();
        var ctSubtype = kelvinSorted.stream().map(LightChannelFactory::formatKelvin).collect(Collectors.joining("-"));

        return extendSystemStateChannelType(DefaultSystemChannelTypeProvider.SYSTEM_COLOR_TEMPERATURE_ABS, LightChannelType.COLOR_TEMPERATURE, ctSubtype)
                .withTag("ColorTemperatureRange-" + ctSubtype)
                .withStateDescriptionFragment(minMaxColorTemperature)
                .build();
    }


    private static final float MIRED_BASE = 1_000_000f; // K

    private static float miredToKelvin(float mired) {
        // https://en.wikipedia.org/wiki/Mired
        return MIRED_BASE / mired;
    }

    private static float kelvinToMired(float kelvin) {
        return MIRED_BASE / kelvin;
    }

    private static String formatKelvin(float colorTemperature) {
        return String.format("%.0f", colorTemperature);
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
