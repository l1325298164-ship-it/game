package de.tum.cit.fop.maze.entities.boss;

import de.tum.cit.fop.maze.entities.boss.config.BossMazeConfig;
/**
 * Holds preloaded data for a single boss phase.
 * <p>
 * Used to cache generated maze data and its corresponding
 * phase configuration before the boss fight starts.
 */
public class BossPhasePreloadData {
    /** Pre-generated maze layout for the phase. */
    public int[][] maze;
    /** Boss phase configuration associated with the maze. */
    public BossMazeConfig.Phase phase;
}