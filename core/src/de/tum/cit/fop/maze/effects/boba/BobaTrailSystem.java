package de.tum.cit.fop.maze.effects.boba;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import de.tum.cit.fop.maze.entities.enemy.EnemyBoba.BobaBullet;
import de.tum.cit.fop.maze.game.GameConstants;

/**
 * Generates and manages trailing visual effects for moving boba bullets.
 * <p>
 * This system attaches fading trail points to active {@link BobaBullet}
 * instances and updates them over time to create a motion blur effect.
 * It is intended for internal use by bullet effect managers.
 */
public class BobaTrailSystem {

    private static class TrailPoint {
        Vector2 position;
        float size;
        float alpha;
        float lifetime;

        TrailPoint(Vector2 pos, float size, float lifetime) {
            this.position = new Vector2(pos);
            this.size = size;
            this.alpha = 0.8f;
            this.lifetime = lifetime;
        }

        void update(float delta) {
            lifetime -= delta;
            alpha = Math.max(0, lifetime);
            size *= 0.95f;
        }
    }
    /** Mapping between bullets and their corresponding trail points. */
    private ObjectMap<BobaBullet, Array<TrailPoint>> bulletTrails;
    /** Intensity factor controlling trail size and density. */
    private float trailIntensity = 0.7f;
    private float trailLifetime = 0.5f;
    private int maxPointsPerBullet = 15;
    /**
     * Creates a new trail system.
     */
    public BobaTrailSystem() {
        bulletTrails = new ObjectMap<>();
    }
    /**
     * Starts tracking a bullet to generate trail effects.
     *
     * @param bullet the bullet to track
     */
    public void trackBullet(BobaBullet bullet) {
        if (bullet == null || bulletTrails.containsKey(bullet)) return;

        bulletTrails.put(bullet, new Array<TrailPoint>());
    }
    /**
     * Stops tracking a bullet and removes its trail.
     *
     * @param bullet the bullet to untrack
     */

    public void untrackBullet(BobaBullet bullet) {
        if (bullet == null) return;

        bulletTrails.remove(bullet);
    }
    /**
     * Updates all trail points for tracked bullets.
     *
     * @param delta time elapsed since last frame (seconds)
     */

    public void update(float delta) {
        for (ObjectMap.Entry<BobaBullet, Array<TrailPoint>> entry : bulletTrails.entries()) {
            BobaBullet bullet = entry.key;
            Array<TrailPoint> trail = entry.value;

            if (bullet.isActive()) {
                Vector2 currentPos = new Vector2(bullet.getRealX(), bullet.getRealY());
                addTrailPoint(bullet, currentPos);
            }

            for (int i = trail.size - 1; i >= 0; i--) {
                TrailPoint point = trail.get(i);
                point.update(delta);

                if (point.lifetime <= 0) {
                    trail.removeIndex(i);
                }
            }
        }
    }

    private void addTrailPoint(BobaBullet bullet, Vector2 position) {
        Array<TrailPoint> trail = bulletTrails.get(bullet);
        if (trail == null) return;

        if (trail.size >= maxPointsPerBullet) {
            trail.removeIndex(0);
        }

        float baseSize = GameConstants.CELL_SIZE * 0.15f * trailIntensity;
        TrailPoint point = new TrailPoint(position, baseSize, trailLifetime);
        trail.add(point);
    }
    /**
     * Renders all active bullet trails.
     *
     * @param batch the sprite batch
     */

    public void render(SpriteBatch batch) {
    }
    /**
     * Sets the trail intensity.
     *
     * @param intensity intensity factor in range [0, 1]
     */
    public void setIntensity(float intensity) {
        this.trailIntensity = Math.max(0, Math.min(1, intensity));
        this.maxPointsPerBullet = (int)(15 * intensity);
    }
    /**
     * Removes all trail data for all bullets.
     */
    public void clearAllTrails() {
        bulletTrails.clear();
    }
    /**
     * Returns the total number of active trail points.
     *
     * @return active trail point count
     */
    public int getActiveParticleCount() {
        int count = 0;
        for (Array<TrailPoint> trail : bulletTrails.values()) {
            count += trail.size;
        }
        return count;
    }

    public void resetStats() {
    }
    /**
     * Clears all internal trail data.
     */
    public void dispose() {
        clearAllTrails();
    }
}