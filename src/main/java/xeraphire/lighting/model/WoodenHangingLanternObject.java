package xeraphire.lighting.model;

import necesse.engine.gameLoop.tickManager.Performance;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.texture.TextureDrawOptionsEnd;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;

import java.util.List;

public class WoodenHangingLanternObject extends OutdoorLampObject {

    private GameTexture texture;
    private GameTexture texture_off;

    public WoodenHangingLanternObject(int lightLevel, float lightHue, float lightSat) {
        super(lightLevel, lightHue, lightSat);
    }

    @Override
    public void loadTextures() {
        super.loadTextures();
        this.texture = GameTexture.fromFile("objects/woodenHangingLantern");
        this.texture_off = GameTexture.fromFile("objects/woodenHangingLantern_off");
    }

    @Override
    public void addLayerDrawables(List<LevelSortedDrawable> list, OrderableDrawables tileList, Level level, int layerID, int tileX, int tileY, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        GameLight light = level.getLightLevel(tileX, tileY);
        int drawX = camera.getTileDrawX(tileX);
        int drawY = camera.getTileDrawY(tileY);
        byte rotation = level.getObjectRotation(layerID, tileX, tileY);
        int spriteIndex = (rotation == 0 || rotation == 3) ? 1 : 0;
        boolean active = this.isActive(level, layerID, tileX, tileY);
        GameTexture tex = active ? this.texture : this.texture_off;
        final TextureDrawOptionsEnd options = tex.initDraw()
                .sprite(spriteIndex, 0, 64, 64)
                .light(light)
                .pos(drawX - 32, drawY - 32);

        list.add(new LevelSortedDrawable(this, tileX, tileY) {
            @Override
            public int getSortY() {
                return 32;
            }

            @Override
            public void draw(TickManager tickManager) {
                Performance.record(tickManager, "woodenLanternDraw", () -> options.draw());
            }
        });
    }

    @Override
    public void drawPreview(Level level, int tileX, int tileY, int rotation, float alpha, PlayerMob player, GameCamera camera) {
        int drawX = camera.getTileDrawX(tileX);
        int drawY = camera.getTileDrawY(tileY);
        int spriteIndex = (rotation == 0 || rotation == 3) ? 1 : 0;
        int posX = (rotation == 0 || rotation == 3) ? (drawX - 32) : drawX;
        this.texture.initDraw().sprite(spriteIndex, 0, 64, 64).alpha(alpha).draw(posX, drawY - (this.texture.getHeight() - 32));
    }
}
