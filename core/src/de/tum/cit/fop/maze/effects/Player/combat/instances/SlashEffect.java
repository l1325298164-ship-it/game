package de.tum.cit.fop.maze.effects.Player.combat.instances;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.effects.Player.combat.CombatEffect;
import de.tum.cit.fop.maze.effects.Player.combat.CombatParticleSystem;

import java.util.ArrayList;
import java.util.List;
/**
 * Combat effect representing a melee slash arc.
 * <p>
 * The slash is rendered as a curved arc in front of the player,
 * with visual intensity and additional effects depending on level.
 * Higher levels add sparks and shockwave visuals.
 */

public class SlashEffect extends CombatEffect {
    /** Direction angle of the slash in degrees. */
    private final float rotation;
    /** Visual level of the slash effect (clamped between 1 and 3). */
    private final int level;
    private final Color coreColor;
    private final Color glowColor;
    /** Precomputed points defining the curved slash arc. */
    private final List<Vector2> arcPoints = new ArrayList<>();
    private float shockwaveRadius = 0f;
    /**
     * Creates a slash combat effect.
     *
     * @param x        world x-coordinate of the slash origin
     * @param y        world y-coordinate of the slash origin
     * @param rotation direction angle of the slash in degrees
     * @param level    visual level of the slash effect (1–3)
     */
    public SlashEffect(float x, float y, float rotation, int level) {
        super(x, y, 0.2f);
        this.rotation = rotation;
        this.level = MathUtils.clamp(level, 1, 3);

        if (this.level == 1) {
            this.coreColor = new Color(1f, 1f, 1f, 0.9f);
            this.glowColor = new Color(0.8f, 0.8f, 1f, 0.3f);
        } else if (this.level == 2) {
            this.coreColor = new Color(1f, 0.9f, 0.2f, 0.9f);
            this.glowColor = new Color(1f, 0.5f, 0f, 0.5f);
        } else {
            this.coreColor = new Color(0.2f, 1f, 1f, 0.9f);
            this.glowColor = new Color(0f, 0.5f, 1f, 0.6f);
        }

        float radius = (level == 3) ? 75f : 55f; // 半径
        int segments = 12;
        float sweepAngle = 120f;
        float startAngle = rotation - sweepAngle / 2f;

        for (int i = 0; i <= segments; i++) {
            float progress = (float) i / segments;
            float angle = startAngle + (progress * sweepAngle);
            float r = radius + MathUtils.sin(progress * MathUtils.PI) * 8f;
            float px = x + MathUtils.cosDeg(angle) * r;
            float py = y + MathUtils.sinDeg(angle) * r;
            arcPoints.add(new Vector2(px, py));
        }
    }
    /**
     * Updates the slash effect.
     * <p>
     * Higher levels may spawn spark particles and an expanding shockwave
     * to enhance visual impact.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    combat particle system used for particle spawning
     */

    @Override
    protected void onUpdate(float delta, CombatParticleSystem ps) {
        if (level >= 2 && MathUtils.randomBoolean(0.4f)) {
            spawnSpark(ps);
        }
        if (level == 3) {
            shockwaveRadius += delta * 400f;
        }
    }
    /**
     * Spawns a spark particle emitted from the slash arc.
     *
     * @param ps combat particle system used to spawn spark particles
     */

    private void spawnSpark(CombatParticleSystem ps) {
        float angle = rotation + MathUtils.random(-50, 50);
        float dist = MathUtils.random(30, 60);
        float px = x + MathUtils.cosDeg(angle) * dist;
        float py = y + MathUtils.sinDeg(angle) * dist;

        float speed = MathUtils.random(80, 200);
        ps.spawn(px, py, glowColor,
                MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed,
                MathUtils.random(3, 5), 0.4f, true, false);
    }
    /**
     * Renders the slash arc using shape primitives.
     * <p>
     * The arc consists of a glowing outer trail and a brighter inner core.
     * Level 3 additionally renders a radial shockwave effect.
     *
     * @param sr shape renderer
     */

    @Override
    public void renderShape(ShapeRenderer sr) {
        float p = timer / maxDuration;
        float alpha = 1f - p;
        if (alpha <= 0) return;

        for (int i = 0; i < arcPoints.size() - 1; i++) {
            Vector2 p1 = arcPoints.get(i);
            Vector2 p2 = arcPoints.get(i + 1);

            float progress = (float) i / (arcPoints.size() - 1);
            float thicknessFactor = MathUtils.sin(progress * MathUtils.PI);

            float baseWidth = (level == 3) ? 10f : 6f;
            if (level == 1) baseWidth = 4f;

            float w = baseWidth * thicknessFactor;

            sr.setColor(glowColor.r, glowColor.g, glowColor.b, alpha * glowColor.a);
            sr.rectLine(p1.x, p1.y, p2.x, p2.y, w * 2.5f);

            sr.setColor(coreColor.r, coreColor.g, coreColor.b, alpha * coreColor.a);
            sr.rectLine(p1.x, p1.y, p2.x, p2.y, w);
        }

        if (level == 3) {
            sr.setColor(glowColor.r, glowColor.g, glowColor.b, alpha * 0.5f);
            float r = shockwaveRadius;
            for(int i=0; i<10; i++) {
                float a = i * 36 + rotation;
                float sx = x + MathUtils.cosDeg(a) * r;
                float sy = y + MathUtils.sinDeg(a) * r;
                float ex = x + MathUtils.cosDeg(a) * (r + 20);
                float ey = y + MathUtils.sinDeg(a) * (r + 20);
                sr.rectLine(sx, sy, ex, ey, 2f);
            }
        }
    }
}