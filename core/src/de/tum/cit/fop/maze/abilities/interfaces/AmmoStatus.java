package de.tum.cit.fop.maze.abilities.interfaces;
/**
 * Represents the ammunition-related status of a skill.
 * <p>
 * Implementations provide information about the current and maximum
 * amount of ammo available for a skill.
 */
public interface AmmoStatus extends SkillStatus {
    /**
     * Returns the current amount of ammo.
     *
     * @return the current ammo count
     */
    int getCurrentAmmo();
    /**
     * Returns the maximum amount of ammo.
     *
     * @return the maximum ammo count
     */
    int getMaxAmmo();
}