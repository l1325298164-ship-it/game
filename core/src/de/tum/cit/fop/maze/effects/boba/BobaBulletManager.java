package de.tum.cit.fop.maze.effects.boba;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import de.tum.cit.fop.maze.entities.enemy.EnemyBoba.BobaBullet;
import de.tum.cit.fop.maze.game.GameConstants;

/**
 * Manages visual effects for Boba bullets.
 * <p>
 * {@code BobaBulletManager} coordinates bullet rendering, trail effects,
 * particle effects, and lifecycle tracking for {@link BobaBullet} instances.
 * It supports different rendering modes and basic performance statistics.
 */
public class BobaBulletManager implements Disposable {
    private final BobaBulletRenderer bulletRenderer;
    private final BobaTrailSystem trailSystem;
    private final BobaParticlePool particlePool;

    private final Array<BobaBullet> managedBullets;
    private boolean isEnabled = true;
    private float effectScale = 1.0f;

    private int maxBulletsInFrame = 0;
    private int bulletsRendered = 0;
    /**
     * Rendering mode for bullet effects.
     */
    public enum RenderMode {
        MANAGED,
        ASSISTED
    }

    private RenderMode renderMode = RenderMode.MANAGED;

    /**
     * Creates a new {@code BobaBulletManager} and initializes
     * all internal effect systems.
     */
    public BobaBulletManager() {
        this.bulletRenderer = new BobaBulletRenderer();
        this.trailSystem = new BobaTrailSystem();
        this.particlePool = new BobaParticlePool();
        this.managedBullets = new Array<>();

        setTrailIntensity(0.7f);

        System.out.println("🔥🔥🔥 BobaBulletManager Constructor executed!");
    }


    /**
     * Adds a single bullet to be managed.
     *
     * @param bullet the bullet to add
     */
    public void addBullet(BobaBullet bullet) {
        if (bullet == null) return;

        if (!managedBullets.contains(bullet, true)) {
            managedBullets.add(bullet);
            bullet.setManagedByEffectManager(true);
            trailSystem.trackBullet(bullet);
            maxBulletsInFrame = Math.max(maxBulletsInFrame, managedBullets.size);
        }
    }

    /**
     * Adds multiple bullets to be managed.
     *
     * @param bullets array of bullets
     */
    public void addBullets(Array<BobaBullet> bullets) {
        if (bullets == null) return;

        for (BobaBullet bullet : bullets) {
            addBullet(bullet);
        }
    }

    /**
     * Removes a bullet from management.
     *
     * @param bullet the bullet to remove
     */
    public void removeBullet(BobaBullet bullet) {
        if (bullet == null) return;

        if (managedBullets.removeValue(bullet, true)) {
            bullet.setManagedByEffectManager(false);
            trailSystem.untrackBullet(bullet);

            if (!bullet.isActive()) {
                createDestructionEffect(bullet);
            }
        }
    }

    /**
     * Updates all managed bullet effects.
     *
     * @param deltaTime time elapsed since last frame (seconds)
     */
    public void update(float deltaTime) {
        if (!isEnabled) return;

        trailSystem.update(deltaTime);

        cleanupInactiveBullets();

        particlePool.update(deltaTime);

        updatePerformanceStats();
    }

    /**
     * Renders all managed bullet effects.
     *
     * @param batch the sprite batch
     */
    public void render(SpriteBatch batch) {
        if (!isEnabled) return;

        bulletsRendered = 0;

        if (renderMode == RenderMode.MANAGED) {
            trailSystem.render(batch);

            for (BobaBullet bullet : managedBullets) {
                if (bullet.isActive()) {
                    bulletRenderer.render(bullet, batch);
                    bulletsRendered++;
                }
            }
        } else {
            trailSystem.render(batch);
        }

        particlePool.render(batch);
    }


