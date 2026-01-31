package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.utils.Logger;
/**
 * A UI compass that points toward the nearest exit.
 * <p>
 * The compass is purely visual and does not affect gameplay logic.
 * It updates its direction based on the player's position
 * and the currently assigned exit door.
 */
public class Compass {

    private final Player player;
    private ExitDoor nearestExit;

    private final Texture baseTexture;
    private final Texture needleTexture;

    private final Sprite baseSprite;
    private final Sprite needleSprite;

    private boolean active = true;
    /**
     * Creates a compass bound to the given player.
     *
     * @param player the player used as the reference point
     */
    public Compass(Player player) {
        this.player = player;



        baseTexture = new Texture(Gdx.files.internal("ui/HUD/compass_base.png"));
        needleTexture = new Texture(Gdx.files.internal("ui/HUD/compass_needle.png"));

        baseSprite = new Sprite(baseTexture);
        needleSprite = new Sprite(needleTexture);

        baseSprite.setSize(120, 120);
        needleSprite.setSize(20, 60);

        baseSprite.setOriginCenter();
        needleSprite.setOriginCenter();

        baseSprite.setScale(2.3f);
        needleSprite.setScale(2.3f);

    }

    /**
     * Enables or disables the compass rendering.
     *
     * @param active whether the compass should be visible
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    /**
     * @return whether the compass is currently active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Updates the exit door used as the compass target.
     *
     * @param exitDoor the nearest exit door
     */
    public void update(ExitDoor exitDoor) {
        this.nearestExit = exitDoor;
    }

    /**
     * Draws the compass at its default UI position.
     *
     * @param batch sprite batch used for rendering
     */
    public void drawAsUI(SpriteBatch batch) {
        float margin = 10f;

        float x = Gdx.graphics.getWidth()
                - getUIWidth()
                - margin - 60;

        float y = margin + 50;

        drawAsUIAt(batch, x, y);
    }


    /**
     * Disposes all textures used by the compass.
     */
    public void dispose() {
        baseTexture.dispose();
        needleTexture.dispose();
    }

    /**
     * @return width of the compass in UI space
     */
    public float getUIWidth() {
        return baseSprite.getWidth() * baseSprite.getScaleX();
    }
    /**
     * @return height of the compass in UI space
     */
    public float getUIHeight() {
        return baseSprite.getHeight() * baseSprite.getScaleY();
    }
    /**
     * Draws the compass at a specific UI position.
     *
     * @param batch sprite batch used for rendering
     * @param x     x-position in screen coordinates
     * @param y     y-position in screen coordinates
     */
    public void drawAsUIAt(SpriteBatch batch, float x, float y) {
        if (!active || nearestExit == null) return;

        float dx = nearestExit.getX() - player.getX();
        float dy = nearestExit.getY() - player.getY();
        float angle =
                MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees - 90f;

        baseSprite.setPosition(x + 6f, y - 6f);
        baseSprite.setColor(0f, 0f, 0f, 0.25f);
        baseSprite.draw(batch);

        baseSprite.setPosition(x, y);
        baseSprite.setColor(1f, 1f, 1f, 1f);
        baseSprite.draw(batch);

        float baseCenterX =
                x + baseSprite.getWidth() * baseSprite.getScaleX() / 2f;
        float baseCenterY =
                y + baseSprite.getHeight() * baseSprite.getScaleY() / 2f;

        float centerX = baseCenterX - 76f;
        float centerY = baseCenterY - 105f;

        needleSprite.setCenter(centerX + 3f, centerY - 3f);
        needleSprite.setRotation(angle);
        needleSprite.setColor(0f, 0f, 0f, 0.35f);
        needleSprite.draw(batch);

        needleSprite.setCenter(centerX, centerY);
        needleSprite.setRotation(angle);
        needleSprite.setColor(
                nearestExit.isLocked() ? Color.YELLOW : Color.GREEN
        );
        needleSprite.draw(batch);
    }


}
