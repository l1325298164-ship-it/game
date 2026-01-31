package de.tum.cit.fop.maze.effects.key;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
/**
 * Visual effect displayed when a key is collected.
 * <p>
 * This effect animates the key sprite with a jump, scale,
 * rotation, and fade-out sequence. It is purely visual and
 * does not handle inventory or game state logic.
 */
public class KeyCollectEffect {
    private Vector2 position;
    private Texture texture;
    private float timer;
    private float duration = 1.0f;
    private boolean finished = false;

    private float startY;
    private float jumpHeight = 64f;
    /**
     * Creates a new key collection visual effect.
     *
     * @param x       world x-coordinate of the effect
     * @param y       world y-coordinate of the effect
     * @param texture texture used to render the key
     */
    public KeyCollectEffect(float x, float y, Texture texture) {
        this.position = new Vector2(x, y);
        this.startY = y;
        this.texture = texture;
        this.timer = 0;
    }
    /**
     * Updates the effect timer.
     *
     * @param delta time elapsed since last frame (seconds)
     */
    public void update(float delta) {
        timer += delta;
        if (timer >= duration) {
            finished = true;
        }
    }
    /**
     * Renders the key collection animation.
     *
     * @param batch sprite batch used for rendering
     */
    public void render(SpriteBatch batch) {
        if (finished) return;

        float progress = Math.min(1.0f, timer / duration);

        float currentY = startY + Interpolation.swingOut.apply(0, jumpHeight, progress);

        float scale = Interpolation.smooth.apply(1.0f, 2.0f, progress);

        float rotation = Interpolation.pow2In.apply(0f, 720f, progress);

        float alpha = 1.0f;
        if (progress > 0.8f) {
            alpha = 1.0f - (progress - 0.8f) / 0.2f;
        }

        int srcFunc = batch.getBlendSrcFunc();
        int dstFunc = batch.getBlendDstFunc();
        Color oldColor = new Color(batch.getColor());

        float width = 32;
        float height = 32;
        float originX = width / 2;
        float originY = height / 2;

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(1f, 0.9f, 0.2f, alpha * 0.6f);

        batch.draw(texture,
                position.x, currentY,
                originX, originY,
                width, height,
                scale * 1.5f, scale * 1.5f,
                rotation,
                0, 0, texture.getWidth(), texture.getHeight(), false, false);

        batch.setBlendFunction(srcFunc, dstFunc);
        batch.setColor(1f, 1f, 1f, alpha);

        batch.draw(texture,
                position.x, currentY,
                originX, originY,
                width, height,
                scale, scale,
                rotation,
                0, 0, texture.getWidth(), texture.getHeight(), false, false);

        batch.setColor(Color.WHITE);
    }
    /**
     * Indicates whether the key collection effect has finished.
     *
     * @return true if the effect has completed
     */
    public boolean isFinished() {
        return finished;
    }
}