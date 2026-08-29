package xeraphire.lighting.model;

import necesse.engine.gameLoop.tickManager.Performance;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.localization.Localization;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.particle.Particle;
import necesse.entity.particle.ParticleOption;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.texture.TextureDrawOptionsEnd;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.inventory.InventoryItem;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;

import java.awt.Rectangle;
import java.util.List;

public class AbyssLanternObject extends OutdoorLampObject {

    private GameTexture texture;
    private GameTexture texture_off;

    public AbyssLanternObject(int lightLevel, float lightHue, float lightSat) {
        super(new Rectangle(4, 16, 24, 14), lightLevel, lightHue, lightSat);
        this.canPlaceOnLiquid = true;
        this.canPlaceOnShore = true;
        this.hoverHitbox = new Rectangle(0, -16, 32, 48);
    }

    @Override
    public void loadTextures() {
        super.loadTextures();
        this.texture = GameTexture.fromFile("objects/abyssLantern");
        this.texture_off = GameTexture.fromFile("objects/abyssLantern_off");
    }

    public float getDesiredHeight(Level level, int tileX, int tileY) {
        if (level.isLiquidTile(tileX, tileY)) {
            long time = level.getWorldEntity() != null ? level.getWorldEntity().getTime() : 0L;
            int seed = Math.abs((tileX * 31 + tileY) * 1000) % 3000;
            float anim = GameUtils.getAnimFloat(time + seed, 3000);
            return GameMath.sin(anim * 360.0F) * 2.5F;
        }
        return 0.0F;
    }

    @Override
    public void addLayerDrawables(List<LevelSortedDrawable> list, OrderableDrawables tileList, Level level, int layerID, int tileX, int tileY, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        GameLight light = level.getLightLevel(tileX, tileY);
        int drawX = camera.getTileDrawX(tileX);
        int drawY = camera.getTileDrawY(tileY);
        int waveOffset = (int) getDesiredHeight(level, tileX, tileY);
        boolean active = this.isActive(level, layerID, tileX, tileY);
        GameTexture tex = active ? this.texture : this.texture_off;
        final TextureDrawOptionsEnd options = tex.initDraw()
                .light(light)
                .pos(drawX, drawY - tex.getHeight() + 32 - waveOffset);

        list.add(new LevelSortedDrawable(this, tileX, tileY) {
            @Override
            public int getSortY() {
                return 20;
            }

            @Override
            public void draw(TickManager tickManager) {
                Performance.record(tickManager, "abyssLanternDraw", () -> options.draw());
            }
        });
    }

    @Override
    public void tickEffect(Level level, int layerID, int tileX, int tileY) {
        if (level.isClient() && this.isActive(level, layerID, tileX, tileY) && GameRandom.globalRandom.getEveryXthChance(30)) {
            float posX = tileX * 32 + 16 + (float) (GameRandom.globalRandom.nextGaussian() * 4.0);
            float posY = tileY * 32 + 20;
            float waveOffset = getDesiredHeight(level, tileX, tileY);
            level.entityManager.addParticle(
                    ParticleOption.standard(posX, posY)
                            .height(18.0f + waveOffset)
                            .color(GameRandom.globalRandom.getChance(0.5F) ? new java.awt.Color(255, 220, 100, 200) : new java.awt.Color(255, 240, 160, 220))
                            .lifeTimeBetween(600, 1100)
                            .movesConstant(0.0f, -6.0f)
                            .sizeFadesInAndOut(4, 8, 100, 300)
                            .givesLight(this.lightHue, this.lightSat),
                    Particle.GType.COSMETIC
            );
        }
    }

    @Override
    public ListGameTooltips getItemTooltips(InventoryItem item, PlayerMob perspective) {
        ListGameTooltips tooltips = super.getItemTooltips(item, perspective);
        tooltips.add(Localization.translate("itemtooltip", "waterplacetip"));
        return tooltips;
    }

    @Override
    public void drawPreview(Level level, int tileX, int tileY, int rotation, float alpha, PlayerMob player, GameCamera camera) {
        int drawX = camera.getTileDrawX(tileX);
        int drawY = camera.getTileDrawY(tileY);
        this.texture.initDraw().sprite(0, 0, 32, this.texture.getHeight()).alpha(alpha).draw(drawX, drawY - (this.texture.getHeight() - 32));
    }
}
