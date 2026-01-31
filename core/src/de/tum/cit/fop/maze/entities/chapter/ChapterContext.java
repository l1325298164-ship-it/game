package de.tum.cit.fop.maze.entities.chapter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import de.tum.cit.fop.maze.utils.Logger;

import java.util.*;
import java.util.stream.Collectors;
/**
 * Holds runtime state and progression data for a chapter.
 * <p>
 * This context tracks relic availability, player interaction
 * outcomes, and chapter-specific flags during a single run.
 */
public class ChapterContext {

    private final int chapterId;
    private ChapterDropData dropData;

    private final Map<String, RelicState> relicStates = new HashMap<>();

    private RelicData activeRelic = null;

    private boolean fogOverride = false;


    /**
     * Creates and initializes the context for Chapter 1.
     * <p>
     * Loads relic drop data and prepares initial relic states.
     *
     * @return initialized chapter context for chapter 1
     */
    public static ChapterContext chapter1() {
        ChapterContext ctx = new ChapterContext(1);
        ctx.loadDropData("story_file/chapters/chapter1_relics.json");
        return ctx;
    }

    private ChapterContext(int chapterId) {
        this.chapterId = chapterId;
    }



    private void loadDropData(String path) {
        Json json = new Json();
        dropData = json.fromJson(
                ChapterDropData.class,
                Gdx.files.internal(path)
        );

        for (RelicData r : dropData.relics) {
            relicStates.put(r.id, RelicState.UNTOUCHED);
        }
    }



    /**
     * Checks whether at least one untouched relic remains.
     *
     * @return {@code true} if there are still available relics
     */
    public boolean hasRemainingRelic() {
        return relicStates.values().stream()
                .anyMatch(s -> s == RelicState.UNTOUCHED);
    }

    /**
     * Requests a random untouched relic to become active.
     * <p>
     * Only one relic may be active at a time. If a relic is
     * already active or none remain, this returns {@code null}.
     *
     * @return selected relic data, or {@code null} if unavailable
     */
    public RelicData requestRelic() {
        Logger.error(
                "📜 requestRelic activeRelic=" +
                        (activeRelic == null ? "null" : activeRelic.id)
        );
        if (activeRelic != null) return null;

        List<RelicData> pool = dropData.relics.stream()
                .filter(r -> relicStates.get(r.id) == RelicState.UNTOUCHED)
                .toList();

        if (pool.isEmpty()) return null;

        activeRelic = pool.get(MathUtils.random(pool.size() - 1));
        return activeRelic;
    }


    /**
     * Marks a relic as read and clears the active relic state.
     *
     * @param id relic identifier
     */
    public void markRelicRead(String id) {
        relicStates.put(id, RelicState.READ);
        Logger.error("✅ Relic READ -> " + id + " | clear activeRelic");
        if (activeRelic != null && activeRelic.id.equals(id)) {
            activeRelic = null;
        }
    }
    /**
     * Marks a relic as discarded and clears the active relic state.
     *
     * @param id relic identifier
     */
    public void markRelicDiscarded(String id) {
        relicStates.put(id, RelicState.DISCARDED);
        Logger.error("🟡 CONTEXT MARK DISCARDED id=" + id);
        if (activeRelic != null && activeRelic.id.equals(id)) {
            activeRelic = null;
        }
    }

    /**
     * Checks whether all relics in the chapter have been read.
     *
     * @return {@code true} if every relic is marked as read
     */
    public boolean areAllRelicsRead() {
        return relicStates.values().stream()
                .allMatch(s -> s == RelicState.READ);
    }
    /**
     * Checks whether the specified relic has not yet been interacted with.
     *
     * @param id relic identifier
     * @return {@code true} if the relic is untouched
     */
    public boolean isRelicUntouched(String id) {
        return relicStates.get(id) == RelicState.UNTOUCHED;
    }
    /**
     * Returns the chapter identifier.
     *
     * @return chapter id
     */
    public int getChapterId() {
        return chapterId;
    }


    /**
     * Whether fog rendering is overridden for this chapter.
     *
     * @return {@code true} if fog override is enabled
     */
    public boolean enableFogOverride() {
        return fogOverride;
    }
    /**
     * Enables or disables fog override for this chapter.
     *
     * @param enable whether fog override should be active
     */
    public void setFogOverride(boolean enable) {
        this.fogOverride = enable;
    }
    /**
     * Checks whether a relic has already been consumed.
     *
     * @param id relic identifier
     * @return {@code true} if the relic is no longer untouched
     */
    public boolean isRelicConsumed(String id) {
        RelicState state = relicStates.get(id);
        return state != null && state != RelicState.UNTOUCHED;
    }
    /**
     * Logs the current relic states for debugging purposes.
     */
    public void dumpRelicStates() {
        for (var e : relicStates.entrySet()) {
            Logger.error("Relic " + e.getKey() + " -> " + e.getValue());
        }
    }


    /**
     * Clears the currently active relic.
     * <p>
     * Typically used during level or phase transitions.
     */
    public void clearActiveRelic() {
        if (activeRelic != null) {
            Logger.error("🧹 Clear active relic due to level transition: " + activeRelic.id);
        }
        activeRelic = null;
    }
}


