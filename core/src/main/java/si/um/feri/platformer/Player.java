package si.um.feri.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public class Player {
    private final Sprite sprite;

    // physics
    private float velocityY = 0;
    private boolean isJumping = false;
    private final float gravity = -500f;
    private final float jumpVelocity = 300f;
    private final float maxFallSpeed = -600f;

    // movement speed (pixels per second)
    private final float speed = 150f;

    // candidate positions used for collision checks before committing
    private float candidateX;
    private float candidateY;

    public Player() {
        Texture t = new Texture("tiled/GraveRobberNew.png");
        sprite = new Sprite(t);
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

    // committers after collision check
    public void commitX(float x) {
        sprite.setX(x);
    }

    public void commitY(float y) {
        sprite.setY(y);
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

    public void setPosition(float x, float y) {
        sprite.setPosition(x, y);
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

