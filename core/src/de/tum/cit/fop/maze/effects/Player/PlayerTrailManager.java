package de.tum.cit.fop.maze.effects.Player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.game.GameConstants;

/**
 * Manages afterimage (ghost trail) effects for the player.
 * <p>
 * This system creates semi-transparent copies of the player's
 * current animation frame to simulate motion blur during
 * running or dashing.
 * <p>
 * The trail effect is purely visual and does not affect gameplay logic.
 */
public class PlayerTrailManager {
    /**
     * Represents a single afterimage of the player.
     * <p>
     * A ghost stores the position, sprite frame, tint color,
     * and remaining alpha used for fading out over time.
     */

    private static class Ghost {
        float x, y;
        float alpha;
        TextureRegion region;
        Color tintColor;

        public Ghost(float x, float y, TextureRegion region, Color color) {
            this.x = x;
            this.y = y;
            this.region = region;
            this.alpha = 1.0f;
            this.tintColor = color;
        }
    }

    private Array<Ghost> ghosts = new Array<>();
    private float spawnTimer = 0;

    private final float DASH_SPAWN_INTERVAL = 0.03f;
    private final float RUN_SPAWN_INTERVAL = 0.1f;

    /**
     * Updates the player trail system.
     * <p>
     * When trail creation is enabled, new afterimages are spawned
     * at fixed intervals depending on the trail color (dash or run).
     * Existing afterimages gradually fade out over time.
     *
     * @param delta              time elapsed since last frame
     * @param playerX            player's grid x-coordinate
     * @param playerY            player's grid y-coordinate
     * @param shouldCreateTrail  whether trail spawning is enabled
     * @param currentFrame       current animation frame of the player
     * @param trailColor         tint color used for the trail effect
     */

    public void update(float delta, float playerX, float playerY, boolean shouldCreateTrail, TextureRegion currentFrame, Color trailColor) {
        if (shouldCreateTrail) {
            spawnTimer += delta;

            float interval = (trailColor.r + trailColor.g + trailColor.b > 2.5f) ? DASH_SPAWN_INTERVAL : RUN_SPAWN_INTERVAL;

            if (spawnTimer >= interval) {
                spawnTimer = 0;
                if (currentFrame != null) {
                    ghosts.add(new Ghost(playerX, playerY, currentFrame, trailColor));
                }
            }
        } else {
            spawnTimer = 0.5f;
        }

        for (int i = ghosts.size - 1; i >= 0; i--) {
            Ghost g = ghosts.get(i);
            g.alpha -= delta * 4.0f;
            if (g.alpha <= 0) {
                ghosts.removeIndex(i);
            }
        }
    }
    /**
     * Renders all active player afterimages using additive blending.
     *
     * @param batch sprite batch used for rendering
     */

    public void render(SpriteBatch batch) {
        if (ghosts.size == 0) return;

        int srcFunc = batch.getBlendSrcFunc();
        int dstFunc = batch.getBlendDstFunc();
        Color oldColor = batch.getColor();

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        for (Ghost g : ghosts) {
            if (g.region == null) continue;

            batch.setColor(g.tintColor.r, g.tintColor.g, g.tintColor.b, g.alpha * 0.5f);

            float scale = (float) GameConstants.CELL_SIZE / g.region.getRegionHeight();
            float visualScale = 2.9f;
            float finalScale = scale * visualScale;

            float drawW = g.region.getRegionWidth() * finalScale;
            float drawH = g.region.getRegionHeight() * finalScale;

            float drawX = g.x * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2f - drawW / 2f;
            float drawY = g.y * GameConstants.CELL_SIZE;

            batch.draw(g.region, drawX, drawY, drawW, drawH);
        }

        batch.setColor(oldColor);
        batch.setBlendFunction(srcFunc, dstFunc);
    }
    /**
     * Clears all active afterimages and releases references.
     */

    public void dispose() {
        ghosts.clear();
    }
}