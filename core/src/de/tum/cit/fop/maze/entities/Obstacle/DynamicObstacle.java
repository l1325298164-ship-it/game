package de.tum.cit.fop.maze.entities.Obstacle;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.fop.maze.entities.GameObject;
import de.tum.cit.fop.maze.game.GameManager;
/**
 * Base class for dynamic obstacles in the maze.
 * <p>
 * Dynamic obstacles can move or change position over time
 * and may block player or enemy movement.
 */
public abstract class DynamicObstacle extends GameObject {

    /**
     * Creates a dynamic obstacle at the given grid position.
     *
     * @param x initial grid x-position
     * @param y initial grid y-position
     */
    public DynamicObstacle(int x, int y) {
        super(x, y);
    }

    protected float worldX, worldY;
    protected boolean isMoving;
    protected float targetX, targetY;

    protected float moveInterval;
    protected float moveCooldown;
    /**
     * Updates the obstacle's behavior and movement.
     *
     * @param delta time elapsed since last frame
     * @param gm    active game manager
     */
    public abstract void update(float delta, GameManager gm);
    /**
     * Draws the obstacle.
     *
     * @param batch sprite batch used for rendering
     */
    public abstract void draw(SpriteBatch batch);
    /**
     * Dynamic obstacles are not passable by default.
     *
     * @return {@code false}
     */
    public boolean isPassable() {
        return false;
    }
    /**
     * @return current world x-position
     */
    public float getWorldX() {
        return worldX;
    }
    /**
     * @return current world y-position
     */
    public float getWorldY() {
        return worldY;
    }

}
