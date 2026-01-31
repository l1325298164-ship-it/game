package de.tum.cit.fop.maze.effects.Player.combat.instances;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.effects.Player.combat.CombatEffect;
import de.tum.cit.fop.maze.effects.Player.combat.CombatParticleSystem;
/**
 * Combat visual effect triggered by a dash action.
 * <p>
 * This effect renders trailing particles behind the player and,
 * at higher levels, an expanding shockwave to enhance dash feedback.
 * It is purely visual and does not affect movement or combat logic.
 */
public class DashEffect extends CombatEffect {
    /** Number of particles used by the dash effect. */
    private final int particleCount;
    private final float[] particlesX;
    private final float[] particlesY;
    private final float[] particlesVX;
    private final float[] particlesVY;
    private final float[] particlesLife;

    private final Color effectColor;
    private final boolean hasShockwave;
    private float shockwaveRadius = 0f;
    private final float maxShockwaveRadius;

    /**
     * Creates a dash effect with default visual level.
     *
     * @param x              world x-coordinate of the dash origin
     * @param y              world y-coordinate of the dash origin
     * @param directionAngle dash direction angle in degrees
     */
    public DashEffect(float x, float y, float directionAngle) {
        this(x, y, directionAngle, 1);
    }

    /**
     * Creates a dash effect with level-based visual enhancements.
     *
     * @param x              world x-coordinate of the dash origin
     * @param y              world y-coordinate of the dash origin
     * @param directionAngle dash direction angle in degrees
     * @param level          dash level used to scale visual intensity
     */
    public DashEffect(float x, float y, float directionAngle, int level) {
        super(x, y, 0.5f);


        if (level >= 5) {
            this.particleCount = 20;
            this.effectColor = new Color(1f, 0.85f, 0.2f, 1f);
            this.hasShockwave = true;
            this.maxShockwaveRadius = 40f;
        } else if (level >= 3) {
            this.particleCount = 12;
            this.effectColor = new Color(0.2f, 1f, 1f, 1f);
            this.hasShockwave = true;
            this.maxShockwaveRadius = 25f;
        } else {
            this.particleCount = 8;
            this.effectColor = new Color(0.9f, 0.9f, 0.9f, 1f);
            this.hasShockwave = false;
            this.maxShockwaveRadius = 0f;
        }

        particlesX = new float[particleCount];
        particlesY = new float[particleCount];
        particlesVX = new float[particleCount];
        particlesVY = new float[particleCount];
        particlesLife = new float[particleCount];

        float rad = directionAngle * MathUtils.degRad;
        float backAngle = rad + MathUtils.PI;

        for (int i = 0; i < particleCount; i++) {
            particlesX[i] = x;
            particlesY[i] = y;

            float spread = MathUtils.random(-0.8f, 0.8f);
            float baseSpeed = (level >= 3) ? 50f : 30f;
            float speed = MathUtils.random(baseSpeed, baseSpeed + 50f);

            particlesVX[i] = MathUtils.cos(backAngle + spread) * speed;
            particlesVY[i] = MathUtils.sin(backAngle + spread) * speed;
            particlesLife[i] = MathUtils.random(0.3f, 0.5f);
        }
    }
    /**
     * Updates dash particle movement and shockwave expansion.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    combat particle system (not used by this effect)
     */
    @Override
    protected void onUpdate(float delta, CombatParticleSystem ps) {
        for (int i = 0; i < particleCount; i++) {
            if (particlesLife[i] > 0) {
                particlesX[i] += particlesVX[i] * delta;
                particlesY[i] += particlesVY[i] * delta;
                particlesLife[i] -= delta;
            }
        }

        if (hasShockwave) {
            float expansionSpeed = maxShockwaveRadius / 0.25f;
            shockwaveRadius += expansionSpeed * delta;
        }
    }
    /**
     * Renders shape-based visuals of the dash effect.
     * <p>
     * Includes trailing particles and an optional shockwave.
     *
     * @param sr shape renderer
     */
    @Override
    public void renderShape(ShapeRenderer sr) {

        for (int i = 0; i < particleCount; i++) {
            if (particlesLife[i] > 0) {
                float lifeRatio = particlesLife[i] / 0.5f;
                sr.setColor(effectColor.r, effectColor.g, effectColor.b, lifeRatio);
                float baseSize = (particleCount > 10) ? 5f : 4f;
                float size = baseSize * lifeRatio;
                sr.circle(particlesX[i], particlesY[i], size);
            }
        }

        if (hasShockwave && shockwaveRadius < maxShockwaveRadius) {
            float waveAlpha = 1.0f - (shockwaveRadius / maxShockwaveRadius);

            if (waveAlpha > 0) {
                sr.setColor(effectColor.r, effectColor.g, effectColor.b, waveAlpha * 0.3f);
                sr.circle(x, y, shockwaveRadius);

                if (waveAlpha > 0.2f) {
                    sr.setColor(effectColor.r, effectColor.g, effectColor.b, waveAlpha * 0.5f);
                    sr.circle(x, y, shockwaveRadius * 0.7f);
                }
            }
        }
    }
    /**
     * Renders sprite-based visuals of the dash effect.
     * <p>
     * This effect does not use sprite rendering.
     *
     * @param batch sprite batch
     */
    @Override
    public void renderSprite(SpriteBatch batch) {
    }
}

