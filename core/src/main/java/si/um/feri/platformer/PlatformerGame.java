package si.um.feri.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
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

    // Hollow Knight Look-Ahead variables
    private float lookAheadX = 0f;
    private float lookAheadTarget = 0f;

    @Override
    public void create() {

        // Box2D world
        world = new World(new com.badlogic.gdx.math.Vector2(0, 0), true);

        // Lighting system
        lightingManager = new LightingManager(world);
        lightingManager.createPlayerLight();

        // Cameras
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
            mapManager.getMapHeightInPx() / PPM);

        camera.zoom = 0.7f;   // Hollow Knight style zoom-in
        camera.update();

        player = new Player(world);

        hud = new HUD();
        hud.setHealth(100);
        hud.setScore(0);

        collisionSystem = new CollisionSystem(mapManager);

        player.setPosition(player.getWidth(), 160f / PPM);
    }

    @Override
    public void render() {

        ScreenUtils.clear(0, 0, 0, 1);

        float dt = Gdx.graphics.getDeltaTime();
        handleConfigurationInput();
        world.step(dt, 6, 2);

        // --- PLAYER UPDATE ---
        if (hud.getHealth() > 0 && hud.getScore() < 40) {
            player.handleInput(dt);
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

        // --- LIGHTING UPDATE ---
        lightingManager.updatePlayerLight(
            player.getCenterX(),
            player.getCenterY(),
            dt
        );

        // =========================================================================
        //                    H O L L O W   K N I G H T   C A M E R A
        // =========================================================================

        float lerp = 5f * dt; // smooth camera follow
        float targetX = player.getCenterX();
        float targetY = player.getCenterY();

        // --- LOOK AHEAD LOGIC ---
        float playerVelocityX = player.getCandidateX() - player.getX();

        if (Math.abs(playerVelocityX) > 0.001f) {
            // player is moving left/right
            float direction = Math.signum(playerVelocityX);
            lookAheadTarget = direction * 1.5f;  // distance camera looks ahead in world units
        } else {
            // player stopped -> relax back to center
            lookAheadTarget = 0f;
        }

        // Smooth interpolation of look-ahead offset
        lookAheadX += (lookAheadTarget - lookAheadX) * 2.5f * dt;

        // Apply follow + look-ahead
        float desiredX = targetX + lookAheadX;
        float desiredY = targetY;

        camera.position.x += (desiredX - camera.position.x) * lerp;
        camera.position.y += (desiredY - camera.position.y) * lerp;

        // --- CAMERA BOUNDARIES ---
        float halfW = camera.viewportWidth * camera.zoom * 0.5f;
        float halfH = camera.viewportHeight * camera.zoom * 0.5f;

        float mapW = mapManager.getMapWidthInPx() / PPM;
        float mapH = mapManager.getMapHeightInPx() / PPM;

        // clamp
        camera.position.x = MathUtils.clamp(camera.position.x, halfW, mapW - halfW);
        camera.position.y = MathUtils.clamp(camera.position.y, halfH, mapH - halfH);

        camera.update();

        // =========================================================================
        //                               R E N D E R
        // =========================================================================

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        lightingManager.getRayHandler().setCombinedMatrix(camera);
        lightingManager.getRayHandler().updateAndRender();

        tiledMapRenderer.getBatch().setProjectionMatrix(camera.combined);
        tiledMapRenderer.getBatch().begin();
        player.draw(tiledMapRenderer.getBatch());
        tiledMapRenderer.getBatch().end();

        // --- HUD ---
        uiCamera.update();
        tiledMapRenderer.getBatch().setProjectionMatrix(uiCamera.combined);
        tiledMapRenderer.getBatch().begin();
        hud.draw(tiledMapRenderer.getBatch());
        tiledMapRenderer.getBatch().end();
    }

    private void handleConfigurationInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) mapManager.toggleLayerVisibility("Background");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) mapManager.toggleLayerVisibility("Foreground");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) mapManager.toggleLayerVisibility("Coin");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) mapManager.toggleLayerVisibility("Trees");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) mapManager.toggleLayerVisibility("Spikes");
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
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
