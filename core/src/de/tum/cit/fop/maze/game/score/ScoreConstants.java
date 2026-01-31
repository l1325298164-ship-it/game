package de.tum.cit.fop.maze.game.score;
/**
 * Defines all score values and achievement-related thresholds used in the game.
 *
 * <p>This class contains constants for enemy kill rewards, item collection
 * rewards, and various achievement target conditions. All values are immutable
 * and shared globally.
 */
public class ScoreConstants {
    /** Score gained for killing an E01 Pearl enemy. */
    public static final int SCORE_E01_PEARL = 150;
    /** Score gained for killing an E02 Coffee enemy. */
    public static final int SCORE_E02_COFFEE = 100;
    /** Score gained for killing an E03 Caramel enemy. */
    public static final int SCORE_E03_CARAMEL = 600;
    /** Score gained for killing an E04 Shell enemy. */
    public static final int SCORE_E04_SHELL = 600;
    /** Score gained for defeating the boss. */
    public static final int SCORE_BOSS = 5000;
    /** Score gained for collecting a heart (healing item). */
    public static final int SCORE_HEART = 50;
    /** Score gained for collecting a treasure item. */
    public static final int SCORE_TREASURE = 800;

    /** Score gained for collecting a key. */
    public static final int SCORE_KEY = 50;
    /** Score gained for clearing fog from the map. */
    public static final int SCORE_FOG_CLEARED = 500;
    /** Alias for {@link #SCORE_E01_PEARL}. */
    public static final int E01 = SCORE_E01_PEARL;
    /** Alias for {@link #SCORE_E02_COFFEE}. */
    public static final int E02 = SCORE_E02_COFFEE;
    /** Alias for {@link #SCORE_E03_CARAMEL}. */
    public static final int E03 = SCORE_E03_CARAMEL;
    /** Alias for {@link #SCORE_E04_SHELL}. */
    public static final int E04 = SCORE_E04_SHELL;
    /** Alias for {@link #SCORE_BOSS}. */
    public static final int BOSS = SCORE_BOSS;
    /** Number of E01 enemies required for the corresponding achievement. */
    public static final int TARGET_KILLS_E01 = 60;
    /** Number of E02 enemies required for the corresponding achievement. */
    public static final int TARGET_KILLS_E02 = 40;
    /** Number of E03 enemies required for the corresponding achievement. */
    public static final int TARGET_KILLS_E03 = 50;
    /** Number of dash kills on E04 enemies required for the achievement. */
    public static final int TARGET_KILLS_E04_DASH = 50;
    /** Total number of enemies required for the global kill achievement. */
    public static final int TARGET_KILLS_GLOBAL = 500;

    /** Number of hearts required to unlock the heart collection achievement. */
    public static final int TARGET_HEARTS_COLLECTED = 50;

    /** Number of different treasure types required for the achievement. */
    public static final int TARGET_TREASURE_TYPES = 3;

    /** Maximum number of hits allowed to qualify for the no-damage achievement. */
    public static final int TARGET_NO_DAMAGE_LIMIT = 3;
}