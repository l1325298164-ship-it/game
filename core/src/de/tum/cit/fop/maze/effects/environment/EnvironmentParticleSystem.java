package de.tum.cit.fop.maze.effects.environment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import java.util.Iterator;

/**
 * Generic particle system for environment-related visual effects.
 * <p>
 * This system manages simple shape-based particles used by
 * environment effects such as traps, items, and ambient visuals.
 * It is purely visual and independent from gameplay logic.
 */
public class EnvironmentParticleSystem {
    /**
     * Supported particle render shapes.
     */
    public enum Shape {
        RECTANGLE,
        CIRCLE,
        TRIANGLE
    }
    /**
     * Data container representing a single environment particle.
     * <p>
     * Stores position, movement, lifetime, and rendering parameters.
     */
    public static class EnvParticle {
        /** World x-coordinate of the particle. */
        public float x, y;
        /** Velocity of the particle. */
        public float vx, vy;
        /** Remaining lifetime of the particle. */
        public float life, maxLife;
        /** Visual size of the particle. */
        public float size;
        /** Base color of the particle. */
        public Color color = new Color();
        /** Whether gravity affects the particle. */
        public boolean gravity = false;
        /** Whether friction is applied to the particle. */
        public boolean friction = false;
        /** Shape used to render the particle. */
        public Shape shape = Shape.RECTANGLE;
        /**
         * Updates particle movement and lifetime.
         *
         * @param dt time elapsed since last frame (seconds)
         */
        public void update(float dt) {
            x += vx * dt;
            y += vy * dt;
            if (gravity) vy -= 800f * dt;
            if (friction) { vx *= 0.95f; vy *= 0.95f; }
            life -= dt;
        }
    }
    /** List of active environment particles. */
    private Array<EnvParticle> particles = new Array<>();
    /**
     * Updates all active particles and removes expired ones.
     *
     * @param delta time elapsed since last frame (seconds)
     */
    public void update(float delta) {
        Iterator<EnvParticle> it = particles.iterator();
        while (it.hasNext()) {
            EnvParticle p = it.next();
            p.update(delta);
            if (p.life <= 0) it.remove();
        }
    }
    /**
     * Renders all active particles using shape rendering.
     *
     * @param sr shape renderer
     */
    public void render(ShapeRenderer sr) {
        for (EnvParticle p : particles) {
            float alpha = p.life / p.maxLife;
            sr.setColor(p.color.r, p.color.g, p.color.b, alpha);

            switch (p.shape) {
                case CIRCLE:
                    sr.circle(p.x, p.y, p.size / 2);
                    break;
                case TRIANGLE:
                    float half = p.size / 2;
                    sr.triangle(p.x, p.y + half,
                            p.x - half, p.y - half,
                            p.x + half, p.y - half);
                    break;
                case RECTANGLE:
                default:
                    sr.rect(p.x - p.size/2, p.y - p.size/2, p.size, p.size);
                    break;
            }
        }
    }

    /**
     * Spawns a new environment particle.
     *
     * @param x        world x-coordinate
     * @param y        world y-coordinate
     * @param c        particle color
     * @param vx       initial x velocity
     * @param vy       initial y velocity
     * @param size     visual size of the particle
     * @param life     particle lifetime in seconds
     * @param gravity  whether gravity affects the particle
     * @param friction whether friction affects the particle
     * @param shape    rendering shape of the particle
     */
    public void spawn(float x, float y, Color c, float vx, float vy, float size, float life, boolean gravity, boolean friction, Shape shape) {
        EnvParticle p = new EnvParticle();
        p.x = x; p.y = y;
        p.vx = vx; p.vy = vy;
        p.color.set(c);
        p.maxLife = life;
        p.life = life;
        p.size = size;
        p.gravity = gravity;
        p.friction = friction;
        p.shape = shape;
        particles.add(p);
    }

    /**
     * Spawns a rectangular environment particle with default shape.
     */
    public void spawn(float x, float y, Color c, float vx, float vy, float size, float life, boolean gravity, boolean friction) {
        spawn(x, y, c, vx, vy, size, life, gravity, friction, Shape.RECTANGLE);
    }
    /**
     * Removes all active particles.
     */
    public void clear() {
        particles.clear();
    }
}