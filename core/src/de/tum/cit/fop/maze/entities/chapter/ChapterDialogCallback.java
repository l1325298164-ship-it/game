package de.tum.cit.fop.maze.entities.chapter;
/**
 * Callback interface for chapter relic dialog actions.
 * <p>
 * Used to notify the caller when a relic is read or discarded.
 */
public interface ChapterDialogCallback {
    /**
     * Called when the player chooses to read the relic.
     */
    void onRead();
    /**
     * Called when the player chooses to discard the relic.
     */
    void onDiscard();
}