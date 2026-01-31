package de.tum.cit.fop.maze.maze;


import de.tum.cit.fop.maze.game.DifficultyConfig;
import de.tum.cit.fop.maze.utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;
/**
 * Procedural maze generator based on grouped cell carving.
 * <p>
 * The maze is generated using a depth-first search (DFS) approach
 * operating on logical cell groups composed of path and wall regions.
 * <p>
 * Each walkable area consists of a fixed-size path block, ensuring
 * wider corridors and improved navigation compared to classic
 */

public class MazeGenerator {

    private static final int WALL_WIDTH = 1;
    private static final int WALL_HEIGHT = 1;
    private static final int PATH_WIDTH = 2;
    private static final int PATH_HEIGHT = 3;
    public static final int BORDER_THICKNESS = 12;
    private final Random random = new Random();
    /**
     * Creates a new maze generator instance.
     */

    public MazeGenerator() {

    }

    /**
     * Generates a maze grid using grouped path carving.
     * <p>
     * The algorithm performs the following steps:
     * <ul>
     *   <li>Adjusts maze dimensions to match path/wall group sizes</li>
     *   <li>Generates a base maze using DFS</li>
     *   <li>Adds additional connections to reduce linearity</li>
     *   <li>Validates and repairs connectivity</li>
     *   <li>Applies a solid outer border</li>
     * </ul>
     *
     * @param config difficulty configuration defining maze size
     * @return a 2D grid where {@code 1} represents paths and {@code 0} walls
     */

    public int[][] generateMaze(DifficultyConfig config) {
        long startTime = System.currentTimeMillis();

        int cellGroupWidth = PATH_WIDTH + WALL_WIDTH;
        int cellGroupHeight = PATH_HEIGHT + WALL_HEIGHT;

        int adjustedWidth = adjustSize(config.mazeWidth, cellGroupWidth);
        int adjustedHeight = adjustSize(config.mazeHeight, cellGroupHeight);

        int[][] maze = new int[adjustedHeight][adjustedWidth];

        for (int y = 0; y < adjustedHeight; y++) {
            Arrays.fill(maze[y], 0);
        }

        for (int x = 0; x < adjustedWidth; x++) {
            maze[0][x] = 0;
            maze[adjustedHeight - 1][x] = 0;
        }
        for (int y = 0; y < adjustedHeight; y++) {
            maze[y][0] = 0;
            maze[y][adjustedWidth - 1] = 0;
        }

        generatenxnPathDFS(maze);


        add3x3AdditionalPaths(maze, 0.19f);

        validate3x3Maze(maze);

        cleanupSmallWalls(maze);

        long endTime = System.currentTimeMillis();

        addOuterBorderWalls(maze);
        return maze;
    }

    /**
     * Applies a thick, solid wall border around the maze.
     *
     * @param maze generated maze grid
     */

    private void addOuterBorderWalls(int[][] maze) {
        int height = maze.length;
        int width = maze[0].length;

        for (int y = 0; y < BORDER_THICKNESS; y++) {
            for (int x = 0; x < width; x++) {
                maze[y][x] = 0;                     // 下
                maze[height - 1 - y][x] = 0;        // 上
            }
        }

        for (int x = 0; x < BORDER_THICKNESS; x++) {
            for (int y = 0; y < height; y++) {
                maze[y][x] = 0;                     // 左
                maze[y][width - 1 - x] = 0;         // 右
            }
        }

        Logger.debug("Applied outer border walls with thickness = " + BORDER_THICKNESS);
    }

    /**
     * Adjusts the maze dimension so it can be evenly divided
     * into path and wall cell groups.
     *
     * @param originalSize requested size
     * @param cellGroup    combined size of one path and wall group
     * @return adjusted dimension size
     */

    private int adjustSize(int originalSize, int cellGroup) {
        int remainder = originalSize % cellGroup;
        if (remainder != 0) {
            int adjusted = originalSize + (cellGroup - remainder);
            Logger.debug("Adjusted size from " + originalSize + " to " + adjusted);
            return adjusted;
        }
        return originalSize;
    }

    /**
     * Generates the main maze structure using depth-first search.
     * <p>
     * Each step carves a fixed-size path block and connects it
     * to neighboring blocks via intermediate path regions.
     *
     * @param maze maze grid to modify
     */

