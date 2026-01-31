package de.tum.cit.fop.maze.effects.environment.items.traps;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.effects.environment.EnvironmentEffect;
import de.tum.cit.fop.maze.effects.environment.EnvironmentParticleSystem;
/**
 * Visual effect for a pearl mine environment trap.
 * <p>
 * This effect emits syrup-like particles while the mine is active.
 * It is purely visual and does not handle gameplay logic such as
 * triggering, damage, or status effects.
 */
public class PearlMineEffect extends EnvironmentEffect {
    /** Primary purple color used for syrup particles. */
    private static final Color SYRUP_PURPLE = new Color(0.6f, 0.4f, 0.8f, 0.9f);
    /** Secondary orange color used for syrup particles. */
    private static final Color SYRUP_ORANGE = new Color(1.0f, 0.6f, 0.2f, 0.9f);
    /**
     * Creates a new pearl mine visual effect.
     *
     * @param x world x-coordinate of the mine
     * @param y world y-coordinate of the mine
     */
    public PearlMineEffect(float x, float y) {
        super(x, y, 2.0f);
    }
    /**
     * Updates the pearl mine effect and emits syrup particles.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    particle system used to spawn visual particles
     */
    @Override
    protected void onUpdate(float delta, EnvironmentParticleSystem ps) {
        if (MathUtils.random() < 0.15f) {
            Color c = MathUtils.randomBoolean() ? SYRUP_PURPLE : SYRUP_ORANGE;

            ps.spawn(
                    x + MathUtils.random(-6, 6),
                    y + MathUtils.random(5, 15),
                    c,
                    0,
                    MathUtils.random(-30, -60),
                    MathUtils.random(3, 6),
                    0.6f,
                    false,
                    false
            );
        }
    }
    /**
     * Renders shape-based visuals for the pearl mine effect.
     * <p>
     * This effect does not use shape rendering.
     *
     * @param sr the shape renderer
     */
    @Override
    public void renderShape(ShapeRenderer sr) {
    }
    /**
     * Renders sprite-based visuals for the pearl mine effect.
     * <p>
     * This effect does not use sprite rendering.
     *
     * @param batch the sprite batch
     */
    @Override
    public void renderSprite(SpriteBatch batch) {}
}