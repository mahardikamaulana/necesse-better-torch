package xeraphire.lighting.autotorch;

import necesse.engine.registries.*;
import necesse.entity.mobs.PlayerMob;
import necesse.inventory.InventoryItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xeraphire.lighting.Config;
import xeraphire.lighting.LightingMod;
import xeraphire.lighting.TestRegistries;

import static org.assertj.core.api.Assertions.assertThat;

public class TorchSelectionAlgorithmTest {

    private static LightingMod mod;
    private static Config config;
    private static BetterAutoTorchServerListener.BetterAutoTorchGameLoop gameLoop;

    static {
        TestRegistries.ensureCoreRegistries();
        config = LightingMod.getConfig();
        BetterAutoTorchServerListener.LightCache.init(config);
        gameLoop = new BetterAutoTorchServerListener.BetterAutoTorchGameLoop(null);
    }

    private static PlayerMob createPlayerWithItems(InventoryItem... items) {
        PlayerMob p = new PlayerMob(1001L, null);
        p.getInv().main.clearInventory();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                p.getInv().main.setItem(i, items[i]);
            }
        }
        return p;
    }

    @Nested
    @DisplayName("BETTER_THEN_VANILLA Mode")
    class BetterThenVanillaTests {

        @Test
        @DisplayName("Picks Better Torch first when both Better Torch and Vanilla Torch are present")
        void testPrefersBetterTorch() {
            PlayerMob player = createPlayerWithItems(
                    new InventoryItem("torch", 10),
                    new InventoryItem("bettertorch", 10)
            );

            BetterAutoTorchServerListener.TorchCandidate candidate =
                    gameLoop.findTorchCandidate(player, false, "BETTER_THEN_VANILLA", "SLOT_ORDER");

            assertThat(candidate).isNotNull();
            assertThat(candidate.gameObject.getStringID()).isEqualTo("bettertorch");
            assertThat(candidate.slotIndex).isEqualTo(1);
        }

        @Test
        @DisplayName("Falls back to Vanilla Torch when no Better Torches are present")
        void testFallbackToVanillaTorch() {
            PlayerMob player = createPlayerWithItems(
                    new InventoryItem("torch", 10)
            );

            BetterAutoTorchServerListener.TorchCandidate candidate =
                    gameLoop.findTorchCandidate(player, false, "BETTER_THEN_VANILLA", "SLOT_ORDER");

            assertThat(candidate).isNotNull();
            assertThat(candidate.gameObject.getStringID()).isEqualTo("torch");
            assertThat(candidate.slotIndex).isEqualTo(0);
        }

        @Test
        @DisplayName("Returns null when inventory contains no torches")
        void testNoTorchesReturnsNull() {
            PlayerMob player = createPlayerWithItems();

            BetterAutoTorchServerListener.TorchCandidate candidate =
                    gameLoop.findTorchCandidate(player, false, "BETTER_THEN_VANILLA", "SLOT_ORDER");

            assertThat(candidate).isNull();
        }
    }

    @Nested
    @DisplayName("VANILLA_THEN_BETTER Mode")
    class VanillaThenBetterTests {

        @Test
        @DisplayName("Picks Vanilla Torch first even if Better Torch is in earlier slot")
        void testPrefersVanillaTorch() {
            PlayerMob player = createPlayerWithItems(
                    new InventoryItem("bettertorch", 10),
                    new InventoryItem("torch", 10)
            );

            BetterAutoTorchServerListener.TorchCandidate candidate =
                    gameLoop.findTorchCandidate(player, false, "VANILLA_THEN_BETTER", "SLOT_ORDER");

            assertThat(candidate).isNotNull();
            assertThat(candidate.gameObject.getStringID()).isEqualTo("torch");
            assertThat(candidate.slotIndex).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("BETTER_ONLY and VANILLA_ONLY Strict Modes")
    class StrictModeTests {

        @Test
        @DisplayName("BETTER_ONLY ignores vanilla torches")
        void testBetterOnlyIgnoresVanilla() {
            PlayerMob player = createPlayerWithItems(
                    new InventoryItem("torch", 10)
            );

            BetterAutoTorchServerListener.TorchCandidate candidate =
                    gameLoop.findTorchCandidate(player, false, "BETTER_ONLY", "SLOT_ORDER");

            assertThat(candidate).isNull();
        }

        @Test
        @DisplayName("VANILLA_ONLY ignores better torches")
        void testVanillaOnlyIgnoresBetter() {
            PlayerMob player = createPlayerWithItems(
                    new InventoryItem("bettertorch", 10)
            );

            BetterAutoTorchServerListener.TorchCandidate candidate =
                    gameLoop.findTorchCandidate(player, false, "VANILLA_ONLY", "SLOT_ORDER");

            assertThat(candidate).isNull();
        }
    }

    @Nested
    @DisplayName("ANY_LIGHT Mode Priorities")
    class AnyLightModeTests {

        @Test
        @DisplayName("SLOT_ORDER selects first available light fixture in slot sequence")
        void testSlotOrderPriority() {
            PlayerMob player = createPlayerWithItems(
                    new InventoryItem("steelLampPost", 5),
                    new InventoryItem("phoenixLamp", 5)
            );

            BetterAutoTorchServerListener.TorchCandidate candidate =
                    gameLoop.findTorchCandidate(player, false, "ANY_LIGHT", "SLOT_ORDER");

            assertThat(candidate).isNotNull();
            assertThat(candidate.gameObject.getStringID()).isEqualTo("steelLampPost");
            assertThat(candidate.slotIndex).isEqualTo(0);
        }

        @Test
        @DisplayName("BRIGHTEST_FIRST selects highest light level fixture (Phoenix Lamp > Steel Lamp > Torch)")
        void testBrightestFirstPriority() {
            PlayerMob player = createPlayerWithItems(
                    new InventoryItem("torch", 20),
                    new InventoryItem("steelLampPost", 5),
                    new InventoryItem("phoenixLamp", 2)
            );

            BetterAutoTorchServerListener.TorchCandidate candidate =
                    gameLoop.findTorchCandidate(player, false, "ANY_LIGHT", "BRIGHTEST_FIRST");

            assertThat(candidate).isNotNull();
            assertThat(candidate.gameObject.getStringID()).isEqualTo("phoenixLamp");
            assertThat(candidate.lightLevel).isEqualTo(config.getPhoenixLampLightLevel());
            assertThat(candidate.slotIndex).isEqualTo(2);
        }

        @Test
        @DisplayName("CONSERVATIVE_FIRST selects lowest light level fixture to preserve rare lamps")
        void testConservativeFirstPriority() {
            PlayerMob player = createPlayerWithItems(
                    new InventoryItem("phoenixLamp", 2),
                    new InventoryItem("steelLampPost", 5),
                    new InventoryItem("torch", 20)
            );

            BetterAutoTorchServerListener.TorchCandidate candidate =
                    gameLoop.findTorchCandidate(player, false, "ANY_LIGHT", "CONSERVATIVE_FIRST");

            assertThat(candidate).isNotNull();
            assertThat(candidate.gameObject.getStringID()).isEqualTo("torch");
            assertThat(candidate.lightLevel).isEqualTo(150);
            assertThat(candidate.slotIndex).isEqualTo(2);
        }
    }
}
