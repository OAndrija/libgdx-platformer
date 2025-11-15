package si.um.feri.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class PlatformerGame extends ApplicationAdapter {
    private OrthographicCamera camera;

    private MapManager mapManager;
    private Player player;
    private CollisionSystem collisionSystem;
    private HUD hud;
    private OrthogonalTiledMapRenderer tiledMapRenderer;

    @Override
    public void create() {
        // Camera
        camera = new OrthographicCamera();

        // Map manager (loads map, layers, sounds)
        mapManager = new MapManager("tiled/MyMap.tmx",
            "sounds/damage-taken.mp3",
            "sounds/coin-collected.mp3");

        tiledMapRenderer = mapManager.getRenderer();

        // Camera sized to full map
        camera.setToOrtho(false, mapManager.getMapWidthInPx(), mapManager.getMapHeightInPx());
        camera.update();

        player = new Player();

        // HUD
        hud = new HUD();
        hud.setHealth(100);
        hud.setScore(0);

        // Collision system (needs map + sounds)
        collisionSystem = new CollisionSystem(mapManager);

        // initial player position (similar to original)
        player.setPosition(player.getWidth(), 160f);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        handleConfigurationInput();

        float dt = Gdx.graphics.getDeltaTime();

        if (hud.getHealth() > 0 && hud.getScore() < 40) {
            // Player processes input & physics
            player.handleInput();
            player.updatePhysics(dt);

            // collision resolution with tile foreground
            // compute candidate new positions and ask for collisions
            float candidateX = player.getCandidateX();
            float candidateY = player.getCandidateY();

            // check X collision (freeze X if colliding)
            boolean collisionX = collisionSystem.collidesWithForeground(candidateX, player.getY(), player.getWidth(), player.getHeight());
            if (!collisionX) {
                player.commitX(candidateX);
            }

            // check Y collision (freeze Y if colliding)
            boolean collisionY = collisionSystem.collidesWithForeground(player.getX(), candidateY, player.getWidth(), player.getHeight());
            if (!collisionY) {
                player.commitY(candidateY);
            } else {
                // landed on ground - reset vertical velocity as in original
                if (player.getVelocityY() < 0) {
                    player.setJumping(false);
                }
                player.setVelocityY(0);
            }

            // coins & damage checks (uses player's bounding box / center)
            collisionSystem.handlePlayerTileCollisions(player, hud);
        }

        // update camera (we keep same behavior as original)
        camera.update();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        // Draw player + HUD using the same batch tiledMapRenderer uses
        tiledMapRenderer.getBatch().begin();

        player.draw(tiledMapRenderer.getBatch());

        hud.draw(tiledMapRenderer.getBatch(), mapManager.getMapWidthInPx(), mapManager.getMapHeightInPx());

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
    }
}
