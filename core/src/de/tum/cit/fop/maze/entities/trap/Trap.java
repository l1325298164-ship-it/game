package de.tum.cit.fop.maze.entities.trap;

import de.tum.cit.fop.maze.effects.environment.items.traps.TrapEffectManager;
import de.tum.cit.fop.maze.entities.GameObject;
import de.tum.cit.fop.maze.entities.Player;
/**
 * Base class for all trap entities in the maze.
 * <p>
 * Traps occupy a grid cell, can be active or inactive,
 * and react when a player steps onto them.
 */
public abstract class Trap extends GameObject {
    /**
     * Indicates whether the trap is currently active.
     */
    protected boolean active = true;


    protected TrapEffectManager effectManager;


    /**
     * Creates a trap at the given grid position.
     *
     * @param x grid x-position
     * @param y grid y-position
     */
    public Trap(int x, int y) {
        super(x, y);
    }
    /**
     * Injects the trap effect manager.
     * Called by world or game screen after trap creation.
     */
    public void setEffectManager(TrapEffectManager effectManager) {
        this.effectManager = effectManager;
    }

    /**
     * @return true if this trap has access to a trap effect manager
     */
    protected boolean hasEffectManager() {
        return effectManager != null;
    }

    /**
     * Updates the trap's internal state.
     *
     * @param delta time elapsed since last frame
     */
    public abstract void update(float delta);
    /**
     * Called when a player steps onto the trap.
     *
     * @param player the player triggering the trap
     */
    public abstract void onPlayerStep(Player player);
    /**
     * @return {@code true} if the trap is active
     */
    public boolean isActive() {
        return active;
    }
    /**
     * Traps are not passable.
     *
     * @return {@code false}
     */
    @Override
    public boolean isPassable() {
        return false;
    }
}
