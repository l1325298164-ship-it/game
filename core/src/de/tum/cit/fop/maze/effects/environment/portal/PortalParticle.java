package de.tum.cit.fop.maze.effects.environment.portal;

import com.badlogic.gdx.math.Vector2;

/**
 * Data container representing a single portal particle.
 * <p>
 * Stores position, movement, lifetime, and visual parameters
 * used by the portal particle system.
 */
public class PortalParticle {
    /** Current world position of the particle. */
    public Vector2 position = new Vector2();
    /** Current velocity applied to the particle. */
    public Vector2 velocity = new Vector2();
    /** Rotation angle of the particle (degrees). */
    public float angle;
    /** Vertical offset used for height-based effects. */
    public float height;

    /** Radial distance from the portal center. */
    public float radius;
    /** Movement speed of the particle. */
    public float speed;

    /** Time the particle has been alive. */
    public float lifeTimer;
    /** Maximum lifetime of the particle. */
    public float maxLife;
    /** Visual scale factor of the particle. */
    public float scale;
    /** Whether the particle is currently active. */
    public boolean active;
}