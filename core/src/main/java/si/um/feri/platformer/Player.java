package si.um.feri.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class Player {

    private Body body;
    private final Sprite sprite;
    private World world;

    private float velocityY = 0;
    private boolean isJumping = false;

    private final float gravity = -500f;
    private final float jumpVelocity = 300f;
    private final float maxFallSpeed = -600f;
    private final float speed = 150f;
    private static final float PPM = 32f;
    private float candidateX;
    private float candidateY;

    public Player(World world) {
        this.world = world;

        // Sprite
        Texture t = new Texture("tiled/GraveRobberNew.png");
        sprite = new Sprite(t);

        // Body definition (static point for lights)
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.KinematicBody;   // moves manually, Box2D won't control
        bdef.position.set(0, 0);

        body = world.createBody(bdef);
    }

    public float getCenterX() {
        return getX() + sprite.getWidth() * 0.5f;
    }

    public float getCenterY() {
        return getY() + sprite.getHeight() * 0.5f;
    }

    public void setPosition(float x, float y) {
        sprite.setPosition(x, y);
        body.setTransform(x / PPM, y / PPM, 0);
    }

    public void commitX(float x) {
        sprite.setX(x);
        body.setTransform(sprite.getX() / PPM,
            sprite.getY() / PPM, 0);
    }

    public void commitY(float y) {
        sprite.setY(y);
        body.setTransform(sprite.getX() / PPM,
            sprite.getY() / PPM, 0);
    }

    public void handleInput() {
        float newX = getX();
        // left / right movement
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            newX -= speed * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            newX += speed * Gdx.graphics.getDeltaTime();
        }

        // Jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isJumping) {
            velocityY = jumpVelocity;
            isJumping = true;
        }

        candidateX = newX;
    }

    public void updatePhysics(float dt) {
        velocityY += gravity * dt;
        if (velocityY < maxFallSpeed) {
            velocityY = maxFallSpeed;
        }

        candidateY = getY() + velocityY * dt;
    }

    public float getCandidateX() {
        return candidateX;
    }

    public float getCandidateY() {
        return candidateY;
    }

    public float getX() {
        return sprite.getX();
    }

    public float getY() {
        return sprite.getY();
    }

    public float getWidth() {
        return sprite.getWidth();
    }

    public float getHeight() {
        return sprite.getHeight();
    }

    public Rectangle getBoundingRectangle() {
        return sprite.getBoundingRectangle();
    }

    public void draw(Batch batch) {
        sprite.draw(batch);
    }

    public float getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(float v) {
        velocityY = v;
    }

    public boolean isJumping() {
        return isJumping;
    }

    public void setJumping(boolean jumping) {
        isJumping = jumping;
    }

    public void dispose() {
        if (sprite.getTexture() != null) sprite.getTexture().dispose();
    }
}

