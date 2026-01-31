package de.tum.cit.fop.maze.entities.trap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.entities.enemy.Enemy;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.utils.Logger;
import de.tum.cit.fop.maze.utils.TextureManager;

/**
 * Trap T02: Pearl Mine.
 * <p>
 * A proximity-triggered explosive trap that damages both players
 * and enemies in a small area. Once activated, the trap goes through
 * a warning phase, explodes, and then permanently disappears.
 */
public class TrapT02_PearlMine extends Trap {
    private boolean warningEffectSpawned = false;

    private enum State {
        IDLE,
        WARNING,
        EXPLODING,
        COLLAPSING,
        VANISHING
    }

    private State state = State.IDLE;
    private float timer = 0f;

    private float explosionScale = 1.0f;
    private float fragmentOffset = 0f;
    private Color currentColor = new Color(1, 1, 1, 1);
    private float rotation = 0f;

    private int fragmentCount = 8;
    private Array<FragmentData> fragments;
    private boolean fragmentsInitialized = false;

    private static class FragmentData {
        float dirX, dirY;
        float speed;
        float rotationSpeed;
        float scale;
        float alpha;
    }

    private TextureAtlas atlas;
    private Array<TextureAtlas.AtlasRegion> frames;
    private int totalFrames = 0;

    private static final float WARNING_DURATION = 0.8f;
    private static final float EXPLODE_DURATION = 0.4f;
    private static final float COLLAPSE_DURATION = 0.3f;
    private static final float VANISH_DURATION = 0.2f;
    private static final int DAMAGE = 25;

    private static final float MAX_EXPLOSION_SCALE = 2.0f;
    private static final float MIN_COLLAPSE_SCALE = 0.3f;
    private static final float MAX_ROTATION = 45f;

    private final GameManager gm;
    /**
     * Creates a pearl mine trap at the given position.
     *
     * @param x  grid x-position
     * @param y  grid y-position
     * @param gm active game manager used to apply explosion damage
     */
    public TrapT02_PearlMine(int x, int y, GameManager gm) {
        super(x, y);
        this.gm = gm;

        Logger.debug("=== T02  (" + x + "," + y + ") ===");
        loadAnimation();
        initFragments();
    }

    private void loadAnimation() {
        try {
            TextureManager tm = TextureManager.getInstance();
            atlas = tm.getTrapT02Atlas();

            if (atlas == null) {
                atlas = new TextureAtlas("ani/T02/T02.atlas");
            }

            if (atlas != null) {
                frames = atlas.findRegions("T02");
                if (frames == null || frames.size == 0) {
                    frames = atlas.findRegions("mine");
                }
                if (frames == null || frames.size == 0) {
                    frames = atlas.findRegions("pearl_mine");
                }

                if (frames != null && frames.size > 0) {
                    totalFrames = frames.size;
                } else {
                    frames = new Array<>();
                }
            } else {
                frames = new Array<>();
            }
        } catch (Exception e) {
            frames = new Array<>();
        }
    }
    /**
     * Pearl mines are passable until they explode.
     *
     * @return {@code true}
     */
    @Override
    public boolean isPassable() {
        return true;
    }
    /**
     * Updates the internal state machine of the mine,
     * progressing through warning, explosion, collapse,
     * and vanishing phases.
     *
     * @param delta time elapsed since last frame
     */
    @Override
    public void update(float delta) {
        if (!active) return;

        timer += delta;

        switch (state) {
            case IDLE:
                updateIdle(delta);
                break;

            case WARNING:
                updateWarning(delta);
                if (timer >= WARNING_DURATION) {
                    state = State.EXPLODING;
                    timer = 0f;
                }
                break;

            case EXPLODING:
                updateExploding(delta);
                if (timer >= EXPLODE_DURATION) {
                    explode();
                    state = State.COLLAPSING;
                    timer = 0f;
                }
                break;

            case COLLAPSING:
                updateCollapsing(delta);
                if (timer >= COLLAPSE_DURATION) {
                    state = State.VANISHING;
                    timer = 0f;
                }
                break;

            case VANISHING:
                updateVanishing(delta);
                if (timer >= VANISH_DURATION) {
                    active = false;
                    warningEffectSpawned = false;
                }
                break;
        }

        if (state == State.COLLAPSING || state == State.VANISHING) {
            updateFragments(delta);
        }
    }

    private void updateFragments(float delta) {
        if (!fragmentsInitialized) return;

        for (FragmentData frag : fragments) {
            frag.alpha = Math.max(0, frag.alpha - delta * 2f);
            frag.scale = Math.max(0.1f, frag.scale - delta * 0.5f);
        }
    }

    private void updateIdle(float delta) {
        float breath = (float) Math.sin(timer * 2f) * 0.05f;
        explosionScale = 1.0f + breath;
        currentColor.set(1, 1, 1, 1);
    }

