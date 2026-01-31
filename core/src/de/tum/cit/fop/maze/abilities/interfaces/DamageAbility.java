package de.tum.cit.fop.maze.abilities.interfaces;
/**
 * Represents an ability that deals damage.
 * <p>
 * Implementations define the damage value and effective range
 * of the ability.
 */
public interface DamageAbility {
    /**
     * Returns the damage dealt by this ability.
     *
     * @return the damage value
     */
    int getDamage();
    /**
     * Returns the effective range of this ability.
     *
     * @return the range of the ability
     */
    float getRange();
}