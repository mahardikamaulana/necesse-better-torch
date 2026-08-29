package xeraphire.lighting.patch;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.server.ServerClient;
import net.bytebuddy.asm.Advice;
import xeraphire.lighting.Config;
import xeraphire.lighting.LightingMod;
import xeraphire.lighting.network.PacketModConfigSync;

public class ServerClientConnectPatch {

    @ModMethodPatch(target = ServerClient.class, name = "onFirstConnecting", arguments = {})
    public static class OnFirstConnectingPatch {
        @Advice.OnMethodExit
        static void onExit(@Advice.This ServerClient client) {
            Config config = LightingMod.getConfig();
            if (config != null) {
                client.sendPacket(new PacketModConfigSync(config));
            }
        }
    }
}