    private void cleanupInactiveBullets() {
        for (int i = managedBullets.size - 1; i >= 0; i--) {
            BobaBullet bullet = managedBullets.get(i);

            if (!bullet.isActive()) {
                createDestructionEffect(bullet);

                removeBullet(bullet);
            }
        }
    }


    private void createDestructionEffect(BobaBullet bullet) {
        float pixelX = bullet.getRealX() * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2f;
        float pixelY = bullet.getRealY() * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2f;

        particlePool.createMistEffect(pixelX, pixelY);

        particlePool.createSplashEffect(pixelX, pixelY);

        System.out.println("Playing Mist & Splash effect at " + pixelX + "," + pixelY);
    }
    /**
     * Sets the global effect intensity.
     *
     * @param intensity intensity factor
     */
    public void setEffectIntensity(float intensity) {
        this.effectScale = Math.max(0.1f, Math.min(2.0f, intensity));
        trailSystem.setIntensity(intensity);
        bulletRenderer.setEffectIntensity(intensity);
    }

    /**
     * Sets the intensity of bullet trail effects.
     *
     * @param intensity trail intensity
     */
    public void setTrailIntensity(float intensity) {
        trailSystem.setIntensity(intensity);
    }

    /**
     * Sets the current rendering mode.
     *
     * @param mode the render mode
     */
    public void setRenderMode(RenderMode mode) {
        this.renderMode = mode;

        for (BobaBullet bullet : managedBullets) {
            if (mode == RenderMode.MANAGED) {
            } else {
            }
        }
    }
    /**
     * Enables or disables all bullet effects.
     *
     * @param enabled {@code true} to enable effects
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        if (!enabled) {
            trailSystem.clearAllTrails();
            particlePool.clearAllParticles();
        }
    }

    /**
     * Returns formatted performance statistics.
     *
     * @return performance statistics string
     */
    public String getPerformanceStats() {
        return String.format(
                "Boba - ",
                managedBullets.size,
                bulletsRendered,
                maxBulletsInFrame,
                trailSystem.getActiveParticleCount(),
                particlePool.getActiveParticleCount()
        );
    }


    /**
     * Resets all performance counters.
     */
    public void resetPerformanceStats() {
        maxBulletsInFrame = 0;
        bulletsRendered = 0;
        trailSystem.resetStats();
        particlePool.resetStats();
    }

    /**
     * Clears all managed bullets.
     *
     * @param showEffects whether destruction effects should be shown
     */
    public void clearAllBullets(boolean showEffects) {
        if (showEffects) {
            for (BobaBullet bullet : managedBullets) {
                createDestructionEffect(bullet);
            }
        }

        managedBullets.clear();
        trailSystem.clearAllTrails();
        particlePool.clearAllParticles();
    }

    /**
     * Clears all managed bullets and shows destruction effects.
     */
    public void clearAllBullets() {
        clearAllBullets(true);
    }

    /**
     * Returns the number of managed bullets.
     *
     * @return managed bullet count
     */
    public int getManagedBulletCount() {
        return managedBullets.size;
    }

    /**
     * Returns a copy of all managed bullets.
     *
     * @return array of managed bullets
     */
    public Array<BobaBullet> getManagedBullets() {
        return new Array<>(managedBullets);
    }

    /**
     * Checks whether a bullet is currently managed.
     *
     * @param bullet the bullet to check
     * @return {@code true} if the bullet is managed
     */
    public boolean isManagingBullet(BobaBullet bullet) {
        return managedBullets.contains(bullet, true);
    }


    private void updatePerformanceStats() {
        maxBulletsInFrame = Math.max(maxBulletsInFrame, managedBullets.size);
    }

    @Override
    public void dispose() {
        clearAllBullets(false);

        bulletRenderer.dispose();
        trailSystem.dispose();
        particlePool.dispose();
    }

    /**
     * Draws debug information for bullet effects.
     *
     * @param batch the sprite batch
     */
    public void drawDebug(SpriteBatch batch) {
    }
}