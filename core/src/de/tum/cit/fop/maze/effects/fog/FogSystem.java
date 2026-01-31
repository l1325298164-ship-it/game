package de.tum.cit.fop.maze.effects.fog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.tools.PerlinNoise;
import de.tum.cit.fop.maze.utils.Logger;
/**
 * Manages a screen-space fog visual effect using shaders.
 * <p>
 * This system renders a dynamic fog overlay with a circular
 * visibility mask around the player. The fog fades in and out
 * over time and is purely visual, independent of gameplay logic.
 */
public class FogSystem {
    private ShaderProgram shader;
    private final Texture fogTexture;

    private boolean active = false;
    private boolean debugEnabled = false;
    private float currentAlpha = 0f;
    private static final float FADE_SPEED = 1.5f;
    private static final float MAX_FOG_ALPHA = 0.85f;
    private float timer = 0f;
    private static final float CYCLE = 60f;
    private static final float FOG_DURATION = 30f;
    private float renderedAlpha;
    private float renderedRadiusMultiplier = 1.0f;
    private boolean disposed = false;
    /**
     * Creates and initializes the fog rendering system.
     * <p>
     * Loads required shaders and textures used for the fog effect.
     */
    public FogSystem() {
        ShaderProgram.pedantic = false;
        fogTexture = new Texture("effects/fog.png");

        String vertexShader = Gdx.files.internal("shaders/vertex.glsl").readString();
        String fragmentShader = Gdx.files.internal("shaders/fragment.glsl").readString();
        shader = new ShaderProgram(vertexShader, fragmentShader);

        if (!shader.isCompiled()) {
            System.err.println("Shader Error: " + shader.getLog());
        }
    }
    /**
     * Updates the fog animation state.
     *
     * @param delta time elapsed since last frame (seconds)
     */
    public void update(float delta) {
        if (disposed) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F7)) {
            debugEnabled = !debugEnabled;
        }

        timer += delta;
        if (timer >= CYCLE) timer -= CYCLE;

        boolean targetActive = (timer <= FOG_DURATION) || debugEnabled;

        if (targetActive) {
            currentAlpha = Math.min(MAX_FOG_ALPHA, currentAlpha + delta * FADE_SPEED);
        } else {
            currentAlpha = Math.max(0f, currentAlpha - delta * FADE_SPEED);
        }


        float noiseValue = PerlinNoise.noise(timer * 0.8f, 0f);


        float dynamicAlpha = currentAlpha * (0.9f + noiseValue * 0.1f);


        float radiusFlicker = 0.95f + noiseValue * 0.1f;

        this.renderedAlpha = dynamicAlpha;
        this.renderedRadiusMultiplier = radiusFlicker;

        this.active = currentAlpha > 0;
    }
    /**
     * Renders the fog overlay using the shader.
     *
     * @param batch      sprite batch used for rendering
     * @param camLeft    world x-coordinate of the camera's left edge
     * @param camBottom  world y-coordinate of the camera's bottom edge
     * @param camWidth   width of the camera view in world units
     * @param camHeight  height of the camera view in world units
     * @param catWorldX  player world x-coordinate
     * @param catWorldY  player world y-coordinate
     */

    public void render(SpriteBatch batch, float camLeft, float camBottom, float camWidth, float camHeight, float catWorldX, float catWorldY) {
        if (shader == null || currentAlpha <= 0 || disposed) return;

        batch.setShader(shader);

        float relativeX = (catWorldX * GameConstants.CELL_SIZE - camLeft) / camWidth;
        float relativeY = (catWorldY * GameConstants.CELL_SIZE - camBottom) / camHeight;
        float screenX = relativeX * Gdx.graphics.getWidth();
        float screenY = relativeY * Gdx.graphics.getHeight();

        float radiusInPixels = 5.0f * GameConstants.CELL_SIZE * (Gdx.graphics.getWidth() / camWidth);

        float noiseValue = PerlinNoise.noise(timer * 1.5f, 0f);
        float flicker = 0.92f + (noiseValue * 0.16f);

        float finalDynamicRadius = radiusInPixels * flicker;

        shader.setUniformf("u_maskCenter", screenX, screenY);
        shader.setUniformf("u_maskRadius", finalDynamicRadius);
        shader.setUniformf("u_maxAlpha", currentAlpha);

        batch.setColor(1, 1, 1, 1);
        batch.draw(fogTexture, camLeft, camBottom, camWidth, camHeight);

        batch.flush();
        batch.setShader(null);
    }

    /**
     * Disposes resources used by the fog system.
     */
    public void dispose() {
        if (disposed) return;

        fogTexture.dispose();
        disposed = true;
    }
    /**
     * @return true if the fog effect is currently visible
     */
    public boolean isActive() {
        return active;
    }
    /**
     * @return true if fog debug mode is enabled
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }
    /**
     * @return current fog animation timer value
     */
    public float getTimer() {
        return timer;
    }
}