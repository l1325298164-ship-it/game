package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.OrthographicCamera;
/**
 * A simple wrapper for an {@link OrthographicCamera} used in boss scenes.
 *
 * <p>This camera is initialized with a fixed viewport size and centered
 * position. It also provides a lightweight screen shake effect by applying
 * small random offsets to the camera position.
 *
 * <p>Typically used for boss battles to enhance visual impact when the boss
 * attacks, takes damage, or enters special phases.
 */
public class BossCamera {
    private OrthographicCamera camera;
    /**
     * Creates a boss camera with the given viewport dimensions.
     *
     * @param width  the viewport width
     * @param height the viewport height
     */
    public BossCamera(float width, float height) {
        camera = new OrthographicCamera(width, height);
        camera.position.set(width / 2f, height / 2f, 0);
        camera.update();
    }
    /**
     * Applies a brief camera shake effect.
     *
     * <p>The camera position is randomly offset in both X and Y directions
     * based on the given strength value.
     *
     * @param strength the intensity of the shake effect
     */
    public void shake(float strength) {
        camera.position.x += (Math.random() - 0.5f) * strength;
        camera.position.y += (Math.random() - 0.5f) * strength;
        camera.update();
    }
    /**
     * Returns the underlying {@link OrthographicCamera}.
     *
     * @return the camera instance
     */
    public OrthographicCamera getCamera() {
        return camera;
    }
}
