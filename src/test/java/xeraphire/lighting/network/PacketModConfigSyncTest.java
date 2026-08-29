package xeraphire.lighting.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xeraphire.lighting.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class PacketModConfigSyncTest {

    @Test
    @DisplayName("PacketModConfigSync serializes and deserializes all 18 fields perfectly")
    void testSerializationRoundtrip() {
        Config config = new Config((java.io.File) null);
        config.applyServerSync(
                "HARD",
                450,
                110.5F,
                0.85F,
                380,
                340,
                750,
                520,
                48.0F,
                0.25F,
                195.0F,
                0.78F,
                true,
                "ANY_LIGHT",
                "BRIGHTEST_FIRST",
                false,
                true,
                85
        );

        // 1. Construct outgoing packet from Config
        PacketModConfigSync outgoing = new PacketModConfigSync(config);

        // 2. Extract serialized byte data
        byte[] packetData = outgoing.getPacketData();
        assertThat(packetData).isNotNull().isNotEmpty();

        // 3. Deserialize into incoming packet
        PacketModConfigSync incoming = new PacketModConfigSync(packetData);

        // 4. Assert equality of all fields
        assertThat(incoming.recipeDifficulty).isEqualTo("HARD");
        assertThat(incoming.torchLightLevel).isEqualTo(450);
        assertThat(incoming.torchLightHue).isCloseTo(110.5F, within(0.001F));
        assertThat(incoming.torchLightSat).isCloseTo(0.85F, within(0.001F));
        assertThat(incoming.steelLampLightLevel).isEqualTo(380);
        assertThat(incoming.woodenLanternLightLevel).isEqualTo(340);
        assertThat(incoming.phoenixLampLightLevel).isEqualTo(750);
        assertThat(incoming.abyssLanternLightLevel).isEqualTo(520);
        assertThat(incoming.outdoorLampHue).isCloseTo(48.0F, within(0.001F));
        assertThat(incoming.outdoorLampSat).isCloseTo(0.25F, within(0.001F));
        assertThat(incoming.abyssLanternHue).isCloseTo(195.0F, within(0.001F));
        assertThat(incoming.abyssLanternSat).isCloseTo(0.78F, within(0.001F));
        assertThat(incoming.autoTorchIntegration).isTrue();
        assertThat(incoming.autoTorchTorchSelection).isEqualTo("ANY_LIGHT");
        assertThat(incoming.autoTorchAnyLightPriority).isEqualTo("BRIGHTEST_FIRST");
        assertThat(incoming.autoTorchConsiderAllLights).isFalse();
        assertThat(incoming.autoTorchDynamicTravelSpacing).isTrue();
        assertThat(incoming.autoTorchMinLightThreshold).isEqualTo(85);
    }
}
