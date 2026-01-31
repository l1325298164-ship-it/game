package de.tum.cit.fop.maze.game.achievement;

import de.tum.cit.fop.maze.game.Difficulty;
import de.tum.cit.fop.maze.game.EnemyTier;
import de.tum.cit.fop.maze.game.save.GameSaveData;
import de.tum.cit.fop.maze.game.event.GameListener;
import de.tum.cit.fop.maze.game.score.DamageSource;
import de.tum.cit.fop.maze.game.score.ScoreConstants;
import de.tum.cit.fop.maze.utils.Logger;
import de.tum.cit.fop.maze.game.save.StorageManager;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Manages all achievement-related logic during gameplay.
 *
 * <p>This class listens to game events and updates career-wide
 * achievement progress, unlocks achievements when conditions
 * are met, and handles persistence and notification queuing.
 */
public class AchievementManager implements GameListener {

    private final CareerData careerData;
    private GameSaveData gameSaveData;
    private final StorageManager storageManager;
    private final Difficulty currentDifficulty;

    private static final int MAX_NOTIFICATION_QUEUE_SIZE = 50;
    private final Queue<AchievementType> notificationQueue = new LinkedList<>();

    private boolean needsSave = false;
    private int currentLevelDamageTaken = 0;
    /**
     * Creates an achievement manager instance.
     *
     * @param careerData the persistent career-wide achievement data
     * @param gameSaveData the current game save data for session tracking
     * @param storageManager the storage manager used for saving progress
     * @param currentDifficulty the current game difficulty
     */
    public AchievementManager(CareerData careerData,
                              GameSaveData gameSaveData,
                              StorageManager storageManager,
                              Difficulty currentDifficulty) {
        this.careerData = careerData;
        this.gameSaveData = gameSaveData;
        this.storageManager = storageManager;
        this.currentDifficulty = currentDifficulty;
    }
    /**
     * Updates the reference to the current game save data.
     *
     * <p>This should be called when the active save data instance changes,
     * for example after loading a new level or switching save slots.
     *
     * @param newData the new {@link GameSaveData} instance to use
     */
    public void updateGameSaveData(GameSaveData newData) {
        this.gameSaveData = newData;
        Logger.info("AchievementManager: GameSaveData reference updated.");
    }
    /**
     * Retrieves and removes the next unlocked achievement notification.
     *
     * @return the next {@link AchievementType} to be displayed,
     *         or {@code null} if no notification is available
     */
    public AchievementType pollNotification() {
        return notificationQueue.poll();
    }
    /**
     * Handles enemy kill events and updates achievement progress.
     *
     * <p>This method tracks total kills, tier-specific kills,
     * dash kills, and boss kills, unlocking achievements when
     * their conditions are met.
     *
     * @param tier the tier of the enemy that was killed
     * @param isDashKill whether the enemy was killed using a dash attack
     */
    @Override
    public void onEnemyKilled(EnemyTier tier, boolean isDashKill) {
        if (gameSaveData != null) {
            gameSaveData.addSessionKill(tier.name());
        }

        careerData.totalKills_Global++;
        if (careerData.totalKills_Global >= ScoreConstants.TARGET_KILLS_GLOBAL) {
            unlock(AchievementType.ACH_08_BEST_SELLER);
        }

        switch (tier) {
            case E01 -> {
                careerData.totalKills_E01++;
                if (careerData.totalKills_E01 >= ScoreConstants.TARGET_KILLS_E01)
                    unlock(AchievementType.ACH_04_PEARL_SWEEPER);
            }
            case E02 -> {
                careerData.totalKills_E02++;
                if (careerData.totalKills_E02 >= ScoreConstants.TARGET_KILLS_E02)
                    unlock(AchievementType.ACH_05_COFFEE_GRINDER);
            }
            case E03 -> {
                careerData.totalKills_E03++;
                if (careerData.totalKills_E03 >= ScoreConstants.TARGET_KILLS_E03)
                    unlock(AchievementType.ACH_06_CARAMEL_MELT);
            }
            case E04 -> {
                if (isDashKill) {
                    careerData.totalDashKills_E04++;
                    if (careerData.totalDashKills_E04 >= ScoreConstants.TARGET_KILLS_E04_DASH)
                        unlock(AchievementType.ACH_07_SHELL_BREAKER);
                }
            }
            case BOSS -> {
                if (!careerData.hasKilledBoss) {
                    careerData.hasKilledBoss = true;
                    unlock(AchievementType.ACH_15_SUCCESS);
                }
            }
        }
    }
    /**
     * Handles player damage events.
     *
     * <p>This method tracks damage taken during the current level
     * for achievements that require completing a level without
     * taking damage.
     *
     * @param currentHp the player's current HP after taking damage
     * @param source the source of the damage
     */
    @Override
    public void onPlayerDamage(int currentHp, DamageSource source) {
        currentLevelDamageTaken++;
    }
    /**
     * Handles item collection events and updates achievement progress.
     *
     * <p>This method tracks healing items and treasure collection
     * and unlocks achievements when the required conditions are met.
     *
     * @param itemType the identifier of the collected item
     */
    @Override
    public void onItemCollected(String itemType) {
        if (itemType == null) return;

        if ("HEART".equals(itemType) || "BOBA".equals(itemType)) {
            careerData.totalHeartsCollected++;
            if (careerData.totalHeartsCollected >= ScoreConstants.TARGET_HEARTS_COLLECTED) {
                unlock(AchievementType.ACH_09_FREE_TOPPING);
            }

            if (!careerData.hasHealedOnce) {
                careerData.hasHealedOnce = true;
                unlock(AchievementType.ACH_03_BOBA_RESCUE);
            }
        }
        else if (itemType.startsWith("TREASURE")) {
            careerData.collectedBuffTypes.add(itemType);
            if (careerData.collectedBuffTypes.size() >= ScoreConstants.TARGET_TREASURE_TYPES) {
                unlock(AchievementType.ACH_10_TREASURE_MASTER);
            }
        }
    }
    /**
     * Handles level completion events.
     *
     * <p>This method evaluates level-based achievements such as
     * first completion, no-damage clears, and hard-mode progression,
     * and triggers a synchronous save of career data.
     *
     * @param levelNumber the number of the level that was completed
     */
    @Override
    public void onLevelFinished(int levelNumber) {
        if (levelNumber == 1) {
            unlock(AchievementType.ACH_02_FIRST_CUP);
        }

        if (currentLevelDamageTaken <= ScoreConstants.TARGET_NO_DAMAGE_LIMIT) {
            unlock(AchievementType.ACH_11_SEALED_TIGHT);
        }

        currentLevelDamageTaken = 0;

        if (levelNumber >= 3 && currentDifficulty == Difficulty.HARD) {
            if (!careerData.hasClearedHardMode) {
                careerData.hasClearedHardMode = true;
                unlock(AchievementType.ACH_14_RENAISSANCE);
            }
        }

        saveCareerSync();
    }
    /**
     * Marks the story PV as watched and unlocks the corresponding achievement.
     *
     * <p>This method ensures the PV-related achievement is unlocked
     * only once and persists the updated career data.
     */
    public void onPVWatched() {
        if (!careerData.hasWatchedPV) {
            careerData.hasWatchedPV = true;
            unlock(AchievementType.ACH_01_TRAINING);
            saveCareerSync();
        }
    }
    /**
     * Forces an immediate save of the career achievement data.
     *
     * <p>This method bypasses deferred saving and writes the current
     * achievement state directly to persistent storage.
     */
    public void forceSave() {
        saveCareer();
    }
    /**
     * Saves career achievement data if there are pending changes.
     *
     * <p>If any achievements were unlocked since the last save,
     * the data will be persisted and the save flag reset.
     */
    public void saveIfNeeded() {
        if (needsSave) {
            saveCareer();
            needsSave = false;
        }
    }

    private void unlock(AchievementType type) {
        if (!careerData.unlockedAchievements.contains(type.id)) {
            careerData.unlockedAchievements.add(type.id);
            gameSaveData.recordNewAchievement(type.id);

            if (notificationQueue.size() < MAX_NOTIFICATION_QUEUE_SIZE) {
                notificationQueue.add(type);
            } else {
                notificationQueue.poll();
                notificationQueue.add(type);
            }

            Logger.info("🏆 Achievement Unlocked: " + type.displayName);
            needsSave = true;
        }
    }

    private void saveCareer() {
        if (storageManager != null) {
            storageManager.saveCareer(careerData);
        }
    }

    private void saveCareerSync() {
        if (storageManager != null) {
            storageManager.saveCareerSync(careerData);
        }
    }
}