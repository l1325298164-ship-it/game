package de.tum.cit.fop.maze.effects.portal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
/**
 * Manages portal-related particles using an object pool.
 * <p>
 * This particle pool is responsible for spawning, updating,
 * and rendering spiral (tornado-like) particles used in
 * portal activation and teleportation effects.
 * <p>
 * Object pooling is used to reduce garbage collection
 * and improve rendering performance.
 */

public class PortalParticlePool {
    /** Currently active portal particles being updated and rendered. */

    private final Array<PortalParticle> activeParticles = new Array<>();
    /** Reusable inactive particles stored for object pooling. */

    private final Array<PortalParticle> freeParticles = new Array<>();

    private Texture trailTexture;

    private final Color startColor = new Color(0.2f, 0.8f, 1.0f, 1f); // 亮青色
    private final Color endColor = new Color(0.1f, 0.1f, 0.9f, 0f);   // 深蓝透明
    /**
     * Creates a new portal particle pool and initializes
     * the trail texture used for rendering.
     */

    public PortalParticlePool() {
        createTrailTexture();
    }

    /**
     * Generates a vertical gradient texture used for particle trails.
     * <p>
     * The texture fades out toward its edges to create
     * a smooth glowing trail effect when rendered with additive blending.
     */

    private void createTrailTexture() {
        int width = 8;
        int height = 32;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float distY = Math.abs(y - height / 2f) / (height / 2f); // 0(中心) -> 1(两端)
                float distX = Math.abs(x - width / 2f) / (width / 2f);

                float alpha = (1.0f - distY) * (1.0f - distX);
                alpha = MathUtils.clamp(alpha, 0f, 1f);

                pixmap.setColor(1, 1, 1, alpha);
                pixmap.drawPixel(x, y);
            }
        }
        trailTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    /**
     * Spawns a small burst of spiral particles around a portal center.
     *
     * @param centerX    portal center x-coordinate
     * @param centerY    portal center y-coordinate
     * @param baseRadius base radius of the particle spiral
     */

    public void spawnTornadoParticles(float centerX, float centerY, float baseRadius) {
        int count = MathUtils.random(3, 6);

        for (int i = 0; i < count; i++) {
            PortalParticle p = obtain();
            initTornadoParticle(p, centerX, centerY, baseRadius);
            activeParticles.add(p);
        }
    }
    /**
     * Initializes a particle with spiral motion parameters
     * around the given portal center.
     *
     * @param p          the particle to initialize
     * @param cx         portal center x-coordinate
     * @param cy         portal center y-coordinate
     * @param baseRadius base radius of the spiral
     */

    private void initTornadoParticle(PortalParticle p, float cx, float cy, float baseRadius) {
        p.lifeTimer = 0;
        p.maxLife = MathUtils.random(0.8f, 1.2f);

        p.angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians;
        p.radius = baseRadius * MathUtils.random(0.5f, 1.5f);
        p.height = 0f;
        p.speed = MathUtils.random(100f, 200f);

        p.scale = MathUtils.random(0.6f, 1.2f);
        p.active = true;

        updateParticlePosition(p, cx, cy, 0);
    }
    /**
     * Updates all active particles and returns expired ones
     * back to the pool.
     *
     * @param delta    time elapsed since last frame
     * @param centerX portal center x-coordinate
     * @param centerY portal center y-coordinate
     */

    public void update(float delta, float centerX, float centerY) {


        for (int i = activeParticles.size - 1; i >= 0; i--) {
            PortalParticle p = activeParticles.get(i);

            p.lifeTimer += delta;
            if (p.lifeTimer >= p.maxLife) {
                free(p);
                activeParticles.removeIndex(i);
                continue;
            }

            p.height += p.speed * delta;
            p.angle += 8.0f * delta;
            p.radius -= 10.0f * delta;
            if (p.radius < 5f) p.radius = 5f;

            updateParticlePosition(p, centerX, centerY, delta);
        }
    }

    private void updateParticlePosition(PortalParticle p, float cx, float cy, float delta) {
        float oldX = p.position.x;
        float oldY = p.position.y;

        float offsetX = MathUtils.cos(p.angle) * p.radius;
        float offsetY = MathUtils.sin(p.angle) * p.radius * 0.3f;

        p.position.set(cx + offsetX, cy + offsetY + p.height);

        if (delta > 0) {
            p.velocity.set(p.position.x - oldX, p.position.y - oldY).nor();
        }
    }
    /**
     * Renders all active portal particles using additive blending.
     *
     * @param batch sprite batch used for rendering
     */

    public void render(SpriteBatch batch) {
        if (activeParticles.size == 0) return;

        int srcFunc = batch.getBlendSrcFunc();
        int dstFunc = batch.getBlendDstFunc();
        Color oldColor = batch.getColor();

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        for (PortalParticle p : activeParticles) {
            float progress = p.lifeTimer / p.maxLife;

            batch.setColor(
                    MathUtils.lerp(startColor.r, endColor.r, progress),
                    MathUtils.lerp(startColor.g, endColor.g, progress),
                    MathUtils.lerp(startColor.b, endColor.b, progress),
                    (1.0f - progress)
            );

            float rotation = p.velocity.angleDeg() - 90;

            float width = 8f * p.scale * (1f - progress);
            float height = 30f * p.scale;

            batch.draw(trailTexture,
                    p.position.x - width/2, p.position.y - height/2,
                    width/2, height/2,
                    width, height,
                    1f, 1f,
                    rotation,
                    0, 0, trailTexture.getWidth(), trailTexture.getHeight(), false, false
            );
        }

        batch.setColor(oldColor);
        batch.setBlendFunction(srcFunc, dstFunc);
    }

    private PortalParticle obtain() {
        return (freeParticles.size > 0) ? freeParticles.pop() : new PortalParticle();
    }

    private void free(PortalParticle p) {
        p.active = false;
        freeParticles.add(p);
    }

    public void dispose() {
        if (trailTexture != null) trailTexture.dispose();
    }
}