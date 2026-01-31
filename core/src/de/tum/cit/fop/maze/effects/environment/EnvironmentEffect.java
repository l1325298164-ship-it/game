package de.tum.cit.fop.maze.effects.environment;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
/**
 * Base class for all environment-related visual effects.
 * <p>
 * An environment effect represents a time-limited visual animation
 * that exists at a fixed world position and is updated and rendered
 * independently of gameplay logic.
 */
public abstract class EnvironmentEffect {
    /** World x-coordinate of the effect. */
    protected float x, y;
    /** Elapsed time since the effect was created. */
    protected float timer;

    /** Maximum duration of the effect in seconds. */
    protected float maxDuration;
    /** Whether the effect has finished and should be removed. */
    protected boolean isFinished;
    /**
     * Creates a new environment effect.
     *
     * @param x        world x-coordinate of the effect
     * @param y        world y-coordinate of the effect
     * @param duration maximum duration of the effect in seconds
     */
    public EnvironmentEffect(float x, float y, float duration) {
        this.x = x;
        this.y = y;
        this.maxDuration = duration;
        this.timer = 0;
    }
    /**
     * Updates the effect state and lifetime.
     * <p>
     * This method advances the internal timer and delegates
     * effect-specific logic to {@link #onUpdate(float, EnvironmentParticleSystem)}.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    particle system used to spawn visual particles
     */
    public void update(float delta, EnvironmentParticleSystem ps) {
        timer += delta;
        if (timer >= maxDuration) isFinished = true;
        onUpdate(delta, ps);
    }
    /**
     * Updates effect-specific behavior.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    particle system used to spawn visual particles
     */
    protected abstract void onUpdate(float delta, EnvironmentParticleSystem ps);

    /**
     * Renders shape-based visuals of the effect.
     *
     * @param sr shape renderer
     */
    public abstract void renderShape(ShapeRenderer sr);

    /**
     * Renders sprite-based visuals of the effect.
     *
     * @param batch sprite batch
     */
    public abstract void renderSprite(SpriteBatch batch);
    /**
     * Indicates whether the effect has finished.
     *
     * @return true if the effect duration has elapsed
     */
    public boolean isFinished() { return isFinished; }
}