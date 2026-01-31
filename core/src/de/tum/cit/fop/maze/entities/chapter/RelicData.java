package de.tum.cit.fop.maze.entities.chapter;

import java.util.List;
/**
 * Configuration data for a chapter relic.
 * <p>
 * Defines the identity, presentation, and textual content
 * of a relic.
 */
public class RelicData {
    /** Unique identifier of the relic. */
    public String id;
    /** Type identifier of the relic. */
    public String type;
    /** Display title of the relic. */
    public String title;
    /** Optional background image path for the relic dialog. */
    public String background;
    /** Text content lines associated with the relic. */
    public List<String> content;
}
