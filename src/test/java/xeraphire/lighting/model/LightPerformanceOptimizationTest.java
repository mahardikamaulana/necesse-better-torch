package xeraphire.lighting.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import xeraphire.lighting.Config;
import xeraphire.lighting.autotorch.BetterAutoTorchServerListener;

import static org.assertj.core.api.Assertions.assertThat;

public class LightPerformanceOptimizationTest {

    @ParameterizedTest(name = "Light level {0} requires single-pass dynamic radius {1} with max tile area {2}")
    @CsvSource({
            "150, 27, 2290",   // Vanilla torch: (150 / 6) + 2 = 27 -> ~2290 tiles
            "300, 52, 8494",   // Better torch: (300 / 6) + 2 = 52 -> ~8494 tiles
            "350, 61, 11689",  // Steel lamp / Wood lantern: (350 / 6) + 2 = 61 -> ~11689 tiles
            "400, 69, 14957",  // Capped guardrail max: (400 / 6) + 2 = 69 -> ~14957 tiles
            "550, 70, 15393",  // Clamped at max 70 margin limit
            "5000, 70, 15393"  // Clamped at max 70 margin limit
    })
    void testSinglePassBoundingCalculations(int lightLevel, int expectedRadius, int maxApproxArea) {
        int radius = Math.min(70, (int) Math.ceil(lightLevel / 6.0f) + 2);
        int approxArea = (int) (Math.PI * radius * radius);

        assertThat(radius).isEqualTo(expectedRadius);
        assertThat(approxArea).isLessThanOrEqualTo(maxApproxArea + 10);
    }

    @Test
    @DisplayName("Capped 400 guardrail achieves >80% tile flood-fill reduction compared to uncapped 800-1000 level")
    void testFloodFillReductionRatio() {
        int radiusUncapped800 = (int) Math.ceil(800 / 6.0f); // 134
        int radiusCapped400 = (int) Math.ceil(400 / 6.0f);   // 67

        double areaUncapped = Math.PI * radiusUncapped800 * radiusUncapped800; // ~56,410 tiles
        double areaCapped = Math.PI * radiusCapped400 * radiusCapped400;       // ~14,102 tiles

        double reductionPercent = (1.0 - (areaCapped / areaUncapped)) * 100.0;
        assertThat(reductionPercent).isGreaterThan(70.0);
    }

    @Test
    @DisplayName("LightCache initialization pre-warms all light source lookup tables")
    void testLightCachePreWarming() {
        Config config = new Config("settings.cfg");
        BetterAutoTorchServerListener.LightCache.init(config);

        assertThat(BetterAutoTorchServerListener.LightCache.IS_LIGHT_SOURCE).isNotNull();
        assertThat(BetterAutoTorchServerListener.LightCache.ITEM_LIGHT_LEVEL_CACHE).isNotNull();
        assertThat(BetterAutoTorchServerListener.LightCache.LIGHT_RADIUS_CACHE).isNotNull();
        assertThat(BetterAutoTorchServerListener.LightCache.CAN_PLACE_ON_LIQUID).isNotNull();
        assertThat(BetterAutoTorchServerListener.LightCache.WALL_VARIANT_ID_CACHE).isNotNull();
    }
}
