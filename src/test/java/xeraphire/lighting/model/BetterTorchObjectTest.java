package xeraphire.lighting.model;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.registries.GNDRegistry;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.ObjectRegistry;
import necesse.inventory.item.toolItem.ToolType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BetterTorchObjectTest {

    @BeforeAll
    static void setupRegistries() {
        xeraphire.lighting.TestRegistries.ensureCoreRegistries();
    }

    @Test
    @DisplayName("BetterTorchObject sets light properties, tool type, wall mount string ID, and item properties")
    void testBetterTorchProperties() {
        BetterTorchObject torch = new BetterTorchObject(50.0F, 0.2F, 300);
        torch.setWallPlaceObjectStringID("betterwalltorch");

        assertThat(torch.lightLevel).isEqualTo(300);
        assertThat(torch.lightHue).isEqualTo(50.0F);
        assertThat(torch.lightSat).isEqualTo(0.2F);
        assertThat(torch.toolType).isEqualTo(ToolType.ALL);
        assertThat(torch.getWallPlaceObjectStringID()).isEqualTo("betterwalltorch");
        assertThat(torch.stackSize).isEqualTo(500);
        assertThat(torch.canPlaceOnShore).isTrue();
        assertThat(torch.displayMapTooltip).isTrue();
        assertThat(torch.replaceCategories).contains("torch");
        assertThat(torch.canReplaceCategories).contains("torch");

        // Verify registered item in ItemRegistry
        assertThat(ItemRegistry.getItem("bettertorch")).isNotNull();
    }

    @Test
    @DisplayName("BetterWallTorchObject sets light properties and ToolType")
    void testBetterWallTorchProperties() {
        BetterWallTorchObject wallTorch = new BetterWallTorchObject(50.0F, 0.2F, 300);

        assertThat(wallTorch.lightLevel).isEqualTo(300);
        assertThat(wallTorch.lightHue).isEqualTo(50.0F);
        assertThat(wallTorch.lightSat).isEqualTo(0.2F);
        assertThat(wallTorch.toolType).isEqualTo(ToolType.ALL);
    }
}
