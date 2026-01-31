package de.tum.cit.fop.maze.effects.Player.combat.instances;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.effects.Player.combat.CombatEffect;
import de.tum.cit.fop.maze.effects.Player.combat.CombatParticleSystem;
/**
 * Combat effect representing a stream of magical essence.
 * <p>
 * The essence travels smoothly from a source position to a target,
 * leaving small magical particles along its path.
 * Commonly used for life drain, mana absorption, or healing visuals.
 */

public class MagicEssenceEffect extends CombatEffect {
    /** Target position that the magic essence moves toward. */
    private final float targetX, targetY;
    private final float startX, startY;
    private final Color color = new Color(0.7f, 0.3f, 1f, 1f);
    /**
     * Creates a magic essence effect that moves from a source to a target.
     *
     * @param startX starting world x-coordinate
     * @param startY starting world y-coordinate
     * @param targetX target world x-coordinate
     * @param targetY target world y-coordinate
     */
    public MagicEssenceEffect(float startX, float startY, float targetX, float targetY) {
        super(startX, startY, 0.6f);
        this.startX = startX;
        this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
    }
    /**
     * Updates the movement of the magic essence and spawns trailing particles.
     * <p>
     * Movement follows a quadratic easing curve for smoother acceleration.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    combat particle system used for trail effects
     */
    @Override
    protected void onUpdate(float delta, CombatParticleSystem ps) {
        float progress = timer / maxDuration;
        float p = progress * progress;
        this.x = startX + (targetX - startX) * p;
        this.y = startY + (targetY - startY) * p;

        if (MathUtils.randomBoolean(0.6f)) {
            ps.spawn(
                    x + MathUtils.random(-3, 3),
                    y + MathUtils.random(-3, 3),
                    new Color(0.8f, 0.5f, 1f, 0.8f),
                    0, 0,
                    3, 0.3f, false, false
            );
        }
    }
    /**
     * Renders the magic essence using simple shape primitives.
     * <p>
     * Draws a glowing outer core and a bright inner core.
     *
     * @param sr shape renderer
     */
    @Override
    public void renderShape(ShapeRenderer sr) {
        sr.setColor(color);
        sr.circle(x, y, 5);
        sr.setColor(1f, 1f, 1f, 0.8f);
        sr.circle(x, y, 2);
    }
    /**
     * Renders sprite-based visuals.
     * <p>
     * This effect does not use sprite rendering.
     *
     * @param batch sprite batch
     */
    @Override
    public void renderSprite(SpriteBatch batch) {}
}