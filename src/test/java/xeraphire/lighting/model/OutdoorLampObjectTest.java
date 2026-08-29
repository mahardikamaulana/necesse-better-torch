package xeraphire.lighting.model;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.registries.GNDRegistry;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.ObjectRegistry;
import necesse.inventory.item.toolItem.ToolType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class OutdoorLampObjectTest {

    @BeforeAll
    static void setupRegistries() {
        try {
            GNDRegistry.instance.registerCore();
            DamageTypeRegistry.instance.registerCore();
            ObjectRegistry.instance.registerCore();
            ItemRegistry.instance.registerCore();
        } catch (Throwable ignored) {
        }
    }

    @ParameterizedTest(name = "Configured light level {0} results in effective light level {1}")
    @CsvSource({
            "100, 100",
            "300, 300",
            "350, 350",
            "400, 400",
            "401, 400",
            "550, 400",
            "800, 400",
            "1200, 400",
            "5000, 400"
    })
    void testEffectiveLightLevelGuardrail(int configuredLevel, int expectedEffective) {
        int effective = OutdoorLampObject.getEffectiveLightLevel(configuredLevel);
        assertThat(effective).isEqualTo(expectedEffective);
    }

    @Test
    @DisplayName("SteelLampPostObject initializes with correct lighting properties and metadata")
    void testSteelLampPostProperties() {
        SteelLampPostObject obj = new SteelLampPostObject(350, 50.0F, 0.2F);

        assertThat(obj.lightLevel).isEqualTo(350);
        assertThat(obj.lightHue).isEqualTo(50.0F);
        assertThat(obj.lightSat).isEqualTo(0.2F);
        assertThat(obj.toolType).isEqualTo(ToolType.ALL);
        assertThat(obj.isLightTransparent).isTrue();
        assertThat(obj.canPlaceOnShore).isTrue();
        assertThat(obj.stackSize).isEqualTo(500);
        assertThat(obj.roomProperties).contains("lights");
    }

    @Test
    @DisplayName("WoodenHangingLanternObject initializes with correct lighting properties and metadata")
    void testWoodenLanternProperties() {
        WoodenHangingLanternObject obj = new WoodenHangingLanternObject(350, 50.0F, 0.2F);

        assertThat(obj.lightLevel).isEqualTo(350);
        assertThat(obj.lightHue).isEqualTo(50.0F);
        assertThat(obj.lightSat).isEqualTo(0.2F);
        assertThat(obj.roomProperties).contains("lights");
    }

    @Test
    @DisplayName("PhoenixLampObject initializes with correct lighting properties and metadata")
    void testPhoenixLampProperties() {
        PhoenixLampObject obj = new PhoenixLampObject(550, 50.0F, 0.2F);

        assertThat(obj.lightLevel).isEqualTo(400);
        assertThat(obj.lightHue).isEqualTo(50.0F);
        assertThat(obj.lightSat).isEqualTo(0.2F);
        assertThat(obj.roomProperties).contains("lights");
    }

    @Test
    @DisplayName("AbyssLanternObject initializes with aquatic shore placement and water lantern hues")
    void testAbyssLanternProperties() {
        AbyssLanternObject obj = new AbyssLanternObject(450, 30.0F, 0.75F);

        assertThat(obj.lightLevel).isEqualTo(400);
        assertThat(obj.lightHue).isEqualTo(30.0F);
        assertThat(obj.lightSat).isEqualTo(0.75F);
        assertThat(obj.canPlaceOnShore).isTrue();
        assertThat(obj.roomProperties).contains("lights");
    }
}
