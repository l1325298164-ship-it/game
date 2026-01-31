package de.tum.cit.fop.maze.effects.Player.combat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
/**
 * Base class for all combat-related visual effects.
 * <p>
 * A combat effect has a fixed duration and is updated over time.
 * Subclasses implement their own visual behavior using shape
 * rendering, sprite rendering, and optional particle spawning.
 */

public abstract class CombatEffect {
    /** World position of the combat effect. */
    protected float x, y;
    /** Elapsed time since the effect was created. */
    protected float timer;
    /** Maximum lifetime of the effect in seconds. */
    protected float maxDuration;
    protected boolean isFinished;
    /**
     * Creates a combat effect at a given position.
     *
     * @param x        initial x-coordinate in world space
     * @param y        initial y-coordinate in world space
     * @param duration maximum duration of the effect in seconds
     */

    public CombatEffect(float x, float y, float duration) {
        this.x = x;
        this.y = y;
        this.maxDuration = duration;
        this.timer = 0;
    }

    /**
     * Updates the effect state.
     * <p>
     * Advances the internal timer, marks the effect as finished when
     * its duration is exceeded, and delegates custom behavior to
     * {@link #onUpdate(float, CombatParticleSystem)}.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    combat particle system used for spawning particles
     */

    public void update(float delta, CombatParticleSystem ps) {
        timer += delta;
        if (timer >= maxDuration) isFinished = true;
        onUpdate(delta, ps);
    }
    /**
     * Updates the effect-specific behavior.
     * <p>
     * Subclasses implement this method to control movement,
     * particle spawning, and internal state changes.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    combat particle system
     */

    protected abstract void onUpdate(float delta, CombatParticleSystem ps);

    /**
     * Renders the effect using shape-based primitives.
     *
     * @param sr shape renderer
     */

    public abstract void renderShape(ShapeRenderer sr);

    /**
     * Renders the effect using sprite-based rendering.
     * <p>
     * Default implementation does nothing. Subclasses may override
     * this method if sprite rendering is required.
     *
     * @param batch sprite batch
     */

    public void renderSprite(SpriteBatch batch) {
    }
    /**
     * Checks whether the effect has completed its lifetime.
     *
     * @return {@code true} if the effect is finished
     */

    public boolean isFinished() { return isFinished; }
}