package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.OrthographicCamera;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.DifficultyConfig;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
/**
 * Central camera controller for the maze game.
 *
 * <p>This manager is responsible for smoothly following one or multiple players,
 * clamping the camera to maze boundaries, handling screen resize logic,
 * applying camera shake effects, and optionally overriding the camera target
 * for cutscenes or debug purposes.
 *
 * <p>The camera position is updated using interpolation to achieve smooth motion.
 * Special modes such as tutorial mode, free target mode, and debug zoom
 * are supported.
 */
public class CameraManager {
    private static CameraManager instance;

    private OrthographicCamera camera;
    /** Target position the camera moves towards. */
    private float targetX, targetY;
    /** Interpolation speed for camera movement. */
    private float smoothSpeed = 5.0f;
    /** Default zoom level of the camera. */
    private float baseZoom = 1.0f;
    /** Enables free camera targeting independent of players. */
    private boolean useFreeTarget = false;
    private float freeTargetX;
    private float freeTargetY;
    private final DifficultyConfig difficultyConfig;
    /** Enables free camera targeting independent of players. */
    private boolean debugForceZoomEnabled = false;
    private float debugForcedZoom = 1.0f;
    /** Camera shake state. */
    private float shakeTime = 0f;
    private float shakeDuration = 0f;
    private float shakeStrength = 0f;
    /**
     * Returns the global camera manager instance.
     *
     * @return the current CameraManager instance
     */
    public static CameraManager getInstance() {
        return instance;
    }
    /**
     * Creates a new camera manager using the given difficulty configuration.
     *
     * @param difficultyConfig configuration used for maze size and boundaries
     */
    public CameraManager(DifficultyConfig difficultyConfig) {
        instance = this;

        this.difficultyConfig = difficultyConfig;
        camera = new OrthographicCamera();
        Logger.debug("CameraManager initialized");
        this.baseZoom = camera.zoom;
    }

    private boolean clampToMap = true;
    private boolean tutorialMode = false;
    /**
     * Enables or disables clamping the camera to maze boundaries.
     *
     * @param enabled true to clamp camera movement to the map
     */
    public void setClampToMap(boolean enabled) {
        this.clampToMap = enabled;
    }
    /**
     * Enables or disables tutorial mode.
     *
     * @param tutorial true if tutorial mode is active
     */
    public void setTutorialMode(boolean tutorial) {
        this.tutorialMode = tutorial;
    }
    /**
     * Updates the camera position based on player positions.
     *
     * <p>The camera follows the average position of all alive players
     * and smoothly interpolates toward that target. Camera movement
     * can be clamped to maze bounds and optionally affected by shake.
     *
     * @param deltaTime time elapsed since last frame
     * @param gm        game manager providing player data
     */
    public void update(float deltaTime, GameManager gm) {
        if (gm == null) return;

        var players = gm.getPlayers();
        if (players == null || players.isEmpty()) return;

        float sumX = 0f;
        float sumY = 0f;
        int count = 0;

        for (Player p : players) {
            if (p == null || p.isDead()) continue;

            float px = (p.getX() + 0.5f) * GameConstants.CELL_SIZE;
            float py = (p.getY() + 0.5f) * GameConstants.CELL_SIZE;

            sumX += px;
            sumY += py;
            count++;
        }

        if (count == 0) return;

        targetX = sumX / count;
        targetY = sumY / count;

        if (clampToMap) {
            targetX = Math.max(
                    camera.viewportWidth / 2f,
                    Math.min(
                            difficultyConfig.mazeWidth * GameConstants.CELL_SIZE - camera.viewportWidth / 2f,
                            targetX
                    )
            );
            targetY = Math.max(
                    camera.viewportHeight / 2f,
                    Math.min(
                            difficultyConfig.mazeHeight * GameConstants.CELL_SIZE - camera.viewportHeight / 2f,
                            targetY
                    )
            );
        }

        float currentX = camera.position.x;
        float currentY = camera.position.y;

        float newX = currentX + (targetX - currentX) * smoothSpeed * deltaTime;
        float newY = currentY + (targetY - currentY) * smoothSpeed * deltaTime;

        if (shakeTime > 0f) {
            shakeTime -= deltaTime;
            float progress = shakeTime / shakeDuration;
            float offsetX = (float)(Math.random() * 2 - 1) * shakeStrength * progress;
            float offsetY = (float)(Math.random() * 2 - 1) * shakeStrength * progress;

            newX += offsetX;
            newY += offsetY;
        }

        camera.position.set(newX, newY, 0);
        camera.update();
    }
    /**
     * Instantly centers the camera on a single player.
     *
     * @param player the player to center on
     */
    public void centerOnPlayerImmediately(Player player) {
        if (player == null) return;

        float playerPixelX = player.getX() * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2;
        float playerPixelY = player.getY() * GameConstants.CELL_SIZE + GameConstants.CELL_SIZE / 2;
        if (clampToMap) {
            playerPixelX = Math.max(camera.viewportWidth / 2f, Math.min(difficultyConfig.mazeWidth * GameConstants.CELL_SIZE - camera.viewportWidth / 2f, playerPixelX));
            playerPixelY = Math.max(camera.viewportHeight / 2f, Math.min(difficultyConfig.mazeHeight * GameConstants.CELL_SIZE - camera.viewportHeight / 2f, playerPixelY));
        }

        camera.position.set(playerPixelX, playerPixelY, 0);
        camera.update();

        Logger.debug("Camera immediately centered on player");
    }
    /**
     * Sets the camera smoothing speed.
     *
     * @param speed smoothing factor (clamped between 1.0 and 20.0)
     */
    public void setSmoothSpeed(float speed) {
        this.smoothSpeed = Math.max(1.0f, Math.min(20.0f, speed));
    }

