package de.tum.cit.fop.maze.effects.boba;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.fop.maze.entities.enemy.EnemyBoba.BobaBullet;
import de.tum.cit.fop.maze.game.GameConstants;
/**
 * Renders visual representation of {@link BobaBullet} instances.
 * <p>
 * This renderer is responsible for drawing bullet sprites, applying
 * scaling and rotation effects, and providing a fallback rendering
 * mode when textures are unavailable.
 */
public class BobaBulletRenderer {
    /** Global intensity multiplier applied to bullet visual effects. */
    private float effectIntensity = 1.0f;
    /** Texture region used to render bullet sprites. */
    private TextureRegion bulletTexture;
    /** Shape renderer used as a fallback when texture rendering fails. */
    private final ShapeRenderer shapeRenderer;
    /**
     * Creates a new bullet renderer and loads required resources.
     */
    public BobaBulletRenderer() {
        shapeRenderer = new ShapeRenderer();
        loadTexture();
    }

    private void loadTexture() {
        try {
            String path = "effects/boba-bullet.png";
            com.badlogic.gdx.files.FileHandle file = Gdx.files.internal(path);

            if (!file.exists()) {
                this.bulletTexture = null;
                return;
            }

            Texture tex = new Texture(file);
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            this.bulletTexture = new TextureRegion(tex);

            System.out.println("✅ [BobaSuccess]: " + path);

        } catch (Exception e) {
            System.err.println("❌ [BobaError] " + e.getMessage());
            e.printStackTrace();
            this.bulletTexture = null;
        }
    }


    /**
     * Renders a single bullet instance.
     * <p>
     * The bullet is rendered using a texture sprite when available.
     * If the texture cannot be loaded, a simple geometric fallback
     * rendering is used instead.
     *
     * @param bullet the bullet to render
     * @param batch  the sprite batch used for rendering
     */
    public void render(BobaBullet bullet, SpriteBatch batch) {
        if (bullet == null || !bullet.isActive()) return;

        float x = bullet.getRealX() * GameConstants.CELL_SIZE;
        float y = bullet.getRealY() * GameConstants.CELL_SIZE;

        float radius = GameConstants.CELL_SIZE * 0.25f;
        float size = radius * 2;

        float scaleX = bullet.getScaleX() * effectIntensity;
        float scaleY = bullet.getScaleY() * effectIntensity;
        float rotation = bullet.getRotation();

        if (bulletTexture != null) {
            batch.setColor(1f, 1f, 1f, 1f);

            float centerX = x + GameConstants.CELL_SIZE / 2f;
            float centerY = y + GameConstants.CELL_SIZE / 2f;

            batch.draw(
                    bulletTexture,
                    centerX - radius, centerY - radius,
                    radius, radius,
                    size, size,
                    scaleX, scaleY,
                    rotation
            );
        } else {
            batch.end();
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            float centerX = x + GameConstants.CELL_SIZE / 2f;
            float centerY = y + GameConstants.CELL_SIZE / 2f;

            shapeRenderer.setColor(Color.ORANGE);
            shapeRenderer.circle(centerX, centerY, radius * Math.min(scaleX, scaleY));

            shapeRenderer.end();
            batch.begin();
        }
    }

    /**
     * Sets the visual effect intensity for bullet rendering.
     *
     * @param intensity intensity multiplier (clamped internally)
     */
    public void setEffectIntensity(float intensity) {
        this.effectIntensity = Math.max(0.1f, Math.min(2.0f, intensity));
    }
    /**
     * Releases all rendering resources used by this renderer.
     */
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (bulletTexture != null && bulletTexture.getTexture() != null) {
            bulletTexture.getTexture().dispose();
        }
    }
}