package de.tum.cit.fop.maze.effects.environment.items.traps;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.effects.environment.EnvironmentEffect;
import de.tum.cit.fop.maze.effects.environment.EnvironmentParticleSystem;
/**
 * Visual effect for a tea shard environment trap.
 * <p>
 * This effect emits small shard-like particles and briefly renders
 * a cross-shaped visual indicator when the trap is triggered.
 * It is purely visual and does not handle gameplay logic such as
 * damage or collision.
 */
public class TeaShardsEffect extends EnvironmentEffect {
    /** Base color used for tea shard particles. */
    private static final Color SHARD_COLOR = new Color(0.95f, 0.95f, 1f, 1f);
    /**
     * Creates a new tea shards visual effect.
     *
     * @param x world x-coordinate of the trap
     * @param y world y-coordinate of the trap
     */
    public TeaShardsEffect(float x, float y) {
        super(x, y, 0.3f);
    }
    /**
     * Updates the tea shards effect and spawns shard particles.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    particle system used to spawn visual particles
     */
    @Override
    protected void onUpdate(float delta, EnvironmentParticleSystem ps) {
        if (timer < delta * 2) {
            for (int i = 0; i < 5; i++) {
                ps.spawn(
                        x, y,
                        SHARD_COLOR,
                        MathUtils.random(-80, 80),
                        MathUtils.random(-50, 100),
                        MathUtils.random(2, 4),
                        0.25f,
                        true,
                        true
                );
            }
        }
    }
    /**
     * Renders a brief shape-based indicator for the tea shards effect.
     *
     * @param sr the shape renderer
     */
    @Override
    public void renderShape(ShapeRenderer sr) {
        if (timer < 0.1f) {
            sr.setColor(1f, 1f, 1f, 1f - timer * 10f);
            float s = 8f;
            sr.line(x - s, y, x + s, y);
            sr.line(x, y - s, x, y + s);
        }
    }
    /**
     * Renders sprite-based visuals for the tea shards effect.
     * <p>
     * This effect does not use sprite rendering.
     *
     * @param batch the sprite batch
     */
    @Override
    public void renderSprite(SpriteBatch batch) {}
}