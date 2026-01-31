package de.tum.cit.fop.maze.game.save;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
/**
 * Container class representing all persistent save data for a game session.
 *
 * <p>This class stores player progress, level state, score information,
 * session statistics, and achievement-related data. It is designed to be
 * serializable and safely copied for auto-save and manual save operations.
 *
 * <p>Instances of this class are managed by the {@code StorageManager}
 * and are passed between game systems such as scoring, achievements,
 * and level loading.
 */
public class GameSaveData {

    /** The maze layout of the current level. */
    public int[][] maze;
    /** The current level number. */
    public int currentLevel = 1;

    /** The total accumulated score across all levels. */
    public int score = 0;
    /** The difficulty setting of the save (e.g. EASY, NORMAL, HARD). */
    public String difficulty = "NORMAL";
    /** Whether the game is played in two-player mode. */
    public boolean twoPlayerMode = false;
    /**
     * Saved player-specific data mapped by player identifier.
     */
    public Map<String, PlayerSaveData> players = new HashMap<>();

    /** Base score earned in the current level. */
    public int levelBaseScore = 0;

    /** Penalty score accumulated in the current level. */
    public int levelPenalty = 0;


    /**
     * Enemy kill counts for the current session,
     * mapped by enemy type identifier.
     */
    public HashMap<String, Integer> sessionKills = new HashMap<>();

    /**
     * Newly unlocked achievements in the current session.
     */
    public HashSet<String> newAchievements = new HashSet<>();


    /** Total damage taken by the player in the current session. */
    public int sessionDamageTaken = 0;

    /**
     * Creates a new empty {@code GameSaveData} instance with default values.
     */
    public GameSaveData() {
    }
    /**
     * Copy constructor.
     *
     * <p>Creates a deep copy of the given {@code GameSaveData} instance.
     * This is primarily used for auto-save and safe state transitions.
     *
     * @param other the save data instance to copy from
     */
    public GameSaveData(GameSaveData other) {
        if (other == null) return;

        this.currentLevel = other.currentLevel;
        this.score = other.score;
        this.difficulty = other.difficulty;
        this.twoPlayerMode = other.twoPlayerMode;

        this.levelBaseScore = other.levelBaseScore;
        this.levelPenalty = other.levelPenalty;
        this.sessionDamageTaken = other.sessionDamageTaken;

        if (other.maze != null) {
            this.maze = new int[other.maze.length][];
            for (int i = 0; i < other.maze.length; i++) {
                this.maze[i] = other.maze[i].clone();
            }
        }

        if (other.players != null) {
            this.players = new HashMap<>(other.players);
        }

        if (other.sessionKills != null) {
            this.sessionKills = new HashMap<>(other.sessionKills);
        }

        if (other.newAchievements != null) {
            this.newAchievements = new HashSet<>(other.newAchievements);
        }
    }

    /**
     * Records a kill for the current session.
     *
     * @param enemyType the identifier of the killed enemy
     */
    public void addSessionKill(String enemyType) {
        sessionKills.put(enemyType, sessionKills.getOrDefault(enemyType, 0) + 1);
    }
    /**
     * Records a newly unlocked achievement for the current session.
     *
     * @param achievementId the unique achievement identifier
     */
    public void recordNewAchievement(String achievementId) {
        newAchievements.add(achievementId);
    }

    /**
     * Resets all session-specific statistics.
     *
     * <p>This method is typically called when starting a new level
     * or after saving progress.
     */
    public void resetSessionStats() {
        sessionKills.clear();
        newAchievements.clear();
        sessionDamageTaken = 0;
        levelBaseScore = 0;
        levelPenalty = 0;
    }
}