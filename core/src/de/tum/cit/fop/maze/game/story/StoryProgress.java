package de.tum.cit.fop.maze.game.story;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages persistent story progression data across game sessions.
 *
 * <p>This class tracks per-chapter progression states such as whether
 * PVs have been watched, tutorials unlocked, bosses defeated, and
 * chapters completed. Progress is stored locally as a JSON file and
 * loaded lazily using a singleton pattern.
 *
 * <p>This class is not intended to be instantiated directly.
 */
public final class StoryProgress {

    /** File name used to store story progress locally. */
    private static final String FILE_NAME = "story_progress.json";
    /** Singleton instance of StoryProgress. */
    private static StoryProgress instance;
    /** Mapping of chapter identifiers to their progression state. */
    private final Map<String, ChapterProgress> chapters = new HashMap<>();


    /**
     * Private constructor to enforce singleton usage.
     */
    private StoryProgress() {}

    /**
     * Loads the story progress from local storage or creates a new instance
     * if no save file exists.
     *
     * @return the loaded or newly created {@code StoryProgress} instance
     */
    public static StoryProgress load() {
        if (instance != null) return instance;

        FileHandle file = Gdx.files.local(FILE_NAME);
        Json json = new Json();

        if (file.exists()) {
            try {
                instance = json.fromJson(StoryProgress.class, file);
            } catch (Exception e) {
                Gdx.app.error("StoryProgress", "Failed to load, creating new", e);
                instance = new StoryProgress();
            }
        } else {
            instance = new StoryProgress();
        }

        return instance;
    }
    /**
     * Saves the current story progress to local storage.
     */
    public void save() {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);

        FileHandle file = Gdx.files.local(FILE_NAME);
        file.writeString(json.prettyPrint(this), false);
    }



    /**
     * Retrieves the progress object for a given chapter, creating it if necessary.
     *
     * @param chapterId the chapter identifier
     * @return the {@link ChapterProgress} for the chapter
     */
    private ChapterProgress chapter(int chapterId) {
        String key = "chapter" + chapterId;
        return chapters.computeIfAbsent(key, k -> new ChapterProgress());
    }


    /**
     * Checks whether the PV for the given chapter has been watched.
     *
     * @param chapterId the chapter identifier
     * @return {@code true} if the PV has been watched
     */
    public boolean isPvWatched(int chapterId) {
        return chapter(chapterId).pvWatched;
    }
    /**
     * Checks whether the tutorial for the given chapter is unlocked.
     *
     * @param chapterId the chapter identifier
     * @return {@code true} if the tutorial is unlocked
     */
    public boolean isTutorialUnlocked(int chapterId) {
        return chapter(chapterId).tutorialUnlocked;
    }
    /**
     * Checks whether the boss encounter for the given chapter is unlocked.
     *
     * @param chapterId the chapter identifier
     * @return {@code true} if the boss is unlocked
     */
    public boolean isBossUnlocked(int chapterId) {
        return chapter(chapterId).bossUnlocked;
    }
    /**
     * Checks whether the boss of the given chapter has been defeated.
     *
     * @param chapterId the chapter identifier
     * @return {@code true} if the boss has been defeated
     */
    public boolean isBossDefeated(int chapterId) {
        return chapter(chapterId).bossDefeated;
    }
    /**
     * Checks whether the given chapter has been fully completed.
     *
     * @param chapterId the chapter identifier
     * @return {@code true} if the chapter is finished
     */
    public boolean isChapterFinished(int chapterId) {
        return chapter(chapterId).chapterFinished;
    }

    /**
     * Marks the PV of the given chapter as watched and unlocks its tutorial.
     *
     * @param chapterId the chapter identifier
     */
    public void markPvWatched(int chapterId) {
        ChapterProgress c = chapter(chapterId);
        c.pvWatched = true;
        c.tutorialUnlocked = true;
    }
    /**
     * Marks the boss of the given chapter as unlocked.
     *
     * @param chapterId the chapter identifier
     */
    public void markBossUnlocked(int chapterId) {
        ChapterProgress c = chapter(chapterId);
        c.bossUnlocked = true;
    }
    /**
     * Marks the boss of the given chapter as defeated and completes the chapter.
     *
     * @param chapterId the chapter identifier
     */
    public void markBossDefeated(int chapterId) {
        ChapterProgress c = chapter(chapterId);
        c.bossDefeated = true;
        c.chapterFinished = true;
    }


    /**
     * Deletes all stored story progress and resets the singleton instance.
     */
    public static void deleteAll() {
        FileHandle file = Gdx.files.local(FILE_NAME);
        if (file.exists()) file.delete();
        instance = null;
    }

    /**
     * Represents progression state for a single story chapter.
     */
    public static class ChapterProgress {
        /** Whether the chapter PV has been watched. */
        public boolean pvWatched = false;
        /** Whether the tutorial has been unlocked. */
        public boolean tutorialUnlocked = false;
        /** Whether the boss encounter has been unlocked. */
        public boolean bossUnlocked = false;
        /** Whether the boss has been defeated. */
        public boolean bossDefeated = false;
        /** Whether the chapter has been fully completed. */
        public boolean chapterFinished = false;
    }
}
