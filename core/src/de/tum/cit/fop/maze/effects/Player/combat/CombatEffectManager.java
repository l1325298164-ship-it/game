package de.tum.cit.fop.maze.effects.Player.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.effects.Player.combat.instances.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * Central manager for all combat-related visual effects.
 * <p>
 * This class is responsible for:
 * <ul>
 *   <li>Updating and removing active {@link CombatEffect} instances</li>
 *   <li>Rendering combat effects and particles</li>
 *   <li>Providing high-level spawn methods for common combat visuals</li>
 * </ul>
 *
 * Effects are automatically removed once their lifetime ends.
 */

public class CombatEffectManager {

    private static final int MAX_EFFECTS = 300;
    private final List<CombatEffect> effects;
    private final CombatParticleSystem particleSystem;

    private final BitmapFont scoreFont;
    private final BitmapFont textFont;
    /**
     * Creates a new combat effect manager.
     * <p>
     * Initializes internal effect storage, particle system,
     * and default fonts used for floating text effects.
     */

    public CombatEffectManager() {
        this.effects = new ArrayList<>();
        this.particleSystem = new CombatParticleSystem();

        BitmapFont tmpScoreFont;
        try {
            if (Gdx.files.internal("ui/font.fnt").exists()) {
                tmpScoreFont = new BitmapFont(Gdx.files.internal("ui/font.fnt"));
            } else {
                tmpScoreFont = new BitmapFont();
            }
        } catch (Exception e) {
            tmpScoreFont = new BitmapFont();
        }
        this.scoreFont = tmpScoreFont;
        this.scoreFont.setUseIntegerPositions(false);
        this.scoreFont.getData().setScale(0.8f);

        this.textFont = new BitmapFont();
        this.textFont.setUseIntegerPositions(false);
        this.textFont.getData().setScale(1.0f);
    }
    /**
     * Updates all active combat effects and particles.
     * <p>
     * Effects that have finished their lifetime are automatically removed.
     *
     * @param delta time elapsed since last frame (seconds)
     */

    public void update(float delta) {
        particleSystem.update(delta);
        Iterator<CombatEffect> iterator = effects.iterator();
        while (iterator.hasNext()) {
            CombatEffect effect = iterator.next();
            effect.update(delta, particleSystem);
            if (effect.isFinished()) {
                iterator.remove();
            }
        }
    }
    /**
     * Renders all shape-based combat effects.
     *
     * @param shapeRenderer shape renderer used for drawing
     */

    public void renderShapes(ShapeRenderer shapeRenderer) {
        for (CombatEffect effect : effects) {
            effect.renderShape(shapeRenderer);
        }
        particleSystem.render(shapeRenderer);
    }
    /**
     * Renders all sprite-based combat effects.
     *
     * @param batch sprite batch used for drawing
     */

    public void renderSprites(SpriteBatch batch) {
        for (CombatEffect effect : effects) {
            effect.renderSprite(batch);
        }
    }
    /**
     * Adds a combat effect while enforcing the maximum effect limit.
     * <p>
     * If the limit is exceeded, the oldest effect is removed.
     *
     * @param effect combat effect to add
     */

    private void safeAddEffect(CombatEffect effect) {
        if (effects.size() >= MAX_EFFECTS) {
            if (!effects.isEmpty()) effects.remove(0);
        }
        effects.add(effect);
    }

    /**
     * Spawns a hit spark effect at the given position.
     *
     * @param x x-coordinate in world space
     * @param y y-coordinate in world space
     */
    public void spawnHitSpark(float x, float y) {
        safeAddEffect(new HitSparkEffect(x, y));
    }

    public void spawnAggroPulse(float x, float y) {
        safeAddEffect(new AggroPulseEffect(x, y));
    }

    public void spawnEnemyDeathEffect(float x, float y) {
        for (int i = 0; i < 12; i++) {
            particleSystem.spawn(
                    x + MathUtils.random(-15, 15),
                    y + MathUtils.random(-15, 15),
                    Color.GRAY,
                    MathUtils.random(-60, 60),
                    MathUtils.random(-60, 60),
                    MathUtils.random(5, 10),
                    MathUtils.random(0.5f, 0.8f),
                    true,
                    false
            );
        }
    }

    public void spawnMagicCircle(float x, float y, float radius, float duration) {
        safeAddEffect(new MagicCircleEffect(x, y, radius, duration));
    }

    public void spawnMagicCircle(float x, float y) {
        spawnMagicCircle(x, y, 64f, 1.0f);
    }

    public void spawnMagicPillar(float x, float y, float radius) {
        safeAddEffect(new MagicPillarEffect(x, y, radius));
    }

    public void spawnMagicPillar(float x, float y) {
        spawnMagicPillar(x, y, 64f);
    }

    public void spawnMagicEssence(float startX, float startY, float targetX, float targetY) {
        safeAddEffect(new MagicEssenceEffect(startX, startY, targetX, targetY));
    }

    public void spawnMagicEssence(float targetX, float targetY) {
        float startX = targetX + MathUtils.random(-100, 100);
        float startY = targetY + MathUtils.random(-100, 100);
        spawnMagicEssence(startX, startY, targetX, targetY);
    }

    public void spawnSlash(float x, float y, float angle, int type) {
        safeAddEffect(new SlashEffect(x, y, angle, type));
    }

    public void spawnDash(float x, float y, float directionAngle) {
        safeAddEffect(new DashEffect(x, y, directionAngle));
    }

    /**
     * Spawns a dash effect with optional level-based visuals.
     *
     * @param x              x-coordinate in world space
     * @param y              y-coordinate in world space
     * @param directionAngle dash direction in degrees
     * @param level          dash level affecting visual intensity
     */

    public void spawnDash(float x, float y, float directionAngle, int level) {
        safeAddEffect(new DashEffect(x, y, directionAngle, level));
    }

    public void spawnHeal(float x, float y) {
        safeAddEffect(new HealEffect(x, y));
    }



    public void spawnScoreText(float x, float y, int score) {
        if (score == 0) return;
        String text = (score > 0 ? "+" : "") + score;
        Color color = (score > 0) ? Color.GOLD : Color.RED;

        FloatingTextEffect effect = new FloatingTextEffect(x, y, text, color, scoreFont);

        effect.setTargetScale(0.55f);

        safeAddEffect(effect);
    }

    public void spawnStatusText(float x, float y, String text, Color color) {
        if (text == null || text.isEmpty()) return;
        safeAddEffect(new FloatingTextEffect(x, y, text, color, textFont));
    }

    public void spawnFloatingText(float x, float y, String text, Color color) {
        spawnStatusText(x, y, text, color);
    }

    public void spawnFloatingText(float x, float y, int value, boolean isCrit) {
        Color c = isCrit ? Color.GOLD : Color.RED;
        spawnStatusText(x, y, String.valueOf(value), c);
    }
    /**
     * Disposes all resources used by the combat effect manager.
     * <p>
     * Clears active effects, particle data, and releases font resources.
     */

    public void dispose() {
        effects.clear();
        if (scoreFont != null) scoreFont.dispose();
        if (textFont != null) textFont.dispose();
        particleSystem.clear();
    }
}