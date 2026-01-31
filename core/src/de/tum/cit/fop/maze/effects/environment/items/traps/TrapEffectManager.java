package de.tum.cit.fop.maze.effects.environment.items.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.effects.environment.EnvironmentEffect;
import de.tum.cit.fop.maze.effects.environment.EnvironmentParticleSystem;
import de.tum.cit.fop.maze.utils.Logger;
import java.util.Iterator;
/**
 * Manages all active visual effects for environment traps.
 * <p>
 * This manager is responsible for spawning, updating, and rendering
 * trap-related {@link EnvironmentEffect} instances, as well as
 * coordinating a shared {@link EnvironmentParticleSystem}.
 * It handles only visual effects and does not contain gameplay logic.
 */
public class TrapEffectManager {
    /** Currently active trap visual effects. */
    private Array<EnvironmentEffect> effects;
    /** Shared particle system used by all trap effects. */
    private EnvironmentParticleSystem particleSystem;
    /**
     * Creates a new trap effect manager.
     */
    public TrapEffectManager() {
        this.effects = new Array<>();
        this.particleSystem = new EnvironmentParticleSystem();
    }

    /**
     * Spawns a mud trap visual effect.
     *
     * @param x world x-coordinate
     * @param y world y-coordinate
     */
    public void spawnMudTrap(float x, float y) {
        effects.add(new MudTrapEffect(x, y));
    }
    /**
     * Spawns a geyser trap visual effect.
     *
     * @param x world x-coordinate
     * @param y world y-coordinate
     */
    public void spawnGeyser(float x, float y) {
        effects.add(new GeyserTrapEffect(x, y));
    }
    /**
     * Spawns a pearl mine visual effect.
     *
     * @param x world x-coordinate
     * @param y world y-coordinate
     */
    public void spawnPearlMine(float x, float y) {
        effects.add(new PearlMineEffect(x, y));
    }
    /**
     * Spawns a tea shards visual effect.
     *
     * @param x world x-coordinate
     * @param y world y-coordinate
     */
    public void spawnTeaShards(float x, float y) {
        effects.add(new TeaShardsEffect(x, y));
    }

    /**
     * Updates all active trap effects and the shared particle system.
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
     * Renders all shape-based visuals for trap effects.
     *
     * @param sr the shape renderer
     */
    public void renderShapes(ShapeRenderer sr) {
        if (sr == null) {
            Logger.warning("ShapeRenderer is null, cannot render trap effect shapes");
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
     * Renders all sprite-based visuals for trap effects.
     *
     * @param batch the sprite batch
     */
    public void renderSprites(SpriteBatch batch) {
        if (batch == null) {
            Logger.warning("SpriteBatch is null, cannot render trap effect sprites");
            return;
        }
        for (EnvironmentEffect effect : effects) {
            effect.renderSprite(batch);
        }
    }
    /**
     * Clears all active trap effects and particle data.
     */
    public void dispose() {
        effects.clear();
        particleSystem.clear();
    }
}