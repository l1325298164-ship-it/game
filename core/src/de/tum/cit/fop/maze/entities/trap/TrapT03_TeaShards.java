package de.tum.cit.fop.maze.entities.trap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.utils.TextureManager;

/**
 * Trap T03: Tea Shards.
 * <p>
 * A ground trap that periodically damages players and applies
 * a temporary slow effect while they remain on the tile.
 */
public class TrapT03_TeaShards extends Trap {
    private boolean effectSpawned = false;
    private enum State {
        IDLE,
        DAMAGING
    }
    private static TextureRegion idleFrame;
    private static Animation<TextureRegion> damageAnimation;

    private float animTime = 0f;
    private State state = State.IDLE;
    private static Animation<TextureRegion> animation;
    private static final int DAMAGE = 5;
    private static final float DAMAGE_INTERVAL = 0.5f;
    private static final float SLOW_DURATION = 2.0f;
    private TextureManager textureManager;
    private float damageTimer = 0f;
    /**
     * Creates a tea shard trap at the given position.
     *
     * @param x grid x-position
     * @param y grid y-position
     */
    public TrapT03_TeaShards(int x, int y) {
        super(x, y);
        TextureAtlas atlas = new TextureAtlas("ani/T03/T03.atlas");
        loadAnimation(atlas);


    }
    /**
     * Tea shards are passable and can be stepped on.
     *
     * @return {@code true}
     */
    @Override
    public boolean isPassable() {
        return true;
    }

    /**
     * Updates animation timing and damage cooldown.
     *
     * @param delta time elapsed since last frame
     */
    @Override
    public void update(float delta) {
        animTime += delta;

        if (state == State.DAMAGING) {
            damageTimer -= delta;
            if (damageTimer <= 0f) {
                damageTimer = 0f;
                state = State.IDLE;
                effectSpawned = false;
            }
        }
    }
    private static void loadAnimation(TextureAtlas atlas) {
        if (damageAnimation != null) return;

        Array<TextureAtlas.AtlasRegion> regions =
                atlas.findRegions("T03");

        idleFrame = regions.get(7);
        damageAnimation = new Animation<>(
                0.15f,
                regions,
                Animation.PlayMode.LOOP
        );
    }


    /**
     * Applies periodic damage and a slow effect
     * when a player steps onto the trap.
     *
     * @param player the player triggering the trap
     */
    @Override
    public void onPlayerStep(Player player) {

        state = State.DAMAGING;

        if (damageTimer <= 0f) {
            player.takeDamage(DAMAGE);
            damageTimer = DAMAGE_INTERVAL;
            if (hasEffectManager() && !effectSpawned) {
                float worldX = x * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2f;
                float worldY = y * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2f;

                effectManager.spawnTeaShards(worldX, worldY);
                effectSpawned = true;
            }
        }

        player.applySlow(SLOW_DURATION);
    }

    /**
     * Draws a debug shape representation of the trap.
     *
     * @param sr shape renderer
     */
    @Override
    public void drawShape(ShapeRenderer sr) {
        if (!active) return;

        float size = GameConstants.CELL_SIZE;
        float px = x * size;
        float py = y * size;

        sr.setColor(new Color(0.1f, 0.6f, 0.2f, 1f));
        sr.rect(px, py, size, size);
    }
    /**
     * Draws the tea shard sprite or animation.
     *
     * @param batch sprite batch used for rendering
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
        if (!active) return;

        float size = GameConstants.CELL_SIZE;
        float px = x * size;
        float py = y * size;

        TextureRegion frame;

        if (state == State.DAMAGING) {
            frame = damageAnimation.getKeyFrame(animTime);
        } else {
            frame = idleFrame;
        }

        batch.draw(frame, px, py, size, size);
    }
    /**
     * @return render type used by this trap
     */
    @Override
    public RenderType getRenderType() {
        return RenderType.SPRITE;
    }
}
