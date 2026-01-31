package de.tum.cit.fop.maze.utils;
/**
 * Simple static logging utility for the game.
 * <p>
 * Provides methods for printing debug, info, warning, error,
 * and game event messages to the console.
 * Debug output can be enabled or disabled at runtime.
 */
public class Logger {

    private static boolean DEBUG_ENABLED = true;   // 默认关闭
    /**
     * Toggles debug logging on or off.
     * <p>
     * When debug mode is enabled, calls to {@link #debug(String)}
     * will print messages to the console.
     */
    public static void toggleDebug() {
        DEBUG_ENABLED = !DEBUG_ENABLED;
        System.out.println("DEBUG MODE = " + DEBUG_ENABLED);
    }
    /**
     * Checks whether debug logging is currently enabled.
     *
     * @return {@code true} if debug logging is enabled, {@code false} otherwise
     */
    public static boolean isDebugEnabled() {
        return DEBUG_ENABLED;
    }
    /**
     * Prints a debug message to the console if debug mode is enabled.
     *
     * @param message the debug message to print
     */
    public static void debug(String message) {
        if (DEBUG_ENABLED) {
            System.out.println("[DEBUG] " + message);
        }
    }
    /**
     * Prints an informational message to the console.
     *
     * @param message the message to print
     */
    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }
    /**
     * Prints a warning message to the console.
     *
     * @param message the warning message to print
     */
    public static void warning(String message) {
        System.out.println("[WARNING] " + message);
    }
    /**
     * Prints an error message to the error output stream.
     *
     * @param message the error message to print
     */
    public static void error(String message) {
        System.err.println("[ERROR] " + message);
    }
    /**
     * Prints a game-related event message to the console.
     *
     * @param event the event description to print
     */
    public static void gameEvent(String event) {
        System.out.println("[GameEvent] " + event);
    }
}
