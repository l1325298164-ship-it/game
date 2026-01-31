package de.tum.cit.fop.maze.entities.enemy;
/**
 * Defines the behavioral states of an enemy.
 */
enum EnemyState {
    /** Enemy is idle and not actively pursuing a target. */
    IDLE,
    /** Enemy is roaming without a target. */
    PATROL,
    /** Enemy is chasing a detected target. */
    CHASING,
    /** Enemy is actively attacking a target. */
    ATTACK
}
