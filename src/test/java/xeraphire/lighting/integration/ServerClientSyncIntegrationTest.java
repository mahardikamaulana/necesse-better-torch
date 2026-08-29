package xeraphire.lighting.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xeraphire.lighting.Config;
import xeraphire.lighting.LightingMod;
import xeraphire.lighting.network.PacketModConfigSync;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class ServerClientSyncIntegrationTest {

    @BeforeAll
    static void setupMod() {
        LightingMod mod = new LightingMod();
        mod.preInit();
    }

    @Test
    @DisplayName("End-to-end network sync: Server serializes custom config, sends across wire, and client processes it")
    void testEndToEndConfigSync() {
        // 1. Server-side custom config
        Config serverConfig = new Config((File) null);
        serverConfig.applyServerSync(
                "HARD",
                800,
                55.0F,
                0.45F,
                400,
                380,
                900,
                650,
                40.0F,
                0.35F,
                175.0F,
                0.80F,
                true,
                "ANY_LIGHT",
                "BRIGHTEST_FIRST",
                true
        );

        // 2. Server creates and transmits network packet
        PacketModConfigSync serverPacket = new PacketModConfigSync(serverConfig);
        byte[] networkBytes = serverPacket.getPacketData();

        // 3. Client receives byte payload and processes packet
        PacketModConfigSync clientReceivedPacket = new PacketModConfigSync(networkBytes);
        clientReceivedPacket.processClient(null, null);

        // 4. Verify client active config is now in exact sync with server
        Config clientConfig = LightingMod.getConfig();
        assertThat(clientConfig).isNotNull();

        assertThat(clientConfig.getRecipeDifficulty()).isEqualTo("HARD");
        assertThat(clientConfig.getTorchLightLevel()).isEqualTo(800);
        assertThat(clientConfig.getTorchLightHue()).isCloseTo(55.0F, within(0.001F));
        assertThat(clientConfig.getTorchLightSat()).isCloseTo(0.45F, within(0.001F));
        assertThat(clientConfig.getSteelLampLightLevel()).isEqualTo(400);
        assertThat(clientConfig.getWoodenLanternLightLevel()).isEqualTo(380);
        assertThat(clientConfig.getPhoenixLampLightLevel()).isEqualTo(900);
        assertThat(clientConfig.getAbyssLanternLightLevel()).isEqualTo(650);
        assertThat(clientConfig.getOutdoorLampHue()).isCloseTo(40.0F, within(0.001F));
        assertThat(clientConfig.getOutdoorLampSat()).isCloseTo(0.35F, within(0.001F));
        assertThat(clientConfig.getAbyssLanternHue()).isCloseTo(175.0F, within(0.001F));
        assertThat(clientConfig.getAbyssLanternSat()).isCloseTo(0.80F, within(0.001F));
        assertThat(clientConfig.isAutoTorchIntegration()).isTrue();
        assertThat(clientConfig.getAutoTorchTorchSelection()).isEqualTo("ANY_LIGHT");
        assertThat(clientConfig.getAutoTorchAnyLightPriority()).isEqualTo("BRIGHTEST_FIRST");
        assertThat(clientConfig.isAutoTorchConsiderAllLights()).isTrue();
    }
}
