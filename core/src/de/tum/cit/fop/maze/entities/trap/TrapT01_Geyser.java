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
 * Trap T01: Geyser.
 * <p>
 * A periodic trap that cycles through warning and eruption phases.
 * Deals damage to players standing on it during the eruption phase.
 */
public class TrapT01_Geyser extends Trap {

    private enum State {
        IDLE,
        WARNING,
        ERUPTING,
        COOLDOWN
    }

    private State state = State.IDLE;
    private float timer = 0f;
    private float damageTickTimer = 0f;

    private final float idleDuration     = 1.0f;
    private final float warningDuration  = 1.0f;
    private final float eruptDuration    = 1.0f;
    private final float cooldownDuration = 0.8f;
    private final int damagePerTick = 10;
    private final float damageInterval = 0.5f;

    private TextureAtlas atlas;
    private Array<TextureAtlas.AtlasRegion> frames;
    private int totalFrames = 0;



    /**
     * Creates a geyser trap at the given position.
     *
     * @param x             grid x-position
     * @param y             grid y-position
     * @param cycleDuration total duration of one full cycle
     */
    public TrapT01_Geyser(int x, int y, float cycleDuration) {
        super(x, y);


        loadAnimation();


    }
    private void loadAnimation() {
        try {
            TextureManager tm = TextureManager.getInstance();
            atlas = tm.getTrapT01Atlas();

            if (atlas == null) {
                atlas = new TextureAtlas("ani/T01/T01.atlas");
            }

            if (atlas != null) {
                String[] possibleNames = {"T01", "geyser", "T01_anim", "geyser_anim", "anim"};
                for (String name : possibleNames) {
                    frames = atlas.findRegions(name);
                    if (frames != null && frames.size > 0) {
                        totalFrames = frames.size;
                        break;
                    }
                }

                if (frames == null || frames.size == 0) {
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
     * Geysers are passable; players can stand on them.
     *
     * @return {@code true}
     */
    @Override
    public boolean isPassable() {
        return true;
    }

    private boolean warningEffectSpawned = false;
    /**
     * Updates the internal state machine of the geyser,
     * advancing through idle, warning, eruption, and cooldown phases.
     *
     * @param delta time elapsed since last frame
     */
    @Override
    public void update(float delta) {
        if (!active) return;

        timer += delta;

        switch (state) {
            case IDLE -> {
                if (timer >= idleDuration) {
                    state = State.WARNING;
                    timer = 0f;
                }
            }

            case WARNING -> {
                if (!warningEffectSpawned && hasEffectManager()) {

                    float worldX = x * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2f;
                    float worldY = y * GameConstants.CELL_SIZE;

                    effectManager.spawnGeyser(worldX, worldY);
                    warningEffectSpawned = true;
                }

                if (timer >= warningDuration) {
                    state = State.ERUPTING;
                    timer = 0f;
                    damageTickTimer = 0f;
                    warningEffectSpawned = false;
                }
            }

            case ERUPTING -> {
                damageTickTimer -= delta;

                if (timer >= eruptDuration) {
                    state = State.COOLDOWN;
                    timer = 0f;
                }
            }

            case COOLDOWN -> {
                if (timer >= cooldownDuration) {
                    state = State.IDLE;
                    timer = 0f;
                }
            }
        }
    }
    /**
     * Applies periodic damage to the player while the geyser
     * is erupting.
     *
     * @param player the player stepping on the trap
     */
    @Override
    public void onPlayerStep(Player player) {
        if (!active || state != State.ERUPTING) return;

        if (damageTickTimer <= 0f) {
            player.takeDamage(damagePerTick);
            damageTickTimer = damageInterval;
        }
    }

    private int getFrameIndex() {
        if (totalFrames == 0) return 0;

        int frameIndex = 0;

        switch (state) {
            case IDLE -> {
                float t = timer / idleDuration;
                int idleFrames = Math.max(1, totalFrames / 5);
                frameIndex = Math.min(idleFrames - 1, (int)(t * idleFrames));
            }

            case WARNING -> {
                float t = timer / warningDuration;
                int warningFrames = Math.max(1, totalFrames / 5);
                int startFrame = Math.max(1, totalFrames / 5);
                frameIndex = startFrame + Math.min(warningFrames - 1, (int)(t * warningFrames));
            }

            case ERUPTING -> {
                float t = timer / eruptDuration;
                int eruptFrames = Math.max(1, totalFrames * 2 / 5);
                int startFrame = Math.max(1, totalFrames * 2 / 5);
                frameIndex = startFrame + Math.min(eruptFrames - 1, (int)(t * eruptFrames));
            }

            case COOLDOWN -> {
                float t = timer / cooldownDuration;
                int cooldownFrames = Math.max(1, totalFrames / 5);
                int startFrame = Math.max(1, totalFrames * 4 / 5);
                frameIndex = startFrame + Math.min(cooldownFrames - 1, (int)(t * cooldownFrames));
            }
        }

        return MathUtils.clamp(frameIndex, 0, totalFrames - 1);
    }

    /**
     * Draws a debug representation of the geyser state.
     *
     * @param sr shape renderer
     */
    @Override
    public void drawShape(ShapeRenderer sr) {
        if (!active) return;

        float size = GameConstants.CELL_SIZE;
        float px = x * size;
        float py = y * size;

        switch (state) {
            case IDLE -> sr.setColor(new Color(0.4f, 0.25f, 0.1f, 0.4f));
            case WARNING -> sr.setColor(Color.RED);
            case ERUPTING -> sr.setColor(new Color(1f, 0.5f, 0f, 1f));
            case COOLDOWN -> sr.setColor(new Color(0.8f, 0.8f, 0.8f, 0.6f));
        }

        sr.rect(px, py, size, size);
    }

    /**
     * Draws the geyser animation sprite.
     *
     * @param batch sprite batch used for rendering
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
        if (!active) return;
        if (frames == null || frames.size == 0) return;

        int frameIndex = getFrameIndex();
        if (frameIndex < 0 || frameIndex >= frames.size) {
            frameIndex = MathUtils.clamp(frameIndex, 0, frames.size - 1);
        }

        TextureRegion frame = frames.get(frameIndex);
        if (frame == null) return;

        float size = GameConstants.CELL_SIZE;

        float verticalScale = 3.5f;
        float horizontalScale = 3.0f;

        float renderWidth = size * horizontalScale;
        float renderHeight = size * verticalScale;


        float offsetX = (size - renderWidth) / 2f;
        float offsetY = 0;

        if (state == State.ERUPTING) {
            float pulse = (float) Math.sin(timer * 10f) * 0.2f + 0.8f;
            batch.setColor(1f, pulse, pulse, 1f);
        }

        switch (state) {
            case IDLE:
                renderHeight = size * 1.2f;
                break;
            case WARNING:
                renderHeight = size * 1.8f;
                break;
            case ERUPTING:
                renderHeight = size * 2.0f;
                break;
            case COOLDOWN:
                renderHeight = size * 1.3f;
                break;
        }

        offsetY = 0;

        batch.draw(
                frame,
                x * size + offsetX,
                y * size + offsetY,
                renderWidth,
                renderHeight
        );

        if (state == State.ERUPTING) {
            batch.setColor(1, 1, 1, 1);
        }



    }
    /**
     * @return render type depending on whether sprite animation is available
     */
    @Override
    public RenderType getRenderType() {
        return (frames != null && frames.size > 0) ? RenderType.SPRITE : RenderType.SHAPE;
    }


}