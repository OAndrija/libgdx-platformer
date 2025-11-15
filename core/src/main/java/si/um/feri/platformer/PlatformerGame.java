package si.um.feri.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;

public class PlatformerGame extends ApplicationAdapter {
    private static final float PPM = 32f;

    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;

    private World world;
    private LightingManager lightingManager;

    private MapManager mapManager;
    private Player player;
    private CollisionSystem collisionSystem;
    private HUD hud;
    private OrthogonalTiledMapRenderer tiledMapRenderer;

    @Override
    public void create() {

        // Box2D world
        world = new World(new Vector2(0, 0), true);

        // Lighting system
        lightingManager = new LightingManager(world);
        lightingManager.createPlayerLight();

        // Camera
        camera = new OrthographicCamera();
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        mapManager = new MapManager(
            "tiled/MyMap.tmx",
            "sounds/damage-taken.mp3",
            "sounds/coin-collected.mp3"
        );

        tiledMapRenderer = mapManager.getRenderer();

        camera.setToOrtho(false,
            mapManager.getMapWidthInPx() / PPM,
            mapManager.getMapHeightInPx() / PPM
        );

        camera.update();

        player = new Player(world);   // <-- player now uses Box2D body

        hud = new HUD();
        hud.setHealth(100);
        hud.setScore(0);

        collisionSystem = new CollisionSystem(mapManager);

        // initial position
        player.setPosition(player.getWidth(), 160f);
    }

    @Override
    public void render() {

        ScreenUtils.clear(0, 0, 0, 1);

        float dt = Gdx.graphics.getDeltaTime();

        handleConfigurationInput();

        world.step(dt, 6, 2);

        // --- Gameplay update ---
        if (hud.getHealth() > 0 && hud.getScore() < 40) {
            player.handleInput();
            player.updatePhysics(dt);

            float cx = player.getCandidateX();
            float cy = player.getCandidateY();

            boolean colX = collisionSystem.collidesWithForeground(
                cx, player.getY(),
                player.getWidth(), player.getHeight()
            );

            if (!colX) player.commitX(cx);

            boolean colY = collisionSystem.collidesWithForeground(
                player.getX(), cy,
                player.getWidth(), player.getHeight()
            );

            if (!colY) {
                player.commitY(cy);
            } else {
                if (player.getVelocityY() < 0) player.setJumping(false);
                player.setVelocityY(0);
            }

            collisionSystem.handlePlayerTileCollisions(player, hud);
        }

        // --- Update player light ---
        lightingManager.updatePlayerLight(
            player.getCenterX(),
            player.getCenterY()
        );

        // --- WORLD RENDER ---
        camera.update();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        // draw player
        tiledMapRenderer.getBatch().setProjectionMatrix(camera.combined);
        tiledMapRenderer.getBatch().begin();
        player.draw(tiledMapRenderer.getBatch());
        tiledMapRenderer.getBatch().end();

        // lights
        lightingManager.getRayHandler().setCombinedMatrix(camera);
        lightingManager.getRayHandler().updateAndRender();

        // --- HUD RENDER (SCREEN CAMERA) ---
        uiCamera.update();
        tiledMapRenderer.getBatch().setProjectionMatrix(uiCamera.combined);
        tiledMapRenderer.getBatch().begin();
        hud.draw(tiledMapRenderer.getBatch());
        tiledMapRenderer.getBatch().end();
    }


    private void handleConfigurationInput() {
        // layer toggles and exit as in original
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            mapManager.toggleLayerVisibility("Background");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            mapManager.toggleLayerVisibility("Foreground");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            mapManager.toggleLayerVisibility("Coin");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            mapManager.toggleLayerVisibility("Trees");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            mapManager.toggleLayerVisibility("Spikes");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void dispose() {
        mapManager.dispose();
        player.dispose();
        hud.dispose();
        lightingManager.dispose();
        world.dispose();
    }
}
