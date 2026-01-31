package de.tum.cit.fop.maze.entities.chapter;

/**
 * Host interface for displaying chapter-related dialogs.
 * <p>
 * Implementations are responsible for presenting the dialog UI
 * and invoking the provided callbacks based on player choice.
 */
public interface ChapterDialogHost {

    /**
     * Opens a dialog for the given relic data.
     *
     * @param data      relic data to display
     * @param onRead    callback executed when the relic is read
     * @param onDiscard callback executed when the relic is discarded
     */
    void openChapterDialog(
            RelicData data,
            Runnable onRead,
            Runnable onDiscard
    );
}
