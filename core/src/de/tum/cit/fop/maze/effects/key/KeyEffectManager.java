package de.tum.cit.fop.maze.effects.key;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * Manages visual effects related to key collection.
 * <p>
 * This manager is responsible for creating, updating, and rendering
 * {@link KeyCollectEffect} instances. It handles only visual animation
 * lifecycle and does not manage key inventory or game logic.
 */

public class KeyEffectManager {
    private List<KeyCollectEffect> keyEffects;
    /**
     * Creates a new manager for key collection effects.
     */
    public KeyEffectManager() {
        keyEffects = new ArrayList<>();
    }
    /**
     * Spawns a new key collection visual effect.
     *
     * @param x       world x-coordinate of the key
     * @param y       world y-coordinate of the key
     * @param texture texture used to render the key
     */
    public void spawnKeyEffect(float x, float y, Texture texture) {
        keyEffects.add(new KeyCollectEffect(x, y, texture));
    }
    /**
     * Updates all active key collection effects.
     *
     * @param delta time elapsed since last frame (seconds)
     */
    public void update(float delta) {
        Iterator<KeyCollectEffect> it = keyEffects.iterator();
        while (it.hasNext()) {
            KeyCollectEffect effect = it.next();
            effect.update(delta);
            if (effect.isFinished()) {
                it.remove();
            }
        }
    }
    /**
     * Renders all active key collection effects.
     *
     * @param batch sprite batch used for rendering
     */
    public void render(SpriteBatch batch) {
        for (KeyCollectEffect effect : keyEffects) {
            effect.render(batch);
        }
    }
    /**
     * Clears all active key collection effects.
     */
    public void dispose() {
        keyEffects.clear();
    }
}