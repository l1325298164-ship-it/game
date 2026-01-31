package de.tum.cit.fop.maze.effects.environment.items.traps;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.effects.environment.EnvironmentEffect;
import de.tum.cit.fop.maze.effects.environment.EnvironmentParticleSystem;
/**
 * Visual effect for a mud-based environment trap.
 * <p>
 * This effect emits short-lived mud splash particles when the trap
 * is triggered. It is purely visual and does not handle gameplay
 * logic such as slowing or damage.
 */
public class MudTrapEffect extends EnvironmentEffect {
    /** Base color used for mud splash particles. */
    private static final Color MUD_COLOR = new Color(0.35f, 0.2f, 0.05f, 1f);
    /**
     * Creates a new mud trap visual effect.
     *
     * @param x world x-coordinate of the trap
     * @param y world y-coordinate of the trap
     */
    public MudTrapEffect(float x, float y) {
        super(x, y, 0.4f);
    }
    /**
     * Updates the mud trap effect and spawns splash particles.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    particle system used to spawn visual particles
     */
    @Override
    protected void onUpdate(float delta, EnvironmentParticleSystem ps) {
        if (timer < delta * 2) {
            for (int i = 0; i < 4; i++) {
                ps.spawn(
                        x + MathUtils.random(-5, 5),
                        y,
                        MUD_COLOR,
                        MathUtils.random(-60, 60),
                        MathUtils.random(20, 80),
                        MathUtils.random(3, 5),
                        0.3f,
                        true,
                        true
                );
            }
        }
    }
    /**
     * Renders shape-based visuals for the mud trap effect.
     * <p>
     * This effect does not use shape rendering.
     *
     * @param sr the shape renderer
     */
    @Override
    public void renderShape(ShapeRenderer sr) {
    }
    /**
     * Renders sprite-based visuals for the mud trap effect.
     * <p>
     * This effect does not use sprite rendering.
     *
     * @param batch the sprite batch
     */
    @Override
    public void renderSprite(SpriteBatch batch) {
    }
}