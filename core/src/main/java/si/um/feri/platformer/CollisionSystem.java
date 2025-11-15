package si.um.feri.platformer;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class CollisionSystem {
    private final MapManager mapManager;
    private final TiledMapTileLayer foreground;
    private final TiledMapTileLayer coins;
    private final float tileW;
    private final float tileH;

    public CollisionSystem(MapManager mapManager) {
        this.mapManager = mapManager;
        this.foreground = mapManager.getForegroundLayer();
        this.coins = mapManager.getCoinLayer();
        this.tileW = mapManager.getTileWidth();
        this.tileH = mapManager.getTileHeight();
    }

    /**
     * Checks if rectangle at (x,y,width,height) overlaps any non-null cell in the Foreground layer.
     */
    public boolean collidesWithForeground(float x, float y, float width, float height) {
        int leftTileX = (int) (x / tileW);
        int rightTileX = (int) ((x + width) / tileW);
        int bottomTileY = (int) (y / tileH);
        int topTileY = (int) ((y + height) / tileH);

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
     */
    public void handlePlayerTileCollisions(Player player, HUD hud) {
        // coin collection using player's center tile (same as original)
        float centerX = player.getX() + player.getWidth() / 2f;
        float centerY = player.getY() + player.getHeight() / 2f;
        int tileX = (int) (centerX / tileW);
        int tileY = (int) (centerY / tileH);

        if (tileX >= 0 && tileY >= 0 && tileX < coins.getWidth() && tileY < coins.getHeight()) {
            if (coins.getCell(tileX, tileY) != null) {
                coins.setCell(tileX, tileY, null);
                if (mapManager.getCoinSound() != null) mapManager.getCoinSound().play();
                hud.addScore(10);
            }
        }

        // damage objects (rectangle objects)
        Rectangle playerRect = player.getBoundingRectangle();
        for (MapObject mo : mapManager.getDamageObjects()) {
            if (mo instanceof RectangleMapObject) {
                RectangleMapObject rmo = (RectangleMapObject) mo;
                Rectangle rect = rmo.getRectangle();
                if (playerRect.overlaps(rect)) {
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
