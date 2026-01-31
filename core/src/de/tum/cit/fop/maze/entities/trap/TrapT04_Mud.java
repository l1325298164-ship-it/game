package de.tum.cit.fop.maze.entities.trap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.utils.Logger;
import de.tum.cit.fop.maze.utils.TextureManager;
/**
 * Trap T04: Mud.
 * <p>
 * A terrain trap that slows players when stepped on.
 * The trap remains permanently active and applies a
 * short slow effect each time a player enters the tile.
 */
public class TrapT04_Mud extends Trap {
    private boolean effectTriggered = false;
    private boolean steppedThisFrame = false;
    private static final float SLOW_DURATION = 1.5f;

    private TextureAtlas atlas;
    private Array<TextureAtlas.AtlasRegion> frames;
    private int totalFrames = 0;
    private float animationTimer = 0f;
    private float frameDuration = 0.2f;

    private float bubbleTimer = 0f;
    private final Array<MudBubble> bubbles = new Array<>();
    private static final int MAX_BUBBLES = 3;
    private static final float BUBBLE_SPAWN_INTERVAL = 1.5f;

    private float waveOffset = 0f;
    private float waveSpeed = 1.5f;
    private float waveAmplitude = 0.05f; // 波动幅度

    private static class MudBubble {
        float x, y;
        float size;
        float speed;
        float life;
        float maxLife;
        float startTime;

        public void update(float delta) {
            life += delta;
            y += speed * delta;
        }

        public boolean isAlive() {
            return life < maxLife;
        }

        public float getAlpha() {
            if (life < 0.3f) {
                return life / 0.3f;
            } else if (life > maxLife - 0.3f) {
                return (maxLife - life) / 0.3f;
            }
            return 1.0f;
        }
    }
    /**
     * Creates a mud trap at the given position.
     *
     * @param x grid x-position
     * @param y grid y-position
     */
    public TrapT04_Mud(int x, int y) {
        super(x, y);
        loadAnimation();
        initBubbles();
    }

    private void loadAnimation() {
        try {
            TextureManager tm = TextureManager.getInstance();
            atlas = tm.getTrapT04Atlas();

            if (atlas == null) {
                Logger.warning("T04 Atlas error");
                atlas = new TextureAtlas("ani/T04/T04.atlas");
            }

            if (atlas != null) {
                frames = atlas.findRegions("mud");
                if (frames == null || frames.size == 0) {
                    frames = atlas.findRegions("T04");
                }
                if (frames == null || frames.size == 0) {
                    frames = atlas.findRegions("swamp");
                }
                if (frames == null || frames.size == 0) {
                    frames = atlas.findRegions("trap_mud");
                }

                if (frames != null && frames.size > 0) {
                    totalFrames = frames.size;
                    Logger.debug(" T04  " + frames.size + "frame");
                } else {
                    frames = new Array<>();
                    Logger.debug("error T04 ");
                }
            } else {
                frames = new Array<>();
            }
        } catch (Exception e) {
            frames = new Array<>();
        }
    }

    private void initBubbles() {
        bubbles.clear();
        for (int i = 0; i < MAX_BUBBLES; i++) {
            createBubble(MathUtils.random(0f, 2f));
        }
    }

    private void createBubble(float delay) {
        MudBubble bubble = new MudBubble();
        bubble.x = MathUtils.random(0.1f, 0.9f);
        bubble.y = -0.1f;
        bubble.size = MathUtils.random(0.05f, 0.15f);
        bubble.speed = MathUtils.random(0.1f, 0.3f);
        bubble.maxLife = MathUtils.random(1.5f, 3.0f);
        bubble.life = -delay;
        bubble.startTime = delay;
        bubbles.add(bubble);
    }
    /**
     * Updates the mud animation and ambient visual effects.
     *
     * @param delta time elapsed since last frame
     */
    @Override
    public void update(float delta) {
        if (!active) return;

        animationTimer += delta;

        waveOffset += delta * waveSpeed;

        bubbleTimer += delta;
        if (bubbleTimer >= BUBBLE_SPAWN_INTERVAL) {
            bubbleTimer = 0f;
            for (int i = bubbles.size - 1; i >= 0; i--) {
                if (!bubbles.get(i).isAlive()) {
                    bubbles.removeIndex(i);
                }
            }
            if (bubbles.size < MAX_BUBBLES) {
                createBubble(0f);
            }
        }

        for (MudBubble bubble : bubbles) {
            bubble.update(delta);
        }
        if (!steppedThisFrame) {
            effectTriggered = false;
        }
        steppedThisFrame = false;
        
    }

