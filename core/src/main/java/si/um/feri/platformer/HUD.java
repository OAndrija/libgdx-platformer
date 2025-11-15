package si.um.feri.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class HUD {

    private BitmapFont font;
    private int health = 100;
    private int score = 0;

    public HUD() {
        font = new BitmapFont();
    }

    public void draw(Batch batch) {
        if (health > 0 && score < 40) {
            font.getData().setScale(1f);

            font.draw(batch, "SCORE: " + score, 20, 30);
            font.draw(batch, "HEALTH: " + health, 20, 60);

        } else {
            String text = (health <= 0) ? "GAME OVER" : "Victory!";
            font.getData().setScale(3f);

            GlyphLayout layout = new GlyphLayout(font, text);

            float x = (Gdx.graphics.getWidth() - layout.width) * 0.5f;
            float y = (Gdx.graphics.getHeight() + layout.height) * 0.5f;

            font.draw(batch, layout, x, y);
        }
    }

    public int getHealth() { return health; }
    public int getScore() { return score; }
    public void setHealth(int h) { health = h; }
    public void setScore(int s) { score = s; }

    public void addScore(int v) { score += v; }
    public void decreaseHealth(int v) { health = Math.max(0, health - v); }

    public void dispose() { font.dispose(); }
}


