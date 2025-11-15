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

    private float tileWidth;
    private float tileHeight;
    private float mapWidthInPx;
    private float mapHeightInPx;

    private Sound damageTakenSound;
    private Sound coinCollectSound;

    public MapManager(String tmxPath, String damageSoundPath, String coinSoundPath) {
        tiledMap = new TmxMapLoader().load(tmxPath);
        renderer = new OrthogonalTiledMapRenderer(tiledMap);

        foregroundLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Foreground");
        coinLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Coin");
        damageObjects = tiledMap.getLayers().get("Damage").getObjects();

        tileWidth = foregroundLayer.getTileWidth();
        tileHeight = foregroundLayer.getTileHeight();
        mapWidthInPx = foregroundLayer.getWidth() * tileWidth;
        mapHeightInPx = foregroundLayer.getHeight() * tileHeight;

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

    public float getTileWidth() {
        return tileWidth;
    }

    public float getTileHeight() {
        return tileHeight;
    }

    public float getMapWidthInPx() {
        return mapWidthInPx;
    }

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
