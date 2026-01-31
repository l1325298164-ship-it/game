package de.tum.cit.fop.maze.audio;
/**
 * Defines high-level categories for audio playback.
 * <p>
 * Audio categories are used to group sounds for volume control,
 * mixing, and semantic organization (e.g. music, combat sounds,
 * or user interface feedback).
 */
public enum AudioCategory {

    /** Background music tracks. */
    MUSIC,

    /** Player-related sounds such as abilities or movement. */
    PLAYER,
    /** Enemy-related sounds such as attacks or death effects. */
    ENEMY,
    /** Combat-related sound effects (hits, explosions, etc.). */
    COMBAT,
    /** Environmental sounds from the game world. */
    ENVIRONMENT,
    /** User interface sound effects. */
    UI,
    /** Ambient background sounds. */
    AMBIENT;
}