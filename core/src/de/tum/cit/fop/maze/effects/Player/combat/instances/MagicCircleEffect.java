package de.tum.cit.fop.maze.effects.Player.combat.instances;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.effects.Player.combat.CombatEffect;
import de.tum.cit.fop.maze.effects.Player.combat.CombatParticleSystem;
/**
 * Combat effect that renders a rotating magic circle.
 * <p>
 * Displays layered circles, rotating triangular glyphs,
 * and a pulsing core to visualize magical activation or charging.
 */
public class MagicCircleEffect extends CombatEffect {
    /** Radius of the magic circle. */
    private final float radius;
    /** Current rotation angle of the inner glyphs (degrees). */
    private float rotation = 0f;
    /**
     * Creates a magic circle effect.
     *
     * @param x        world x-coordinate of the circle center
     * @param y        world y-coordinate of the circle center
     * @param radius   radius of the magic circle
     * @param duration total duration of the effect (seconds)
     */
    public MagicCircleEffect(float x, float y, float radius, float duration) {
        super(x, y, duration);
        this.radius = radius;
    }
    /**
     * Updates the rotation of the magic circle glyphs.
     *
     * @param delta            time elapsed since last frame (seconds)
     * @param particleSystem   combat particle system (not used)
     */
    @Override
    protected void onUpdate(float delta, CombatParticleSystem particleSystem) {
        rotation += 90f * delta;
    }
    /**
     * Renders the magic circle using shape-based primitives.
     * <p>
     * Includes outer rings, rotating triangular glyphs,
     * and a pulsing central core.
     *
     * @param sr shape renderer
     */
    @Override
    public void renderShape(ShapeRenderer sr) {
        float alpha = 1.0f;
        if (timer < 0.2f) alpha = timer / 0.2f;
        if (timer > maxDuration - 0.2f) alpha = (maxDuration - timer) / 0.2f;

        sr.setColor(0.6f, 0f, 0.8f, alpha);
        sr.circle(x, y, radius);
        sr.circle(x, y, radius * 0.95f);

        float innerR = radius * 0.7f;
        drawRotatingTriangle(sr, x, y, innerR, rotation, alpha);
        drawRotatingTriangle(sr, x, y, innerR, rotation + 180f, alpha);

        float pulse = 0.5f + 0.5f * MathUtils.sin(timer * 10f);
        sr.setColor(0.8f, 0.4f, 1f, alpha * pulse);
        sr.circle(x, y, radius * 0.1f);
    }
    /**
     * Draws a rotating triangular glyph inside the magic circle.
     *
     * @param sr          shape renderer
     * @param cx          center x-coordinate
     * @param cy          center y-coordinate
     * @param r           radius of the triangle
     * @param angleOffset rotation offset in degrees
     * @param alpha       transparency value
     */
    private void drawRotatingTriangle(ShapeRenderer sr, float cx, float cy, float r, float angleOffset, float alpha) {
        float x1 = cx + r * MathUtils.cosDeg(angleOffset);
        float y1 = cy + r * MathUtils.sinDeg(angleOffset);
        float x2 = cx + r * MathUtils.cosDeg(angleOffset + 120);
        float y2 = cy + r * MathUtils.sinDeg(angleOffset + 120);
        float x3 = cx + r * MathUtils.cosDeg(angleOffset + 240);
        float y3 = cy + r * MathUtils.sinDeg(angleOffset + 240);

        sr.setColor(0.5f, 0f, 0.7f, alpha);
        sr.triangle(x1, y1, x2, y2, x3, y3);
    }
    /**
     * Renders sprite-based visuals.
     * <p>
     * This effect is rendered entirely using shapes.
     *
     * @param batch sprite batch
     */
    @Override
    public void renderSprite(SpriteBatch batch) {}
}