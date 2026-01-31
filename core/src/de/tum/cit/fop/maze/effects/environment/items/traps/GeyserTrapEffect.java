package de.tum.cit.fop.maze.effects.environment.items.traps;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.effects.environment.EnvironmentEffect;
import de.tum.cit.fop.maze.effects.environment.EnvironmentParticleSystem;
/**
 * Visual effect for a geyser-style environment trap.
 * <p>
 * This effect periodically emits steam particles and provides
 * a short visual warning before activation.
 * It is purely visual and does not handle any gameplay logic
 * such as damage or collision.
 */
public class GeyserTrapEffect extends EnvironmentEffect {
    /** Base color used for steam particles emitted by the geyser. */
    private static final Color STEAM_COLOR = new Color(1f, 1f, 1f, 0.5f);
    /**
     * Creates a new geyser trap visual effect.
     *
     * @param x world x-coordinate of the geyser
     * @param y world y-coordinate of the geyser
     */
    public GeyserTrapEffect(float x, float y) {
        super(x, y, 1.0f);
    }
    /**
     * Updates the geyser effect and emits steam particles.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    particle system used to spawn visual particles
     */
    @Override
    protected void onUpdate(float delta, EnvironmentParticleSystem ps) {
        if (MathUtils.random() < 0.2f) {
            ps.spawn(
                    x + MathUtils.random(-8, 8),
                    y + MathUtils.random(0, 10),
                    STEAM_COLOR,
                    MathUtils.random(-10, 10),
                    MathUtils.random(60, 100),
                    MathUtils.random(6, 12),
                    0.8f,
                    false,
                    false
            );
        }
    }
    /**
     * Renders a brief warning shape before the geyser activates.
     *
     * @param sr the shape renderer
     */
    @Override
    public void renderShape(ShapeRenderer sr) {
        if (timer < 0.2f) {
            sr.setColor(1f, 0.2f, 0.2f, (0.2f - timer) * 2f); // 淡红 -> 透明
            sr.circle(x, y, 20);
        }
    }
    /**
     * Renders sprite-based visuals for the geyser effect.
     * <p>
     * This effect does not use sprite rendering.
     *
     * @param batch the sprite batch
     */
    @Override
    public void renderSprite(SpriteBatch batch) {}
}