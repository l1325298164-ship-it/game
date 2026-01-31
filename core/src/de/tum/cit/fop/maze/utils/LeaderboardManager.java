package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
/**
 * Manages the local leaderboard of high scores.
 * <p>
 * Scores are stored persistently in a JSON file and kept sorted in
 * descending order. Only the top {@value #MAX_SCORES} scores are retained.
 */
public class LeaderboardManager {
    private static final String LEADERBOARD_FILE = "leaderboard.json";
    private static final int MAX_SCORES = 10;
    /**
     * Represents a single high score entry in the leaderboard.
     * <p>
     * Each entry consists of a player name and a score value.
     * Entries are comparable so they can be sorted by score in descending order.
     */
    public static class HighScore implements Comparable<HighScore> {
        public String name;
        public int score;
        /**
         * Empty constructor required for JSON deserialization.
         */
        public HighScore() {}
        /**
         * Creates a new high score entry.
         *
         * @param name  the player's name
         * @param score the achieved score
         */
        public HighScore(String name, int score) {
            this.name = name;
            this.score = score;
        }
        /**
         * Compares this score with another score for sorting.
         * <p>
         * Higher scores are ordered before lower scores.
         *
         * @param other the other high score to compare to
         * @return a negative value if this score is higher,
         *         zero if equal, or a positive value otherwise
         */
        @Override
        public int compareTo(HighScore other) {
            return Integer.compare(other.score, this.score);
        }
    }

    private Array<HighScore> scores;
    /**
     * Creates a new leaderboard manager and loads stored scores from disk.
     */
    public LeaderboardManager() {
        scores = new Array<>();
        load();
    }
    /**
     * Adds a new score to the leaderboard.
     * <p>
     * The leaderboard is sorted and trimmed automatically,
     * and the updated data is saved to disk.
     *
     * @param name  the player's name
     * @param score the achieved score
     */
    public void addScore(String name, int score) {
        scores.add(new HighScore(name, score));
        sortAndTrim();
        save();
    }
    /**
     * Checks whether a given score qualifies as a high score.
     *
     * @param score the score to check
     * @return {@code true} if the score would appear in the leaderboard,
     *         {@code false} otherwise
     */
    public boolean isHighScore(int score) {
        if (scores.size < MAX_SCORES) return true;
        return score > scores.get(scores.size - 1).score;
    }
    /**
     * Returns the list of stored high scores.
     *
     * @return an array of high score entries sorted by score
     */
    public Array<HighScore> getScores() {
        return scores;
    }

    private void sortAndTrim() {
        scores.sort();
        if (scores.size > MAX_SCORES) {
            scores.truncate(MAX_SCORES);
        }
    }

    private void save() {
        Json json = new Json();
        FileHandle file = Gdx.files.local(LEADERBOARD_FILE);
        file.writeString(json.toJson(scores), false);
    }

    @SuppressWarnings("unchecked")
    private void load() {
        FileHandle file = Gdx.files.local(LEADERBOARD_FILE);
        if (file.exists()) {
            try {
                Json json = new Json();
                scores = json.fromJson(Array.class, HighScore.class, file);
                if (scores == null) {
                    scores = new Array<>();
                }
                sortAndTrim();
            } catch (Exception e) {
                scores = new Array<>();
                Logger.warning("Failed to load leaderboard: " + e.getMessage());
            }
        }
    }
}