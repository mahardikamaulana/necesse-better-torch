package xeraphire.lighting.autotorch;

import necesse.engine.registries.LevelRegistry;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.util.LevelIdentifier;
import necesse.engine.world.WorldEntity;
import necesse.gfx.drawables.LevelDrawUtils;
import necesse.level.maps.Level;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import xeraphire.lighting.Config;
import xeraphire.lighting.LightingMod;
import xeraphire.lighting.TestRegistries;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoTorchTravelOptimizationTest {

    private static Config config;
    private static BetterAutoTorchServerListener.BetterAutoTorchGameLoop gameLoop;

    public static class HeadlessTravelTestLevel extends Level {
        private final Map<Point, Integer> grid = new HashMap<>();

        public HeadlessTravelTestLevel(LevelIdentifier identifier, int width, int height, WorldEntity worldEntity) {
            super(identifier, width, height, worldEntity);
        }

        public HeadlessTravelTestLevel() {
            this(new LevelIdentifier("test"), 200, 200, null);
        }

        @Override
        public LevelDrawUtils constructLevelDrawUtils() {
            return null;
        }

        @Override
        public int getObjectID(int tileX, int tileY) {
            return grid.getOrDefault(new Point(tileX, tileY), 0);
        }

        public void setTestObject(int tileX, int tileY, int objectID) {
            grid.put(new Point(tileX, tileY), objectID);
        }
    }

    static {
        TestRegistries.ensureCoreRegistries();
        try {
            LevelRegistry.registerLevel("headless_travel_test_level", HeadlessTravelTestLevel.class);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        config = LightingMod.getConfig();
        BetterAutoTorchServerListener.LightCache.init(config);
        gameLoop = new BetterAutoTorchServerListener.BetterAutoTorchGameLoop(null);
    }

    @Nested
    @DisplayName("Full-Radius Spatial Scanning for High-Light Fixtures")
    class SpatialScanningTests {

        @Test
        @DisplayName("Small-radius candidate (Vanilla Torch, r=10) detects distant Phoenix Lamp at distance 14")
        void testDetectsDistantHighLightFixture() {
            HeadlessTravelTestLevel level = new HeadlessTravelTestLevel();
            int phoenixLampID = ObjectRegistry.getObjectID("phoenixLamp");
            level.setTestObject(20, 20, phoenixLampID); // Phoenix lamp radius is 18

            // Target at (34, 20) -> Chebyshev distance is 14 (<= 18)
            // Even though candidate radius is small (10 -> candidateHalf = 5),
            // full-radius scan range (MAX_LIGHT_RADIUS=20) must detect the Phoenix Lamp!
            boolean detected = gameLoop.isTorchNearby(34, 20, level, 10, true);
            assertThat(detected).isTrue();

            // Target at (45, 20) -> distance is 25 (> 18), outside illumination reach
            boolean farAway = gameLoop.isTorchNearby(45, 20, level, 10, true);
            assertThat(farAway).isFalse();
        }

        @Test
        @DisplayName("Small-radius candidate detects distant Abyss Lantern at distance 12")
        void testDetectsDistantAbyssLantern() {
            HeadlessTravelTestLevel level = new HeadlessTravelTestLevel();
            int abyssLanternID = ObjectRegistry.getObjectID("abyssLantern");
            level.setTestObject(50, 50, abyssLanternID); // Abyss lantern radius is 15

            // Target at (62, 50) -> Chebyshev distance is 12 (<= 15)
            boolean detected = gameLoop.isTorchNearby(62, 50, level, 10, true);
            assertThat(detected).isTrue();

            // Target at (70, 50) -> distance is 20 (> 15)
            boolean outside = gameLoop.isTorchNearby(70, 50, level, 10, true);
            assertThat(outside).isFalse();
        }
    }

    @Nested
    @DisplayName("Velocity-Aware Lookahead & Diagonal Travel Projection")
    class LookaheadProjectionTests {

        @Test
        @DisplayName("Calculates dynamic lookahead for Better Torch (300) along cardinal UP vector")
        void testCardinalLookaheadBetterTorch() {
            // Player at (50, 50), facing UP (dir=0), moving UP (moveDx=0, moveDy=-1.0f), baseDistance=3, light=300
            int[] target = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.calculateTargetTile(
                    50, 50, 0.0f, -1.0f, 0, 3, 300, true
            );

            // Dynamic radius for 300 light is 20 -> lookahead is 10
            assertThat(target[0]).isEqualTo(50);
            assertThat(target[1]).isEqualTo(40); // 50 - 10 = 40
        }

        @Test
        @DisplayName("Calculates dynamic lookahead for Phoenix Lamp (550) along cardinal RIGHT vector")
        void testCardinalLookaheadPhoenixLamp() {
            // Player at (50, 50), facing RIGHT (dir=1), moving RIGHT (moveDx=1.0f, moveDy=0.0f), baseDistance=3, light=550
            int[] target = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.calculateTargetTile(
                    50, 50, 1.0f, 0.0f, 1, 3, 550, true
            );

            // Dynamic radius for 550 light is 37 -> lookahead clamped to 16
            assertThat(target[0]).isEqualTo(66); // 50 + 16 = 66
            assertThat(target[1]).isEqualTo(50);
        }

        @Test
        @DisplayName("Projects diagonally when moving NORTHEAST (moveDx > 0, moveDy < 0)")
        void testDiagonalNortheastProjection() {
            int[] target = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.calculateTargetTile(
                    50, 50, 0.7f, -0.7f, 1, 3, 300, true
            );

            // Lookahead 10: X increases by 10, Y decreases by 10
            assertThat(target[0]).isEqualTo(60);
            assertThat(target[1]).isEqualTo(40);
        }

        @Test
        @DisplayName("Projects diagonally when moving SOUTHWEST (moveDx < 0, moveDy > 0)")
        void testDiagonalSouthwestProjection() {
            int[] target = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.calculateTargetTile(
                    50, 50, -0.7f, 0.7f, 2, 3, 300, true
            );

            // Lookahead 10: X decreases by 10, Y increases by 10
            assertThat(target[0]).isEqualTo(40);
            assertThat(target[1]).isEqualTo(60);
        }

        @Test
        @DisplayName("Falls back to cardinal facing direction when player is stationary")
        void testStationaryFallback() {
            int[] target = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.calculateTargetTile(
                    50, 50, 0.0f, 0.0f, 2, 3, 150, false
            );

            // dir=2 (DOWN), baseDistance=3
            assertThat(target[0]).isEqualTo(50);
            assertThat(target[1]).isEqualTo(53);
        }
    }

    @Nested
    @DisplayName("Recent Placement Hysteresis Tracker")
    class HysteresisTests {

        @Test
        @DisplayName("Recent placement suppresses duplicate placement within illumination envelope")
        void testRecentPlacementSuppression() {
            long now = System.currentTimeMillis();
            BetterAutoTorchServerListener.BetterAutoTorchGameLoop.RecentPlacement recent =
                    new BetterAutoTorchServerListener.BetterAutoTorchGameLoop.RecentPlacement(10, 10, 10, now);

            // Target at (16, 10) -> distance is 6 (<= 10)
            int distInside = Math.max(Math.abs(16 - recent.tileX), Math.abs(10 - recent.tileY));
            assertThat(distInside <= recent.lightRadius).isTrue();

            // Target at (25, 10) -> distance is 15 (> 10)
            int distOutside = Math.max(Math.abs(25 - recent.tileX), Math.abs(10 - recent.tileY));
            assertThat(distOutside <= recent.lightRadius).isFalse();
        }
    }

    @ParameterizedTest(name = "Light level {0} calculates lookahead stride {1}")
    @CsvSource({
            "150, 3",  // Vanilla Torch -> base distance 3
            "300, 10", // Better Torch (300) -> radius 20 -> stride 10
            "350, 12", // Steel Lamp (350) -> radius 23 -> stride 12
            "450, 15", // Abyss Lantern (450) -> radius 30 -> stride 15
            "550, 16"  // Phoenix Lamp (550) -> radius 37 -> clamped to 16
    })
    void testDynamicStrideCalculations(int lightLevel, int expectedStride) {
        int[] target = BetterAutoTorchServerListener.BetterAutoTorchGameLoop.calculateTargetTile(
                100, 100, 1.0f, 0.0f, 1, 3, lightLevel, true
        );
        assertThat(target[0] - 100).isEqualTo(expectedStride);
    }
}
