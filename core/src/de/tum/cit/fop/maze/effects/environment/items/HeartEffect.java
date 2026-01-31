package de.tum.cit.fop.maze.effects.environment.items;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.effects.environment.EnvironmentEffect;
import de.tum.cit.fop.maze.effects.environment.EnvironmentParticleSystem;
/**
 * Visual effect representing a heart-based environment item.
 * <p>
 * This effect displays a glowing heart-like animation with
 * particle bursts and fading rings. It is purely visual and
 * does not handle gameplay logic such as healing or item pickup.
 */
public class HeartEffect extends EnvironmentEffect {
    /** Core color used for the main heart visual. */
    private final Color coreColor = new Color(1.0f, 0.4f, 0.7f, 0.9f);

    /** Glow color used for surrounding heart aura effects. */
    private final Color glowColor = new Color(1.0f, 0.7f, 0.85f, 0.4f);
    /**
     * Creates a new heart visual effect.
     *
     * @param x world x-coordinate of the heart effect
     * @param y world y-coordinate of the heart effect
     */
    public HeartEffect(float x, float y) {
        super(x, y, 1.5f);
    }
    /**
     * Updates the heart effect and spawns glow and burst particles.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    particle system used to spawn visual particles
     */
    @Override
    protected void onUpdate(float delta, EnvironmentParticleSystem ps) {
        if (timer < maxDuration * 0.9f) {
            if (MathUtils.randomBoolean(0.4f)) {
                float offsetX = MathUtils.random(-15, 15);
                ps.spawn(x + offsetX, y, glowColor,
                        MathUtils.random(-5, 5), MathUtils.random(40, 100),
                        MathUtils.random(3, 8), 1.2f, false, false);
            }
        }
        if (timer == 0) {
            for(int i=0; i<6; i++) {
                float angle = MathUtils.random(0, 360) * MathUtils.degRad;
                float speed = MathUtils.random(50, 80);
                ps.spawn(x, y, coreColor, MathUtils.cos(angle)*speed, MathUtils.sin(angle)*speed, 8, 0.5f, false, false);
            }
        }
    }
    /**
     * Renders the heart-shaped glow using layered circles.
     *
     * @param sr the shape renderer
     */
    @Override
    public void renderShape(ShapeRenderer sr) {
        float p = timer / maxDuration;
        float fade = 1f - p;

        sr.setColor(coreColor.r, coreColor.g, coreColor.b, 0.5f * fade);
        float r1 = 15 + MathUtils.sin(timer * 3) * 3;
        sr.circle(x, y, r1);

        sr.setColor(glowColor.r, glowColor.g, glowColor.b, 0.3f * fade);
        float r2 = r1 + 15;
        sr.circle(x, y, r2);

        sr.setColor(glowColor.r, glowColor.g, glowColor.b, 0.1f * fade);
        sr.circle(x, y, r2 + 10 + MathUtils.sin(timer * 8) * 5);
    }
    /**
     * Renders sprite-based visuals for the heart effect.
     * <p>
     * This effect does not use sprite rendering.
     *
     * @param batch the sprite batch
     */
    @Override
    public void renderSprite(SpriteBatch batch) {
    }
}