package xeraphire.lighting.patch;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.level.maps.Level;
import necesse.level.maps.light.StaticLightUpdater;
import necesse.level.maps.light.StaticLightUpdaterCompute;
import net.bytebuddy.asm.Advice;
import xeraphire.lighting.autotorch.BetterAutoTorchServerListener;

import java.lang.reflect.Field;

public class StaticLightUpdaterPatch {

    private static final Field LEVEL_FIELD;

    static {
        Field f = null;
        try {
            f = StaticLightUpdater.class.getDeclaredField("level");
            f.setAccessible(true);
        } catch (Throwable ignored) {
        }
        LEVEL_FIELD = f;
    }

    @ModMethodPatch(target = StaticLightUpdaterCompute.class, name = "<init>", arguments = {StaticLightUpdater.class, int.class, int.class, int.class, int.class})
    public static class ComputeInitPatch {

        @Advice.OnMethodEnter
        public static void onEnter(
                @Advice.Argument(0) StaticLightUpdater updater,
                @Advice.Argument(value = 1, readOnly = false) int minTileX,
                @Advice.Argument(value = 2, readOnly = false) int minTileY,
                @Advice.Argument(value = 3, readOnly = false) int maxTileX,
                @Advice.Argument(value = 4, readOnly = false) int maxTileY
        ) {
            try {
                if (updater == null || LEVEL_FIELD == null) {
                    return;
                }

                Level level = (Level) LEVEL_FIELD.get(updater);
                if (level == null) {
                    return;
                }

                int centerX = (minTileX + maxTileX) / 2;
                int centerY = (minTileY + maxTileY) / 2;
                int currentSpan = Math.max(Math.abs(maxTileX - minTileX), Math.abs(maxTileY - minTileY)) / 2;

                int objID = level.getObjectID(centerX, centerY);
                int lightLevel = 0;
                if (BetterAutoTorchServerListener.LightCache.ITEM_LIGHT_LEVEL_CACHE != null
                        && objID >= 0
                        && objID < BetterAutoTorchServerListener.LightCache.ITEM_LIGHT_LEVEL_CACHE.length) {
                    lightLevel = BetterAutoTorchServerListener.LightCache.ITEM_LIGHT_LEVEL_CACHE[objID];
                }

                if (lightLevel > 150) {
                    int requiredRadius = Math.min(70, (int) Math.ceil(lightLevel / 6.0f) + 2);
                    if (requiredRadius > currentSpan) {
                        minTileX = level.limitTileXToBounds(centerX - requiredRadius);
                        maxTileX = level.limitTileXToBounds(centerX + requiredRadius);
                        minTileY = level.limitTileYToBounds(centerY - requiredRadius);
                        maxTileY = level.limitTileYToBounds(centerY + requiredRadius);
                    }
                }
            } catch (Throwable ignored) {
                // Fail-safe to avoid disrupting normal lighting calculations on unexpected reflection failure
            }
        }
    }
}
