package xeraphire.lighting.model;

import necesse.engine.gameLoop.tickManager.Performance;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.projectile.BombProjectile;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.texture.TextureDrawOptionsEnd;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;

import java.awt.Rectangle;
import java.util.List;

public class SteelLampPostObject extends OutdoorLampObject {

    private GameTexture texture;
    private GameTexture texture_off;

    public SteelLampPostObject(int lightLevel, float lightHue, float lightSat) {
        super(new Rectangle(12, 20, 10, 26), lightLevel, lightHue, lightSat);
    }

    @Override
    public void loadTextures() {
        super.loadTextures();
        this.texture = GameTexture.fromFile("objects/steelLampPost");
        this.texture_off = GameTexture.fromFile("objects/steelLampPost_off");
    }

    @Override
    public void addLayerDrawables(List<LevelSortedDrawable> list, OrderableDrawables tileList, Level level, int layerID, int tileX, int tileY, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        GameLight light = level.getLightLevel(tileX, tileY);
        int drawX = camera.getTileDrawX(tileX);
        int drawY = camera.getTileDrawY(tileY);
        boolean active = this.isActive(level, layerID, tileX, tileY);
        GameTexture tex = active ? this.texture : this.texture_off;
        final TextureDrawOptionsEnd options = tex.initDraw().light(light).pos(drawX, drawY - tex.getHeight() + 32);
        list.add(new LevelSortedDrawable(this, tileX, tileY) {
            @Override
            public int getSortY() {
                return 26;
            }

            @Override
            public void draw(TickManager tickManager) {
                Performance.record(tickManager, "steelLampDraw", () -> options.draw());
            }
        });
    }

    @Override
    public void tickEffect(Level level, int layerID, int tileX, int tileY) {
        if (level.isClient() && this.isActive(level, layerID, tileX, tileY) && GameRandom.globalRandom.getEveryXthChance(40)) {
            int startHeight = 50 + (int) (GameRandom.globalRandom.nextGaussian() * 2.0);
            BombProjectile.spawnFuseParticle(level, (float) (tileX * 32 + 16), (float) (tileY * 32 + 16 + 2), (float) startHeight, this.flameHue, this.smokeHue);
        }
    }

    @Override
    public void drawPreview(Level level, int tileX, int tileY, int rotation, float alpha, PlayerMob player, GameCamera camera) {
        int drawX = camera.getTileDrawX(tileX);
        int drawY = camera.getTileDrawY(tileY);
        this.texture.initDraw().sprite(0, 0, 32, this.texture.getHeight()).alpha(alpha).draw(drawX, drawY - (this.texture.getHeight() - 32));
    }
}
