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
 * A collectible container that permanently increases the player's maximum HP.
 *
 * <p>
 * The {@code HeartContainer} occupies a single grid cell and can be collected
 * once. Upon interaction, it increases the player's maximum health and then
 * becomes inactive.
 * </p>
 *
 * <p>
 * Rendering behavior depends on the current {@link TextureManager.TextureMode}:
 * <ul>
 *   <li>{@code SPRITE}: renders a textured heart container.</li>
 *   <li>{@code SHAPE}: renders a simple colored rectangle as fallback.</li>
 * </ul>
 * </p>
 */
public class HeartContainer extends GameObject {

    private Color color = Color.ORANGE;
    private Texture containerTexture;
    private boolean collected = false;

    private TextureManager textureManager;
    private boolean needsTextureUpdate = true;

    private static final int INCREASE_AMOUNT = 10;
    /**
     * Creates a new {@code HeartContainer} at the given grid position.
     *
     * @param x grid x-coordinate
     * @param y grid y-coordinate
     */
    public HeartContainer(int x, int y) {
        super(x, y);
        this.textureManager = TextureManager.getInstance();
        updateTexture();
        Logger.debug("HeartContainer created at " + getPositionString());
    }

    /**
     * Returns whether the container can currently be interacted with.
     *
     * @return {@code true} if active and not yet collected
     */
    @Override
    public boolean isInteractable() {
        return active && !collected;
    }

    /**
     * Returns whether the container can currently be interacted with.
     *
     * @return {@code true} if active and not yet collected
     */
    @Override
    public void onInteract(Player player) {
        if (isInteractable()) {
            collect();

            player.increaseMaxLives(INCREASE_AMOUNT);

            if (player.getGameManager() != null && player.getGameManager().getCombatEffectManager() != null) {
                float tx = x * GameConstants.CELL_SIZE;
                float ty = y * GameConstants.CELL_SIZE + 50;

                player.getGameManager().getCombatEffectManager().spawnStatusText(
                        tx, ty,
                        "MAX HP +" + INCREASE_AMOUNT,
                        Color.ORANGE
                );
            }

            Logger.gameEvent("MAXHP +10！");
        }
    }
    /**
     * Indicates that this object does not block movement.
     *
     * @return {@code true}, as players can pass through it
     */
    @Override
    public boolean isPassable() {
        return true;
    }
    /**
     * Indicates that this object does not block movement.
     *
     * @return {@code true}, as players can pass through it
     */
    private void updateTexture() {
        if (containerTexture == null) {
            try {

                containerTexture = new Texture(Gdx.files.internal("imgs/Items/heart_container.png"));
            } catch (Exception e) {
                Logger.error("HeartContainer texture missing, using fallback shape: " + e.getMessage());
            }
        }
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
     * Renders a simple fallback shape when sprite rendering is unavailable.
     *
     * @param shapeRenderer active shape renderer
     */
    @Override
    public void drawShape(ShapeRenderer shapeRenderer) {
        if (!active || collected || containerTexture != null) return;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(
                x * GameConstants.CELL_SIZE + 8,
                y * GameConstants.CELL_SIZE + 8,
                GameConstants.CELL_SIZE - 16,
                GameConstants.CELL_SIZE - 16
        );
        shapeRenderer.end();
    }

    /**
     * Renders the container sprite if available.
     *
     * @param batch active sprite batch
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
        if (!active || collected) return;

        if (containerTexture == null || needsTextureUpdate) {
            updateTexture();
        }

        if (containerTexture == null) return;

        batch.draw(containerTexture,
                x * GameConstants.CELL_SIZE + 4,
                y * GameConstants.CELL_SIZE + 4,
                GameConstants.CELL_SIZE - 8,
                GameConstants.CELL_SIZE - 8);
    }

    /**
     * Returns the appropriate render type based on texture availability.
     *
     * @return {@link RenderType#SPRITE} or {@link RenderType#SHAPE}
     */
    @Override
    public RenderType getRenderType() {
        if (textureManager.getCurrentMode() == TextureManager.TextureMode.MINIMAL ||
                containerTexture == null) {
            return RenderType.SHAPE;
        }
        return RenderType.SPRITE;
    }
    /**
     * Marks this container as collected and deactivates it.
     */
    public void collect() {
        this.collected = true;
        this.active = false;
    }

    /**
     * Releases the texture resource used by this container.
     */
    public void dispose() {
        if (containerTexture != null) {
            containerTexture.dispose();
            containerTexture = null;
        }
    }
}