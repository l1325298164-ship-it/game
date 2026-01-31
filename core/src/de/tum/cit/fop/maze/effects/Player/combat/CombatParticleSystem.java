package de.tum.cit.fop.maze.effects.Player.combat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import java.util.Iterator;
/**
 * Lightweight particle system for combat effects.
 * <p>
 * This system manages simple shape-based particles used by
 * {@link CombatEffect} instances. It handles particle movement,
 * lifetime, and rendering, but contains no game logic.
 */
public class CombatParticleSystem {
    /**
     * Simple data container representing a single combat particle.
     */
    public static class Particle {
        float x, y;
        float vx, vy;
        float life, maxLife;
        float size;
        Color color = new Color();

        boolean friction = false;
        boolean gravity = false;

        public void update(float dt) {
            x += vx * dt;
            y += vy * dt;

            if (friction) {
                vx *= 0.90f;
                vy *= 0.90f;
            }
            if (gravity) {
                vy += 400f * dt;
            }

            life -= dt;
        }
    }

    private Array<Particle> particles = new Array<>();
    /**
     * Updates all active particles.
     * <p>
     * Applies movement, friction, gravity, and removes particles
     * whose lifetime has expired.
     *
     * @param delta time elapsed since last frame (seconds)
     */

    public void update(float delta) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update(delta);
            if (p.life <= 0) it.remove();
        }
    }
    /**
     * Renders all particles using simple shape primitives.
     *
     * @param sr shape renderer used for drawing particles
     */

    public void render(ShapeRenderer sr) {
        for (Particle p : particles) {
            float alpha = p.life / p.maxLife;
            sr.setColor(p.color.r, p.color.g, p.color.b, alpha);

            sr.rect(p.x - p.size/2, p.y - p.size/2, p.size, p.size);

            if (p.size > 4) {
                float len = p.size;
                sr.rectLine(p.x - len, p.y, p.x + len, p.y, 1);
                sr.rectLine(p.x, p.y - len, p.x, p.y + len, 1);
            }
        }
    }

    /**
     * Spawns a new combat particle.
     *
     * @param x        initial x-coordinate
     * @param y        initial y-coordinate
     * @param c        particle color
     * @param vx       initial x velocity
     * @param vy       initial y velocity
     * @param size     visual size of the particle
     * @param life     lifetime in seconds
     * @param friction whether friction is applied
     * @param gravity  whether gravity is applied
     */

    public void spawn(float x, float y, Color c, float vx, float vy, float size, float life, boolean friction, boolean gravity) {
        Particle p = new Particle();
        p.x = x; p.y = y;
        p.vx = vx; p.vy = vy;
        p.color.set(c);
        p.maxLife = life;
        p.life = life;
        p.size = size;
        p.friction = friction;
        p.gravity = gravity;
        particles.add(p);
    }
    /**
     * Removes all active particles.
     */

    public void clear() {
        particles.clear();
    }
}