package xeraphire.lighting.autotorch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicRadiusCalculationTest {

    @ParameterizedTest(name = "Light level {0} with base radius {1} calculates dynamic radius {2}")
    @CsvSource({
            "150, 10, 10", // Vanilla Torch (150) -> 10
            "300, 10, 20", // Better Torch (300) -> 20
            "350, 10, 23", // Steel Lamp / Wooden Lantern (350) -> 23
            "450, 10, 30", // Abyss Lantern (450) -> 30
            "550, 10, 37", // Phoenix Lamp (550) -> 37
            "50, 5, 8",    // Minimum clamping guardrail (clamped to 8)
            "5000, 20, 40" // Maximum clamping guardrail (clamped to 40)
    })
    void testDynamicRadiusCalculations(int itemLightLevel, int baseRadius, int expectedDynamicRadius) {
        int calculated = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.calculateDynamicRadius(itemLightLevel, baseRadius);
        assertThat(calculated).isEqualTo(expectedDynamicRadius);
    }

    @Test
    @DisplayName("Calculated radius is always bounded within [8, 40]")
    void testBoundaryEnforcement() {
        for (int light = 0; light <= 10000; light += 100) {
            for (int base = 1; base <= 50; base += 5) {
                int r = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.calculateDynamicRadius(light, base);
                assertThat(r).isGreaterThanOrEqualTo(8).isLessThanOrEqualTo(40);
            }
        }
    }
}
