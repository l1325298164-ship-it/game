package de.tum.cit.fop.maze.game.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import de.tum.cit.fop.maze.game.achievement.CareerData;
import de.tum.cit.fop.maze.utils.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
/**
 * Manages persistent storage of game save data and career progression.
 *
 * <p>This class provides functionality for:
 * <ul>
 *   <li>Saving and loading game progress (auto save and manual slots)</li>
 *   <li>Managing multiple save slots</li>
 *   <li>Saving and loading career-wide achievement data</li>
 *   <li>Supporting optional compression and asynchronous saving</li>
 * </ul>
 *
 * <p>The manager uses local file storage and is implemented as a singleton.
 */
public class StorageManager {

    /**
     * Represents a target location for saving game progress.
     *
     * <p>A save target can be either the automatic save or a specific manual slot.
     */
    public enum SaveTarget {
        AUTO(-1),
        SLOT_1(1),
        SLOT_2(2),
        SLOT_3(3),
        SLOT_4(4),
        SLOT_5(5);

        private final int slotIndex;

        SaveTarget(int slotIndex) {
            this.slotIndex = slotIndex;
        }
        /**
         * Returns the numeric slot index associated with this save target.
         *
         * @return slot index, or -1 for auto save
         */
        public int getSlotIndex() {
            return slotIndex;
        }
        /**
         * Checks whether this target refers to a manual save slot.
         *
         * @return true if this is a manual save slot
         */
        public boolean isSlot() {
            return slotIndex > 0;
        }
        /**
         * Converts a numeric slot index to a save target.
         *
         * @param slot the slot index
         * @return the corresponding save target, or AUTO if none matches
         */
        public static SaveTarget fromSlot(int slot) {
            for (SaveTarget t : values()) {
                if (t.slotIndex == slot) return t;
            }
            return AUTO;
        }
    }

    public static final int MAX_SAVE_SLOTS = 5;
    private static final String AUTO_SAVE_FILE = "save_auto.json.gz";
    private static final String SAVE_SLOT_PATTERN = "save_slot_%d.json.gz";


    private static StorageManager instance;
    /**
     * Returns the singleton instance of the storage manager.
     *
     * @return the global {@code StorageManager} instance
     */
    public static StorageManager getInstance() {
        if (instance == null) {
            instance = new StorageManager();
        }
        return instance;
    }


    private static final String SAVE_FILE_NAME = "save_data.json.gz";
    private static final String CAREER_FILE_NAME = "career_data.json.gz";
    private static final String SAVE_FILE_NAME_LEGACY = "save_data.json";
    private static final String CAREER_FILE_NAME_LEGACY = "career_data.json";


    private final ExecutorService saveExecutor;


    private final ConcurrentLinkedQueue<Future<?>> pendingSaves = new ConcurrentLinkedQueue<>();

    private boolean compressionEnabled = true;


    private boolean asyncEnabled = true;

