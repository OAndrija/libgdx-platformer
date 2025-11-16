package si.um.feri.platformer.entities;

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

    private static final float PPM = 32f;

    private final Body body;
    private final Sprite sprite;
    private final World world;

    // physics (world units)
    private float velocityY = 0;
    private boolean isJumping = false;

    private final float gravity = -26f;        // world units
    private final float jumpVelocity = 12f;     // world units
    private final float maxFallSpeed = -20f;
    private final float moveSpeed = 7f;

    // movement candidates
    private float candidateX;
    private float candidateY;

    public Player(World world) {
        this.world = world;

        // Load sprite
        Texture t = new Texture("tiled/GraveRobberNew.png");
        sprite = new Sprite(t);

        // scale sprite so pixels -> world units (1 world unit = PPM pixels)
        sprite.setSize(sprite.getWidth() / PPM, sprite.getHeight() / PPM);

        // Body
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.KinematicBody;

        // We'll treat the body's position as the sprite's bottom-left (not center).
        // That keeps sprite position and body position aligned and simple.
        bd.position.set(1f, 5f); // initial world coords (bottom-left)

        body = world.createBody(bd);
        body.setFixedRotation(true);

        // Ensure sprite is aligned with body bottom-left initially
        sprite.setPosition(bd.position.x, bd.position.y);

        // initialize candidates to current position
        candidateX = getX();
        candidateY = getY();
    }

    public Rectangle getBoundingRectangle() {
        // builds world-unit rectangle.
        return new Rectangle(
            getX(),
            getY(),
            getWidth(),   // world units
            getHeight()   // world units
        );
    }

    // positions now return world coords directly (we use body position as bottom-left)
    public float getX() { return body.getPosition().x; }
    public float getY() { return body.getPosition().y; }

    public float getWidth() { return sprite.getWidth(); }   // world units
    public float getHeight() { return sprite.getHeight(); } // world units

    public void setPosition(float x, float y) {
        body.setTransform(x, y, 0);
        sprite.setPosition(x, y);
        candidateX = x;
        candidateY = y;
    }

    public void commitX(float x) {
        body.setTransform(x, body.getPosition().y, 0);
        sprite.setPosition(x, sprite.getY());
    }

    public void commitY(float y) {
        body.setTransform(body.getPosition().x, y, 0);
        sprite.setPosition(sprite.getX(), y);
    }

    public void handleInput(float dt) {
        float nx = getX();

        if (Gdx.input.isKeyPressed(Input.Keys.A)) nx -= moveSpeed * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) nx += moveSpeed * dt;

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isJumping) {
            velocityY = jumpVelocity;
            isJumping = true;
        }

        candidateX = nx;
    }

    public void updatePhysics(float dt) {
        velocityY += gravity * dt;
        if (velocityY < maxFallSpeed) velocityY = maxFallSpeed;

        candidateY = getY() + velocityY * dt;
    }

    public float getCandidateX() { return candidateX; }
    public float getCandidateY() { return candidateY; }

    public float getVelocityY() { return velocityY; }
    public void setVelocityY(float v) { velocityY = v; }

    public boolean isJumping() { return isJumping; }
    public void setJumping(boolean j) { isJumping = j; }

    public void draw(Batch batch) {
        sprite.draw(batch);
    }

    public float getCenterX() { return getX() + getWidth() * 0.5f; }
    public float getCenterY() { return getY() + getHeight() * 0.5f; }

    public void dispose() {
        sprite.getTexture().dispose();
    }
}


