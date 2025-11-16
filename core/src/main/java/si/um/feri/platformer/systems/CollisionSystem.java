package si.um.feri.platformer.systems;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import si.um.feri.platformer.HUD;
import si.um.feri.platformer.managers.MapManager;
import si.um.feri.platformer.entities.Player;

public class CollisionSystem {
    private final MapManager mapManager;
    private final TiledMapTileLayer foreground;
    private final TiledMapTileLayer coins;
    private final float tileW; // world units
    private final float tileH; // world units

    public CollisionSystem(MapManager mapManager) {
        this.mapManager = mapManager;
        this.foreground = mapManager.getForegroundLayer();
        this.coins = mapManager.getCoinLayer();
        this.tileW = mapManager.getTileWidth();   // world units
        this.tileH = mapManager.getTileHeight();  // world units
    }

    /**
     * Checks if rectangle at (x,y,width,height) overlaps any non-null cell in the Foreground layer.
     * All coordinates and sizes are expected in WORLD UNITS.
     */
    public boolean collidesWithForeground(float x, float y, float width, float height) {
        if (foreground == null) return false;

        int leftTileX = (int) Math.floor(x / tileW);
        int rightTileX = (int) Math.floor((x + width - 0.0001f) / tileW); // subtract tiny eps to avoid exact border issues
        int bottomTileY = (int) Math.floor(y / tileH);
        int topTileY = (int) Math.floor((y + height - 0.0001f) / tileH);

        // bounds safety (map may be smaller or negative indexes)
        if (leftTileX < 0) leftTileX = 0;
        if (bottomTileY < 0) bottomTileY = 0;

        for (int tx = leftTileX; tx <= rightTileX; tx++) {
            for (int ty = bottomTileY; ty <= topTileY; ty++) {
                if (tx < 0 || ty < 0) continue;
                if (tx >= foreground.getWidth() || ty >= foreground.getHeight()) continue;
                if (foreground.getCell(tx, ty) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handles coin collection and damage object collisions.
     * Coin = remove coin cell and play coin sound and add score.
     * Damage = check overlap with rectangle MapObjects and reduce health.
     * Player positions and sizes are in WORLD UNITS. Tiled objects are in PIXELS,
     * so we convert them to world units for comparisons.
     */
    public void handlePlayerTileCollisions(Player player, HUD hud) {
        if (coins != null) {
            // coin collection using player's center tile
            float centerX = player.getX() + player.getWidth() / 2f;
            float centerY = player.getY() + player.getHeight() / 2f;
            int tileX = (int) Math.floor(centerX / tileW);
            int tileY = (int) Math.floor(centerY / tileH);

            if (tileX >= 0 && tileY >= 0 && tileX < coins.getWidth() && tileY < coins.getHeight()) {
                if (coins.getCell(tileX, tileY) != null) {
                    coins.setCell(tileX, tileY, null);
                    if (mapManager.getCoinSound() != null) mapManager.getCoinSound().play();
                    hud.addScore(10);
                }
            }
        }

        Rectangle playerRect = player.getBoundingRectangle();
        for (MapObject mo : mapManager.getDamageObjects()) {
            if (mo instanceof RectangleMapObject) {
                RectangleMapObject rmo = (RectangleMapObject) mo;
                Rectangle rectPx = rmo.getRectangle();

                float PPM = mapManager.getTileWidthPx() / mapManager.getTileWidth();

                Rectangle rectWorld = new Rectangle(
                    rectPx.x / PPM,
                    rectPx.y / PPM,
                    rectPx.width / PPM,
                    rectPx.height / PPM
                );

                if (playerRect.overlaps(rectWorld)) {
                    // only play sound when health is full? original played only on first hit if health==100
                    if (hud.getHealth() == 100) {
                        if (mapManager.getDamageSound() != null) mapManager.getDamageSound().play();
                        hud.decreaseHealth(1);
                    } else {
                        hud.decreaseHealth(1);
                    }
                }
            }
        }
    }
}
