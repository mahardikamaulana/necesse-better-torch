package xeraphire.lighting.patch;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.level.gameObject.WallTorchObject;
import necesse.level.maps.Level;
import net.bytebuddy.asm.Advice;

public class WallTorchObjectPatch {

    @ModMethodPatch(target = WallTorchObject.class, name = "isActive", arguments = {Level.class, int.class, int.class, int.class})
    public static class IsActivePatch {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit
        static void onExit(@Advice.Argument(0) Level level, @Advice.Argument(2) int tileX, @Advice.Argument(3) int tileY, @Advice.Return(readOnly = false) boolean returned) {
            returned = !level.wireManager.isWireActiveAny(tileX, tileY);
        }
    }

    @ModMethodPatch(target = WallTorchObject.class, name = "onWireUpdate", arguments = {Level.class, int.class, int.class, int.class, int.class, boolean.class})
    public static class OnWireUpdatePatch {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        static boolean onEnter(@Advice.Argument(0) Level level, @Advice.Argument(2) int tileX, @Advice.Argument(3) int tileY) {
            level.lightManager.updateStaticLight(tileX, tileY, tileX, tileY, true);
            return true;
        }
    }

    @ModMethodPatch(target = WallTorchObject.class, name = "tickEffect", arguments = {Level.class, int.class, int.class, int.class})
    public static class TickEffectPatch {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        static boolean onEnter(@Advice.This WallTorchObject wallTorch, @Advice.Argument(0) Level level, @Advice.Argument(1) int layerID, @Advice.Argument(2) int tileX, @Advice.Argument(3) int tileY) {
            return !level.isClient() || !wallTorch.isActive(level, layerID, tileX, tileY);
        }
    }
}
