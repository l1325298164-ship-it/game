package de.tum.cit.fop.maze.game.event;

import de.tum.cit.fop.maze.game.EnemyTier;
import de.tum.cit.fop.maze.game.score.DamageSource;
import de.tum.cit.fop.maze.utils.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
/**
 * Central event dispatcher for game-wide events.
 *
 * <p>This class implements a singleton event source that broadcasts
 * gameplay events (enemy kills, damage, item collection, level completion)
 * to all registered {@link GameListener} instances.
 *
 * <p>Listeners can be added or removed dynamically, and events are dispatched
 * in a thread-safe manner using a copy-on-write listener list.
 */

public class GameEventSource {

    private static GameEventSource instance;

    private final List<GameListener> listeners;
    
    private static final int MAX_LISTENERS_WARNING = 10;


    private GameEventSource() {
        this.listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Returns the singleton instance of the game event source.
     *
     * @return the global {@code GameEventSource} instance
     */
    public static GameEventSource getInstance() {
        if (instance == null) {
            instance = new GameEventSource();
        }
        return instance;
    }

    /**
     * Registers a game event listener.
     *
     * <p>If the listener is already registered or {@code null},
     * the request is ignored.
     *
     * @param listener the listener to register
     */
    public void addListener(GameListener listener) {
        if (listener == null) return;
        
        if (listeners.contains(listener)) {
            Logger.warning("GameEventSource: Listener already registered, skipping: " + listener.getClass().getSimpleName());
            return;
        }
        
        listeners.add(listener);
        
        if (listeners.size() > MAX_LISTENERS_WARNING) {
            Logger.warning("GameEventSource: Too many listeners (" + listeners.size() + "), possible memory leak!");
        }
        
        Logger.debug("GameEventSource: Added listener " + listener.getClass().getSimpleName() + ", total: " + listeners.size());
    }

    /**
     * Unregisters a previously registered game event listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(GameListener listener) {
        if (listener == null) return;
        boolean removed = listeners.remove(listener);
        if (removed) {
            Logger.debug("GameEventSource: Removed listener " + listener.getClass().getSimpleName() + ", remaining: " + listeners.size());
        }
    }

    /**
     * Removes all registered game event listeners.
     */
    public void clearListeners() {
        int count = listeners.size();
        listeners.clear();
        Logger.debug("GameEventSource: Cleared " + count + " listeners");
    }

    /**
     * Returns the number of currently registered listeners.
     *
     * @return the listener count
     */
    public int getListenerCount() {
        return listeners.size();
    }

    /**
     * Dispatches an enemy killed event.
     *
     * @param tier the tier of the enemy that was killed
     * @param isDashKill whether the kill was performed using a dash
     */
    public void onEnemyKilled(EnemyTier tier, boolean isDashKill) {
        for (GameListener listener : listeners) {
            try {
                listener.onEnemyKilled(tier, isDashKill);
            } catch (Exception e) {
                Logger.error("GameEventSource: Error in onEnemyKilled listener: " + e.getMessage());
            }
        }
    }

    /**
     * Dispatches a player damage event.
     *
     * @param currentHp the player's current health after taking damage
     * @param source the source of the damage
     */
    public void onPlayerDamage(int currentHp, DamageSource source) {
        for (GameListener listener : listeners) {
            try {
                listener.onPlayerDamage(currentHp, source);
            } catch (Exception e) {
                Logger.error("GameEventSource: Error in onPlayerDamage listener: " + e.getMessage());
            }
        }
    }

    /**
     * Dispatches an item collected event.
     *
     * @param itemType the identifier of the collected item
     */
    public void onItemCollected(String itemType) {
        for (GameListener listener : listeners) {
            try {
                listener.onItemCollected(itemType);
            } catch (Exception e) {
                Logger.error("GameEventSource: Error in onItemCollected listener: " + e.getMessage());
            }
        }
    }
    /**
     * Dispatches a level finished event.
     *
     * @param levelNumber the completed level number
     */
    public void onLevelFinished(int levelNumber) {
        for (GameListener listener : listeners) {
            try {
                listener.onLevelFinished(levelNumber);
            } catch (Exception e) {
                Logger.error("GameEventSource: Error in onLevelFinished listener: " + e.getMessage());
            }
        }
    }
    /**
     * Resets the event source by removing all registered listeners.
     */
    public void reset() {
        clearListeners();
    }
}