    private void generatenxnPathDFS(int[][] maze) {
        int startX = BORDER_THICKNESS;
        int startY = BORDER_THICKNESS;

        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startX, startY});

        set3x3AreaAsPath(maze, startX, startY);

        int horizontalStep = PATH_WIDTH + WALL_WIDTH;
        int verticalStep = PATH_HEIGHT + WALL_HEIGHT;

        int[][] directions = {
            {0, verticalStep},
            {horizontalStep, 0},
            {0, -verticalStep},
            {-horizontalStep, 0}
        };

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int x = current[0];
            int y = current[1];

            ArrayList<int[]> unvisitedNeighbors = new ArrayList<>();

            for (int[] dir : directions) {
                int nx = x + dir[0];
                int ny = y + dir[1];

                if (canCreatenxnPath(maze, nx, ny)) {
                    int midX = x + dir[0] / 2;
                    int midY = y + dir[1] / 2;
                    unvisitedNeighbors.add(new int[]{nx, ny, midX, midY});
                }
            }

            if (!unvisitedNeighbors.isEmpty()) {
                int[] neighbor = unvisitedNeighbors.get(random.nextInt(unvisitedNeighbors.size()));
                int nx = neighbor[0];
                int ny = neighbor[1];
                int midX = neighbor[2];
                int midY = neighbor[3];

                set3x3AreaAsPath(maze, midX, midY);

                set3x3AreaAsPath(maze, nx, ny);

                stack.push(new int[]{nx, ny});
            } else {
                stack.pop();
            }
        }
    }

    /**
     * Checks whether a path block can be safely carved at the given position.
     *
     * @param maze   maze grid
     * @param startX top-left x coordinate
     * @param startY top-left y coordinate
     * @return true if the area is empty and within bounds
     */

    private boolean canCreatenxnPath(int[][] maze, int startX, int startY) {
        int width = maze[0].length;
        int height = maze.length;

        if (startX < BORDER_THICKNESS ||
                startY < BORDER_THICKNESS ||
                startX >= width - BORDER_THICKNESS - PATH_WIDTH ||
                startY >= height - BORDER_THICKNESS - PATH_HEIGHT) {
            return false;
        }

        for (int dy = 0; dy < PATH_HEIGHT; dy++) {
            for (int dx = 0; dx < PATH_WIDTH; dx++) {
                if (maze[startY + dy][startX + dx] != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Marks a rectangular path block as walkable.
     *
     * @param maze   maze grid
     * @param startX top-left x coordinate
     * @param startY top-left y coordinate
     */

    private void set3x3AreaAsPath(int[][] maze, int startX, int startY) {
        for (int dy = 0; dy < PATH_HEIGHT; dy++) {
            for (int dx = 0; dx < PATH_WIDTH; dx++) {
                if (startY + dy < maze.length && startX + dx < maze[0].length) {
                    maze[startY + dy][startX + dx] = 1;
                }
            }
        }
    }




    private void ensureStartEnd3x3Areas(int[][] maze) {
        int width = maze[0].length;
        int height = maze.length;

        set3x3AreaAsPath(maze, WALL_WIDTH, WALL_HEIGHT);

        int endX = width - WALL_WIDTH - PATH_WIDTH;
        int endY = height - WALL_HEIGHT - PATH_HEIGHT;
        set3x3AreaAsPath(maze, endX, endY);

        ensureBoundaryWalls(maze);
    }

    private void ensureBoundaryWalls(int[][] maze) {
        int width = maze[0].length;
        int height = maze.length;

        for (int x = 0; x < width; x++) {
            maze[0][x] = 0;
            maze[height - 1][x] = 0;
        }
        for (int y = 0; y < height; y++) {
            maze[y][0] = 0;
            maze[y][width - 1] = 0;
        }

        for (int y = height - 3; y < height; y++) {
            for (int x = width - 3; x < width; x++) {
                if (y >= 0 && x >= 0) {
                    maze[y][x] = 0;
                }
            }
        }

        Logger.debug("Ensured boundary walls including fixed top-right corner");
    }

    private void ensurePathToStartEnd(int[][] maze) {
        int width = maze[0].length;
        int height = maze.length;

        int startX = WALL_WIDTH;
        int startY = WALL_HEIGHT;

        int startCenterX = startX + PATH_WIDTH / 2;
        int startCenterY = startY + PATH_HEIGHT / 2;

        if (startCenterX + 1 < width) {
            for (int x = startCenterX; x <= startCenterX + 2; x++) {
                if (startCenterY < height) {
                    maze[startCenterY][x] = 1;
                }
            }
        }

        if (startCenterY + 1 < height) {
            for (int y = startCenterY; y <= startCenterY + 2; y++) {
                if (startCenterX < width) {
                    maze[y][startCenterX] = 1;
                }
            }
        }

        int endX = width - WALL_WIDTH - PATH_WIDTH;
        int endY = height - WALL_HEIGHT - PATH_HEIGHT;
        int endCenterX = endX + PATH_WIDTH / 2;
        int endCenterY = endY + PATH_HEIGHT / 2;

        if (endCenterX - 1 >= 0) {
            for (int x = endCenterX - 2; x <= endCenterX; x++) {
                if (endCenterY < height) {
                    maze[endCenterY][x] = 1;
                }
            }
        }

        if (endCenterY - 1 >= 0) {
            for (int y = endCenterY - 2; y <= endCenterY; y++) {
                if (endCenterX < width) {
                    maze[y][endCenterX] = 1;
                }
            }
        }
    }
    /**
     * Adds additional connections between existing path blocks.
     * <p>
     * This reduces long linear corridors and increases exploration options.
     *
     * @param maze   maze grid
     * @param chance probability of converting a wall block into a path
     */

    private void add3x3AdditionalPaths(int[][] maze, float chance) {
        int width = maze[0].length;
        int height = maze.length;
        int pathsAdded = 0;

        int horizontalStep = PATH_WIDTH + WALL_WIDTH;
        int verticalStep = PATH_HEIGHT + WALL_HEIGHT;

        for (int y = verticalStep; y < height - verticalStep; y += verticalStep) {
            for (int x = horizontalStep; x < width - horizontalStep; x += horizontalStep) {
                if (is1x2WallArea(maze, x, y)) {
                    int adjacentPaths = 0;

                    int[][] checkDirs = {
                        {0, -verticalStep}, {0, verticalStep},
                        {-horizontalStep, 0}, {horizontalStep, 0}
                    };

                    for (int[] dir : checkDirs) {
                        int checkX = x + dir[0];
                        int checkY = y + dir[1];

                        if (checkX >= 0 && checkX < width - PATH_WIDTH &&
                            checkY >= 0 && checkY < height - PATH_HEIGHT &&
                            is3x3PathArea(maze, checkX, checkY)) {
                            adjacentPaths++;
                        }
                    }

                    if (adjacentPaths >= 2 && random.nextFloat() < chance) {
                        convertWallToPath(maze, x, y);
                        pathsAdded++;
                    }
                }
            }
        }

        Logger.debug("Added " + pathsAdded + " additional 3x3 paths");
    }

    private boolean is1x2WallArea(int[][] maze, int startX, int startY) {
        for (int dy = 0; dy < WALL_HEIGHT; dy++) {
            for (int dx = 0; dx < WALL_WIDTH; dx++) {
                if (startY + dy < maze.length && startX + dx < maze[0].length) {
                    if (maze[startY + dy][startX + dx] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean is3x3PathArea(int[][] maze, int startX, int startY) {
        for (int dy = 0; dy < PATH_HEIGHT; dy++) {
            for (int dx = 0; dx < PATH_WIDTH; dx++) {
                if (startY + dy >= maze.length || startX + dx >= maze[0].length ||
                    maze[startY + dy][startX + dx] != 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private void convertWallToPath(int[][] maze, int wallX, int wallY) {
        int pathX = Math.max(0, wallX - 1);
        int pathY = Math.max(0, wallY - 1);
        set3x3AreaAsPath(maze, pathX, pathY);
    }
    /**
     * Validates overall maze connectivity from start to end.
     * <p>
     * If connectivity issues are detected, corrective steps
     * are applied to restore reachability.
     *
     * @param maze maze grid
     */

    private void validate3x3Maze(int[][] maze) {
        int width = maze[0].length;
        int height = maze.length;

        int startX = WALL_WIDTH + 1;
        int startY = WALL_HEIGHT + 1;
        int endX = width - WALL_WIDTH - PATH_WIDTH + 1;
        int endY = height - WALL_HEIGHT - PATH_HEIGHT + 1;

        if (!isPathReachable(maze, startX, startY, endX, endY)) {
            Logger.warning("3x3 Maze may not be fully connected, fixing problematic areas");
            fix3x3MazeConnectivity(maze);
        }
    }

    private void fix3x3MazeConnectivity(int[][] maze) {
        int width = maze[0].length;
        int height = maze.length;

        int horizontalStep = PATH_WIDTH + WALL_WIDTH;
        int verticalStep = PATH_HEIGHT + WALL_HEIGHT;

        for (int y = verticalStep; y < height - verticalStep; y += verticalStep) {
            for (int x = horizontalStep; x < width - horizontalStep; x += horizontalStep) {
                if (is1x2WallArea(maze, x, y)) {
                    int connectedRegions = 0;

                    int[][] checkDirs = {
                        {0, -verticalStep}, {0, verticalStep},
                        {-horizontalStep, 0}, {horizontalStep, 0}
                    };

                    for (int[] dir : checkDirs) {
                        int nx = x + dir[0];
                        int ny = y + dir[1];

                        if (nx >= 0 && nx < width - PATH_WIDTH &&
                            ny >= 0 && ny < height - PATH_HEIGHT &&
                            is3x3PathArea(maze, nx, ny)) {
                            connectedRegions++;
                        }
                    }

                    if (connectedRegions >= 2) {
                        convertWallToPath(maze, x, y);
                        Logger.debug("Fixed 3x3 connectivity at (" + x + ", " + y + ")");
                        return;
                    }
                }
            }
        }
    }
    /**
     * Removes small isolated wall tiles surrounded by paths.
     * <p>
     * Improves visual clarity and movement smoothness.
     *
     * @param maze maze grid
     */

    private void cleanupSmallWalls(int[][] maze) {
        int width = maze[0].length;
        int height = maze.length;
        int cleaned = 0;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (maze[y][x] == 0) {
                    int pathNeighbors = 0;

                    if (maze[y-1][x] == 1) pathNeighbors++;
                    if (maze[y+1][x] == 1) pathNeighbors++;
                    if (maze[y][x-1] == 1) pathNeighbors++;
                    if (maze[y][x+1] == 1) pathNeighbors++;

                    if (pathNeighbors >= 3 && random.nextFloat() < 0.7f) {
                        maze[y][x] = 1;
                        cleaned++;
                    }
                }
            }
        }

        if (cleaned > 0) {
            Logger.debug("Cleaned " + cleaned + " small isolated walls");
        }
    }
    /**
     * Checks whether a path exists between two points using BFS.
     *
     * @param maze   maze grid
     * @param startX start x coordinate
     * @param startY start y coordinate
     * @param endX   end x coordinate
     * @param endY   end y coordinate
     * @return true if a valid path exists
     */

    private boolean isPathReachable(int[][] maze, int startX, int startY, int endX, int endY) {
        int width = maze[0].length;
        int height = maze.length;

        boolean[][] visited = new boolean[height][width];
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();

        queue.offer(new int[]{startX, startY});
        visited[startY][startX] = true;

        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];

            if (x == endX && y == endY) {
                return true;
            }

            for (int[] dir : directions) {
                int nx = x + dir[0];
                int ny = y + dir[1];

                if (nx >= 0 && nx < width &&
                    ny >= 0 && ny < height &&
                    !visited[ny][nx] && maze[ny][nx] == 1) {
                    visited[ny][nx] = true;
                    queue.offer(new int[]{nx, ny});
                }
            }
        }

        return false;
    }
    /**
     * Determines whether a position is suitable for placing entities.
     *
     * @param maze maze grid
     * @param x    x coordinate
     * @param y    y coordinate
     * @return true if surrounding area contains sufficient path tiles
     */

    public static boolean isValidPosition(int[][] maze, int x, int y) {
        if (x < 0 || x >= maze[0].length || y < 0 || y >= maze.length) {
            return false;
        }

        int pathCount = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < maze[0].length && ny >= 0 && ny < maze.length) {
                    if (maze[ny][nx] == 1) {
                        pathCount++;
                    }
                }
            }
        }

        return pathCount >= 5;
    }
    /**
     * Prints a visual debug representation of the maze to the log.
     *
     * @param maze maze grid to visualize
     */

    public static void printMazeForDebug(int[][] maze) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== 3x2 MAZE DEBUG ===\n");

        sb.append("   ");
        for (int x = 0; x < Math.min(maze[0].length, 50); x++) {
            sb.append(x % 10);
        }
        sb.append("\n");

        for (int y = maze.length - 1; y >= Math.max(0, maze.length - 30); y--) {
            sb.append(String.format("%2d ", y));
            for (int x = 0; x < Math.min(maze[0].length, 50); x++) {
                if (maze[y][x] == 1) {
                    int neighbors = 0;
                    if (y > 0 && maze[y-1][x] == 1) neighbors++;
                    if (y < maze.length-1 && maze[y+1][x] == 1) neighbors++;
                    if (x > 0 && maze[y][x-1] == 1) neighbors++;
                    if (x < maze[0].length-1 && maze[y][x+1] == 1) neighbors++;

                    if (neighbors >= 3) {
                        sb.append("╋");
                    } else if (neighbors == 2) {
                        sb.append("━");
                    } else {
                        sb.append("·");
                    }
                } else {
                    boolean isTallWall = false;
                    if (y > 0 && maze[y-1][x] == 0) isTallWall = true;
                    if (y < maze.length-1 && maze[y+1][x] == 0) isTallWall = true;

                    if (isTallWall) {
                        sb.append("█");
                    } else {
                        sb.append("░");
                    }
                }
            }
            sb.append("\n");
        }
        sb.append("=======================\n");
        Logger.debug(sb.toString());
    }
}