    /**
     * Returns the controlled camera.
     *
     * @return the orthographic camera
     */
    public OrthographicCamera getCamera() {
        return camera;
    }
    /**
     * Updates the camera viewport after a screen resize.
     *
     * @param width  new screen width
     * @param height new screen height
     */
    public void resize(int width, int height) {
        float aspectRatio = (float) width / height;

        if (aspectRatio > GameConstants.VIEWPORT_WIDTH / GameConstants.VIEWPORT_HEIGHT) {
            camera.viewportWidth = GameConstants.VIEWPORT_HEIGHT * aspectRatio;
            camera.viewportHeight = GameConstants.VIEWPORT_HEIGHT;
        } else {
            camera.viewportWidth = GameConstants.VIEWPORT_WIDTH;
            camera.viewportHeight = GameConstants.VIEWPORT_WIDTH / aspectRatio;
        }

        camera.update();
        Logger.debug(String.format("Camera resized to: %.0fx%.0f",
                camera.viewportWidth, camera.viewportHeight));
    }

    public void setDebugZoom(float zoom) {
        debugForcedZoom = zoom;
        debugForceZoomEnabled = true;
    }

    public void clearDebugZoom() {
        debugForceZoomEnabled = false;
    }
    /**
     * Sets a free camera target independent of player positions.
     *
     * @param x target x position
     * @param y target y position
     */
    public void setTarget(float x, float y) {
        this.freeTargetX = x;
        this.freeTargetY = y;
        this.useFreeTarget = true;
    }
    /**
     * Updates the camera when free target mode is active.
     *
     * @param deltaTime time elapsed since last frame
     */
    public void update(float deltaTime) {
        if (!useFreeTarget) return;

        targetX = freeTargetX;
        targetY = freeTargetY;

        targetX = Math.max(camera.viewportWidth / 2f,
                Math.min(difficultyConfig.mazeWidth * GameConstants.CELL_SIZE - camera.viewportWidth / 2f, targetX));
        targetY = Math.max(camera.viewportHeight / 2f,
                Math.min(difficultyConfig.mazeHeight * GameConstants.CELL_SIZE - camera.viewportHeight / 2f, targetY));

        float currentX = camera.position.x;
        float currentY = camera.position.y;

        float newX = currentX + (targetX - currentX) * smoothSpeed * deltaTime;
        float newY = currentY + (targetY - currentY) * smoothSpeed * deltaTime;

        if (shakeTime > 0f) {
            shakeTime -= deltaTime;
            float progress = shakeTime / shakeDuration;

            float offsetX = (float)(Math.random() * 2 - 1) * shakeStrength * progress;
            float offsetY = (float)(Math.random() * 2 - 1) * shakeStrength * progress;

            newX += offsetX;
            newY += offsetY;
        }

        camera.position.set(newX, newY, 0);
        camera.update();
    }

    /**
     * Disables free target mode and returns to player-following behavior.
     */
    public void disableFreeTarget() {
        useFreeTarget = false;
    }

    public boolean isDebugZoom() {
        return debugForceZoomEnabled;
    }

    /**
     * Triggers a camera shake effect.
     *
     * @param duration  duration of the shake
     * @param strength  shake intensity
     */
    public void shake(float duration, float strength) {
        shakeDuration = duration;
        shakeTime = duration;
        shakeStrength = strength;
    }
}