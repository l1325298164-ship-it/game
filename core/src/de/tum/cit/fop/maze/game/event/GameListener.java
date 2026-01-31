package de.tum.cit.fop.maze.game.event;

import de.tum.cit.fop.maze.game.EnemyTier;
import de.tum.cit.fop.maze.game.score.DamageSource;

/**
 * Listener interface for receiving gameplay-related events.
 *
 * <p>Classes implementing this interface can subscribe to
 * {@link GameEventSource} to react to various game events such as
 * enemy kills, player damage, item collection, and level completion.
 *
 * <p>Typical implementations include achievement tracking,
 * statistics collection, analytics, or tutorial triggers.
 */
public interface GameListener {

    /**
     * Called when an enemy is killed.
     *
     * @param tier the tier of the enemy that was killed
     * @param isDashKill whether the enemy was killed using a dash attack
     */
    void onEnemyKilled(EnemyTier tier, boolean isDashKill);

    /**
     * Called when the player takes damage.
     *
     * @param currentHp the player's current health after taking damage
     * @param source the source of the damage
     */
    void onPlayerDamage(int currentHp, DamageSource source);
    /**
     * Called when the player collects an item.
     *
     * @param itemType the identifier of the collected item
     */
    void onItemCollected(String itemType);
    /**
     * Called when a level is completed.
     *
     * @param levelNumber the completed level number
     */
    void onLevelFinished(int levelNumber);
}