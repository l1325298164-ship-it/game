package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.utils.Logger;

/**
 * A collectible heart item that restores player health.
 *
 * <p>
 * The heart occupies a single grid cell and becomes inactive
 * once collected by a player.
 * </p>
 *
 * <p>
 * Rendering:
 * <ul>
 *   <li>Sprite mode: floating heart texture.</li>
 *   <li>Shape mode: red debug circle.</li>
 * </ul>
 * </p>
 */
public class Heart extends GameObject {
    private boolean active = true;
    private Texture texture;
    /**
     * Creates a heart at the given grid position.
     */
    public Heart(int x, int y) {
        super(x, y);
        loadTexture();
    }
    /**
     * Creates a heart from world coordinates (floored to grid).
     */
    public Heart(float x, float y) {
        super((int) x, (int) y);
        loadTexture();
    }

    private void loadTexture() {
        try {
            texture = new Texture(Gdx.files.internal("imgs/Items/heart.png"));
        } catch (Exception e) {
            Logger.error("Heart texture missing");
        }
    }
    /**
     * Deactivates the heart when collected by a player.
     */
    @Override
    public void onInteract(Player player) {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public RenderType getRenderType() {
        return RenderType.SPRITE;
    }

    @Override
    public void drawSprite(SpriteBatch batch) {
        if (active && texture != null) {
            float bob = (float) Math.sin(System.currentTimeMillis() / 200.0) * 3;
            batch.draw(texture,
                    x * GameConstants.CELL_SIZE + 8,
                    y * GameConstants.CELL_SIZE + 8 + bob,
                    32, 32);
        }
    }

    /**
     * Debug rendering for shape mode.
     */
    @Override
    public void drawShape(ShapeRenderer shapeRenderer) {
        if (!active) return;
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(
                x * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2f,
                y * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2f,
                10
        );
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}