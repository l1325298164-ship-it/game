package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.fop.maze.effects.portal.PortalEffectManager;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.utils.Logger;

import java.util.EnumMap;
/**
 * ExitDoor represents a level exit tile that transitions the player to the next level.
 *
 * <p>
 * The exit door is a static, non-passable world object that can be either
 * locked or unlocked. Once unlocked, stepping onto the door triggers
 * a portal animation and begins a level transition.
 * </p>
 *
 * <p>
 * Design characteristics:
 * <ul>
 *   <li>Occupies exactly one grid cell.</li>
 *   <li>Is never passable, even when unlocked.</li>
 *   <li>Cannot be interacted with manually (step-trigger only).</li>
 *   <li>Uses a portal animation for visual feedback.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Important notes:
 * <ul>
 *   <li>The door does NOT move and is treated as solid terrain.</li>
 *   <li>Level transition logic is managed externally by {@link GameManager}.</li>
 *   <li>This class only handles visuals and trigger state.</li>
 * </ul>
 * </p>
 */
public class ExitDoor extends GameObject {
    /**
     * Represents the facing direction of the exit door.
     *
     * <p>
     * The direction determines both rendering orientation and positional offsets
     * for the door sprite.
     * </p>
     */
    public enum DoorDirection {
        UP, DOWN, LEFT, RIGHT
    }

    private final PortalEffectManager portalEffect = new PortalEffectManager();

    private final EnumMap<DoorDirection, Texture> lockedTextures = new EnumMap<>(DoorDirection.class);
    private final EnumMap<DoorDirection, Texture> unlockedTextures = new EnumMap<>(DoorDirection.class);

    private final DoorDirection direction;
    private boolean locked = true;
    private boolean triggered = false;

    private float unlockEffectTimer = 0f;
    private static final float UNLOCK_EFFECT_DURATION = 2f; // 解锁特效持续时间
    /**
     * Creates a new exit door at the given grid position.
     *
     * @param x         grid X coordinate
     * @param y         grid Y coordinate
     * @param direction facing direction of the door
     */
    public ExitDoor(int x, int y, DoorDirection direction) {
        super(x, y);
        this.direction = direction;
        this.active = true;

        try {
            lockedTextures.put(DoorDirection.UP,
                    new Texture(Gdx.files.internal("imgs/Items/door_up_locked.png")));
            lockedTextures.put(DoorDirection.DOWN,
                    new Texture(Gdx.files.internal("imgs/Items/door_down_locked.png")));
            lockedTextures.put(DoorDirection.LEFT,
                    new Texture(Gdx.files.internal("imgs/Items/door_left_locked.png")));
            lockedTextures.put(DoorDirection.RIGHT,
                    new Texture(Gdx.files.internal("imgs/Items/door_right_locked.png")));

            unlockedTextures.put(DoorDirection.UP,
                    new Texture(Gdx.files.internal("imgs/Items/door_up_locked.png")));
            unlockedTextures.put(DoorDirection.DOWN,
                    new Texture(Gdx.files.internal("imgs/Items/door_down_locked.png")));
            unlockedTextures.put(DoorDirection.LEFT,
                    new Texture(Gdx.files.internal("imgs/Items/door_left_locked.png")));
            unlockedTextures.put(DoorDirection.RIGHT,
                    new Texture(Gdx.files.internal("imgs/Items/door_right_locked.png")));

            Logger.debug("ExitDoor created at (" + x + ", " + y + ") facing " + direction);
        } catch (Exception e) {
            Logger.error("Failed to load door textures: " + e.getMessage());
            for (DoorDirection dir : DoorDirection.values()) {
                Texture lockedTex = lockedTextures.get(dir);
                if (lockedTex != null) {
                    unlockedTextures.put(dir, lockedTex);
                }
            }
        }
    }
    /**
     * @return true if the door is currently locked
     */

    public boolean isLocked() {
        return locked;
    }
    /**
     * Unlocks the exit door and starts the unlock visual effect.
     *
     * <p>
     * Calling this method multiple times has no additional effect.
     * </p>
     */
    public void unlock() {
        if (locked) {
            locked = false;
            unlockEffectTimer = 0f;
            Logger.gameEvent("Exit unlocked at " + getPositionString() + " (direction: " + direction + ")");
        }
    }
    /**
     * Updates the door state.
     *
     * <p>
     * This method updates the portal animation and unlock visual effects.
     * It does not handle level transitions directly.
     * </p>
     *
     * @param delta time elapsed since last frame
     * @param gm    game manager reference
     */
    public void update(float delta, GameManager gm) {
        portalEffect.update(delta);

        if (!locked && unlockEffectTimer < UNLOCK_EFFECT_DURATION) {
            unlockEffectTimer += delta;
        }
    }

