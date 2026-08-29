package xeraphire.lighting.model;

import necesse.engine.localization.Localization;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.particle.ParticleOption;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;
import necesse.inventory.item.placeableItem.objectItem.TorchObjectItem;
import necesse.inventory.item.toolItem.ToolType;
import necesse.level.gameObject.GameObject;
import necesse.level.maps.Level;

import java.awt.Color;
import java.awt.Rectangle;

public abstract class OutdoorLampObject extends GameObject {

    protected float flameHue;
    protected float smokeHue;

    public OutdoorLampObject(Rectangle collision, int lightLevel, float lightHue, float lightSat) {
        super(collision);
        initLamp(lightLevel, lightHue, lightSat);
    }

    public OutdoorLampObject(int lightLevel, float lightHue, float lightSat) {
        super();
        initLamp(lightLevel, lightHue, lightSat);
    }

    public static int getEffectiveLightLevel(int configuredLevel) {
        if (configuredLevel <= 300) {
            return configuredLevel;
        }
        // Adaptive performance guardrail: 400 light level (~67 tiles radius) guarantees pure maximum
        // daylight (255) across full 4K viewports while preventing exponential flood-fill tile explosion.
        return Math.min(configuredLevel, 400);
    }

    private void initLamp(int lightLevel, float lightHue, float lightSat) {
        this.hoverHitbox = new Rectangle(0, -32, 32, 64);
        this.toolType = ToolType.ALL;
        this.isLightTransparent = true;
        this.flameHue = ParticleOption.defaultFlameHue;
        this.smokeHue = ParticleOption.defaultSmokeHue;
        this.mapColor = new Color(240, 200, 10);
        this.displayMapTooltip = true;
        this.lightLevel = getEffectiveLightLevel(lightLevel);
        this.drawDamage = false;
        this.objectHealth = 1;
        this.stackSize = 500;
        this.roomProperties.add("lights");
        this.setItemCategory("objects", "lighting");
        this.setCraftingCategory("objects", "lighting");
        this.lightHue = lightHue;
        this.lightSat = lightSat;
        this.canPlaceOnShore = true;
        this.replaceCategories.add("torch");
        this.canReplaceCategories.add("torch");
        this.canReplaceCategories.add("furniture");
        this.canReplaceCategories.add("column");
        this.replaceRotations = false;
    }

    @Override
    public Item generateNewObjectItem() {
        return new TorchObjectItem(this, false);
    }

    @Override
    public ListGameTooltips getItemTooltips(InventoryItem item, PlayerMob perspective) {
        ListGameTooltips tooltips = super.getItemTooltips(item, perspective);
        tooltips.add(Localization.translate("itemtooltip", "lightlevelinfo", "light", this.lightLevel));
        tooltips.add(Localization.translate("itemtooltip", "wirecontrolinfo"));
        tooltips.add(Localization.translate("itemtooltip", this.getStringID() + "tip"));
        return tooltips;
    }

    @Override
    public int getLightLevel(Level level, int layerID, int tileX, int tileY) {
        return this.isActive(level, layerID, tileX, tileY) ? this.lightLevel : 0;
    }

    public boolean isActive(Level level, int layerID, int tileX, int tileY) {
        return !level.wireManager.isWireActiveAny(tileX, tileY);
    }

    @Override
    public void onWireUpdate(Level level, int layerID, int tileX, int tileY, int wireID, boolean active) {
        level.lightManager.updateStaticLight(tileX, tileY, tileX, tileY, true);
    }
}
