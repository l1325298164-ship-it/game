package de.tum.cit.fop.maze.tools;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Utility class for loading and caching PV (preview / story) animations.
 *
 * <p>This class retrieves {@link Animation} instances from a {@link TextureAtlas}
 * managed by the global {@link com.badlogic.gdx.assets.AssetManager}.
 *
 * <p>Animations are created using all atlas regions matching the given
 * region name and are played at a fixed frame rate of 24 FPS.
 *
 * <p>This class is stateless and cannot be instantiated.
 */
public final class PVAnimationCache {

    private static final ObjectMap<String, Animation<TextureRegion>> CACHE =
            new ObjectMap<>();

    private PVAnimationCache() {}

    /**
     * Retrieves an animation from the specified texture atlas.
     *
     * <p>The animation frames are obtained by calling
     * {@code findRegions(regionName)} on the atlas. The resulting animation
     * uses a fixed frame duration corresponding to 24 frames per second.
     *
     * @param atlasPath  the asset path of the {@link TextureAtlas}
     * @param regionName the base name of the animation regions in the atlas
     * @return an {@link Animation} containing the matched texture regions
     */
    public static Animation<TextureRegion> get(
            String atlasPath,
            String regionName
    ) {
        AssetManager assets =
                MazeRunnerGameHolder.get().getAssets();

        TextureAtlas atlas =
                assets.get(atlasPath, TextureAtlas.class);

        Array<TextureAtlas.AtlasRegion> frames =
                atlas.findRegions(regionName);

        return new Animation<>(1f / 24f, frames);
    }
}
