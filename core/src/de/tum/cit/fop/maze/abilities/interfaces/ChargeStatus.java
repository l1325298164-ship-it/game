package de.tum.cit.fop.maze.abilities.interfaces;
/**
 * Represents the charge-based status of a skill.
 * <p>
 * Implementations provide information about the current and maximum
 * number of charges available for a skill.
 */
public interface ChargeStatus extends SkillStatus {

    /**
     * Returns the current number of charges.
     *
     * @return the current charge count
     */
    int getCurrentCharges();
    /**
     * Returns the maximum number of charges.
     *
     * @return the maximum charge count
     */
    int getMaxCharges();
}
