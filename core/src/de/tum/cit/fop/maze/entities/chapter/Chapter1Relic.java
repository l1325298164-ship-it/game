package de.tum.cit.fop.maze.entities.chapter;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.fop.maze.entities.GameObject;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.utils.Logger;
import de.tum.cit.fop.maze.utils.TextureManager;
/**
 * Represents a collectible story relic in Chapter 1.
 * <p>
 * A relic can be interacted with once per run and may be
 * read or discarded, affecting the chapter context state.
 */
public class Chapter1Relic extends GameObject {

    private final RelicData data;
    private final ChapterContext chapterContext;

    private boolean removedThisRun = false;

    private static Texture relicTexture;
    /**
     * Creates a chapter relic at the given grid position.
     * <p>
     * If the relic has already been consumed in the current
     * chapter context, it will not be spawned.
     *
     * @param x              grid x-position
     * @param y              grid y-position
     * @param data           relic configuration data
     * @param chapterContext chapter context tracking relic state
     */
    public Chapter1Relic(int x, int y, RelicData data, ChapterContext chapterContext) {
        super(x, y);
        this.data = data;
        this.chapterContext = chapterContext;

        if (chapterContext != null && chapterContext.isRelicConsumed(data.id)) {
            removedThisRun = true;
            return;
        }

        if (relicTexture == null) {
            relicTexture = new Texture("imgs/Items/chapter1_relic.png");
        }

        Logger.gameEvent("📜 Relic spawned id=" + data.id + " at " + getPositionString());
    }

    /**
     * Called when a player interacts with the relic.
     * <p>
     * Interaction requests the player to handle relic logic,
     * such as opening a relic reading UI.
     *
     * @param player interacting player
     */
    @Override
    public void onInteract(Player player) {
        if (removedThisRun) {
            Logger.error("❌ onInteract called but relic already removed id=" + data.id);
            return;
        }
        if (player == null) {
            Logger.error("❌ onInteract called with null player id=" + data.id);
            return;
        }

        Logger.error("👉 RELIC INTERACT id=" + data.id);

        player.requestChapter1Relic(this);
    }

    /**
     * Marks the relic as read and updates the chapter context.
     * <p>
     * After being read, the relic is removed for the remainder
     * of the current run.
     */
    public void onRead() {
        if (removedThisRun) {
            Logger.error("❌ onRead called but already removed id=" + data.id);
            return;
        }

        Logger.error("📖 RELIC READ CLICKED id=" + data.id);

        if (chapterContext != null) {
            chapterContext.markRelicRead(data.id);
        } else {
            Logger.error("❌ chapterContext is NULL onRead id=" + data.id);
        }

        removedThisRun = true;
    }
    /**
     * Discards the relic and updates the chapter context.
     * <p>
     * Discarded relics are permanently removed for the
     * current run.
     */
    public void onDiscard() {
        if (removedThisRun) {
            Logger.error("❌ onDiscard called but already removed id=" + data.id);
            return;
        }

        Logger.error("🗑 RELIC DISCARDED id=" + data.id);

        if (chapterContext != null) {
            chapterContext.markRelicDiscarded(data.id);
        }

        removedThisRun = true;
    }

    /**
     * Returns the relic's configuration data.
     *
     * @return relic data
     */
    public RelicData getData() {
        return data;
    }

    /**
     * Whether the relic can currently be interacted with.
     *
     * @return {@code true} if the relic is still available
     */
    @Override
    public boolean isInteractable() {
        return !removedThisRun;
    }

    /**
     * Relics do not block player movement.
     *
     * @return {@code true}
     */
    @Override
    public boolean isPassable() {
        return true;
    }
    /**
     * Draws the relic sprite if it has not been removed.
     *
     * @param batch sprite batch used for rendering
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
        if (removedThisRun || relicTexture == null) return;

        batch.draw(
                relicTexture,
                x * GameConstants.CELL_SIZE,
                y * GameConstants.CELL_SIZE,
                GameConstants.CELL_SIZE,
                GameConstants.CELL_SIZE
        );
    }

    @Override
    public void drawShape(ShapeRenderer shapeRenderer) {

    }
    /**
     * Determines the rendering mode for the relic.
     *
     * @return sprite or shape render type based on texture mode
     */
    @Override
    public RenderType getRenderType() {
        TextureManager.TextureMode mode = TextureManager.getInstance().getCurrentMode();
        if (mode == TextureManager.TextureMode.IMAGE || mode == TextureManager.TextureMode.PIXEL) {
            return RenderType.SPRITE;
        }
        return RenderType.SHAPE;
    }
}
