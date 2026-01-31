package de.tum.cit.fop.maze.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.entities.ExitDoor;
import de.tum.cit.fop.maze.game.DifficultyConfig;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.utils.TextureManager;

import java.util.ArrayList;
import java.util.List;
/**
 * Renders maze floors and walls based on a pre-generated maze grid.
 * <p>
 * Wall tiles are analyzed and grouped into horizontal segments
 * to reduce draw calls and improve visual consistency.
 * <p>
 * Rendering logic is decoupled from maze generation and supports
 * dynamic maze updates.
 */

public class MazeRenderer {

    private final GameManager gameManager;
    protected final DifficultyConfig difficultyConfig;
    private final TextureManager textureManager = TextureManager.getInstance();
    private int[][] lastMazeRef = null;


    private Texture floorTexture;

    private TextureAtlas wallAtlas;
    private TextureRegion[] wallRegions;

    private boolean analyzed = false;
    private final List<WallGroup> wallGroups = new ArrayList<>();

    /**
     * Represents a continuous horizontal wall segment.
     * <p>
     * Used to batch wall rendering and apply texture variation
     * based on segment length.
     */

    public static class WallGroup {
        public int startX, startY, length, textureIndex;
        /**
         * Creates a wall segment definition.
         *
         * @param x   starting x cell
         * @param y   y cell
         * @param len length in cells
         * @param tex texture index
         */

        public WallGroup(int x, int y, int len, int tex) {
            startX = x;
            startY = y;
            length = len;
            textureIndex = tex;
        }
    }
    /**
     * Creates a maze renderer instance.
     *
     * @param gm               game manager providing maze and entities
     * @param difficultyConfig maze size configuration
     */

    public MazeRenderer(GameManager gm, DifficultyConfig difficultyConfig) {
        this.gameManager = gm;
        this.difficultyConfig = difficultyConfig;
        loadTextures();
    }

    /**
     * Loads floor and wall textures used for maze rendering.
     */

    private void loadTextures() {
        floorTexture = textureManager.getFloorTexture();

        FileHandle fh = Gdx.files.internal("Wallpaper/Wallpaper.atlas");
        wallAtlas = new TextureAtlas(fh);

        Array<TextureAtlas.AtlasRegion> regions =
                wallAtlas.findRegions("Wallpaper");

        wallRegions = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            wallRegions[i] = regions.get(i % regions.size);
        }
    }

    /**
     * Renders the maze floor covering the entire maze area.
     *
     * @param batch sprite batch for rendering
     */

    public void renderFloor(SpriteBatch batch) {
        if (floorTexture == null) return;

        float w = difficultyConfig.mazeWidth * GameConstants.CELL_SIZE;
        float h = difficultyConfig.mazeHeight * GameConstants.CELL_SIZE;

        batch.draw(floorTexture, 0, 0, w, h);
    }

    /**
     * Analyzes the maze grid and groups horizontal wall segments.
     * <p>
     * This preprocessing step reduces per-frame rendering overhead
     * and enables texture variation based on wall length.
     */

    private void analyze() {
        wallGroups.clear();

        int[][] maze = gameManager.getMaze();
        if (maze == null) return;

        for (int y = 0; y < maze.length; y++) {
            int x = 0;
            while (x < maze[y].length) {

                if (!isWallCellButNotExit(x, y)) {
                    x++;
                    continue;
                }

                int startX = x;
                int len = 0;

                while (x < maze[y].length && isWallCellButNotExit(x, y)) {
                    len++;
                    x++;
                }

                splitWall(startX, y, len);
            }
        }

        analyzed = true;
    }

    /**
     * Checks whether a cell is a wall and not occupied by an exit door.
     *
     * @param x x cell coordinate
     * @param y y cell coordinate
     * @return true if the cell is a renderable wall
     */

    private boolean isWallCellButNotExit(int x, int y) {
        return gameManager.getMaze()[y][x] == 0
                && !gameManager.isExitDoorAt(x, y);
    }
    /**
     * Splits a long wall segment into smaller groups
     * based on predefined texture sizes.
     *
     * @param x   starting x cell
     * @param y   y cell
     * @param len total length of the wall segment
     */

    private void splitWall(int x, int y, int len) {
        int cx = x;
        int remain = len;

        while (remain > 0) {
            if (remain >= 5) {
                wallGroups.add(new WallGroup(cx, y, 5, 3));
                cx += 5;
                remain -= 5;
            } else if (remain == 4) {
                wallGroups.add(new WallGroup(cx, y, 2, 1));
                wallGroups.add(new WallGroup(cx + 2, y, 2, 1));
                return;
            } else if (remain >= 3) {
                wallGroups.add(new WallGroup(cx, y, 3, 2));
                cx += 3;
                remain -= 3;
            } else if (remain == 2) {
                wallGroups.add(new WallGroup(cx, y, 2, 1));
                return;
            } else {
                wallGroups.add(new WallGroup(cx, y, 1, 0));
                return;
            }
        }
    }
    /**
     * Returns analyzed wall groups.
     * <p>
     * Automatically re-analyzes the maze if the maze reference changes.
     *
     * @return list of wall groups
     */

    public List<WallGroup> getWallGroups() {
        int[][] currentMaze = gameManager.getMaze();

        if (!analyzed || currentMaze != lastMazeRef) {
            analyze();
            lastMazeRef = currentMaze;
        }

        return wallGroups;
    }


    /**
     * Determines whether a wall is visually in front of any entity.
     * <p>
     * Used for depth-aware rendering decisions.
     *
     * @param wx wall x cell
     * @param wy wall y cell
     * @return true if the wall should be rendered in front
     */

    public boolean isWallInFrontOfAnyEntity(int wx, int wy) {
        var p = gameManager.getPlayer();
        if (p != null && wy > p.getY()) return true;

        for (var e : gameManager.getEnemies()) {
            if (e.isActive() && wy > e.getY()) return true;
        }

        for (ExitDoor d : gameManager.getExitDoors()) {
            if (wy > d.getY()) return true;
        }

        return false;
    }

    /**
     * Renders a single wall group using texture slicing.
     *
     * @param batch sprite batch for rendering
     * @param g     wall group definition
     */

    public void renderWallGroup(SpriteBatch batch, WallGroup g) {
        float cs = GameConstants.CELL_SIZE;
        float h = cs * 2.4f;
        int overlap = 6;

        TextureRegion base = wallRegions[g.textureIndex];

        float u0 = base.getU();
        float u1 = base.getU2();
        float v0 = base.getV();

        float step = (u1 - u0) / g.length;

        for (int i = 0; i < g.length; i++) {
            TextureRegion slice = new TextureRegion(
                    base.getTexture(),
                    (int) ((u0 + i * step) * base.getTexture().getWidth()),
                    (int) (v0 * base.getTexture().getHeight()),
                    (int) (step * base.getTexture().getWidth()),
                    base.getRegionHeight()
            );

            batch.draw(
                    slice,
                    (g.startX + i) * cs,
                    g.startY * cs - overlap,
                    cs,
                    h
            );
        }
    }
    /**
     * Disposes loaded texture resources.
     */

    public void dispose() {
        if (wallAtlas != null) wallAtlas.dispose();
    }


}
