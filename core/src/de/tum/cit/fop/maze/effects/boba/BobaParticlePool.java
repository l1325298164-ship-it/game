package de.tum.cit.fop.maze.effects.boba;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Particle pool for boba-related visual effects.
 * <p>
 * This class manages short-lived particle effects such as mist,
 * splashes, and burst effects using object pooling for performance.
 * It is intended to be used internally by effect managers.
 */
public class BobaParticlePool {
    /**
     * Defines the visual behavior type of a particle.
     */
    public enum ParticleType {
        DEFAULT,
        MIST,
        DROPLET
    }

    private static class Particle {
        float x, y;
        float vx, vy;
        float size;
        float alpha;
        float lifetime;
        float maxLifetime;
        boolean active;
        ParticleType type;
    }
    /** Pool used to reuse particle instances for performance. */
    private final Pool<Particle> particlePool;
    /** Currently active particles being updated and rendered. */
    private final Array<Particle> activeParticles;
    /** Shape renderer used for drawing particle primitives. */
    private final ShapeRenderer shapeRenderer;
    /**
     * Creates a new particle pool and initializes internal resources.
     */
    public BobaParticlePool() {
        particlePool = new Pool<Particle>() {
            @Override
            protected Particle newObject() {
                return new Particle();
            }
        };
        activeParticles = new Array<>();
        shapeRenderer = new ShapeRenderer();
    }

    /**
     * Creates a mist particle effect at the given position.
     *
     * @param x world x-coordinate
     * @param y world y-coordinate
     */
    public void createMistEffect(float x, float y) {
        int count = MathUtils.random(6, 10);
        for (int i = 0; i < count; i++) {
            Particle p = particlePool.obtain();
            initParticle(p, x, y, ParticleType.MIST);
            activeParticles.add(p);
        }
    }

    /**
     * Creates a splash particle effect at the given position.
     *
     * @param x world x-coordinate
     * @param y world y-coordinate
     */
    public void createSplashEffect(float x, float y) {
        int count = MathUtils.random(8, 14);
        for (int i = 0; i < count; i++) {
            Particle p = particlePool.obtain();
            initParticle(p, x, y, ParticleType.DROPLET);
            activeParticles.add(p);
        }
    }

    /**
     * Creates a short burst particle effect at the given position.
     *
     * @param x world x-coordinate
     * @param y world y-coordinate
     */
    public void createBurstEffect(float x, float y) {
        int count = 8;
        for (int i = 0; i < count; i++) {
            Particle p = particlePool.obtain();
            initParticle(p, x, y, ParticleType.DEFAULT);
            activeParticles.add(p);
        }
    }


    private void initParticle(Particle p, float x, float y, ParticleType type) {
        p.x = x;
        p.y = y;
        p.type = type;
        p.active = true;

        if (type == ParticleType.MIST) {
            float angle = MathUtils.random(45f, 135f) * MathUtils.degreesToRadians;
            float speed = MathUtils.random(15f, 40f);

            p.vx = MathUtils.cos(angle) * speed;
            p.vy = MathUtils.sin(angle) * speed + 15f;
            p.size = MathUtils.random(6f, 10f);
            p.lifetime = MathUtils.random(0.6f, 1.0f);

        } else if (type == ParticleType.DROPLET) {
            float angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians;
            float speed = MathUtils.random(60f, 160f);

            p.vx = MathUtils.cos(angle) * speed;
            p.vy = MathUtils.sin(angle) * speed;
            p.size = MathUtils.random(3f, 5f);
            p.lifetime = MathUtils.random(0.4f, 0.6f);

        } else {
            float angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians;
            float speed = MathUtils.random(50f, 100f);
            p.vx = MathUtils.cos(angle) * speed;
            p.vy = MathUtils.sin(angle) * speed;
            p.size = MathUtils.random(2f, 5f);
            p.lifetime = 0.5f;
        }
        p.maxLifetime = p.lifetime;
        p.alpha = 1.0f;
    }
    /**
     * Updates all active particles.
     *
     * @param delta time elapsed since last frame (seconds)
     */
    public void update(float delta) {
        for (int i = activeParticles.size - 1; i >= 0; i--) {
            Particle p = activeParticles.get(i);

            p.x += p.vx * delta;
            p.y += p.vy * delta;
            p.lifetime -= delta;

            if (p.type == ParticleType.MIST) {
                p.vy += 20f * delta;
                p.size += 15f * delta;
            } else if (p.type == ParticleType.DROPLET) {
                p.vy -= 500f * delta;
            } else {
                p.vy -= 200f * delta;
            }

            p.alpha = Math.max(0, p.lifetime / p.maxLifetime);

            if (p.lifetime <= 0) {
                activeParticles.removeIndex(i);
                particlePool.free(p);
            }
        }
    }
    /**
     * Renders all active particles.
     * <p>
     * This method temporarily switches rendering state to allow
     * alpha blending and restores it afterward.
     *
     * @param batch the sprite batch
     */
    public void render(SpriteBatch batch) {
        if (activeParticles.size == 0) return;

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Particle p : activeParticles) {
            if (p.type == ParticleType.MIST) {
                shapeRenderer.setColor(0.95f, 0.92f, 0.85f, p.alpha * 0.6f);
            } else if (p.type == ParticleType.DROPLET) {
                shapeRenderer.setColor(0.85f, 0.75f, 0.65f, p.alpha);
            } else {
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, p.alpha);
            }
            shapeRenderer.circle(p.x, p.y, p.size / 2);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();
    }
    /**
     * Removes all active particles and returns them to the pool.
     */
    public void clearAllParticles() {
        particlePool.freeAll(activeParticles);
        activeParticles.clear();
    }
    /**
     * Returns the number of currently active particles.
     *
     * @return active particle count
     */
    public int getActiveParticleCount() {
        return activeParticles.size;
    }

    public void resetStats() {
    }
    /**
     * Releases all rendering resources used by this particle pool.
     */
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}