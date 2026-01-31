package de.tum.cit.fop.maze.effects.environment.items;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import de.tum.cit.fop.maze.effects.environment.EnvironmentEffect;
import de.tum.cit.fop.maze.effects.environment.EnvironmentParticleSystem;
/**
 * Visual effect displayed when a key item is collected.
 * <p>
 * This effect animates the key sprite using vertical motion,
 * scaling, rotation, and fading to provide visual feedback
 * for key collection. It is purely visual and does not handle
 * gameplay logic such as unlocking doors.
 */
public class KeyCollectEffect extends EnvironmentEffect {
    /** Texture used to render the key sprite. */
    private final Texture texture;
    /** Initial y-coordinate used as the base for the jump animation. */
    private final float startY;

    /** Maximum vertical offset applied during the jump animation. */
    private final float jumpHeight = 64f;
    /**
     * Creates a new key collection visual effect.
     *
     * @param x       world x-coordinate of the effect
     * @param y       world y-coordinate of the effect
     * @param texture texture used to render the key sprite
     */
    public KeyCollectEffect(float x, float y, Texture texture) {
        super(x, y, 1.0f);
        this.startY = y;
        this.texture = texture;
    }
    /**
     * Updates the key collection effect.
     *
     * @param delta time elapsed since last frame (seconds)
     * @param ps    particle system used to spawn visual particles
     */
    @Override
    protected void onUpdate(float delta, EnvironmentParticleSystem ps) {

    }
    /**
     * Renders shape-based visuals for the key collection effect.
     * <p>
     * This effect does not use shape rendering.
     *
     * @param sr the shape renderer
     */
    @Override
    public void renderShape(ShapeRenderer sr) {

    }
    /**
     * Renders the animated key sprite.
     * <p>
     * The animation applies vertical movement, scaling, rotation,
     * and fading over the lifetime of the effect.
     *
     * @param batch the sprite batch
     */
    @Override
    public void renderSprite(SpriteBatch batch) {
        float progress = Math.min(1.0f, timer / maxDuration);


        float currentY = startY + Interpolation.swingOut.apply(0, jumpHeight, progress);
        float scale = Interpolation.smooth.apply(1.0f, 2.0f, progress);
        float rotation = Interpolation.pow2In.apply(0f, 720f, progress);

        float alpha = 1.0f;
        if (progress > 0.8f) {
            alpha = 1.0f - (progress - 0.8f) / 0.2f;
        }

        float width = 32;
        float height = 32;
        float originX = width / 2;
        float originY = height / 2;

        int srcFunc = batch.getBlendSrcFunc();
        int dstFunc = batch.getBlendDstFunc();


        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(1f, 0.9f, 0.2f, alpha * 0.5f);
        batch.draw(texture, x, currentY, originX, originY, width, height,
                scale * 1.5f, scale * 1.5f, rotation, 0, 0,
                texture.getWidth(), texture.getHeight(), false, false);


        batch.setBlendFunction(srcFunc, dstFunc);
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, x, currentY, originX, originY, width, height,
                scale, scale, rotation, 0, 0,
                texture.getWidth(), texture.getHeight(), false, false);

        batch.setColor(Color.WHITE);
    }
}