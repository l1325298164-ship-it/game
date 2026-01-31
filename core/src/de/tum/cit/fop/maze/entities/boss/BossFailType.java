package de.tum.cit.fop.maze.entities.boss;

/**
 * Defines the possible failure reasons for a boss fight.
 */
public enum BossFailType {
    /** The player was defeated during the boss fight. */
    PLAYER_DEAD,
    /** The boss fight ended because insufficient damage was dealt. */
    DAMAGE_NOT_ENOUGH
}