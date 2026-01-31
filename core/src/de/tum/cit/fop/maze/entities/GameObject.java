package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
/**
 * Base class for all world objects placed on the maze grid.
 *
 * <p>
 * A {@code GameObject} represents a logical entity that occupies
 * exactly one grid cell at integer coordinates {@code (x, y)}.
 * </p>
 *
 * <p>
 * Responsibilities of this class:
 * <ul>
 *   <li>Provide a unified position model (grid-based).</li>
 *   <li>Define rendering behavior (shape or sprite).</li>
 *   <li>Define basic interaction and collision semantics.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Important design rules:
 * <ul>
 *   <li>{@code GameObject} itself does NOT implement movement.</li>
 *   <li>Passability, interaction, and collision are semantic flags,
 *       not physics-based behavior.</li>
 *   <li>Subclasses must explicitly override behavior if they differ
 *       from the defaults.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class is intentionally minimal and engine-agnostic.
 * Higher-level logic (combat, triggers, AI, pathfinding)
 * must be handled externally (e.g. by {@link de.tum.cit.fop.maze.game.GameManager}).
 * </p>
 */

public abstract class GameObject {
    protected int x, y;
    protected boolean active = true;
    /**
     * Defines how this object should be rendered.
     *
     * <p>
     * {@code SHAPE} is typically used for debug rendering or simple primitives.
     * {@code SPRITE} is used for textured rendering.
     * </p>
     */
    public enum RenderType {
        SHAPE,
        SPRITE
    }
    /**
     * Creates a new game object at the given grid position.
     *
     * @param x grid X coordinate
     * @param y grid Y coordinate
     */
    public GameObject(int x, int y) {
        this.x = x;
        this.y = y;
    }
    /**
     * Renders this object using shape-based rendering.
     *
     * <p>
     * This method is only called if {@link #getRenderType()} returns {@code SHAPE}.
     * </p>
     *
     * @param shapeRenderer shape renderer instance
     */
    public abstract void drawShape(ShapeRenderer shapeRenderer);
    /**
     * Renders this object using sprite-based rendering.
     *
     * <p>
     * This method is only called if {@link #getRenderType()} returns {@code SPRITE}.
     * </p>
     *
     * @param batch sprite batch used for rendering
     */
    public abstract void drawSprite(SpriteBatch batch);
    /**
     * @return the render type used by this object
     */
    public abstract RenderType getRenderType();
    /**
     * Called when the global texture or rendering mode changes.
     *
     * <p>
     * Subclasses may override this to reload textures or update
     * rendering state. Default implementation does nothing.
     * </p>
     */
    public void onTextureModeChanged() {
    }
    /**
     * @return true if this object can be interacted with by the player
     */
    public boolean isInteractable() {
        return false;
    }
    /**
     * Called when a player interacts with this object.
     *
     * <p>
     * This method is only meaningful if {@link #isInteractable()} returns true.
     * </p>
     *
     * @param player the interacting player
     */
    public void onInteract(Player player) {
    }
    /**
     * @return true if this object allows entities to pass through its grid cell
     */
    public boolean isPassable() {
        return true;
    }

    /**
     * Checks grid-based collision with another game object.
     *
     * <p>
     * Collision is defined purely by matching grid coordinates.
     * This method does not consider size, bounding boxes,
     * or continuous movement.
     * </p>
     *
     * @param other another game object
     * @return true if both objects occupy the same grid cell
     */
    public boolean collidesWith(GameObject other) {
        return this.x == other.x && this.y == other.y;
    }
    /** @return grid X coordinate */
    public int getX() { return x; }

    /** @return grid Y coordinate */
    public int getY() { return y; }

    /** Sets grid X coordinate */
    public void setX(int x) { this.x = x; }

    /** Sets grid Y coordinate */
    public void setY(int y) { this.y = y; }

    /**
     * Sets the grid position of this object.
     *
     * @param x grid X coordinate
     * @param y grid Y coordinate
     */
    public void setPosition(int x, int y) { this.x = x; this.y = y; }

    /** @return true if this object is active */
    public boolean isActive() { return active; }

    /** Sets whether this object is active */
    public void setActive(boolean active) { this.active = active; }


    /**
     * @return a human-readable string describing this object's grid position
     */
    public String getPositionString() {
        return "(" + x + ", " + y + ")";
    }
}
