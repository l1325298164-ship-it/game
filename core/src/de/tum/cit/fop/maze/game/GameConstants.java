package de.tum.cit.fop.maze.game;

import com.badlogic.gdx.graphics.Color;
/**
 * Global game-wide constants used for rendering, world configuration,
 * camera setup, and debugging options.
 *
 * <p>This class is not instantiable and only provides static constant values.
 */
public final class GameConstants {
    /** Size of a single maze cell in world units (pixels). */
    public static final int CELL_SIZE = 45;
    /** Default viewport width in pixels. */
    public static final float VIEWPORT_WIDTH  = 800;
    /** Default viewport height in pixels. */
    public static final float VIEWPORT_HEIGHT = 600;
    /** Delay between player movement steps under normal conditions. */
    public static final float MOVE_DELAY_NORMAL = 0.08f;
    /** Maximum number of levels in the game. */
    public static final int MAX_LEVELS = 9;
    /** Color used to render floor tiles. */
    public static final Color FLOOR_COLOR = new Color(0f, 200f/255f, 0f, 1f);
    /** Color used to render wall tiles. */
    public static final Color WALL_COLOR  = new Color(100f/255f, 100f/255f, 100f/255f, 1f);
    /** Color used to render the player. */
    public static final Color PLAYER_COLOR = Color.RED;
    /** Color used to render keys. */
    public static final Color KEY_COLOR = Color.YELLOW;
    /** Color used to render unlocked doors. */
    public static final Color DOOR_COLOR = Color.BLUE;
    /** Color used to render locked doors. */
    public static final Color LOCKED_DOOR_COLOR = Color.RED;

    /** Color used to render heart or healing items. */
    public static final Color HEART_COLOR = Color.RED;
    /** Enables or disables debug-related features. */
    public static final boolean DEBUG_MODE = false;
    /** Number of columns in the default maze grid. */
    public static final int MAZE_COLS = 25;
    /** Number of rows in the default maze grid. */
    public static final int MAZE_ROWS = 15;
    /** Total number of columns in the world grid. */
    public static final int WORLD_COLS = 64;
    /** Total number of rows in the world grid. */
    public static final int WORLD_ROWS = 64;
    /** Total width of the world in world units. */
    public static final float WORLD_WIDTH  = WORLD_COLS * CELL_SIZE;
    /** Total height of the world in world units. */
    public static final float WORLD_HEIGHT = WORLD_ROWS * CELL_SIZE;
    /** Number of columns visible by the camera. */
    public static final float CAMERA_COLS = 20f;
    /** Number of rows visible by the camera. */
    public static final float CAMERA_ROWS = 12f;
    /** Width of the camera view in world units. */
    public static final float CAMERA_VIEW_WIDTH  = CAMERA_COLS * CELL_SIZE;
    /** Height of the camera view in world units. */
    public static final float CAMERA_VIEW_HEIGHT = CAMERA_ROWS * CELL_SIZE;
    /**
     * Private constructor to prevent instantiation.
     */
    private GameConstants() {}
}
