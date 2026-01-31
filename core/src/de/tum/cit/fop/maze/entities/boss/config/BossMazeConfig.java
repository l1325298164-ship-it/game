package de.tum.cit.fop.maze.entities.boss.config;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
/**
 * Root configuration for a boss maze encounter.
 * <p>
 * Defines shared base parameters, AOE timeline behavior,
 * and a sequence of boss phases.
 */
public class BossMazeConfig {
    /**
     * Base configuration shared across all boss phases.
     */
    public Base base;
    /**
     * Timeline configuration for boss AOE attacks.
     */
    public AoeTimeline aoeTimeline;
    /**
     * Ordered list of boss phases.
     */
    public Array<Phase> phases;
    /**
     * Shared base settings applied to the entire boss encounter.
     */
    public static class Base {
        /** Multiplier applied to all enemy health values. */
        public float enemyHpMultiplier;
        /** Multiplier applied to all enemy damage values. */
        public float enemyDamageMultiplier;
        /** Initial number of lives given to the player. */
        public int initialLives;
        /** Number of exits generated in the maze. */
        public int exitCount;
        /** Number of keys required to complete the maze. */
        public int keyCount;
        /** Score multiplier applied during the boss encounter. */
        public float scoreMultiplies;
        /** Damage multiplier applied to player damage. */
        public float damageMultiplies;
    }
    /**
     * Configuration for a single boss phase.
     * <p>
     * Each phase defines its own maze layout, duration,
     * and spawned enemies and traps.
     */
    public static class Phase {
        /** Sequential index of the phase. */
        public int index;
        /** Duration of the phase in seconds. */
        public float duration;
        /** Width of the maze for this phase. */
        public int mazeWidth;
        /** Height of the maze for this phase. */
        public int mazeHeight;
        /** Enemy spawn counts mapped by enemy identifier. */
        public ObjectMap<String, Integer> enemies;
        /** Trap spawn counts mapped by trap identifier. */
        public ObjectMap<String, Integer> traps;
    }
}
