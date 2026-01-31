package de.tum.cit.fop.maze.game;

import de.tum.cit.fop.maze.entities.chapter.Chapter1Relic;
/**
 * Listener interface for handling Chapter 1 relic-related events.
 *
 * <p>This listener is used to notify interested systems when a
 * {@link Chapter1Relic} is requested, typically as part of story
 * progression or chapter-specific gameplay logic.
 */
public interface Chapter1RelicListener {

    /**
     * Called when a Chapter 1 relic is requested.
     *
     * <p>This method is typically triggered by story or gameplay logic
     * that requires interaction with a specific relic instance.
     *
     * @param relic the {@link Chapter1Relic} being requested
     */
    void onChapter1RelicRequested(Chapter1Relic relic);
}