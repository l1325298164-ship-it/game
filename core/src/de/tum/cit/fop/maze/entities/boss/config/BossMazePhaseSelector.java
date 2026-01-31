package de.tum.cit.fop.maze.entities.boss.config;

import com.badlogic.gdx.utils.Array;
/**
 * Controls progression through boss maze phases based on elapsed time.
 * <p>
 * This selector tracks the current phase, determines when the next phase
 * should be prepared, and advances the phase when requested.
 */
public class BossMazePhaseSelector {

    private final Array<BossMazeConfig.Phase> phases;

    private int index = 0;
    private float timer = 0f;

    private boolean prepareTriggered = false;
    /**
     * Creates a phase selector for the given phase sequence.
     *
     * @param phases ordered list of boss maze phases
     */
    public BossMazePhaseSelector(Array<BossMazeConfig.Phase> phases) {
        this.phases = phases;
    }

    /**
     * Returns the currently active phase.
     *
     * @return current boss maze phase
     */
    public BossMazeConfig.Phase getCurrent() {
        return phases.get(index);
    }
    /**
     * Checks whether the current phase is the final phase.
     *
     * @return {@code true} if the current phase is the last one
     */
    public boolean isLastPhase() {
        return index >= phases.size - 1;
    }

    /**
     * Determines whether the next phase should be prepared.
     * <p>
     * This method should be called every frame with the elapsed time.
     * It returns {@code true} once when the current phase duration
     * has been reached.
     *
     * @param delta time elapsed since the last frame, in seconds
     * @return {@code true} if the next phase should be prepared
     */
    public boolean shouldPrepareNextPhase(float delta) {
        if (isLastPhase()) return false;

        timer += delta;

        if (!prepareTriggered && timer >= getCurrent().duration) {
            prepareTriggered = true;
            return true;
        }

        return false;
    }
    /**
     * Advances to the next phase and returns it.
     * <p>
     * If the current phase is the last one, the phase index
     * remains unchanged.
     *
     * @return the new current boss maze phase
     */
    public BossMazeConfig.Phase advanceAndGet() {
        if (!isLastPhase()) {
            index++;
        }

        timer = 0f;
        prepareTriggered = false;

        return getCurrent();
    }
}
