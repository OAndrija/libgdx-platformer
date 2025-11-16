package si.um.feri.platformer.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.Filter;

public class MapManager {

    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;

    private final Array<Integer> backgroundLayers = new Array<>();
    private final Array<Integer> foregroundLayers = new Array<>();

    private TiledMapTileLayer foregroundLayer;
    private TiledMapTileLayer coinLayer;

    private final Sound damageSound;
    private final Sound coinSound;

    private final int tileWidthPx;
    private final int tileHeightPx;

    private final float tileWidthWorld;
    private final float tileHeightWorld;

    private final int mapWidthInPx;
    private final int mapHeightInPx;

    private final Array<MapObject> damageObjects = new Array<>();

    public MapManager(String mapPath, String damageSoundPath, String coinSoundPath) {

        map = new TmxMapLoader().load(mapPath);
        renderer = new OrthogonalTiledMapRenderer(map, 1f / 32f);

        // Load sounds
        damageSound = Gdx.audio.newSound(Gdx.files.internal(damageSoundPath));
        coinSound   = Gdx.audio.newSound(Gdx.files.internal(coinSoundPath));

        // Get tile size (in pixels)
        TiledMapTileLayer base = (TiledMapTileLayer) map.getLayers().get(0);
        tileWidthPx = base.getTileWidth();
        tileHeightPx = base.getTileHeight();

        // Convert to world units
        tileWidthWorld  = tileWidthPx / 32f;
        tileHeightWorld = tileHeightPx / 32f;

        // Map size
        mapWidthInPx  = base.getWidth() * tileWidthPx;
        mapHeightInPx = base.getHeight() * tileHeightPx;

        classifyLayers();
        extractDamageObjects();
    }

    private void classifyLayers() {
        for (int i = 0; i < map.getLayers().getCount(); i++) {

            MapLayer layer = map.getLayers().get(i);
            String name = layer.getName();

            if (name.equalsIgnoreCase("Foreground")) {
                foregroundLayers.add(i);
                foregroundLayer = (TiledMapTileLayer) layer;
            }

            if (name.equalsIgnoreCase("Coin")) {
                coinLayer = (TiledMapTileLayer) layer;
            }

            if (name.equalsIgnoreCase("Background") ||
                name.equalsIgnoreCase("Trees"))
                backgroundLayers.add(i);

            if (name.equalsIgnoreCase("Spikes"))
                foregroundLayers.add(i);
        }
    }

    private void extractDamageObjects() {
        MapLayer damageLayer = map.getLayers().get("Damage");
        if (damageLayer != null) {
            for (MapObject obj : damageLayer.getObjects()) {
                damageObjects.add(obj);
            }
        }
    }

    // ------------------------------
    // REQUIRED BY CollisionSystem
    // ------------------------------

    public TiledMapTileLayer getForegroundLayer() { return foregroundLayer; }
    public TiledMapTileLayer getCoinLayer() { return coinLayer; }

    public float getTileWidth() { return tileWidthWorld; }
    public float getTileHeight() { return tileHeightWorld; }

    public int getTileWidthPx() { return tileWidthPx; }

    public Sound getCoinSound() { return coinSound; }
    public Sound getDamageSound() { return damageSound; }

    public Array<MapObject> getDamageObjects() { return damageObjects; }

    // ------------------------------
    // Rendering helpers
    // ------------------------------

    private int[] toIntArray(Array<Integer> arr) {
        int[] result = new int[arr.size];
        for (int i = 0; i < arr.size; i++) result[i] = arr.get(i);
        return result;
    }

    public int[] getBackgroundLayerIndices() {
        return toIntArray(backgroundLayers);
    }

    public int[] getForegroundLayerIndices() {
        return toIntArray(foregroundLayers);
    }

    public OrthogonalTiledMapRenderer getRenderer() { return renderer; }

    public int getMapWidthInPx() { return mapWidthInPx; }
    public int getMapHeightInPx() { return mapHeightInPx; }

    // -----------------------------------
    // Toggle visibility
    // -----------------------------------
    public void toggleLayerVisibility(String name) {
        MapLayer layer = map.getLayers().get(name);
        if (layer != null) layer.setVisible(!layer.isVisible());
    }

    public int[] getCoinLayerIndex() {
        if (coinLayer == null) return new int[0];
        return new int[] { map.getLayers().getIndex(coinLayer) };
    }


    // --------------------------------------------------------
// Build Box2D collision from the Foreground layer
// --------------------------------------------------------
    public void buildCollision(World world) {
        if (foregroundLayer == null) return;

        for (int x = 0; x < foregroundLayer.getWidth(); x++) {
            for (int y = 0; y < foregroundLayer.getHeight(); y++) {
                if (foregroundLayer.getCell(x, y) != null) {
                    createTileBody(world, x, y);
                }
            }
        }
    }

    // --------------------------------------------------------
// Create a single tile body with Box2D Lights shadow filtering
// --------------------------------------------------------
    private void createTileBody(World world, int x, int y) {

        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bd);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            tileWidthWorld / 2f,
            tileHeightWorld / 2f,
            new Vector2(
                (x + 0.5f) * tileWidthWorld,
                (y + 0.5f) * tileHeightWorld
            ),
            0f
        );

        // IMPORTANT: get the fixture reference
        Fixture fixture = body.createFixture(shape, 0);

        // ---------- LIGHTING FILTER (required to cast shadows) ----------
        Filter filter = new Filter();
        filter.categoryBits = 0x0002;   // solid tile category
        filter.maskBits     = -1;       // collide with all (lights included)
        fixture.setFilterData(filter);
        // ----------------------------------------------------------------

        shape.dispose();
    }

    public void dispose() {
        map.dispose();
        renderer.dispose();
        coinSound.dispose();
        damageSound.dispose();
    }
}

