// Key.java
package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.utils.Logger;
import de.tum.cit.fop.maze.utils.TextureManager;
/**
 * A collectible key that unlocks all exit doors in the current level.
 *
 * <p>
 * The {@code Key} occupies a single grid cell and can be collected once by any
 * player. Upon collection, it notifies the {@link GameManager} to unlock all
 * exits and then becomes inactive.
 * </p>
 *
 * <p>
 * Rendering behavior depends on the current {@link TextureManager.TextureMode}:
 * <ul>
 *   <li>{@code SPRITE}: renders a textured key.</li>
 *   <li>{@code SHAPE}/{@code COLOR}/{@code MINIMAL}: renders a colored shape.</li>
 * </ul>
 * </p>
 */
public class Key extends GameObject {

    private Color color = GameConstants.KEY_COLOR;
    private Texture keyTexture;
    private boolean collected = false;

    private final TextureManager textureManager;
    private final GameManager gm;

    private boolean needsTextureUpdate = true;
    /** Flag indicating that a player has collected this key. */
    public boolean playerCollectedKey;
    /**
     * Creates a new {@code Key} at the given grid position.
     *
     * @param x  grid x-coordinate
     * @param y  grid y-coordinate
     * @param gm active game manager instance
     */
    public Key(int x, int y, GameManager gm) {
        super(x, y);
        this.gm = gm;
        this.textureManager = TextureManager.getInstance();

        this.active = true;
        this.collected = false;

        updateTexture();
        Logger.debug("Key created at " + getPositionString());
    }


    /**
     * Returns whether this key can currently be interacted with.
     *
     * @return {@code true} if the key is active
     */
    @Override
    public boolean isInteractable() {
        return active;
    }
    /**
     * Handles player interaction with this key.
     *
     * <p>
     * Plays a pickup sound, unlocks all exit doors via the {@link GameManager},
     * and marks this key as collected.
     * </p>
     *
     * @param player the interacting player
     */
    @Override
    public void onInteract(Player player) {
        if (!active) return;

        collect();
        AudioManager.getInstance().play(AudioType.PLAYER_GET_KEY);
        gm.onKeyCollected();
        playerCollectedKey = true;

        Logger.gameEvent("Key picked up");
    }
    /**
     * Indicates that this object does not block movement.
     *
     * @return {@code true}, as players can pass through the key
     */
    @Override
    public boolean isPassable() {
        return true;
    }
    /**
     * Loads or reloads the key texture from the {@link TextureManager}.
     */
    private void updateTexture() {
        keyTexture = textureManager.getKeyTexture();
        needsTextureUpdate = false;
    }
    /**
     * Marks this object as requiring a texture reload when the texture mode changes.
     */
    @Override
    public void onTextureModeChanged() {
        needsTextureUpdate = true;
    }
    /**
     * Renders a fallback shape representation of the key.
     *
     * @param shapeRenderer active shape renderer
     */
    @Override
    public void drawShape(ShapeRenderer shapeRenderer) {
        if (!active || collected || keyTexture != null) return;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.circle(
                x * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2f,
                y * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2f,
                GameConstants.CELL_SIZE / 2f - 4
        );
        shapeRenderer.end();
    }
    /**
     * Renders the key sprite if a texture is available.
     *
     * @param batch active sprite batch
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
        if (!active || collected || keyTexture == null) return;

        if (needsTextureUpdate) updateTexture();

        batch.draw(
                keyTexture,
                x * GameConstants.CELL_SIZE + 4,
                y * GameConstants.CELL_SIZE + 4,
                GameConstants.CELL_SIZE + 10,
                GameConstants.CELL_SIZE + 10
        );
    }
    /**
     * Returns the appropriate render type based on texture mode and availability.
     *
     * @return {@link RenderType#SPRITE} or {@link RenderType#SHAPE}
     */
    @Override
    public RenderType getRenderType() {
        if (textureManager.getCurrentMode() == TextureManager.TextureMode.COLOR ||
                textureManager.getCurrentMode() == TextureManager.TextureMode.MINIMAL ||
                keyTexture == null) {
            return RenderType.SHAPE;
        }
        return RenderType.SPRITE;
    }
    /**
     * Marks this key as collected and deactivates it.
     */
    public void collect() {
        collected = true;
        active = false;
        Logger.gameEvent("Key collected at " + getPositionString());
    }
    /**
     * Returns the texture used to render this key.
     *
     * @return key texture, or {@code null} if unavailable
     */
    public Texture getTexture() {
        return  keyTexture;
    }
    /**
     * Returns whether this key has been collected by a player.
     *
     * @return {@code true} if the key was collected
     */
    public boolean isCollected() {
        return playerCollectedKey;
    }
}
