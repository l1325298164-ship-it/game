package de.tum.cit.fop.maze.game.score;

import de.tum.cit.fop.maze.game.DifficultyConfig;
import de.tum.cit.fop.maze.game.EnemyTier;
import de.tum.cit.fop.maze.game.save.GameSaveData;
import de.tum.cit.fop.maze.game.event.GameListener;
import de.tum.cit.fop.maze.utils.Logger;
/**
 * Manages score calculation, accumulation, and penalties during gameplay.
 *
 * <p>The ScoreManager listens to game events and updates score values
 * accordingly. It tracks both persistent accumulated score and
 * per-level temporary scores.
 */
public class ScoreManager implements GameListener {

    private final DifficultyConfig config;

    /** Total accumulated score from previous levels. */
    private int accumulatedScore = 0;

    /** Base score earned in the current level. */
    private int levelBaseScore = 0;
    /** Total penalty score accumulated in the current level. */
    private int levelPenalty = 0;

    /** Number of times the player was hit during the current level. */
    private int hitsTaken = 0;
    /**
     * Creates a new ScoreManager with the given difficulty configuration.
     *
     * @param config the difficulty configuration affecting score multipliers
     */
    public ScoreManager(DifficultyConfig config) {
        this.config = config;
    }

    /**
     * Saves the current scoring state into the given save data object.
     *
     * @param data the save data to store score-related fields into
     */
    public void saveState(GameSaveData data) {
        data.levelBaseScore = this.levelBaseScore;
        data.levelPenalty = this.levelPenalty;
        data.sessionDamageTaken = this.hitsTaken;
    }
    /**
     * Restores the scoring state from the given save data.
     *
     * @param data the save data containing previously stored score values
     */
    public void restoreState(GameSaveData data) {
        this.accumulatedScore = data.score;
        this.levelBaseScore = data.levelBaseScore;
        this.levelPenalty = data.levelPenalty;
        this.hitsTaken = data.sessionDamageTaken;
        Logger.info("ScoreManager Restored: Total=" + accumulatedScore + ", LevelBase=" + levelBaseScore);
    }
    /**
     * Calculates the player's current total score including the current level.
     *
     * @return the total score capped at {@link Integer#MAX_VALUE}
     */
    public int getCurrentScore() {
        int currentLevelRaw = Math.max(0, levelBaseScore - levelPenalty);
        int currentLevelFinal = (int) (currentLevelRaw * config.scoreMultiplier);
        long totalScore = (long) accumulatedScore + currentLevelFinal;
        return (int) Math.min(totalScore, Integer.MAX_VALUE);
    }

    @Override
    public void onEnemyKilled(EnemyTier tier, boolean isDashKill) {
        int points = 0;
        switch (tier) {
            case E01 -> points = ScoreConstants.SCORE_E01_PEARL;
            case E02 -> points = ScoreConstants.SCORE_E02_COFFEE;
            case E03 -> points = ScoreConstants.SCORE_E03_CARAMEL;

            case E04 -> {
                if (isDashKill) {
                    points = ScoreConstants.SCORE_E04_SHELL;
                } else {
                    points = 0;
                    Logger.debug("E04 Normal Kill - No Score (Requires Dash)");
                }
            }

            case BOSS -> points = ScoreConstants.SCORE_BOSS;
        }

        if (points > 0) {
            levelBaseScore += points;
        }
    }

    @Override
    public void onPlayerDamage(int currentHp, DamageSource source) {
        hitsTaken++;
        int penalty = (int) (source.penaltyScore * config.penaltyMultiplier);
        levelPenalty += penalty;
    }

    @Override
    public void onItemCollected(String itemType) {
        if (itemType == null) return;
        int points = 0;

        if (itemType.equals("HEART") || itemType.equals("BOBA")) {
            points = ScoreConstants.SCORE_HEART;
        } else if (itemType.startsWith("TREASURE")) {
            points = ScoreConstants.SCORE_TREASURE;
        } else if (itemType.equals("KEY")) {
            points = ScoreConstants.SCORE_KEY;
        } else if (itemType.equals("FOG_CLEARED")) {
            points = ScoreConstants.SCORE_FOG_CLEARED;
        }

        if (points > 0) {
            levelBaseScore += points;
        }
    }

    @Override
    public void onLevelFinished(int levelNumber) {
    }
    /**
     * Calculates the final level result including rank.
     *
     * @param theoreticalMaxBaseScore the maximum possible base score for the level
     * @return a {@link LevelResult} containing score breakdown and rank
     */
    public LevelResult calculateResult(int theoreticalMaxBaseScore) {
        int rawScore = Math.max(0, levelBaseScore - levelPenalty);
        int finalScore = (int) (rawScore * config.scoreMultiplier);
        double maxPossibleScore = theoreticalMaxBaseScore * config.scoreMultiplier;

        String rank = determineRank(finalScore, maxPossibleScore);

        return new LevelResult(
                finalScore,
                levelBaseScore,
                levelPenalty,
                rank,
                hitsTaken,
                (float) config.scoreMultiplier
        );
    }
    /**
     * Determines the rank letter based on achieved score ratio.
     *
     * @param score the achieved score
     * @param maxScore the maximum possible score
     * @return the rank letter (S–D)
     */
    private String determineRank(int score, double maxScore) {
        if (maxScore <= 0) return "S";
        double ratio = score / maxScore;

        if (ratio >= 0.90) return "S";
        if (ratio >= 0.70) return "A";
        if (ratio >= 0.50) return "B";
        if (ratio >= 0.30) return "C";
        return "D";
    }

    public void reset() {
        levelBaseScore = 0;
        levelPenalty = 0;
        hitsTaken = 0;
    }
    /**
     * Spends accumulated score if available.
     *
     * @param amount the amount of score to spend
     * @return {@code true} if the score was successfully spent
     */
    public boolean spendScore(int amount) {
        if (amount <= 0) return true;

        int available = accumulatedScore;
        if (available < amount) {
            return false;
        }

        accumulatedScore -= amount;

        Logger.debug("Score spent: -" + amount + ", remaining=" + accumulatedScore);
        return true;
    }
    /**
     * Spends score for upgrades, allowing use of current level score if needed.
     *
     * @param cost the total cost of the upgrade
     * @return {@code true} if the upgrade was successfully purchased
     */
    public boolean spendUpgradeScore(int cost) {
        if (cost <= 0) return true;

        int currentLevelRaw =
                Math.max(0, levelBaseScore - levelPenalty);
        int currentLevelFinal =
                (int) (currentLevelRaw * config.scoreMultiplier);

        int totalAvailable = accumulatedScore + currentLevelFinal;

        if (totalAvailable < cost) {
            Logger.error(
                    "[UPGRADE FAIL] cost=" + cost +
                            " accumulated=" + accumulatedScore +
                            " levelFinal=" + currentLevelFinal
            );
            return false;
        }

        int remain = cost;

        int useFromAccumulated = Math.min(accumulatedScore, remain);
        accumulatedScore -= useFromAccumulated;
        remain -= useFromAccumulated;

        if (remain > 0) {
            int rawNeed =
                    (int) Math.ceil(remain / config.scoreMultiplier);

            levelBaseScore = Math.max(0, levelBaseScore - rawNeed);
        }

        Logger.debug(
                "[UPGRADE OK] cost=" + cost +
                        " remainingTotal=" + getCurrentScore()
        );
        return true;
    }

    /**
     * Returns the number of hits taken during the current level.
     *
     * @return hit count
     */
    public int getHitsTaken() { return hitsTaken; }
}