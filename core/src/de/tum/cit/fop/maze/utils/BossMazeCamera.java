package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.DifficultyConfig;
import de.tum.cit.fop.maze.game.GameConstants;

import java.util.List;

/**
 * Camera controller for boss maze scenes.
 *
 * <p>This camera follows all alive players inside the boss maze by tracking
 * the average player position. The movement is smoothly interpolated and
 * clamped to the maze boundaries to prevent showing areas outside the maze.
 *
 * <p>An optional subtle shake effect is applied during updates to enhance
 * tension during boss encounters.
 */
public class BossMazeCamera {
    /**
     * Vertical visual offset applied to the camera target position.
     * This slightly raises the view to improve visibility.
     */
    private static final float VISUAL_Y_OFFSET =
            GameConstants.CELL_SIZE * 0.5f;

    private final OrthographicCamera camera;
    private final DifficultyConfig dc;

    /**
     * Smoothing speed for camera movement interpolation.
     */
    private static final float SMOOTH_SPEED = 5.0f;
    /**
     * Shake amplitudes applied during camera updates.
     */
    private static final float SHAKE_X = 6f;
    private static final float SHAKE_Y = 4f;
    /**
     * Creates a boss maze camera controller.
     *
     * @param camera the camera to control
     * @param dc     the difficulty configuration used for maze dimensions
     */
    public BossMazeCamera(OrthographicCamera camera, DifficultyConfig dc) {
        this.camera = camera;
        this.dc = dc;
    }

    /**
     * Updates the camera position based on the average position of all alive players.
     *
     * <p>The camera movement is smoothed using linear interpolation and clamped
     * to the maze bounds defined by the difficulty configuration. A subtle
     * oscillating shake effect is applied for visual impact.
     *
     * @param delta   time elapsed since last frame
     * @param players list of players to track
     */
    public void update(float delta, List<Player> players) {

        if (players == null || players.isEmpty()) {
            camera.update();
            return;
        }

        float sumX = 0f;
        float sumY = 0f;
        int count = 0;

        for (Player p : players) {
            if (p == null || p.isDead()) continue;

            sumX += p.getX() * GameConstants.CELL_SIZE
                    + GameConstants.CELL_SIZE / 2f;

            sumY += p.getY() * GameConstants.CELL_SIZE
                    + GameConstants.CELL_SIZE / 2f;

            count++;
        }

        if (count == 0) {
            camera.update();
            return;
        }

        float targetX = sumX / count;
        float targetY = sumY / count + VISUAL_Y_OFFSET;

        float halfW = camera.viewportWidth  * camera.zoom / 2f;
        float halfH = camera.viewportHeight * camera.zoom / 2f;

        float mazeWorldW = dc.mazeWidth  * GameConstants.CELL_SIZE;
        float mazeWorldH = dc.mazeHeight * GameConstants.CELL_SIZE;

        targetX = MathUtils.clamp(targetX, halfW, mazeWorldW - halfW);
        targetY = MathUtils.clamp(targetY, halfH, mazeWorldH - halfH);

        float newX = MathUtils.lerp(camera.position.x, targetX, SMOOTH_SPEED * delta);
        float newY = MathUtils.lerp(camera.position.y, targetY, SMOOTH_SPEED * delta);

        float t = (float) (System.currentTimeMillis() * 0.001);
        newX += MathUtils.sin(t * 1.3f) * SHAKE_X;
        newY += MathUtils.cos(t * 1.7f) * SHAKE_Y;

        camera.position.set(newX, newY, 0f);
        camera.update();
    }

    /**
     * Instantly moves the camera to the average position of all alive players.
     *
     * <p>This method bypasses smoothing and is typically used when entering
     * a boss scene or after major transitions.
     *
     * @param players list of players to center the camera on
     */
    public void snapToPlayers(List<Player> players) {

        if (players == null || players.isEmpty()) return;

        float sumX = 0f;
        float sumY = 0f;
        int count = 0;

        for (Player p : players) {
            if (p == null || p.isDead()) continue;

            sumX += p.getX() * GameConstants.CELL_SIZE
                    + GameConstants.CELL_SIZE / 2f;
            sumY += p.getY() * GameConstants.CELL_SIZE
                    + GameConstants.CELL_SIZE / 2f;
            count++;
        }

        if (count == 0) return;

        float targetX = sumX / count;
        float targetY = sumY / count + VISUAL_Y_OFFSET;
        Gdx.app.log(
                "BOSS_CAM",
                "snap to " + targetX + "," + targetY
        );
        float halfW = camera.viewportWidth * camera.zoom / 2f;
        float halfH = camera.viewportHeight * camera.zoom / 2f;

        float mazeWorldW = dc.mazeWidth * GameConstants.CELL_SIZE;
        float mazeWorldH = dc.mazeHeight * GameConstants.CELL_SIZE;

        targetX = MathUtils.clamp(targetX, halfW, mazeWorldW - halfW);
        targetY = MathUtils.clamp(targetY, halfH, mazeWorldH - halfH);

        camera.position.set(targetX, targetY, 0f);
        camera.update();
    }

    /**
     * Returns the controlled camera instance.
     *
     * @return the orthographic camera
     */
    public OrthographicCamera getCamera() {
        return camera;
    }
}
