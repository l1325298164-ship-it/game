package de.tum.cit.fop.maze.effects.Player.combat.instances;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.effects.Player.combat.CombatEffect;
import de.tum.cit.fop.maze.effects.Player.combat.CombatParticleSystem;
/**
 * Combat visual effect representing a healing action.
 * <p>
 * Periodically spawns upward-moving green particles around the target
 * to visually indicate health recovery.
 */

public class HealEffect extends CombatEffect {

    private float spawnTimer = 0f;
    /**
     * Creates a healing combat effect at the given position.
     *
     * @param x world x-coordinate of the healing effect
     * @param y world y-coordinate of the healing effect
     */
    public HealEffect(float x, float y) {
        super(x, y, 1.0f); // 持续1秒
    }
    /**
     * Updates the healing effect and periodically spawns healing particles.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    combat particle system used to spawn particles
     */
    @Override
    protected void onUpdate(float delta, CombatParticleSystem ps) {
        spawnTimer += delta;
        if (spawnTimer > 0.1f) {
            spawnTimer = 0f;
            for (int i = 0; i < 2; i++) {
                float offsetX = MathUtils.random(-10, 10);
                float offsetY = MathUtils.random(-10, 10);

                ps.spawn(
                        x + offsetX,
                        y + offsetY,
                        Color.GREEN,
                        0,
                        30f,
                        MathUtils.random(4f, 7f),
                        0.8f,
                        false,
                        true
                );
            }
        }
    }
    /**
     * Renders shape-based visuals.
     * <p>
     * This effect does not use shape rendering.
     *
     * @param sr shape renderer
     */
    @Override
    public void renderShape(ShapeRenderer sr) {
    }
    /**
     * Renders sprite-based visuals.
     * <p>
     * This effect relies entirely on particle rendering.
     *
     * @param batch sprite batch
     */
    @Override
    public void renderSprite(SpriteBatch batch) {
    }
}