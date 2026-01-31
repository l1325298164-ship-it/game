package de.tum.cit.fop.maze.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.DifficultyConfig;
import de.tum.cit.fop.maze.game.GameManager;

import java.util.Random;
/**
 * Specialized maze renderer used for boss encounters.
 * <p>
 * This renderer overrides floor and wall rendering to apply
 * boss-specific visual styles, including custom floor textures
 * and segmented wall skins.
 * <p>
 * Wall textures are selected deterministically to ensure visual
 * consistency across frames.
 */

public class BossMazeRenderer extends MazeRenderer {
    private Texture bossFloorTexture;

    private static final float BOSS_WALL_HEIGHT_MULT = 1f;
    private static final int OVERLAP = 4;

    /**
     * Predefined pools of wall texture regions.
     * <p>
     * Each pool represents a visual variation group used for
     * deterministic wall rendering.
     */

    private TextureRegion[][] wallSkinPools;
    /**
     * Creates a boss maze renderer with boss-specific visual assets.
     *
     * @param gm               game manager reference
     * @param difficultyConfig active difficulty configuration
     */

    public BossMazeRenderer(GameManager gm, DifficultyConfig difficultyConfig) {
        super(gm, difficultyConfig);
        loadBossWallTextures();
        loadBossFloorTexture();
    }
    /**
     * Loads the boss arena floor texture.
     */

    private void loadBossFloorTexture() {
        bossFloorTexture = new Texture(
                Gdx.files.internal("Wallpaper/boss/floor.png")
        );
    }
    /**
     * Renders the boss arena floor using a single large texture
     * covering the entire maze area.
     *
     * @param batch sprite batch used for rendering
     */

    @Override
    public void renderFloor(SpriteBatch batch) {
        if (bossFloorTexture == null) return;

        float w = difficultyConfig.mazeWidth * GameConstants.CELL_SIZE;
        float h = difficultyConfig.mazeHeight * GameConstants.CELL_SIZE;

        batch.draw(bossFloorTexture, 0, 0, w, h);
    }

    /**
     * Loads boss wall textures from a texture atlas and
     * organizes them into variation pools.
     */

    private void loadBossWallTextures() {
        FileHandle fh = Gdx.files.internal("Wallpaper/boss/wallpaper.atlas");
        TextureAtlas atlas = new TextureAtlas(fh);

        var regions = atlas.findRegions("wallpaper");

        wallSkinPools = new TextureRegion[][] {
                { regions.get(0), regions.get(1), regions.get(2) }, // group 0
                { regions.get(3), regions.get(4) },                 // group 1
                { regions.get(5), regions.get(6) },                 // group 2
                { regions.get(7), regions.get(8) }                  // group 3
        };
    }
    /**
     * Selects a deterministic wall texture for the given wall group.
     * <p>
     * A hash-based seed is used to ensure that the same wall group
     * always receives the same texture variation.
     *
     * @param g wall group to be rendered
     * @return a stable texture region from the corresponding pool
     */

    private TextureRegion pickStableTexture(WallGroup g) {
        TextureRegion[] pool = wallSkinPools[g.textureIndex];

        long seed = 1469598103934665603L;
        seed ^= g.startX; seed *= 1099511628211L;
        seed ^= g.startY; seed *= 1099511628211L;
        seed ^= g.length; seed *= 1099511628211L;
        seed ^= g.textureIndex; seed *= 1099511628211L;

        Random r = new Random(seed);
        return pool[r.nextInt(pool.length)];
    }
    /**
     * Renders a continuous wall group using sliced texture regions.
     * <p>
     * The base texture is horizontally segmented to match the wall
     * length while preserving visual continuity.
     *
     * @param batch sprite batch used for rendering
     * @param g     wall group to render
     */

    @Override
    public void renderWallGroup(SpriteBatch batch, WallGroup g) {
        float cs = GameConstants.CELL_SIZE;
        float h  = cs * BOSS_WALL_HEIGHT_MULT;

        TextureRegion base = pickStableTexture(g);

        float u0 = base.getU();
        float u1 = base.getU2();
        float v0 = base.getV();

        float step = (u1 - u0) / g.length;

        for (int i = 0; i < g.length; i++) {
            TextureRegion slice = new TextureRegion(
                    base.getTexture(),
                    (int)((u0 + i * step) * base.getTexture().getWidth()),
                    (int)(v0 * base.getTexture().getHeight()),
                    (int)(step * base.getTexture().getWidth()),
                    base.getRegionHeight()
            );

            batch.draw(
                    slice,
                    (g.startX + i) * cs,
                    g.startY * cs - OVERLAP,
                    cs,
                    h
            );
        }
    }
}
