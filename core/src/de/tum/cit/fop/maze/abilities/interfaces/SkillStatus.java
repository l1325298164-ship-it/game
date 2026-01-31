package de.tum.cit.fop.maze.abilities.interfaces;
/**
 * Represents the runtime status of a skill.
 * <p>
 * Implementations indicate whether a skill is currently active.
 */
public interface SkillStatus {
    /**
     * Returns whether the skill is currently active.
     *
     * @return {@code true} if the skill is active, {@code false} otherwise
     */
    boolean isActive();
}
