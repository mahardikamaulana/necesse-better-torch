package xeraphire.lighting.patch;

import autotorch.AutoTorchMod;
import necesse.engine.GameEvents;
import necesse.engine.events.ServerStartEvent;
import necesse.engine.input.Control;
import necesse.engine.localization.message.StaticMessage;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.Packet;
import necesse.engine.registries.PacketRegistry;
import net.bytebuddy.asm.Advice;
import xeraphire.lighting.autotorch.BetterAutoTorchServerListener;

public class AutoTorchModPatch {

    @ModMethodPatch(target = AutoTorchMod.class, name = "init", arguments = {})
    public static class InitPatch {
        @SuppressWarnings("unchecked")
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter() {
            try {
                // Register AutoTorch Network Packets
                Class<? extends Packet> placePacketCls = (Class<? extends Packet>) Class.forName("autotorch.AutoTorchMod$AutoTorchPlacePacket");
                Class<? extends Packet> togglePacketCls = (Class<? extends Packet>) Class.forName("autotorch.AutoTorchMod$PacketAutoTorchToggle");
                PacketRegistry.registerPacket(placePacketCls);
                PacketRegistry.registerPacket(togglePacketCls);

                // Register Toggle Key Control (Default 'V')
                AutoTorchMod.TOGGLE_CONTROL = Control.addModControl(new Control(86, "autotorch_toggle", new StaticMessage("AutoTorch Toggle")));

                // Register Enhanced Server Listener
                GameEvents.addListener(ServerStartEvent.class, new BetterAutoTorchServerListener());

                System.out.println("[Let there be Light!] AutoTorch successfully patched with Better Torch support!");
                return true; // Skip original AutoTorchMod.init() to avoid default limited listener
            } catch (Throwable t) {
                System.err.println("[Let there be Light!] Error during AutoTorch init patch: " + t.getMessage());
                t.printStackTrace();
                return false; // Fallback to original init() on error
            }
        }
    }
}
