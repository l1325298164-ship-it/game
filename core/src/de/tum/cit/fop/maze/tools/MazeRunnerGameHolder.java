package de.tum.cit.fop.maze.tools;

import de.tum.cit.fop.maze.MazeRunnerGame;
/**
 * Static holder for the {@link MazeRunnerGame} instance.
 *
 * <p>This class provides global access to the active game instance in
 * situations where dependency injection is not practical, such as
 * utility classes or static helpers.
 *
 * <p>The holder must be initialized exactly once at application startup
 * using {@link #init(MazeRunnerGame)} before calling {@link #get()}.
 *
 * <p>This class is intentionally non-instantiable.
 */
public final class MazeRunnerGameHolder {

    private static MazeRunnerGame game;

    private MazeRunnerGameHolder() {}
    /**
     * Initializes the game holder with the active {@link MazeRunnerGame} instance.
     *
     * <p>This method should be called once during application startup.
     *
     * @param g the active game instance
     */
    public static void init(MazeRunnerGame g) {
        game = g;
    }

    /**
     * Returns the currently stored {@link MazeRunnerGame} instance.
     *
     * @return the active game instance
     * @throws IllegalStateException if the holder has not been initialized
     */
    public static MazeRunnerGame get() {
        if (game == null) {
            throw new IllegalStateException(
                    "MazeRunnerGameHolder not initialized"
            );
        }
        return game;
    }
}
