package de.tum.cit.fop.maze.entities.boss.config;
/**
 * Defines a single event within a boss timeline.
 * <p>
 * Events are triggered at a specific time and interpreted
 * based on their {@code type}.
 */
public class BossTimelineEvent {
    /** Trigger time of the event in seconds. */
    public float time;
    /** Type identifier used to interpret this event. */
    public String type;
    /** Speaker identifier for dialogue-related events. */
    public String speaker;
    /** Text content for dialogue-related events. */
    public String text;
    /** Voice or audio identifier associated with the event. */
    public String voice;
    /** Optional threshold value for conditional events. */
    public Float threshold;
    /** Duration of the event in seconds, if applicable. */
    public Float duration;
    /** Interval between repeated ticks for periodic events. */
    public Float tickInterval;
    /** Damage applied by damage-related events. */
    public Integer damage;
    /** Horizontal amplitude for camera or entity movement effects. */
    public Float xAmp;
    /** Vertical amplitude for camera or entity movement effects. */
    public Float yAmp;
    /** Horizontal frequency for camera or entity movement effects. */
    public Float xFreq;
    /** Vertical frequency for camera or entity movement effects. */
    public Float yFreq;
    /** Whether this event has already been triggered. */
    public boolean triggered = false;
}

