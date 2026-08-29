package xeraphire.lighting.autotorch;

import necesse.engine.registries.*;
import necesse.entity.mobs.PlayerMob;
import necesse.inventory.InventoryItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xeraphire.lighting.Config;
import xeraphire.lighting.LightingMod;
import xeraphire.lighting.TestRegistries;

import static org.assertj.core.api.Assertions.assertThat;

public class TerrainAwarenessTest {

    private static Config config;
    private static BetterAutoTorchServerListener.BetterAutoTorchGameLoop gameLoop;

    static {
        TestRegistries.ensureCoreRegistries();
        config = LightingMod.getConfig();
        BetterAutoTorchServerListener.LightCache.init(config);
        gameLoop = new BetterAutoTorchServerListener.BetterAutoTorchGameLoop(null);
    }

    private static PlayerMob createPlayerWithItems(InventoryItem... items) {
        PlayerMob p = new PlayerMob(1002L, null);
        p.getInv().main.clearInventory();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                p.getInv().main.setItem(i, items[i]);
            }
        }
        return p;
    }

    @Test
    @DisplayName("Over liquid/water, non-aquatic fixtures (Torch, Better Torch, Steel Lamp, Phoenix Lamp) are skipped")
    void testNonAquaticFixturesSkippedOnWater() {
        PlayerMob player = createPlayerWithItems(
                new InventoryItem("bettertorch", 50),
                new InventoryItem("torch", 50),
                new InventoryItem("steelLampPost", 10),
                new InventoryItem("phoenixLamp", 5)
        );

        // When isTargetLiquid = true
        BetterAutoTorchServerListener.TorchCandidate candidate =
                gameLoop.findTorchCandidate(player, true, "ANY_LIGHT", "SLOT_ORDER");

        assertThat(candidate).isNull();
    }

    @Test
    @DisplayName("Over liquid/water, Abyss Lantern is selected when available")
    void testAbyssLanternSelectedOnWater() {
        PlayerMob player = createPlayerWithItems(
                new InventoryItem("bettertorch", 50),
                new InventoryItem("abyssLantern", 10)
        );

        // When isTargetLiquid = true
        BetterAutoTorchServerListener.TorchCandidate candidate =
                gameLoop.findTorchCandidate(player, true, "ANY_LIGHT", "SLOT_ORDER");

        assertThat(candidate).isNotNull();
        assertThat(candidate.gameObject.getStringID()).isEqualTo("abyssLantern");
        assertThat(candidate.slotIndex).isEqualTo(1);
        assertThat(candidate.canPlaceOnLiquid).isTrue();
    }

    @Test
    @DisplayName("On land, all fixtures (including Abyss Lantern and standard torches) are valid candidates")
    void testLandAcceptsBothAquaticAndTerrestrial() {
        PlayerMob player = createPlayerWithItems(
                new InventoryItem("abyssLantern", 10),
                new InventoryItem("bettertorch", 50)
        );

        BetterAutoTorchServerListener.TorchCandidate candidate =
                gameLoop.findTorchCandidate(player, false, "ANY_LIGHT", "SLOT_ORDER");

        assertThat(candidate).isNotNull();
        assertThat(candidate.gameObject.getStringID()).isEqualTo("abyssLantern");
        assertThat(candidate.slotIndex).isEqualTo(0);
    }
}
