package de.tum.cit.fop.maze;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
/**
 * Desktop entry point for the Maze Runner game.
 *
 * <p>
 * This launcher configures and starts the LWJGL3 desktop application,
 * including window size, aspect ratio, frame rate, and rendering settings.
 * </p>
 */
public class DesktopLauncher {
    /**
     * Launches the desktop version of the game.
     *
     * <p>
     * The window is initialized at 80% of the current screen size
     * while preserving a 16:9 aspect ratio.
     * </p>
     *
     * @param arg command-line arguments (unused)
     */
    public static void main(String[] arg) {
        try {
            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("Maze Runner");

            Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
            int screenWidth = displayMode.width;
            int screenHeight = displayMode.height;

            int initialWidth = Math.round(0.8f * screenWidth);
            int initialHeight = Math.round(0.8f * screenHeight);

            float targetRatio = 16f / 9f;
            float currentRatio = (float) initialWidth / initialHeight;

            if (currentRatio > targetRatio) {
                initialWidth = Math.round(initialHeight * targetRatio);
            } else {
                initialHeight = Math.round(initialWidth / targetRatio);
            }

            config.setWindowedMode(initialWidth, initialHeight);
            config.setResizable(true);
            config.setWindowSizeLimits(800, 600, -1, -1);
            config.setWindowPosition(-1, -1);

            config.useVsync(true);
            config.setForegroundFPS(60);
            config.setIdleFPS(30);

            config.setBackBufferConfig(
                    8,   // red
                    8,   // green
                    8,   // blue
                    8,   // alpha
                    16,  // depth
                    8,   // stencil
                    0    // samples
            );
            new Lwjgl3Application(new MazeRunnerGame(), config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}