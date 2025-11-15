package si.um.feri.platformer;

import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;

public class LightingManager {

    private final RayHandler rayHandler;
    private PointLight playerLight;

    public LightingManager(World world) {
        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(0.7f);  // darker world
        rayHandler.setBlur(true);
    }

    public void createPlayerLight() {
        playerLight = new PointLight(rayHandler, 128, new Color(1f, 0.95f, 0.8f, 1f), 5f, 0, 0);
    }

    public void updatePlayerLight(float x, float y) {
        if (playerLight != null) {
            playerLight.setPosition(x, y);
        }
    }

    public RayHandler getRayHandler() {
        return rayHandler;
    }

    public void dispose() {
        rayHandler.dispose();
    }
}

