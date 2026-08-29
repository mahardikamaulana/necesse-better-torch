package xeraphire.lighting.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xeraphire.lighting.Config;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigSyncStateTest {

    @Test
    @DisplayName("applyServerSync updates all fields with clamping and normalization")
    void testApplyServerSync() {
        Config config = new Config((java.io.File) null);

        config.applyServerSync(
                "hard",
                9999, // should clamp to 5000
                45.5F,
                0.6F,
                20,   // should clamp to 50
                400,
                800,
                600,
                35.0F,
                0.4F,
                190.0F,
                0.85F,
                false,
                "better_only",
                "brightest_first",
                false,
                true,
                999 // should clamp to 200
        );

        assertThat(config.getRecipeDifficulty()).isEqualTo("HARD");
        assertThat(config.getTorchLightLevel()).isEqualTo(5000);
        assertThat(config.getTorchLightHue()).isEqualTo(45.5F);
        assertThat(config.getTorchLightSat()).isEqualTo(0.6F);

        assertThat(config.getSteelLampLightLevel()).isEqualTo(50);
        assertThat(config.getWoodenLanternLightLevel()).isEqualTo(400);
        assertThat(config.getPhoenixLampLightLevel()).isEqualTo(800);
        assertThat(config.getAbyssLanternLightLevel()).isEqualTo(600);

        assertThat(config.getOutdoorLampHue()).isEqualTo(35.0F);
        assertThat(config.getOutdoorLampSat()).isEqualTo(0.4F);
        assertThat(config.getAbyssLanternHue()).isEqualTo(190.0F);
        assertThat(config.getAbyssLanternSat()).isEqualTo(0.85F);

        assertThat(config.isAutoTorchIntegration()).isFalse();
        assertThat(config.getAutoTorchTorchSelection()).isEqualTo("BETTER_ONLY");
        assertThat(config.getAutoTorchAnyLightPriority()).isEqualTo("BRIGHTEST_FIRST");
        assertThat(config.isAutoTorchConsiderAllLights()).isFalse();
        assertThat(config.isAutoTorchDynamicTravelSpacing()).isTrue();
        assertThat(config.getAutoTorchMinLightThreshold()).isEqualTo(200);
    }
}
