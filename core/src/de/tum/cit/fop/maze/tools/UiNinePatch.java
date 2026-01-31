package de.tum.cit.fop.maze.tools;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
/**
 * Utility class for creating {@link NinePatchDrawable} objects from a texture atlas.
 *
 * <p>This helper simplifies the creation of UI nine-patch drawables by
 * extracting a region from a {@link TextureAtlas} and applying the specified
 * stretchable margins.
 */
public class UiNinePatch {

    /**
     * Creates a {@link NinePatchDrawable} from a texture atlas region.
     *
     * @param atlas     the texture atlas containing the nine-patch region
     * @param regionName the name of the region inside the atlas
     * @param left      left stretchable border size (in pixels)
     * @param right     right stretchable border size (in pixels)
     * @param top       top stretchable border size (in pixels)
     * @param bottom    bottom stretchable border size (in pixels)
     * @return a {@link NinePatchDrawable} constructed from the given parameters
     */
    public static NinePatchDrawable fromAtlas(
            TextureAtlas atlas,
            String regionName,
            int left, int right, int top, int bottom
    ) {
        return new NinePatchDrawable(
                new NinePatch(
                        atlas.findRegion(regionName),
                        left, right, top, bottom
                )
        );
    }
}
