package de.tum.cit.fop.maze.effects.environment.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.effects.environment.EnvironmentEffect;
import de.tum.cit.fop.maze.effects.environment.EnvironmentParticleSystem;
import de.tum.cit.fop.maze.utils.Logger;

import java.util.Iterator;
/**
 * Manages all active visual effects for environment items.
 * <p>
 * This manager is responsible for spawning, updating, and rendering
 * item-related {@link EnvironmentEffect} instances, such as treasure
 * pickups or healing effects. It handles only visual feedback and
 * does not contain gameplay logic.
 */
public class ItemEffectManager {
    /** Maximum number of simultaneous item effects allowed. */
    private static final int MAX_EFFECTS = 100;
    /** Currently active item visual effects. */
    private Array<EnvironmentEffect> effects;
    /** Shared particle system used by item effects. */
    private EnvironmentParticleSystem particleSystem;
    /**
     * Creates a new item effect manager.
     */
    public ItemEffectManager() {
        this.effects = new Array<>();
        this.particleSystem = new EnvironmentParticleSystem();
    }


    /**
     * Safely adds a new item effect while enforcing the maximum effect limit.
     *
     * @param effect the effect to add
     */
    private void safeAddEffect(EnvironmentEffect effect) {
        if (effect == null) {
            Logger.warning("Attempted to add null effect to ItemEffectManager");
            return;
        }
        if (effects.size >= MAX_EFFECTS) {
            if (effects.size > 0) {
                effects.removeIndex(0);
                Logger.debug("ItemEffectManager reached max effects limit, removed oldest effect");
            }
        }
        effects.add(effect);
    }
    /**
     * Spawns a treasure pickup visual effect.
     *
     * @param x world x-coordinate
     * @param y world y-coordinate
     */
    public void spawnTreasure(float x, float y) {
        safeAddEffect(new TreasureEffect(x, y));
    }
    /**
     * Spawns a heart pickup visual effect.
     *
     * @param x world x-coordinate
     * @param y world y-coordinate
     */

    public void spawnHeart(float x, float y) {
        safeAddEffect(new HeartEffect(x, y));
    }
    /**
     * Spawns a key collection visual effect.
     *
     * @param x       world x-coordinate
     * @param y       world y-coordinate
     * @param texture texture used for the key visual
     */

    public void spawnKeyEffect(float x, float y, Texture texture) {
        safeAddEffect(new KeyCollectEffect(x, y, texture));
    }

    /**
     * Updates all active item effects and the shared particle system.
     *
     * @param delta time elapsed since last frame (seconds)
     */

    public void update(float delta) {
        Iterator<EnvironmentEffect> it = effects.iterator();
        while (it.hasNext()) {
            EnvironmentEffect effect = it.next();
            effect.update(delta, particleSystem);
            if (effect.isFinished()) it.remove();
        }
        particleSystem.update(delta);
    }


    /**
     * Renders all shape-based visuals for item effects.
     *
     * @param sr the shape renderer
     */
    public void renderShapes(ShapeRenderer sr) {
        if (sr == null) {
            Logger.warning("ShapeRenderer is null, cannot render item effect shapes");
            return;
        }
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        for (EnvironmentEffect effect : effects) {
            effect.renderShape(sr);
        }
        particleSystem.render(sr);

        sr.end();
    }

    /**
     * Renders all sprite-based visuals for item effects.
     *
     * @param batch the sprite batch
     */
    public void renderSprites(SpriteBatch batch) {
        if (batch == null) {
            Logger.warning("SpriteBatch is null, cannot render item effect sprites");
            return;
        }
        for (EnvironmentEffect effect : effects) {
            effect.renderSprite(batch);
        }
    }
    /**
     * Clears all active item effects and particle data.
     */
    public void dispose() {
        effects.clear();
        particleSystem.clear();
    }
}