    private StorageManager() {

        this.saveExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "StorageManager-SaveThread");
            t.setDaemon(true);
            return t;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                waitForAllSaves(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                Logger.warning("Error during shutdown save: " + e.getMessage());
            }
        }));
    }


    private Json createJson() {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);
        return json;
    }

    private String getSlotFileName(int slot) {
        if (slot < 1 || slot > MAX_SAVE_SLOTS) {
            throw new IllegalArgumentException("Invalid save slot: " + slot);
        }
        return String.format(SAVE_SLOT_PATTERN, slot);
    }

    private FileHandle getSaveSlotFile(int slot) {
        return getFile(getSlotFileName(slot));
    }

    private FileHandle getAutoSaveFile() {
        return getFile(AUTO_SAVE_FILE);
    }
    /**
     * Determines the best save slot to use for starting a new game.
     *
     * <p>If an empty slot exists, it will be returned.
     * Otherwise, the slot with the lowest progress (and oldest timestamp)
     * will be selected.
     *
     * @return the recommended save slot index
     */
    public int getBestSlotForNewGame() {
        for (int i = 1; i <= MAX_SAVE_SLOTS; i++) {
            if (!getSaveSlotFile(i).exists()) {
                return i;
            }
        }

        int bestSlot = 1;
        int minLevel = Integer.MAX_VALUE;
        long oldestTime = Long.MAX_VALUE;

        for (int i = 1; i <= MAX_SAVE_SLOTS; i++) {
            GameSaveData data = loadGameFromSlot(i);
            FileHandle file = getSaveSlotFile(i);

            if (data == null) return i;

            if (data.currentLevel < minLevel) {
                minLevel = data.currentLevel;
                oldestTime = file.lastModified();
                bestSlot = i;
            } else if (data.currentLevel == minLevel) {
                if (file.lastModified() < oldestTime) {
                    oldestTime = file.lastModified();
                    bestSlot = i;
                }
            }
        }

        Logger.info("Slots full. Auto-selecting Slot " + bestSlot + " (Level " + minLevel + ") for overwrite.");
        return bestSlot;
    }

    /**
     * Returns the formatted last modification time of a save slot.
     *
     * @param slotIndex save slot index, or -1 for auto save
     * @return formatted timestamp string
     */
    public String getSlotLastModifiedTime(int slotIndex) {
        FileHandle file = (slotIndex == -1) ? getAutoSaveFile() : getSaveSlotFile(slotIndex);
        if (!file.exists()) return "Unknown";

        long lastModified = file.lastModified();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(lastModified));
    }

    /**
     * Saves game data to a specific manual save slot.
     *
     * @param slot the target slot index
     * @param data the game save data to store
     */
    public void saveGameToSlot(int slot, GameSaveData data) {
        if (data == null) return;

        String fileName = getSlotFileName(slot);

        if (asyncEnabled) {
            writeJsonSafelyAsync(fileName, data, compressionEnabled);
            Logger.debug("Game saved to slot " + slot + " (async)");
        } else {
            writeJsonSafelySync(fileName, data, compressionEnabled);
            Logger.info("Game saved to slot " + slot);
        }
    }
    /**
     * Saves game data to the automatic save slot.
     *
     * @param data the game save data to store
     */
    public void saveAuto(GameSaveData data) {
        if (data == null) return;

        if (asyncEnabled) {
            writeJsonSafelyAsync(AUTO_SAVE_FILE, data, compressionEnabled);
        } else {
            writeJsonSafelySync(AUTO_SAVE_FILE, data, compressionEnabled);
        }
    }
    /**
     * Saves game data to the automatic save slot.
     *
     * @param data the game save data to store
     */
    public void saveGameAuto(SaveTarget target, GameSaveData data) {
        if (target != SaveTarget.AUTO) {
            return;
        }
        saveAuto(data);
    }
    /**
     * Loads game data from a specific save slot.
     *
     * @param slot the slot index
     * @return the loaded game save data, or null if not found
     */
    public GameSaveData loadGameFromSlot(int slot) {
        String fileName = getSlotFileName(slot);
        return loadGameInternal(fileName);
    }
    /**
     * Checks whether a specific save slot exists.
     *
     * @param slot the slot index
     * @return true if a save exists in the slot
     */
    public boolean hasSaveInSlot(int slot) {
        String fileName = getSlotFileName(slot);
        return getFile(fileName).exists();
    }

    public boolean[] getSaveSlotStates() {
        boolean[] result = new boolean[MAX_SAVE_SLOTS + 1];
        for (int i = 1; i <= MAX_SAVE_SLOTS; i++) {
            result[i] = hasSaveInSlot(i);
        }
        return result;
    }

    private GameSaveData loadGameInternal(String fileName) {
        FileHandle file = getFile(fileName);
        boolean isCompressed = fileName.endsWith(".gz");

        if (!file.exists()) return null;

        try {
            String jsonStr;

            if (isCompressed) {
                byte[] compressed = file.readBytes();
                jsonStr = decompressData(compressed);
            } else {
                jsonStr = file.readString();
            }

            if (jsonStr == null || jsonStr.isBlank()) return null;

            Json json = createJson();
            GameSaveData data = json.fromJson(GameSaveData.class, jsonStr);

            if (data != null) {
                if (data.currentLevel < 1) {
                    Logger.warning("Invalid level in save: " + data.currentLevel + ", setting to 1");
                    data.currentLevel = 1;
                }
                if (data.score < 0) {
                    Logger.warning("Invalid score in save: " + data.score + ", setting to 0");
                    data.score = 0;
                }
            }

            return data;

        } catch (Exception e) {
            Logger.error("Failed to load save: " + fileName);
            e.printStackTrace();
            return null;
        }
    }

    public void setCompressionEnabled(boolean enabled) {
        this.compressionEnabled = enabled;
    }

    public void setAsyncEnabled(boolean enabled) {
        this.asyncEnabled = enabled;
    }
    /**
     * Waits for all pending asynchronous save operations to complete.
     *
     * @param timeout maximum wait time
     * @param unit time unit
     * @return true if all saves completed within the timeout
     */
    public boolean waitForAllSaves(long timeout, TimeUnit unit) {
        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
        while (!pendingSaves.isEmpty() && System.currentTimeMillis() < deadline) {
            Future<?> future = pendingSaves.poll();
            if (future != null) {
                try {
                    future.get(Math.max(1, deadline - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    Logger.warning("Save task failed: " + e.getMessage());
                }
            }
        }
        return pendingSaves.isEmpty();
    }
    /**
     * Forces completion of all pending save operations.
     */
    public void flushAllSaves() {
        Logger.info("Flushing all pending saves...");
        waitForAllSaves(10, TimeUnit.SECONDS);
        Logger.info("All saves flushed.");
    }

    private byte[] compressData(String jsonStr) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
            gzos.write(jsonStr.getBytes("UTF-8"));
        }
        return baos.toByteArray();
    }

    private String decompressData(byte[] compressed) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        try (GZIPInputStream gzis = new GZIPInputStream(bais)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = gzis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toString("UTF-8");
        }
    }

    private void writeJsonSafelySync(String fileName, Object data, boolean useCompression) {
        if (data == null) return;
        FileHandle tmpFile = null;
        try {
            FileHandle oldTmpFile = getFile(fileName + ".tmp");
            if (oldTmpFile.exists()) {
                try {
                    oldTmpFile.delete();
                } catch (Exception e) {
                    Logger.warning("Failed to delete old temp file: " + e.getMessage());
                }
            }

            Json json = createJson();
            String jsonStr = json.toJson(data);

            tmpFile = getFile(fileName + ".tmp");
            if (useCompression) {
                byte[] compressed = compressData(jsonStr);
                tmpFile.writeBytes(compressed, false);
            } else {
                tmpFile.writeString(jsonStr, false);
            }

            FileHandle targetFile = getFile(fileName);
            tmpFile.moveTo(targetFile);
            tmpFile = null;

        } catch (Exception e) {
            Logger.error("Failed to save data to " + fileName + ": " + e.getMessage());
            e.printStackTrace();

            if (tmpFile != null && tmpFile.exists()) {
                try {
                    tmpFile.delete();
                } catch (Exception cleanupEx) {
                    Logger.warning("Failed to cleanup temp file: " + cleanupEx.getMessage());
                }
            }
        }
    }

    private void writeJsonSafelyAsync(String fileName, Object data, boolean useCompression) {
        if (data == null) return;

        Object dataCopy = deepCopy(data);

        Future<?> future = saveExecutor.submit(() -> {
            writeJsonSafelySync(fileName, dataCopy, useCompression);
        });

        pendingSaves.offer(future);

        while (!pendingSaves.isEmpty()) {
            Future<?> first = pendingSaves.peek();
            if (first.isDone()) {
                pendingSaves.poll();
            } else {
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T deepCopy(T obj) {
        try {
            Json json = createJson();
            String jsonStr = json.toJson(obj);
            return (T) json.fromJson(obj.getClass(), jsonStr);
        } catch (Exception e) {
            Logger.warning("Failed to deep copy object, using original: " + e.getMessage());
            return obj;
        }
    }
    /**
     * Loads the most appropriate game save.
     *
     * <p>Auto save is preferred, followed by manual slots and legacy saves.
     *
     * @return loaded game save data, or null if none exist
     */
    public GameSaveData loadGame() {
        GameSaveData auto = loadAutoSave();
        if (auto != null) {
            Logger.info("Loaded auto save");
            return auto;
        }

        GameSaveData slot1 = loadGameFromSlot(1);
        if (slot1 != null) {
            Logger.info("Loaded save from slot 1");
            return slot1;
        }

        FileHandle legacy = getFile(SAVE_FILE_NAME);
        if (legacy.exists()) {
            Logger.warning("Legacy save detected");
            return loadGameInternal(SAVE_FILE_NAME);
        }

        Logger.info("No save file found");
        return null;
    }
    /**
     * Deletes all save files, including legacy saves.
     */
    public void deleteSave() {
        for (int i = 1; i <= MAX_SAVE_SLOTS; i++) {
            FileHandle slot = getFile(getSlotFileName(i));
            if (slot.exists()) slot.delete();
        }

        FileHandle legacy = getFile(SAVE_FILE_NAME);
        if (legacy.exists()) legacy.delete();

        FileHandle legacyRaw = getFile(SAVE_FILE_NAME_LEGACY);
        if (legacyRaw.exists()) legacyRaw.delete();

        Logger.info("All save files deleted.");
    }
    /**
     * Checks whether any save data exists.
     *
     * @return true if at least one save exists
     */
    public boolean hasAnySave() {
        if (hasAutoSave()) return true;
        for (int i = 1; i <= MAX_SAVE_SLOTS; i++) {
            if (hasSaveInSlot(i)) return true;
        }
        return getFile(SAVE_FILE_NAME).exists();
    }
    /**
     * Saves career-wide progression data.
     *
     * @param data the career data to save
     */
    public void saveCareer(CareerData data) {
        if (asyncEnabled) {
            writeJsonSafelyAsync(CAREER_FILE_NAME, data, compressionEnabled);
            Logger.debug("Career data queued for async save.");
        } else {
            writeJsonSafelySync(CAREER_FILE_NAME, data, compressionEnabled);
        }
    }

    public void saveCareerSync(CareerData data) {
        writeJsonSafelySync(CAREER_FILE_NAME, data, compressionEnabled);
    }
    /**
     * Loads career-wide progression data.
     *
     * @return loaded career data, or a new instance if none exists
     */
    public CareerData loadCareer() {
        FileHandle file = getFile(CAREER_FILE_NAME);
        boolean isCompressed = true;

        if (!file.exists()) {
            file = getFile(CAREER_FILE_NAME_LEGACY);
            isCompressed = false;
        }

        if (!file.exists()) {
            Logger.info("No career data found, creating new profile.");
            return new CareerData();
        }

        try {
            String jsonStr;

            if (isCompressed) {
                byte[] compressed = file.readBytes();
                jsonStr = decompressData(compressed);
            } else {
                jsonStr = file.readString();
            }

            if (jsonStr == null || jsonStr.trim().isEmpty()) {
                Logger.warning("Career file is empty, creating new profile.");
                return new CareerData();
            }

            Json json = createJson();
            CareerData data = json.fromJson(CareerData.class, jsonStr);

            if (data == null) {
                Logger.warning("Failed to parse career data: data is null, creating new profile.");
                return new CareerData();
            }

            if (data.totalKills_E01 < 0) data.totalKills_E01 = 0;
            if (data.totalKills_E02 < 0) data.totalKills_E02 = 0;
            if (data.totalKills_E03 < 0) data.totalKills_E03 = 0;
            if (data.totalDashKills_E04 < 0) data.totalDashKills_E04 = 0;
            if (data.totalKills_Global < 0) data.totalKills_Global = 0;
            if (data.totalHeartsCollected < 0) data.totalHeartsCollected = 0;

            if (data.collectedBuffTypes == null) {
                data.collectedBuffTypes = new java.util.HashSet<>();
            }
            if (data.unlockedAchievements == null) {
                data.unlockedAchievements = new java.util.HashSet<>();
            }

            Logger.info("Career data loaded successfully (" + (isCompressed ? "compressed" : "legacy") + ").");
            return data;
        } catch (Exception e) {
            Logger.error("Failed to load career data, resetting: " + e.getMessage());
            e.printStackTrace();
            return new CareerData();
        }
    }

    private FileHandle getFile(String fileName) {
        return Gdx.files.local(fileName);
    }
    /**
     * Deletes a specific save slot.
     *
     * @param slot the slot index
     * @return true if deletion was successful
     */
    public boolean deleteSaveSlot(int slot) {
        if (slot < 1 || slot > MAX_SAVE_SLOTS) {
            Logger.warning("Attempted to delete invalid save slot: " + slot);
            return false;
        }

        FileHandle file = getFile(getSlotFileName(slot));
        if (file.exists()) {
            boolean success = file.delete();
            if (success) {
                Logger.info("Save slot " + slot + " deleted.");
            } else {
                Logger.warning("Failed to delete save slot " + slot);
            }
            return success;
        }

        Logger.info("Save slot " + slot + " does not exist.");
        return false;
    }

    public GameSaveData loadAutoSave() {
        return loadGameInternal(AUTO_SAVE_FILE);
    }

    public boolean hasAutoSave() {
        return getFile(AUTO_SAVE_FILE).exists();
    }

    public void deleteAutoSave() {
        FileHandle f = getFile(AUTO_SAVE_FILE);
        if (f.exists()) f.delete();
    }

    public void saveGameSync(GameSaveData data) {
        if (data == null) return;
        writeJsonSafelySync(AUTO_SAVE_FILE, data, compressionEnabled);
    }

    public int getFirstEmptySlot() {
        for (int i = 1; i <= MAX_SAVE_SLOTS; i++) {
            if (!hasSaveInSlot(i)) return i;
        }
        return -1;
    }
}