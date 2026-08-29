package xeraphire.lighting.model;

import necesse.gfx.gameTexture.GameTexture;
import necesse.level.gameObject.WallTorchObject;
import necesse.level.maps.Level;

public class BetterWallTorchObject extends WallTorchObject {

    public BetterWallTorchObject(float lightHue, float lightSat, int lightLevel) {
        super();
        this.lightHue = lightHue;
        this.lightSat = lightSat;
        this.lightLevel = OutdoorLampObject.getEffectiveLightLevel(lightLevel);
        this.setItemDroppedStringID("bettertorch");
    }

    @Override
    public void loadTextures() {
        super.loadTextures();
        this.texture = GameTexture.fromFile("objects/betterwalltorch");
    }

    @Override
    public int getLightLevel(Level level, int layerID, int tileX, int tileY) {
        return this.isActive(level, layerID, tileX, tileY) ? this.lightLevel : 0;
    }

    @Override
    public boolean isActive(Level level, int layerID, int tileX, int tileY) {
        return !level.wireManager.isWireActiveAny(tileX, tileY);
    }
}
