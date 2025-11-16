package si.um.feri.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class MapManager {
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer renderer;

    private TiledMapTileLayer foregroundLayer;
    private TiledMapTileLayer coinLayer;
    private MapObjects damageObjects;

    private float tileWidthPx;
    private float tileHeightPx;
    private float mapWidthInPx;
    private float mapHeightInPx;
    private static final float PPM = 32f;

    private Sound damageTakenSound;
    private Sound coinCollectSound;

    public MapManager(String tmxPath, String damageSoundPath, String coinSoundPath) {
        tiledMap = new TmxMapLoader().load(tmxPath);

        // renderer uses unitScale to convert pixels -> world units
        renderer = new OrthogonalTiledMapRenderer(tiledMap, 1f / PPM);

        foregroundLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Foreground");
        coinLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Coin");
        damageObjects = tiledMap.getLayers().get("Damage").getObjects();

        // store pixel sizes (Tiled stores tile sizes in pixels)
        if (foregroundLayer != null) {
            tileWidthPx = foregroundLayer.getTileWidth();
            tileHeightPx = foregroundLayer.getTileHeight();
            mapWidthInPx = foregroundLayer.getWidth() * tileWidthPx;
            mapHeightInPx = foregroundLayer.getHeight() * tileHeightPx;
        } else {
            // fallback defaults (in pixels)
            tileWidthPx = 32f;
            tileHeightPx = 32f;
            mapWidthInPx = 0f;
            mapHeightInPx = 0f;
        }

        damageTakenSound = Gdx.audio.newSound(Gdx.files.internal(damageSoundPath));
        coinCollectSound = Gdx.audio.newSound(Gdx.files.internal(coinSoundPath));
    }

    public TiledMapTileLayer getForegroundLayer() {
        return foregroundLayer;
    }

    public TiledMapTileLayer getCoinLayer() {
        return coinLayer;
    }

    public MapObjects getDamageObjects() {
        return damageObjects;
    }

    public OrthogonalTiledMapRenderer getRenderer() {
        return renderer;
    }

    /**
     * Returns tile width in WORLD UNITS (pixels / PPM).
     */
    public float getTileWidth() {
        return tileWidthPx / PPM;
    }

    /**
     * Returns tile height in WORLD UNITS (pixels / PPM).
     */
    public float getTileHeight() {
        return tileHeightPx / PPM;
    }

    /**
     * Returns tile width in PIXELS (for callers that need pixel values).
     */
    public float getTileWidthPx() {
        return tileWidthPx;
    }

    /**
     * Returns tile height in PIXELS (for callers that need pixel values).
     */
    public float getTileHeightPx() {
        return tileHeightPx;
    }

    /**
     * map width in PIXELS (kept as original for camera conversion in PlatformerGame).
     */
    public float getMapWidthInPx() {
        return mapWidthInPx;
    }

    /**
     * map height in PIXELS (kept as original for camera conversion in PlatformerGame).
     */
    public float getMapHeightInPx() {
        return mapHeightInPx;
    }

    public void toggleLayerVisibility(String layerName) {
        if (tiledMap.getLayers().get(layerName) != null) {
            boolean visible = tiledMap.getLayers().get(layerName).isVisible();
            tiledMap.getLayers().get(layerName).setVisible(!visible);
        }
    }

    public Sound getDamageSound() {
        return damageTakenSound;
    }

    public Sound getCoinSound() {
        return coinCollectSound;
    }

    public void dispose() {
        if (tiledMap != null) tiledMap.dispose();
        if (renderer != null) renderer.dispose();
        if (damageTakenSound != null) damageTakenSound.dispose();
        if (coinCollectSound != null) coinCollectSound.dispose();
    }
}