    private void updateWarning(float delta) {
        float blink = (float) Math.sin(timer * 20f);
        if (blink > 0) {
            currentColor.set(1, 0.2f, 0.2f, 1);
            explosionScale = 1.0f + 0.1f;
        } else {
            currentColor.set(1, 1, 1, 1);
            explosionScale = 1.0f;
        }
    }

    private void updateExploding(float delta) {
        float progress = timer / EXPLODE_DURATION;

        explosionScale = 1.0f + progress * (MAX_EXPLOSION_SCALE - 1.0f);

        if (progress < 0.5f) {
            float redProgress = progress * 2;
            currentColor.r = 1.0f;
            currentColor.g = 1.0f - redProgress;
            currentColor.b = 1.0f - redProgress * 0.8f;
        } else {
            float darkProgress = (progress - 0.5f) * 2;
            currentColor.r = 1.0f - darkProgress * 0.3f;
            currentColor.g = 0.2f;
            currentColor.b = 0.2f;
        }

        rotation = progress * MAX_ROTATION;
    }

    private void updateCollapsing(float delta) {
        float progress = timer / COLLAPSE_DURATION;

        explosionScale = MAX_EXPLOSION_SCALE - progress * (MAX_EXPLOSION_SCALE - MIN_COLLAPSE_SCALE);

        currentColor.r = 0.7f - progress * 0.7f;
        currentColor.g = 0.2f - progress * 0.2f;
        currentColor.b = 0.2f - progress * 0.2f;

        rotation = MAX_ROTATION + progress * 30f;
        fragmentOffset = progress * 15f;
    }

    private void updateVanishing(float delta) {
        float progress = timer / VANISH_DURATION;

        explosionScale = MIN_COLLAPSE_SCALE * (1.0f - progress);

        currentColor.a = 1.0f - progress;

        rotation += delta * 180f;
        fragmentOffset += delta * 30f;
    }
    /**
     * Activates the mine when a player steps onto it.
     *
     * @param player the player triggering the mine
     */
    @Override
    public void onPlayerStep(Player player) {
        if (state != State.IDLE) return;

        state = State.WARNING;
        timer = 0f;

        if (hasEffectManager() && !warningEffectSpawned) {
            float worldX = x * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2f;
            float worldY = y * GameConstants.CELL_SIZE;

            effectManager.spawnPearlMine(worldX, worldY);
            warningEffectSpawned = true;
        }
    }


    private void explode() {
        int cx = x;
        int cy = y;

        for (Player p : gm.getPlayers()) {
            if (p == null || p.isDead()) continue;

            if (Math.abs(p.getX() - cx) <= 1 &&
                    Math.abs(p.getY() - cy) <= 1) {

                p.takeDamage(DAMAGE);
            }
        }

        for (Enemy enemy : gm.getEnemies()) {
            if (Math.abs(enemy.getX() - cx) <= 1 &&
                    Math.abs(enemy.getY() - cy) <= 1) {
                enemy.takeDamage(DAMAGE);
            }
        }
    }

    private int getFrameIndex() {
        if (totalFrames == 0) return 0;

        float progress = 0f;

        switch (state) {
            case IDLE:
                progress = (timer % 3.0f) / 3.0f;
                return (int)(progress * Math.min(4, totalFrames));

            case WARNING:
                progress = timer / WARNING_DURATION;
                int warningStart = Math.min(4, totalFrames - 1);
                int warningEnd = Math.min(8, totalFrames - 1);
                int warningFrames = warningEnd - warningStart + 1;
                if (warningFrames <= 0) warningFrames = 1;
                return warningStart + (int)(progress * warningFrames);

            case EXPLODING:
            case COLLAPSING:
            case VANISHING:
                progress = timer / (EXPLODE_DURATION + COLLAPSE_DURATION + VANISH_DURATION);
                int explodeStart = Math.max(0, totalFrames - 6);
                int explodeFrames = totalFrames - explodeStart;
                if (explodeFrames <= 0) explodeFrames = 1;
                return explodeStart + (int)(progress * explodeFrames);

            default:
                return 0;
        }
    }

