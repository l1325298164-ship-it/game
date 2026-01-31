package de.tum.cit.fop.maze.entities;

/**
 * Represents an entity that can apply a push force to the player.
 *
 * <p>
 * Implementations define how strong the push is and whether the push
 * is lethal (e.g. pits, crushers, deadly traps).
 * </p>
 */
public interface PushSource {
    /**
     * Returns the strength of the push.
     *
     * <p>
     * The value determines how many grid cells the target is pushed.
     * </p>
     *
     * @return push strength in grid units
     */
    int getPushStrength();
    /**
     * Indicates whether this push source is lethal.
     *
     * <p>
     * A lethal push typically causes instant death if the player
     * cannot be pushed safely.
     * </p>
     *
     * @return {@code true} if the push is lethal
     */
    boolean isLethal();
}
