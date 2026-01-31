package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.utils.Logger;
import de.tum.cit.fop.maze.utils.TextureManager;

/**
 * Represents a treasure chest that can be opened by the player.
 *
 * <p>
 * The treasure has two visual states: closed and opened.
 * Once opened, it becomes non-interactable but remains passable.
 * </p>
 */
public class Treasure extends GameObject {

    private boolean isOpened = false;
    private Texture closedTexture;
    private Texture openTexture;
    private TextureManager textureManager;
    private boolean needsTextureUpdate = true;
    /**
     * Represents a treasure chest that can be opened by the player.
     *
     * <p>
     * The treasure has two visual states: closed and opened.
     * Once opened, it becomes non-interactable but remains passable.
     * </p>
     */
    public Treasure(int x, int y) {
        super(x, y);
        this.textureManager = TextureManager.getInstance();
        updateTexture();
    }
    /**
     * Handles interaction with the player.
     *
     * <p>
     * Interacting with the treasure opens it if it has not been opened yet.
     * </p>
     *
     * @param player the interacting player
     */
    @Override
    public void onInteract(Player player) {
        if (!isOpened) {
            isOpened = true;
            Logger.debug("Treasure visual state set to OPENED");
        }
    }

    /**
     * Indicates whether the treasure can currently be interacted with.
     *
     * @return {@code true} if the treasure is still closed
     */
    @Override
    public boolean isInteractable() {
        return !isOpened;
    }
    /**
     * The treasure does not block movement.
     *
     * @return {@code true}
     */
    @Override
    public boolean isPassable() {
        return true;
    }
    /**
     * Loads the textures for the treasure.
     */
    private void updateTexture() {
        if (closedTexture == null || openTexture == null) {
            try {
                closedTexture = new Texture(Gdx.files.internal("imgs/Items/chest_closed.png"));
                openTexture = new Texture(Gdx.files.internal("imgs/Items/chest_open.png"));
            } catch (Exception e) {
                Logger.error("Failed to load treasure textures: " + e.getMessage());
            }
        }
        needsTextureUpdate = false;
    }
    /**
     * Marks the textures for reloading when the render mode changes.
     */
    @Override
    public void onTextureModeChanged() {
        needsTextureUpdate = true;
    }
    /**
     * Draws the treasure using sprite rendering.
     *
     * @param batch sprite batch used for rendering
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
        if (needsTextureUpdate) updateTexture();
        Texture currentTexture = isOpened ? openTexture : closedTexture;
        if (currentTexture != null) {
            batch.draw(currentTexture, x * GameConstants.CELL_SIZE, y * GameConstants.CELL_SIZE, GameConstants.CELL_SIZE, GameConstants.CELL_SIZE);
        }
    }
    /**
     * Draws a fallback shape if textures are unavailable.
     *
     * @param shapeRenderer shape renderer used for rendering
     */
    @Override
    public void drawShape(ShapeRenderer shapeRenderer) {
        if (closedTexture != null) return;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(isOpened ? Color.GRAY : Color.GOLD);
        shapeRenderer.rect(x * GameConstants.CELL_SIZE + 4, y * GameConstants.CELL_SIZE + 4, GameConstants.CELL_SIZE - 8, GameConstants.CELL_SIZE - 8);
        shapeRenderer.end();
    }
    /**
     * Determines how the treasure should be rendered.
     *
     * @return the render type to use
     */
    @Override
    public RenderType getRenderType() {
        if (textureManager.getCurrentMode() == TextureManager.TextureMode.COLOR ||
                textureManager.getCurrentMode() == TextureManager.TextureMode.MINIMAL ||
                closedTexture == null) {
            return RenderType.SHAPE;
        }
        return RenderType.SPRITE;
    }
    /**
     * Releases texture resources.
     */
    public void dispose() {
        if (closedTexture != null) closedTexture.dispose();
        if (openTexture != null) openTexture.dispose();
    }
}