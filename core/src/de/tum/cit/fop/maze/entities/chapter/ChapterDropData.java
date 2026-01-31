package de.tum.cit.fop.maze.entities.chapter;

import java.util.List;
/**
 * Configuration data for chapter-specific drops.
 * <p>
 * Defines available relics, lore items, and their
 * corresponding drop rates for a chapter.
 */
public class ChapterDropData {
    /** Identifier of the chapter this data belongs to. */
    public int chapterId;
    /** Drop rate configuration for different item types. */
    public DropRates drops;
    /** List of relics that can appear in this chapter. */
    public List<RelicData> relics;
    /** List of lore-only items available in this chapter. */
    public List<RelicData> loreItems;
    /**
     * Drop rate definitions for chapter items.
     */
    public static class DropRates {
        /** Probability of spawning a relic. */
        public float relicChance;
        /** Probability of spawning a lore item. */
        public float loreChance;
    }
}
