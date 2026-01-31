package de.tum.cit.fop.maze.effects.Player.combat.instances;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.effects.Player.combat.CombatEffect;
import de.tum.cit.fop.maze.effects.Player.combat.CombatParticleSystem;

/**
 * Combat visual effect representing an aggro pulse.
 * <p>
 * This effect emits a short-lived radial burst of particles
 * to visually indicate an aggro-related combat event.
 * It is purely visual and does not affect combat mechanics.
 */

public class AggroPulseEffect extends CombatEffect {
    /** Whether the particle burst has already been spawned. */
    private boolean spawned = false;
    /**
     * Creates a new aggro pulse combat effect.
     *
     * @param x world x-coordinate of the effect center
     * @param y world y-coordinate of the effect center
     */
    public AggroPulseEffect(float x, float y) {
        super(x, y, 0.5f);
    }
    /**
     * Spawns the aggro pulse particle burst.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    combat particle system used to spawn particles
     */
    @Override
    protected void onUpdate(float delta, CombatParticleSystem ps) {
        if (!spawned) {
            spawned = true;

            int particleCount = 40;
            float angleStep = 360f / particleCount;

            for (int i = 0; i < particleCount; i++) {
                float angle = i * angleStep + MathUtils.random(-5f, 5f);

                float speed = MathUtils.random(280, 350);

                Color waveColor = new Color(0.85f, 0.95f, 1.0f, 0.5f);

                float startOffset = 15f;
                float startX = x + MathUtils.cosDeg(angle) * startOffset;
                float startY = y + MathUtils.sinDeg(angle) * startOffset;

                ps.spawn(
                        startX,
                        startY,
                        waveColor,
                        MathUtils.cosDeg(angle) * speed,
                        MathUtils.sinDeg(angle) * speed,
                        MathUtils.random(15, 25),
                        0.4f,
                        false,
                        false
                );
            }
        }
    }
    /**
     * Renders shape-based visuals for this effect.
     * <p>
     * This effect does not use shape rendering.
     *
     * @param sr shape renderer
     */

    @Override
    public void renderShape(ShapeRenderer sr) {}
    /**
     * Renders sprite-based visuals for this effect.
     * <p>
     * This effect does not use sprite rendering.
     *
     * @param batch sprite batch
     */

    @Override
    public void renderSprite(SpriteBatch batch) {}
}