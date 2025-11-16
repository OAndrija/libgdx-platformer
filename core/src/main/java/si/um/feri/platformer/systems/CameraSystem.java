package si.um.feri.platformer.systems;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

import si.um.feri.platformer.managers.MapManager;
import si.um.feri.platformer.entities.Player;

public class CameraSystem {

    private final OrthographicCamera camera;

    private final MapManager mapManager;
    private final Player player;

    // Hollow Knight look-ahead
    private float lookAheadX = 0f;
    private float lookAheadTarget = 0f;

    public CameraSystem(OrthographicCamera camera, Player player, MapManager mapManager) {
        this.camera = camera;
        this.player = player;
        this.mapManager = mapManager;
    }

    public void update(float dt) {

        // ======================================================
        //              FOLLOW + LOOK-AHEAD LOGIC
        // ======================================================

        float lerp = 5f * dt;
        float playerCenterX = player.getCenterX();
        float playerCenterY = player.getCenterY();

        // detect player horizontal movement
        float playerVelocityX = player.getCandidateX() - player.getX();

        if (Math.abs(playerVelocityX) > 0.001f) {
            float direction = Math.signum(playerVelocityX);
            lookAheadTarget = direction * 7f;
        } else {
            lookAheadTarget = 0f;
        }

        // smooth look-ahead interpolation
        lookAheadX += (lookAheadTarget - lookAheadX) * 2.5f * dt;

        float desiredX = playerCenterX + lookAheadX;
        float desiredY = playerCenterY;

        camera.position.x += (desiredX - camera.position.x) * lerp;
        camera.position.y += (desiredY - camera.position.y) * lerp;

        // ======================================================
        //                  WORLD BOUNDARIES
        // ======================================================

        float halfW = camera.viewportWidth * camera.zoom * 0.5f;
        float halfH = camera.viewportHeight * camera.zoom * 0.5f;

        float mapW = mapManager.getMapWidthInPx() / 32f;
        float mapH = mapManager.getMapHeightInPx() / 32f;

        camera.position.x = MathUtils.clamp(camera.position.x, halfW, mapW - halfW);
        camera.position.y = MathUtils.clamp(camera.position.y, halfH, mapH - halfH);

        camera.update();
    }
}