    /**
     * Draws a debug shape representation of the mine
     * when sprite animation is unavailable.
     *
     * @param sr shape renderer
     */
    @Override
    public void drawShape(ShapeRenderer sr) {
        if (frames != null && frames.size > 0) return;
        if (!active) return;

        float size = GameConstants.CELL_SIZE;
        float px = x * size;
        float py = y * size;

        switch (state) {
            case IDLE:
                sr.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
                sr.rect(px, py, size, size);
                break;
            case WARNING:
                float blink = (float) Math.sin(timer * 10f) * 0.5f + 0.5f;
                sr.setColor(1f, blink, blink, 1f);
                sr.rect(px, py, size, size);
                break;
            case EXPLODING:
            case COLLAPSING:
            case VANISHING:
                sr.setColor(currentColor);
                float scaledSize = size * explosionScale;
                float offset = (scaledSize - size) / 2f;
                sr.rect(px - offset, py - offset, scaledSize, scaledSize);
                break;
        }
    }
    /**
     * Draws the animated sprite of the mine,
     * including explosion and fragment effects.
     *
     * @param batch sprite batch used for rendering
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
        if (!active) return;
        if (frames == null || frames.size == 0) return;

        int frameIndex = getFrameIndex();
        frameIndex = MathUtils.clamp(frameIndex, 0, frames.size - 1);
        TextureRegion frame = frames.get(frameIndex);
        if (frame == null) return;

        float size = GameConstants.CELL_SIZE;
        float halfSize = size / 2f;
        float centerX = x * size + halfSize;
        float centerY = y * size + halfSize;

        batch.setColor(currentColor);

        switch (state) {
            case WARNING:
                float scaledSize = size * explosionScale;
                float offset = (scaledSize - size) / 2f;
                batch.draw(frame,
                        x * size - offset,
                        y * size - offset,
                        scaledSize, scaledSize);
                break;

            case EXPLODING:
                renderExplosionPhase(batch, frame, centerX, centerY, halfSize);
                break;

            case COLLAPSING:
            case VANISHING:
                if (fragmentsInitialized) {
                    renderFragments(batch, frame, centerX, centerY, halfSize);
                } else {
                    renderExplosionPhase(batch, frame, centerX, centerY, halfSize);
                }
                break;

            default:
                batch.draw(frame, x * size, y * size, size, size);
                break;
        }

        batch.setColor(1, 1, 1, 1);
    }

    private void renderExplosionPhase(SpriteBatch batch, TextureRegion frame,
                                      float centerX, float centerY, float halfSize) {
        float scaledHalfSize = halfSize * explosionScale;
        batch.draw(frame,
                centerX - scaledHalfSize,
                centerY - scaledHalfSize,
                scaledHalfSize, scaledHalfSize,
                scaledHalfSize * 2, scaledHalfSize * 2,
                1, 1,
                rotation);
    }

    private void initFragments() {
        fragments = new Array<>();

        for (int i = 0; i < fragmentCount; i++) {
            FragmentData frag = new FragmentData();

            float angle = MathUtils.random(0, 360);
            frag.dirX = MathUtils.cosDeg(angle);
            frag.dirY = MathUtils.sinDeg(angle);

            frag.speed = MathUtils.random(0.8f, 2.0f);

            frag.rotationSpeed = MathUtils.random(-360f, 360f);

            frag.scale = MathUtils.random(0.3f, 0.8f);

            frag.alpha = 1.0f;

            fragments.add(frag);
        }
        fragmentsInitialized = true;
    }

    private void renderFragments(SpriteBatch batch, TextureRegion frame,
                                 float centerX, float centerY, float halfSize) {
        if (!fragmentsInitialized || fragments.size == 0) return;

        float baseSize = halfSize * 0.7f;

        Color originalColor = batch.getColor();

        for (FragmentData frag : fragments) {
            float currentOffset = fragmentOffset * frag.speed;
            float fragSize = baseSize * frag.scale;

            Color fragColor = new Color(currentColor);
            fragColor.a *= frag.alpha;
            batch.setColor(fragColor);

            float fragX = centerX + frag.dirX * currentOffset;
            float fragY = centerY + frag.dirY * currentOffset;

            batch.draw(frame,
                    fragX - fragSize,
                    fragY - fragSize,
                    fragSize, fragSize,
                    fragSize * 2, fragSize * 2,
                    1, 1,
                    rotation + frag.rotationSpeed * timer);
        }

        batch.setColor(originalColor);
    }
    /**
     * Sets the number of fragments spawned during the explosion.
     *
     * @param count fragment count (minimum 1)
     */
    public void setFragmentCount(int count) {
        this.fragmentCount = Math.max(1, count);
        fragmentsInitialized = false;
    }
    /**
     * @return render type depending on sprite availability
     */
    @Override
    public RenderType getRenderType() {
        return (frames != null && frames.size > 0) ? RenderType.SPRITE : RenderType.SHAPE;
    }
    /**
     * @return current internal state of the mine
     */
    public State getState() {
        return state;
    }
    /**
     * @return time elapsed in the current state
     */
    public float getTimer() {
        return timer;
    }
    /**
     * @return current explosion scale factor
     */
    public float getExplosionScale() {
        return explosionScale;
    }
    /**
     * @return current render color of the mine
     */
    public Color getCurrentColor() {
        return currentColor;
    }
    /**
     * @return current rotation angle used for rendering
     */
    public float getRotation() {
        return rotation;
    }
    /**
     * @return number of explosion fragments
     */
    public int getFragmentCount() {
        return fragmentCount;
    }
}