    /**
     * Mud is passable and can be stepped on.
     *
     * @return {@code true}
     */
    @Override
    public boolean isPassable() {
        return true;
    }


    /**
     * Applies a slow effect to the player when stepping
     * onto the mud tile.
     *
     * @param player the player triggering the trap
     */
    @Override
    public void onPlayerStep(Player player) {
        steppedThisFrame = true;

        player.applySlow(SLOW_DURATION);
        if (!effectTriggered && hasEffectManager()) {
            float worldX = x * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2f;
            float worldY = y * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2f;

            effectManager.spawnMudTrap(worldX, worldY);
            effectTriggered = true;
        }
        if (bubbles.size < MAX_BUBBLES * 2) {
            for (int i = 0; i < 2; i++) {
                createBubble(MathUtils.random(0f, 0.5f));
            }
        }
    }

    /**
     * Draws a debug shape representation of the mud tile.
     *
     * @param sr shape renderer
     */
    @Override
    public void drawShape(ShapeRenderer sr) {
        if (!active) return;

        float size = GameConstants.CELL_SIZE;
        float px = x * size;
        float py = y * size;

        Color baseColor = new Color(0.35f, 0.25f, 0.15f, 1f);

        float wave = (float) Math.sin(waveOffset) * waveAmplitude;
        float adjustedSize = size * (1 + wave);
        float offset = (adjustedSize - size) / 2f;

        sr.setColor(baseColor);
        sr.rect(px - offset, py - offset, adjustedSize, adjustedSize);

        drawBubbles(sr, px, py, size);
    }

    private void drawBubbles(ShapeRenderer sr, float px, float py, float cellSize) {
        sr.setColor(new Color(0.45f, 0.35f, 0.25f, 0.7f));

        for (MudBubble bubble : bubbles) {
            if (!bubble.isAlive()) continue;

            float alpha = bubble.getAlpha();
            if (alpha <= 0) continue;

            float bubbleX = px + bubble.x * cellSize;
            float bubbleY = py + bubble.y * cellSize;
            float bubbleSize = bubble.size * cellSize * alpha;

            sr.circle(bubbleX, bubbleY, bubbleSize / 2, 8);

            sr.setColor(new Color(0.55f, 0.45f, 0.35f, alpha * 0.6f));
            sr.circle(bubbleX - bubbleSize * 0.2f, bubbleY + bubbleSize * 0.2f,
                    bubbleSize * 0.2f, 6);
            sr.setColor(new Color(0.45f, 0.35f, 0.25f, alpha * 0.7f));
        }
    }
    /**
     * Draws the animated mud sprite and effects.
     *
     * @param batch sprite batch used for rendering
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
        if (!active) return;
        if (frames == null || frames.size == 0) return;

        float size = GameConstants.CELL_SIZE;
        float px = x * size;
        float py = y * size;

        int frameIndex = (int)(animationTimer / frameDuration) % frames.size;
        TextureRegion frame = frames.get(frameIndex);
        if (frame == null) return;

        float wave = (float) Math.sin(waveOffset) * waveAmplitude;
        float adjustedSize = size * (1 + wave);
        float offset = (adjustedSize - size) / 2f;

        batch.setColor(0.8f, 0.8f, 0.8f, 1f);

        batch.draw(frame,
                px - offset,
                py - offset,
                adjustedSize,
                adjustedSize);

        drawBubbles(batch, px, py, size);

        batch.setColor(1, 1, 1, 1);
    }

    private void drawBubbles(SpriteBatch batch, float px, float py, float cellSize) {
    }

    /**
     * @return render type depending on sprite availability
     */
    @Override
    public RenderType getRenderType() {
        return (frames != null && frames.size > 0) ? RenderType.SPRITE : RenderType.SHAPE;
    }
    /**
     * @return current animation frame index
     */
    public int getCurrentFrameIndex() {
        if (frames == null || frames.size == 0) return 0;
        return (int)(animationTimer / frameDuration) % frames.size;
    }
    /**
     * @return current number of visible mud bubbles
     */
    public int getBubbleCount() {
        return bubbles.size;
    }
}