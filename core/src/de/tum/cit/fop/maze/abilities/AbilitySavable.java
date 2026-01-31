package de.tum.cit.fop.maze.abilities;

import java.util.Map;
/**
 * Defines persistence support for abilities.
 * <p>
 * Implementations are responsible for serializing and restoring
 * their internal runtime state using a key–value map.
 */
public interface AbilitySavable {
    /**
     * Saves the current state of the object.
     *
     * @return a map containing serialized state data
     */
    Map<String, Object> saveState();
    /**
     * Restores the state of the object from the given data map.
     *
     * @param data the map containing previously saved state data
     */
    void loadState(Map<String, Object> data);
}
