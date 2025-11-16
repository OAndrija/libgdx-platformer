package si.um.feri.platformer;

import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;

public class LightingManager {

    private final RayHandler rayHandler;
    private PointLight playerLight;

    private float pulseTime = 0f;

    public LightingManager(World world) {
        rayHandler = new RayHandler(world);

        // Hollow Knight style: darker world, soft glow
        rayHandler.setAmbientLight(0.55f);  // slightly darker for high contrast
        rayHandler.setBlurNum(3);           // stronger blur for smooth glow
        rayHandler.setBlur(true);
    }

    public void createPlayerLight() {
        // Color: warm golden white
        Color hkColor = new Color(1f, 0.92f, 0.80f, 0.7f);

        playerLight = new PointLight(
            rayHandler,
            128,             // more rays = smoother cone
            hkColor,
            13f,              // base radius (world units)
            0, 0
        );

        playerLight.setSoft(true);          // very important!
        playerLight.setSoftnessLength(2.4f); // bigger = smoother falloff
    }

    public void updatePlayerLight(float x, float y, float dt) {
        if (playerLight != null) {

            // Smooth pulsation (subtle)
            pulseTime += dt;
            float pulse = (float) Math.sin(pulseTime * 1.8f) * 0.25f;  // amplitude
            float radius = 13f + pulse;

            playerLight.setDistance(radius);

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


