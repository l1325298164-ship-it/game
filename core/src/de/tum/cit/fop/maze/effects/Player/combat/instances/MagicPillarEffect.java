package de.tum.cit.fop.maze.effects.Player.combat.instances;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.effects.Player.combat.CombatEffect;
import de.tum.cit.fop.maze.effects.Player.combat.CombatParticleSystem;
/**
 * Combat effect representing a vertical magic pillar.
 * <p>
 * The pillar erupts from the ground, dealing damage in an area
 * while spawning upward-moving magical particles.
 * Often used as the main impact visual of magic-based abilities.
 */
public class MagicPillarEffect extends CombatEffect {
    /** Radius of the pillar's base area. */
    private final float radius;
    /** Visual height of the magic pillar in world units. */
    private final float height = 600f;
    /**
     * Creates a magic pillar effect at the given position.
     *
     * @param x      world x-coordinate of the pillar center
     * @param y      world y-coordinate of the pillar base
     * @param radius radius of the pillar's area of effect
     */
    public MagicPillarEffect(float x, float y, float radius) {
        super(x, y, 0.6f);
        this.radius = radius;
    }
    /**
     * Updates the magic pillar effect and spawns rising magic particles.
     * <p>
     * Particles are emitted mainly during the early phase of the effect
     * to emphasize the pillar eruption.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    combat particle system used for particle spawning
     */

    @Override
    protected void onUpdate(float delta, CombatParticleSystem ps) {
        if (timer < 0.3f) {
            for (int i = 0; i < 5; i++) {
                float angle = MathUtils.random(0, 360);
                float dist = MathUtils.random(0, radius);
                float px = x + dist * MathUtils.cosDeg(angle);
                float py = y + dist * MathUtils.sinDeg(angle);

                ps.spawn(
                        px, py,
                        new Color(0.8f, 0.2f, 1f, 1f),
                        0, MathUtils.random(200, 400),
                        MathUtils.random(4, 8),
                        0.5f,
                        false, false
                );
            }
        }
    }
    /**
     * Renders the magic pillar using shape primitives.
     * <p>
     * Includes a wide outer column, a brighter inner core,
     * and an expanding ground wave at the base.
     *
     * @param sr shape renderer
     */

    @Override
    public void renderShape(ShapeRenderer sr) {
        float alpha = 1.0f - (timer / maxDuration);
        sr.setColor(0.6f, 0f, 0.8f, alpha * 0.5f);
        sr.rect(x - radius, y, radius * 2, height);
        sr.setColor(1f, 0.6f, 1f, alpha * 0.8f);
        sr.rect(x - radius * 0.2f, y, radius * 0.4f, height);
        float waveRadius = radius * (1f + timer * 2f);
        sr.setColor(0.8f, 0.2f, 1f, alpha);
        sr.circle(x, y, waveRadius);
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