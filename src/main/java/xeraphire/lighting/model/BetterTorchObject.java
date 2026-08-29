package xeraphire.lighting.model;

import necesse.engine.localization.Localization;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;
import necesse.inventory.item.placeableItem.objectItem.TorchObjectItem;
import necesse.inventory.item.toolItem.ToolType;
import necesse.level.gameObject.TorchObject;

import java.awt.Color;

public class BetterTorchObject extends TorchObject {

    public BetterTorchObject(float lightHue, float lightSat, int lightLevel) {
        super("bettertorch", ToolType.ALL, new Color(200, 200, 0), lightHue, lightSat);
        this.lightLevel = OutdoorLampObject.getEffectiveLightLevel(lightLevel);
        this.stackSize = 500;
        this.canPlaceOnShore = true;
        this.displayMapTooltip = true;
        this.setItemCategory("objects", "lighting");
        this.setCraftingCategory("objects", "lighting");
        this.replaceCategories.add("torch");
        this.canReplaceCategories.add("torch");
        this.canReplaceCategories.add("furniture");
        this.canReplaceCategories.add("column");
        this.replaceRotations = false;
        System.out.println("Better Torch Object created with lightHue {" + lightHue + "}, lightSat {" + lightSat + "}, lightLevel {" + this.lightLevel + "}");
    }

    @Override
    public Item generateNewObjectItem() {
        return new TorchObjectItem(this, false);
    }

    @Override
    public void loadTextures() {
        super.loadTextures();
        this.texture = GameTexture.fromFile("objects/bettertorch");
        this.texture_off = GameTexture.fromFile("objects/bettertorch_off");
    }

    @Override
    public ListGameTooltips getItemTooltips(InventoryItem item, PlayerMob perspective) {
        ListGameTooltips tooltips = super.getItemTooltips(item, perspective);
        tooltips.add(Localization.translate("itemtooltip", "lightlevelinfo", "light", this.lightLevel));
        tooltips.add(Localization.translate("itemtooltip", "wirecontrolinfo"));
        tooltips.add(Localization.translate("itemtooltip", "bettertorchtip"));
        return tooltips;
    }

    public String getWallPlaceObjectStringID() {
        return this.wallPlaceObjectStringID;
    }
}
