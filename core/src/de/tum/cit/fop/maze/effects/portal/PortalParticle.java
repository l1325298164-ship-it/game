package de.tum.cit.fop.maze.effects.portal;

import com.badlogic.gdx.math.Vector2;

/**
 * Represents a single particle used in portal visual effects.
 * <p>
 * Each particle follows a spiral / tornado-like motion
 * around a portal center and fades out over its lifetime.
 */

public class PortalParticle {
    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();
    public float angle;
    public float height;
    public float radius;
    public float speed;
    public float lifeTimer;
    public float maxLife;
    public float scale;
    public boolean active;
}