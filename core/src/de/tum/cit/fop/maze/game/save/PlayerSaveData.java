package de.tum.cit.fop.maze.game.save;

import java.util.HashMap;
import java.util.Map;
/**
 * Data container representing the persistent save state of a single player.
 *
 * <p>This class stores positional data, health and mana values,
 * key possession status, active buffs, and ability-related progress.
 *
 * <p>It is designed as a plain data object (DTO) used by
 * {@code GameSaveData} and {@code StorageManager} for serialization
 * and restoration of player state.
 */
public class PlayerSaveData {
    /** Player's X position in the maze grid. */
    public int x;

    /** Player's Y position in the maze grid. */
    public int y;
    /** Current number of lives the player has. */
    public int lives;

    /** Maximum number of lives the player can have. */
    public int maxLives;
    /** Current mana value of the player. */
    public int mana;
    /** Whether the player currently possesses a key. */
    public boolean hasKey;
    /** Whether the attack buff is active. */
    public boolean buffAttack;

    /** Whether the health regeneration buff is active. */
    public boolean buffRegen;
    /** Whether the mana efficiency buff is active. */
    public boolean buffManaEfficiency;
    /**
     * Current upgrade levels of player abilities,
     * mapped by ability identifier.
     */
    public Map<String, Map<String, Object>> abilityStates = new HashMap<>();

    public Map<String, Integer> abilityLevels = new HashMap<>();
}
