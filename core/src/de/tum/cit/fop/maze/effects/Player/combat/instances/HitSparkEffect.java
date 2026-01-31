package de.tum.cit.fop.maze.effects.Player.combat.instances;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.effects.Player.combat.CombatEffect;
import de.tum.cit.fop.maze.effects.Player.combat.CombatParticleSystem;
/**
 * Short-lived combat effect representing a hit impact spark.
 * <p>
 * Combines a brief cross-shaped flash with small burst particles
 * to emphasize successful hits.
 */

public class HitSparkEffect extends CombatEffect {
    /** Base size of the hit spark visual. */
    private final float size;
    private boolean particlesSpawned = false;
    /**
     * Creates a hit spark effect at the given position.
     *
     * @param x world x-coordinate of the hit location
     * @param y world y-coordinate of the hit location
     */
    public HitSparkEffect(float x, float y) {
        super(x, y, 0.15f);
        this.size = 20f;
    }
    /**
     * Spawns hit spark particles once at the beginning of the effect.
     *
     * @param delta           time elapsed since last frame (seconds)
     * @param particleSystem  combat particle system used to spawn particles
     */
    @Override
    protected void onUpdate(float delta, CombatParticleSystem particleSystem) {
        if (!particlesSpawned) {
            particlesSpawned = true;
            for (int i = 0; i < 5; i++) {
                particleSystem.spawn(
                        x, y,
                        new Color(1f, MathUtils.random(0.5f, 1f), 0f, 1f),
                        MathUtils.random(-150, 150),
                        MathUtils.random(-150, 150),
                        MathUtils.random(3, 6),
                        MathUtils.random(0.2f, 0.4f),
                        true,
                        false
                );
            }
        }
    }
    /**
     * Renders the hit spark shape visuals.
     * <p>
     * Draws a fading cross and a small central flash to represent impact.
     *
     * @param sr shape renderer
     */
    @Override
    public void renderShape(ShapeRenderer sr) {
        float alpha = 1.0f - (timer / maxDuration);
        sr.setColor(1f, 1f, 0.7f, alpha);
        float s = size * (0.8f + 0.2f * (timer / maxDuration));
        sr.line(x - s, y + s, x + s, y - s);
        sr.line(x - s, y - s, x + s, y + s);
        sr.setColor(1f, 1f, 1f, alpha);
        sr.circle(x, y, s * 0.3f);
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