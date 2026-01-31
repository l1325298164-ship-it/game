package de.tum.cit.fop.maze.game;

/**
 * Enumeration of all supported game difficulty modes.
 *
 * <p>Each difficulty defines a different gameplay experience,
 * including enemy strength, scoring multipliers, and progression rules.
 */
public enum Difficulty {
    /** Beginner-friendly mode with reduced difficulty. */
    EASY,
    /** Standard difficulty intended for normal gameplay experience. */
    NORMAL,
    /** Hard mode with increased challenge and stricter mechanics. */
    HARD,
    /** Tutorial mode used for teaching basic game mechanics. */
    TUTORIAL,

    /** Endless mode with no fixed endpoint and increasing difficulty. */
    ENDLESS,
    /** Boss-only mode focused on boss encounters. */
    BOSS
}
