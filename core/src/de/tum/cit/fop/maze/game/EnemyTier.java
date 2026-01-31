package de.tum.cit.fop.maze.game;

/**
 * Represents the tier or category of an enemy.
 *
 * <p>Enemy tiers are used to distinguish different enemy types
 * for purposes such as scoring, achievements, and game events.
 */
public enum EnemyTier {
    /** Tier 1 enemy (basic enemy type). */
    E01,
    /** Tier 2 enemy with increased difficulty. */
    E02,
    /** Tier 3 enemy with higher threat level. */
    E03,
    /** Tier 4 enemy, often requiring special conditions to defeat. */
    E04,
    /** Boss enemy representing a major encounter. */
    BOSS
}