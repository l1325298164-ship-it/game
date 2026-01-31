package de.tum.cit.fop.maze.game.score;
/**
 * Represents the source of damage received by the player.
 *
 * <p>Each damage source is associated with a penalty score
 * that is deducted from the player's total score when damage
 * is taken.
 */

public enum DamageSource {


    ENEMY_E01(50),

    ENEMY_E02(50),

    ENEMY_E03(100),

    ENEMY_E04(100),

    BOSS(200),


    TRAP_SPIKE(50),

    TRAP_GEYSER(100),

    TRAP_MINE(150),

    TRAP_MUD(20),



    OBSTACLE_WALL(200),

    UNKNOWN(0);
    /**
     * Score penalty applied when damage is taken from this source.
     */
    public final int penaltyScore;
    /**
     * Creates a damage source with the given score penalty.
     *
     * @param score penalty score applied when damage occurs
     */
    DamageSource(int score) {
        this.penaltyScore = score;
    }
}