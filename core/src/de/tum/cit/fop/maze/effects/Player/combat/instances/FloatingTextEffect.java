package de.tum.cit.fop.maze.effects.Player.combat.instances;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.fop.maze.effects.Player.combat.CombatEffect;
import de.tum.cit.fop.maze.effects.Player.combat.CombatParticleSystem;
/**
 * Combat visual effect that displays floating text.
 * <p>
 * The text gradually moves upward and fades out over time.
 * Commonly used for damage numbers, status messages, or combat feedback.
 */
public class FloatingTextEffect extends CombatEffect {
    private String text;
    private Color color;
    private BitmapFont font;

    private float targetScale = 1.0f;
    /**
     * Creates a floating text combat effect.
     *
     * @param x     initial world x-coordinate of the text
     * @param y     initial world y-coordinate of the text
     * @param text  text content to display
     * @param color text color
     * @param font  font used for rendering
     */
    public FloatingTextEffect(float x, float y, String text, Color color, BitmapFont font) {
        super(x, y, 1.0f);
        this.text = text;
        this.color = color;
        this.font = font;
        this.targetScale = font.getData().scaleX;
    }
    /**
     * Sets the target font scale used when rendering the text.
     *
     * @param scale desired font scale
     */
    public void setTargetScale(float scale) {
        this.targetScale = scale;
    }
    /**
     * Updates the floating text position over time.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    combat particle system (not used)
     */
    @Override
    protected void onUpdate(float delta, CombatParticleSystem ps) {
        y += delta * 50f;
    }
    /**
     * Renders shape-based visuals.
     * <p>
     * This effect does not use shape rendering.
     *
     * @param sr shape renderer
     */
    @Override
    public void renderShape(ShapeRenderer sr) {
    }
    /**
     * Renders the floating text using a bitmap font.
     *
     * @param batch sprite batch
     */
    @Override
    public void renderSprite(SpriteBatch batch) {
        if (font == null) return;

        Color oldColor = font.getColor();
        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;

        float alpha = Math.max(0, 1f - (timer / maxDuration));
        font.setColor(color.r, color.g, color.b, alpha * 0.8f);

        font.getData().setScale(targetScale);

        font.draw(batch, text, x, y);

        font.setColor(oldColor);
        font.getData().setScale(oldScaleX, oldScaleY);
    }
}