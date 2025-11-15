package si.um.feri.platformer;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class HUD {
    private BitmapFont font;
    private int health;
    private int score;

    public HUD() {
        font = new BitmapFont();
        health = 100;
        score = 0;
    }

    public void draw(Batch batch, float mapWidthInPx, float mapHeightInPx) {
        if (health > 0 && score < 40) {
            font.getData().setScale(1f);
            font.setColor(1f, 1f, 1f, 1f);
            font.draw(batch, "SCORE: " + score, 20f, 20f);
            font.draw(batch, "HEALTH: " + health, 20f, 30f + font.getCapHeight());
        } else {
            String endText = (health <= 0) ? "GAME OVER" : "Victory!";
            font.getData().setScale(3f);
            if (health <= 0) font.setColor(1f, 0f, 0f, 1f);
            else font.setColor(0f, 1f, 0f, 1f);

            GlyphLayout layout = new GlyphLayout(font, endText);
            float textWidth = layout.width;
            float textX = (mapWidthInPx - textWidth) / 2f;
            float textY = mapHeightInPx / 2f + 40;
            font.draw(batch, layout, textX, textY);
        }
    }

    public void addScore(int v) {
        score += v;
    }

    public void decreaseHealth(int v) {
        health -= v;
        if (health < 0) health = 0;
    }

    public int getHealth() {
        return health;
    }

    public int getScore() {
        return score;
    }

    public void setHealth(int h) {
        health = h;
    }

    public void setScore(int s) {
        score = s;
    }

    public void dispose() {
        if (font != null) font.dispose();
    }
}