    @Override
    public boolean isPassable() {
        return false;
    }
    /**
     * Called when a player steps onto the door tile.
     *
     * <p>
     * If the door is unlocked and not already triggered, this starts
     * the exit portal animation.
     * </p>
     *
     * @param player the player stepping onto the door
     */
    public void onPlayerStep(Player player) {
        if (locked || triggered) return;

        triggered = true;
        portalEffect.startExitAnimation(
                (x + 0.5f) * GameConstants.CELL_SIZE,
                (y + 0.5f) * GameConstants.CELL_SIZE
        );
    }

    @Override
    public boolean isInteractable() {
        return false;
    }

    @Override
    public void onInteract(Player player) {

    }
    /**
     * Renders the exit door sprite and associated portal effects.
     *
     * <p>
     * The door appearance depends on its locked state and facing direction.
     * </p>
     *
     * @param batch sprite batch used for rendering
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
        float px = x * GameConstants.CELL_SIZE;
        float py = y * GameConstants.CELL_SIZE;

        portalEffect.renderBack(batch, px, py);

        Texture tex;
        if (locked) {
            tex = lockedTextures.get(direction);
        } else {
            tex = unlockedTextures.get(direction);
        }

        if (tex == null) {
            Logger.warning("Texture not found for door direction: " + direction + ", locked: " + locked);
            return;
        }

        if (locked) {
            batch.setColor(0.7f, 0.7f, 0.7f, 1f);
        } else {
            if (unlockEffectTimer < UNLOCK_EFFECT_DURATION) {
                float alpha = (float) Math.sin(unlockEffectTimer * 10) * 0.3f + 0.7f;
                float goldR = 1.0f;
                float goldG = 0.8f;
                float goldB = 0.3f;
                batch.setColor(goldR, goldG, goldB, alpha);
            } else {
                batch.setColor(1f, 1f, 1f, 1f);
            }
        }

        float drawWidth = GameConstants.CELL_SIZE;
        float drawHeight = GameConstants.CELL_SIZE * 1.5f;

        float offsetX = 0;
        float offsetY = portalEffect.getDoorFloatOffset();

        switch (direction) {
            case UP:
                break;
            case DOWN:
                offsetY -= GameConstants.CELL_SIZE * 0.5f;
                break;
            case LEFT:
                offsetX = -GameConstants.CELL_SIZE * 0.25f;
                break;
            case RIGHT:
                offsetX = GameConstants.CELL_SIZE * 0.25f;
                break;
        }

        batch.draw(
                tex,
                px + offsetX,
                py + offsetY,
                drawWidth,
                drawHeight
        );

        batch.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void drawShape(ShapeRenderer shapeRenderer) {
    }

    @Override
    public RenderType getRenderType() {
        return RenderType.SPRITE;
    }

    public void renderPortalFront(SpriteBatch batch) {
        portalEffect.renderFront(batch);
    }

    public boolean isAnimationPlaying() {
        return portalEffect.isActive();
    }
    /**
     * Resets the door to its initial locked and untriggered state.
     *
     * <p>
     * Used when restarting or regenerating a level.
     * </p>
     */
    public void resetDoor() {
        triggered = false;
        locked = true;
        unlockEffectTimer = 0f;
        portalEffect.reset();
    }
    /**
     * Releases all textures and visual resources used by this door.
     */
    public void dispose() {
        for (Texture tex : lockedTextures.values()) {
            if (tex != null) tex.dispose();
        }
        for (Texture tex : unlockedTextures.values()) {
            if (tex != null) tex.dispose();
        }
        portalEffect.dispose();
    }

    public void renderPortalBack(SpriteBatch batch) {
        portalEffect.renderBack(
                batch,
                x * GameConstants.CELL_SIZE,
                y * GameConstants.CELL_SIZE
        );
    }

    @Override
    public String getPositionString() {
        return "(" + x + ", " + y + ", " + direction + ")";
    }
    /**
     * Checks whether a player is within a given Manhattan range of the door.
     *
     * @param playerX player grid X coordinate
     * @param playerY player grid Y coordinate
     * @param range   maximum distance
     * @return true if the player is within range
     */
    public boolean isPlayerNearby(int playerX, int playerY, int range) {
        int dx = Math.abs(playerX - this.x);
        int dy = Math.abs(playerY - this.y);
        return dx <= range && dy <= range;
    }
}