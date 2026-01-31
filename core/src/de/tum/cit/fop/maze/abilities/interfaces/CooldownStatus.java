package de.tum.cit.fop.maze.abilities.interfaces;
/**
 * Represents the cooldown-related status of a skill.
 * <p>
 * Implementations provide information about the current cooldown progress
 * of a skill.
 */
public interface CooldownStatus extends SkillStatus {
    /**
     * Returns the current cooldown progress.
     * <p>
     * The value is normalized to the range {@code 0.0f} to {@code 1.0f},
     * where {@code 0.0f} means the skill has just been used and
     * {@code 1.0f} means the cooldown has fully completed.
     *
     * @return the cooldown progress in the range [0, 1]
     */
    float getCooldownProgress(); // 0~1
}
