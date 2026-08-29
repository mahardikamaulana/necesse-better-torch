package xeraphire.lighting.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import xeraphire.lighting.Config;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigLoadingTest {

    @Nested
    @DisplayName("Default Values")
    class DefaultValues {

        @Test
        @DisplayName("Config initializes with standard default values")
        void testDefaults() {
            Config config = new Config((java.io.File) null);

            assertThat(config.getTorchLightLevel()).isEqualTo(300);
            assertThat(config.getTorchLightHue()).isEqualTo(50.0F);
            assertThat(config.getTorchLightSat()).isEqualTo(0.2F);
            assertThat(config.getRecipeDifficulty()).isEqualTo("MEDIUM");

            assertThat(config.getSteelLampLightLevel()).isEqualTo(350);
            assertThat(config.getWoodenLanternLightLevel()).isEqualTo(350);
            assertThat(config.getPhoenixLampLightLevel()).isEqualTo(550);
            assertThat(config.getAbyssLanternLightLevel()).isEqualTo(450);

            assertThat(config.getOutdoorLampHue()).isEqualTo(50.0F);
            assertThat(config.getOutdoorLampSat()).isEqualTo(0.2F);
            assertThat(config.getAbyssLanternHue()).isEqualTo(30.0F);
            assertThat(config.getAbyssLanternSat()).isEqualTo(0.75F);

            assertThat(config.isAutoTorchIntegration()).isTrue();
            assertThat(config.getAutoTorchTorchSelection()).isEqualTo("BETTER_THEN_VANILLA");
            assertThat(config.getAutoTorchAnyLightPriority()).isEqualTo("SLOT_ORDER");
            assertThat(config.isAutoTorchConsiderAllLights()).isTrue();
            assertThat(config.isAutoTorchDynamicTravelSpacing()).isTrue();
            assertThat(config.getAutoTorchMinLightThreshold()).isEqualTo(90);
        }
    }

    @Nested
    @DisplayName("Custom Properties Parsing")
    class ParsingTests {

        @Test
        @DisplayName("Parses full custom config string correctly")
        void testCustomConfigParsing() {
            String configContent = ""
                    + "# Custom Comment\n"
                    + "torchLightLevel=600\n"
                    + "torchLightHue=120.5\n"
                    + "torchLightSat=0.8\n"
                    + "recipeDifficulty=hard\n"
                    + "\n"
                    + "steelLampLightLevel=400\n"
                    + "woodenLanternLightLevel=320\n"
                    + "phoenixLampLightLevel=700\n"
                    + "abyssLanternLightLevel=500\n"
                    + "outdoorLampHue=45.0\n"
                    + "outdoorLampSat=0.3\n"
                    + "abyssLanternHue=200.0\n"
                    + "abyssLanternSat=0.9\n"
                    + "autoTorchIntegration=false\n"
                    + "autoTorchTorchSelection=ANY_LIGHT\n"
                    + "autoTorchAnyLightPriority=BRIGHTEST_FIRST\n"
                    + "autoTorchConsiderAllLights=false\n"
                    + "autoTorchDynamicTravelSpacing=false\n"
                    + "autoTorchMinLightThreshold=75\n";

            Config config = new Config((java.io.File) null);
            config.loadFromString(configContent);

            assertThat(config.getTorchLightLevel()).isEqualTo(600);
            assertThat(config.getTorchLightHue()).isEqualTo(120.5F);
            assertThat(config.getTorchLightSat()).isEqualTo(0.8F);
            assertThat(config.getRecipeDifficulty()).isEqualTo("HARD");

            assertThat(config.getSteelLampLightLevel()).isEqualTo(400);
            assertThat(config.getWoodenLanternLightLevel()).isEqualTo(320);
            assertThat(config.getPhoenixLampLightLevel()).isEqualTo(700);
            assertThat(config.getAbyssLanternLightLevel()).isEqualTo(500);

            assertThat(config.getOutdoorLampHue()).isEqualTo(45.0F);
            assertThat(config.getOutdoorLampSat()).isEqualTo(0.3F);
            assertThat(config.getAbyssLanternHue()).isEqualTo(200.0F);
            assertThat(config.getAbyssLanternSat()).isEqualTo(0.9F);

            assertThat(config.isAutoTorchIntegration()).isFalse();
            assertThat(config.getAutoTorchTorchSelection()).isEqualTo("ANY_LIGHT");
            assertThat(config.getAutoTorchAnyLightPriority()).isEqualTo("BRIGHTEST_FIRST");
            assertThat(config.isAutoTorchConsiderAllLights()).isFalse();
            assertThat(config.isAutoTorchDynamicTravelSpacing()).isFalse();
            assertThat(config.getAutoTorchMinLightThreshold()).isEqualTo(75);
        }

        @Test
        @DisplayName("Handles legacy alias keys lightHue, lightSat, and lightLevel")
        void testLegacyAliases() {
            String configContent = ""
                    + "lightLevel=450\n"
                    + "lightHue=30.0\n"
                    + "lightSat=0.5\n";

            Config config = new Config((java.io.File) null);
            config.loadFromString(configContent);

            assertThat(config.getTorchLightLevel()).isEqualTo(450);
            assertThat(config.getTorchLightHue()).isEqualTo(30.0F);
            assertThat(config.getTorchLightSat()).isEqualTo(0.5F);
        }
    }

    @Nested
    @DisplayName("Clamping Logic")
    class ClampingTests {

        @ParameterizedTest(name = "Light level {0} clamped to {1}")
        @CsvSource({
                "10, 50",
                "0, 50",
                "-100, 50",
                "50, 50",
                "300, 300",
                "5000, 5000",
                "5001, 5000",
                "99999, 5000"
        })
        void testTorchLightLevelClamping(int input, int expected) {
            String content = "torchLightLevel=" + input + "\n"
                    + "steelLampLightLevel=" + input + "\n"
                    + "woodenLanternLightLevel=" + input + "\n"
                    + "phoenixLampLightLevel=" + input + "\n"
                    + "abyssLanternLightLevel=" + input + "\n";

            Config config = new Config((java.io.File) null);
            config.loadFromString(content);

            assertThat(config.getTorchLightLevel()).isEqualTo(expected);
            assertThat(config.getSteelLampLightLevel()).isEqualTo(expected);
            assertThat(config.getWoodenLanternLightLevel()).isEqualTo(expected);
            assertThat(config.getPhoenixLampLightLevel()).isEqualTo(expected);
            assertThat(config.getAbyssLanternLightLevel()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Torch Selection and Priority Normalization")
    class NormalizationTests {

        @ParameterizedTest(name = "Selection input ''{0}'' normalizes to ''{1}''")
        @CsvSource({
                "BETTER_THEN_VANILLA, BETTER_THEN_VANILLA",
                "better_then_vanilla, BETTER_THEN_VANILLA",
                "VANILLA_THEN_BETTER, VANILLA_THEN_BETTER",
                "BETTER_ONLY, BETTER_ONLY",
                "VANILLA_ONLY, VANILLA_ONLY",
                "ANY_LIGHT, ANY_LIGHT",
                "invalid_mode, BETTER_THEN_VANILLA",
                "'', BETTER_THEN_VANILLA"
        })
        void testTorchSelectionNormalization(String input, String expected) {
            String content = "autoTorchTorchSelection=" + input + "\n";
            Config config = new Config((java.io.File) null);
            config.loadFromString(content);

            assertThat(config.getAutoTorchTorchSelection()).isEqualTo(expected);
        }

        @ParameterizedTest(name = "Priority input ''{0}'' normalizes to ''{1}''")
        @CsvSource({
                "SLOT_ORDER, SLOT_ORDER",
                "slot_order, SLOT_ORDER",
                "BRIGHTEST_FIRST, BRIGHTEST_FIRST",
                "brightest_first, BRIGHTEST_FIRST",
                "CONSERVATIVE_FIRST, CONSERVATIVE_FIRST",
                "conservative_first, CONSERVATIVE_FIRST",
                "random_string, SLOT_ORDER",
                "'', SLOT_ORDER"
        })
        void testAnyLightPriorityNormalization(String input, String expected) {
            String content = "autoTorchAnyLightPriority=" + input + "\n";
            Config config = new Config((java.io.File) null);
            config.loadFromString(content);

            assertThat(config.getAutoTorchAnyLightPriority()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Backward Compatibility")
    class BackwardCompatibilityTests {

        @Test
        @DisplayName("autoTorchPreferBetterTorch=true maps to BETTER_THEN_VANILLA")
        void testPreferBetterTorchTrue() {
            String content = "autoTorchPreferBetterTorch=true\n";
            Config config = new Config((java.io.File) null);
            config.loadFromString(content);

            assertThat(config.getAutoTorchTorchSelection()).isEqualTo("BETTER_THEN_VANILLA");
        }

        @Test
        @DisplayName("autoTorchPreferBetterTorch=false maps to VANILLA_THEN_BETTER")
        void testPreferBetterTorchFalse() {
            String content = "autoTorchPreferBetterTorch=false\n";
            Config config = new Config((java.io.File) null);
            config.loadFromString(content);

            assertThat(config.getAutoTorchTorchSelection()).isEqualTo("VANILLA_THEN_BETTER");
        }
    }
}
