package xeraphire.lighting.integration;

import necesse.engine.registries.LevelRegistry;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.util.LevelIdentifier;
import necesse.engine.world.WorldEntity;
import necesse.gfx.drawables.LevelDrawUtils;
import necesse.level.maps.Level;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xeraphire.lighting.Config;
import xeraphire.lighting.LightingMod;
import xeraphire.lighting.TestRegistries;
import xeraphire.lighting.autotorch.BetterAutoTorchServerListener;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoTorchSpatialSimulationTest {

    private static Config config;
    private static BetterAutoTorchServerListener.BetterAutoTorchGameLoop gameLoop;

    public static class HeadlessTestLevel extends Level {
        private final Map<Point, Integer> grid = new HashMap<>();

        public HeadlessTestLevel(LevelIdentifier identifier, int width, int height, WorldEntity worldEntity) {
            super(identifier, width, height, worldEntity);
        }

        public HeadlessTestLevel() {
            this(new LevelIdentifier("test"), 100, 100, null);
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
            LevelRegistry.registerLevel("headless_test_level", HeadlessTestLevel.class);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        config = LightingMod.getConfig();
        BetterAutoTorchServerListener.LightCache.init(config);
        gameLoop = new BetterAutoTorchServerListener.BetterAutoTorchGameLoop(null);
    }

    @Test
    @DisplayName("isTorchNearby detects existing Better Torch within radius on Level grid")
    void testDetectsNearbyBetterTorch() {
        HeadlessTestLevel level = new HeadlessTestLevel();
        int betterTorchID = ObjectRegistry.getObjectID("bettertorch");
        level.setTestObject(10, 10, betterTorchID);

        // Target at (12, 10) with search radius 20 (half-radius 10)
        boolean nearby = gameLoop.isTorchNearby(12, 10, level, 20, true);
        assertThat(nearby).isTrue();

        // Far away tile (50, 50)
        boolean farAway = gameLoop.isTorchNearby(50, 50, level, 20, true);
        assertThat(farAway).isFalse();
    }

    @Test
    @DisplayName("considerAllLights=true detects Phoenix Lamp across larger fixture radius")
    void testDetectsPhoenixLampWithConsiderAllLightsTrue() {
        HeadlessTestLevel level = new HeadlessTestLevel();
        int phoenixLampID = ObjectRegistry.getObjectID("phoenixLamp");
        level.setTestObject(20, 20, phoenixLampID);

        // Within Phoenix Lamp coverage zone (dist <= 18)
        boolean detected = gameLoop.isTorchNearby(25, 20, level, 30, true);
        assertThat(detected).isTrue();
    }

    @Test
    @DisplayName("considerAllLights=false ignores decorative lamps and only checks basic torches")
    void testIgnoresLampsWhenConsiderAllLightsFalse() {
        HeadlessTestLevel level = new HeadlessTestLevel();
        int phoenixLampID = ObjectRegistry.getObjectID("phoenixLamp");
        level.setTestObject(20, 20, phoenixLampID);

        // Even if Phoenix Lamp is nearby, considerAllLights=false ignores it
        boolean detected = gameLoop.isTorchNearby(22, 20, level, 30, false);
        assertThat(detected).isFalse();
    }
}
