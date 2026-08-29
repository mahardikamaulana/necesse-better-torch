package xeraphire.lighting.network;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import xeraphire.lighting.Config;
import xeraphire.lighting.LightingMod;

public class PacketModConfigSync extends Packet {

    public final String recipeDifficulty;
    public final int torchLightLevel;
    public final float torchLightHue;
    public final float torchLightSat;
    public final int steelLampLightLevel;
    public final int woodenLanternLightLevel;
    public final int phoenixLampLightLevel;
    public final int abyssLanternLightLevel;
    public final float outdoorLampHue;
    public final float outdoorLampSat;
    public final float abyssLanternHue;
    public final float abyssLanternSat;
    public final boolean autoTorchIntegration;
    public final String autoTorchTorchSelection;
    public final String autoTorchAnyLightPriority;
    public final boolean autoTorchConsiderAllLights;
    public final boolean autoTorchDynamicTravelSpacing;
    public final int autoTorchMinLightThreshold;

    public PacketModConfigSync(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        this.recipeDifficulty = reader.getNextString();
        this.torchLightLevel = reader.getNextInt();
        this.torchLightHue = reader.getNextFloat();
        this.torchLightSat = reader.getNextFloat();
        this.steelLampLightLevel = reader.getNextInt();
        this.woodenLanternLightLevel = reader.getNextInt();
        this.phoenixLampLightLevel = reader.getNextInt();
        this.abyssLanternLightLevel = reader.getNextInt();
        this.outdoorLampHue = reader.getNextFloat();
        this.outdoorLampSat = reader.getNextFloat();
        this.abyssLanternHue = reader.getNextFloat();
        this.abyssLanternSat = reader.getNextFloat();
        this.autoTorchIntegration = reader.getNextBoolean();
        this.autoTorchTorchSelection = reader.getNextString();
        this.autoTorchAnyLightPriority = reader.getNextString();
        this.autoTorchConsiderAllLights = reader.getNextBoolean();
        this.autoTorchDynamicTravelSpacing = reader.getNextBoolean();
        this.autoTorchMinLightThreshold = reader.getNextInt();
    }

    public PacketModConfigSync(Config config) {
        this.recipeDifficulty = config.getRecipeDifficulty();
        this.torchLightLevel = config.getTorchLightLevel();
        this.torchLightHue = config.getTorchLightHue();
        this.torchLightSat = config.getTorchLightSat();
        this.steelLampLightLevel = config.getSteelLampLightLevel();
        this.woodenLanternLightLevel = config.getWoodenLanternLightLevel();
        this.phoenixLampLightLevel = config.getPhoenixLampLightLevel();
        this.abyssLanternLightLevel = config.getAbyssLanternLightLevel();
        this.outdoorLampHue = config.getOutdoorLampHue();
        this.outdoorLampSat = config.getOutdoorLampSat();
        this.abyssLanternHue = config.getAbyssLanternHue();
        this.abyssLanternSat = config.getAbyssLanternSat();
        this.autoTorchIntegration = config.isAutoTorchIntegration();
        this.autoTorchTorchSelection = config.getAutoTorchTorchSelection();
        this.autoTorchAnyLightPriority = config.getAutoTorchAnyLightPriority();
        this.autoTorchConsiderAllLights = config.isAutoTorchConsiderAllLights();
        this.autoTorchDynamicTravelSpacing = config.isAutoTorchDynamicTravelSpacing();
        this.autoTorchMinLightThreshold = config.getAutoTorchMinLightThreshold();

        PacketWriter writer = new PacketWriter(this);
        writer.putNextString(this.recipeDifficulty);
        writer.putNextInt(this.torchLightLevel);
        writer.putNextFloat(this.torchLightHue);
        writer.putNextFloat(this.torchLightSat);
        writer.putNextInt(this.steelLampLightLevel);
        writer.putNextInt(this.woodenLanternLightLevel);
        writer.putNextInt(this.phoenixLampLightLevel);
        writer.putNextInt(this.abyssLanternLightLevel);
        writer.putNextFloat(this.outdoorLampHue);
        writer.putNextFloat(this.outdoorLampSat);
        writer.putNextFloat(this.abyssLanternHue);
        writer.putNextFloat(this.abyssLanternSat);
        writer.putNextBoolean(this.autoTorchIntegration);
        writer.putNextString(this.autoTorchTorchSelection);
        writer.putNextString(this.autoTorchAnyLightPriority);
        writer.putNextBoolean(this.autoTorchConsiderAllLights);
        writer.putNextBoolean(this.autoTorchDynamicTravelSpacing);
        writer.putNextInt(this.autoTorchMinLightThreshold);
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        Config config = LightingMod.getConfig();
        if (config != null) {
            config.applyServerSync(
                    this.recipeDifficulty,
                    this.torchLightLevel,
                    this.torchLightHue,
                    this.torchLightSat,
                    this.steelLampLightLevel,
                    this.woodenLanternLightLevel,
                    this.phoenixLampLightLevel,
                    this.abyssLanternLightLevel,
                    this.outdoorLampHue,
                    this.outdoorLampSat,
                    this.abyssLanternHue,
                    this.abyssLanternSat,
                    this.autoTorchIntegration,
                    this.autoTorchTorchSelection,
                    this.autoTorchAnyLightPriority,
                    this.autoTorchConsiderAllLights,
                    this.autoTorchDynamicTravelSpacing,
                    this.autoTorchMinLightThreshold
            );
        }
    }
}
