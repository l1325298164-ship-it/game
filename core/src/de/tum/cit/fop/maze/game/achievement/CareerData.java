package de.tum.cit.fop.maze.game.achievement;

import java.util.HashSet;
/**
 * Stores persistent, career-wide achievement and progress data.
 *
 * <p>This class contains cumulative statistics and flags that
 * persist across multiple game runs, and is used by the
 * achievement system to track long-term player progress.
 */

public class CareerData {
    /**
     * Total number of E01 enemies killed across all runs.
     */
    public int totalKills_E01 = 0;

    /**
     * Total number of E02 enemies killed across all runs.
     */
    public int totalKills_E02 = 0;

    /**
     * Total number of E03 enemies killed across all runs.
     */
    public int totalKills_E03 = 0;
    /**
     * Total number of E04 enemies killed using dash attacks.
     */
    public int totalDashKills_E04 = 0;
    /**
     * Total number of enemies killed across all tiers.
     */
    public int totalKills_Global = 0;
    /**
     * Total number of healing items collected.
     */
    public int totalHeartsCollected = 0;
    /**
     * Set of unique treasure buff types collected.
     */
    public HashSet<String> collectedBuffTypes = new HashSet<>();
    /**
     * Whether the player has watched the story PV at least once.
     */
    public boolean hasWatchedPV = false;
    /**
     * Whether the player has healed at least once.
     */
    public boolean hasHealedOnce = false;
    /**
     * Whether the player has cleared the game in hard mode.
     */
    public boolean hasClearedHardMode = false;
    /**
     * Whether the boss has been defeated at least once.
     */
    public boolean hasKilledBoss = false;
    /**
     * Set of unlocked achievement identifiers.
     */
    public HashSet<String> unlockedAchievements = new HashSet<>();
}