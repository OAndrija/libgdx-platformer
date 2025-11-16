package si.um.feri.platformer.managers;

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

        // Darker world → stronger contrast
        rayHandler.setAmbientLight(0.35f);
        rayHandler.setBlur(true);
        rayHandler.setBlurNum(8);
    }

    public void createPlayerLight() {

        Color warmGlow = new Color(1f, 0.92f, 0.80f, 0.6f);

        playerLight = new PointLight(
            rayHandler,
            128,           // smooth light
            warmGlow,
            15f,           // initial radius
            0, 0
        );

        playerLight.setSoft(true);
        playerLight.setSoftnessLength(4f);
        playerLight.setStaticLight(false);
        playerLight.setXray(false);          // IMPORTANT: If true → light ignores shadows
        playerLight.setContactFilter((short)0xFFFF, (short)0xFFFF, (short)0xFFFF);
    }

    public void updatePlayerLight(float x, float y, float dt) {
        if (playerLight == null) return;

        // Slight breathing effect
        pulseTime += dt;
        float pulse = (float) Math.sin(pulseTime * 1.8f) * 0.25f;

        playerLight.setDistance(15f + pulse);
        playerLight.setPosition(x, y);
    }

    public RayHandler getRayHandler() {
        return rayHandler;
    }

    public void dispose() {
        rayHandler.dispose();
    }
}



