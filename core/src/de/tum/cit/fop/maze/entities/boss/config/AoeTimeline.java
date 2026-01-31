package de.tum.cit.fop.maze.entities.boss.config;

import com.badlogic.gdx.utils.Array;
/**
 * Defines a timeline-based configuration for boss AOE (Area of Effect) attacks.
 * <p>
 * A timeline consists of multiple {@link AoePattern}s that are evaluated
 * repeatedly within a cycle.
 */
public class AoeTimeline {
    /**
     * Duration of one full timeline cycle in seconds.
     */
    public float cycle;
    /**
     * List of AOE patterns executed within the timeline cycle.
     */
    public Array<AoePattern> patterns;

    /**
     * Describes a single AOE pattern within the timeline.
     * <p>
     * Each pattern defines when it starts, how often it triggers,
     * and the properties of the generated AOE.
     */
    public static class AoePattern {
        /** Start time (in seconds) within the timeline cycle. */
        public float start;
        /** End time (in seconds) within the timeline cycle. */
        public float end;
        /** Time interval (in seconds) between repeated AOE spawns. */
        public float interval;
        /** Number of AOEs spawned per trigger. */
        public int count;
        /** Radius of the AOE in world units. */
        public float radius;
        /** Duration (in seconds) the AOE remains active. */
        public float duration;
        /** Damage dealt by a single AOE hit. */
        public int damage;
    }
}
