package de.tum.cit.fop.maze.game.score;

/**
 * Immutable container holding the result of a completed level.
 *
 * <p>This class stores score-related values, performance statistics,
 * and the final rank achieved by the player. It is typically used
 * after a level ends to calculate progression, achievements,
 * and leaderboard submissions.
 */
public class LevelResult {
    /**
     * Final score gained for the level after applying penalties
     * and score multipliers.
     */
    public final int finalScore;

    /**
     * Base score obtained before penalties and multipliers.
     */
    public final int baseScore;
    /**
     * Total score deducted due to damage or other penalties.
     */
    public final int penaltyScore;

    /**
     * Rank achieved for the level (e.g. "S", "A", "B", "C", "D").
     */
    public final String rank;

    /**
     * Number of hits taken by the player during the level.
     */
    public final int hitsTaken;

    /**
     * Score multiplier applied based on difficulty or performance.
     */
    public final float scoreMultiplier;
    /**
     * Creates a new {@code LevelResult} with the given scoring data.
     *
     * @param finalScore      final score after all calculations
     * @param baseScore       base score before penalties
     * @param penaltyScore    score deducted due to penalties
     * @param rank            rank achieved for the level
     * @param hitsTaken       number of hits taken by the player
     * @param scoreMultiplier multiplier applied to the base score
     */
    public LevelResult(int finalScore, int baseScore, int penaltyScore, String rank, int hitsTaken, float scoreMultiplier) {
        this.finalScore = finalScore;
        this.baseScore = baseScore;
        this.penaltyScore = penaltyScore;
        this.rank = rank;
        this.hitsTaken = hitsTaken;
        this.scoreMultiplier = scoreMultiplier;
    }
    /**
     * Returns a human-readable summary of the level result.
     *
     * @return formatted string containing rank and score information
     */
    @Override
    public String toString() {
        return "Rank: " + rank + " | Score: " + finalScore + " (Base: " + baseScore + " - Penalty: " + penaltyScore + ")";
    }
}