package de.tum.cit.fop.maze.game;
/**
 * Configuration container describing all gameplay parameters
 * associated with a specific {@link Difficulty}.
 *
 * <p>This class defines maze size, enemy and trap distribution,
 * combat modifiers, scoring rules, and progression constraints
 * for a single difficulty mode.
 *
 * <p>Instances of this class are immutable and should be created
 * via {@link #of(Difficulty)}.
 */
public class DifficultyConfig {
    /** The difficulty level this configuration represents. */
    public final Difficulty difficulty;
    /** Width of the generated maze (in tiles). */
    public final int mazeWidth;
    /** Height of the generated maze (in tiles). */
    public final int mazeHeight;
    /** Number of exits placed in the maze. */
    public final int exitCount;
    /** Number of E01 (Pearl) enemies spawned. */
    public final int enemyE01PearlCount;
    /** Number of E02 (Coffee Bean) enemies spawned. */
    public final int enemyE02CoffeeBeanCount;
    /** Number of E03 (Caramel) enemies spawned. */
    public final int enemyE03CaramelCount;
    /** Number of E04 (Shell) enemies spawned. */
    public int enemyE04ShellCount;
    /** Number of geyser traps placed in the maze. */
    public final int trapT01GeyserCount;
    /** Number of pearl mine traps placed in the maze. */
    public final int trapT02PearlMineCount;
    /** Number of tea shard traps placed in the maze. */
    public final int trapT03TeaShardCount;
    /** Number of mud tile traps placed in the maze. */
    public final int trapT04MudTileCount;
    /** Initial number of lives given to the player. */
    public final int initialLives;
    /** Multiplier applied to enemy health values. */
    public final float enemyHpMultiplier;
    /** Multiplier applied to enemy damage values. */
    public final float enemyDamageMultiplier;
    /** Number of keys available in the level. */
    public final int keyCount;
    /** Score multiplier applied to level score calculation. */
    public final float scoreMultiplier;
    /** Penalty multiplier applied when taking damage. */
    public final float penaltyMultiplier;
    /**
     * Creates a new difficulty configuration with fully specified parameters.
     *
     * @param difficulty the difficulty level
     * @param mazeWidth width of the maze
     * @param mazeHeight height of the maze
     * @param exitCount number of exits
     * @param enemyE01PearlCount number of E01 enemies
     * @param enemyE02CoffeeBeanCount number of E02 enemies
     * @param enemyE03CaramelCount number of E03 enemies
     * @param enemyE04ShellCount number of E04 enemies
     * @param trapT01GeyserCount number of geyser traps
     * @param trapT02PearlMineCount number of pearl mine traps
     * @param trapT03TeaShardCount number of tea shard traps
     * @param trapT04MudTileCount number of mud tile traps
     * @param initialLives initial player lives
     * @param enemyHpMultiplier enemy health multiplier
     * @param enemyDamageMultiplier enemy damage multiplier
     * @param keyCount number of keys in the level
     * @param scoreMultiplier score multiplier
     * @param penaltyMultiplier penalty multiplier
     */
    public DifficultyConfig(
            Difficulty difficulty, int mazeWidth, int mazeHeight, int exitCount,
            int enemyE01PearlCount, int enemyE02CoffeeBeanCount, int enemyE03CaramelCount, int enemyE04ShellCount,
            int trapT01GeyserCount, int trapT02PearlMineCount, int trapT03TeaShardCount, int trapT04MudTileCount,
            int initialLives, float enemyHpMultiplier, float enemyDamageMultiplier, int keyCount,
            float scoreMultiplier, float penaltyMultiplier

    ) {
        this.difficulty = difficulty;
        this.mazeWidth = mazeWidth;
        this.mazeHeight = mazeHeight;
        this.exitCount = exitCount;

        this.enemyE01PearlCount = enemyE01PearlCount;
        this.enemyE02CoffeeBeanCount = enemyE02CoffeeBeanCount;
        this.enemyE03CaramelCount = enemyE03CaramelCount;
        this.enemyE04ShellCount = enemyE04ShellCount;

        this.trapT01GeyserCount = trapT01GeyserCount;
        this.trapT02PearlMineCount = trapT02PearlMineCount;
        this.trapT03TeaShardCount = trapT03TeaShardCount;
        this.trapT04MudTileCount = trapT04MudTileCount;

        this.initialLives = initialLives;
        this.enemyHpMultiplier = enemyHpMultiplier;
        this.enemyDamageMultiplier = enemyDamageMultiplier;
        this.keyCount = keyCount;


        this.scoreMultiplier = scoreMultiplier;
        this.penaltyMultiplier = penaltyMultiplier;
    }
    /**
     * Creates a predefined {@link DifficultyConfig} instance
     * for the given difficulty level.
     *
     * @param d the desired difficulty
     * @return a fully initialized configuration for the given difficulty
     */
    public static DifficultyConfig of(Difficulty d) {
        return switch (d) {

            case EASY -> new DifficultyConfig(
                    Difficulty.EASY, 50, 50, 3,
                    5, 10, 3, 0,  // 敌人
                    3, 1, 1, 1,  // 陷阱
                    100, 0.7f, 0.6f, 5, // 战斗
                    1.0f, 0.5f //  scoreMultiplier, penaltyMultiplier

            );

            case NORMAL -> new DifficultyConfig(
                    Difficulty.NORMAL, 80, 80, 3,
                    10, 6, 5, 1,

                    /* 敌人 */
                    8, 6, 2,0,

                    /* 陷阱 */
                    50, 1, 1, 2,
                    1f, 1.0f
            );

            case HARD -> new DifficultyConfig(
                    /* 地图 */
                    Difficulty.HARD, 130, 130, 1,
                    20, 10, 6, 1,

                    /* 敌人 */
                    12, 10, 16,5,

                    /* 陷阱 */
                    25, 3, 1.4f, 1,
                    2f, 1.4f
            );

            case TUTORIAL -> new DifficultyConfig(
                    /* 地图 */
                    Difficulty.TUTORIAL, 40, 40, 1,
                    0, 0, 0, 0,

                    /* 敌人 */
                    0, 0, 0,0,

                    /* 陷阱 */
                    0, 0, 0, 0,
                    1f, 1.4f
            );
            case ENDLESS -> new DifficultyConfig(
                    Difficulty.ENDLESS, 140, 140, 0,
                    7, 5, 4, 0,
                    10, 5, 3, 2,
                    100, 1.3f, 1.3f, 0,
                    1.0f, 1.2f
            );
            case BOSS -> new DifficultyConfig(
                    Difficulty.BOSS, 40, 40, 0,

                    5, 3, 0, 0,   // enemies
                    0, 0, 0, 0,   // traps

                    9999,
                    2.0f,
                    1.3f,
                    0,0.4f,1
            );


        };
    }
}