package de.tum.cit.fop.maze.entities.boss.config;

import java.util.List;
/**
 * Defines a time-based timeline for boss-related events.
 * <p>
 * A timeline has a fixed length and contains ordered events
 * that are evaluated over time.
 */
public class BossTimeline {
    /**
     * Total length of the timeline in seconds.
     */
    public float length;
    /**
     * List of events occurring within the timeline.
     */
    public List<BossTimelineEvent> events;
}
