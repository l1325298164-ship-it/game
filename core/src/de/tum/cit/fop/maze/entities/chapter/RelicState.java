package de.tum.cit.fop.maze.entities.chapter;
/**
 * Represents the interaction state of a chapter relic.
 */
public enum RelicState {
    /** The relic has not been interacted with. */
    UNTOUCHED,
    /** The relic has been read by the player. */
    READ,
    /** The relic has been discarded by the player. */
    DISCARDED
}