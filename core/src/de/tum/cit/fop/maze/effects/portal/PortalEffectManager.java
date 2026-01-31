package de.tum.cit.fop.maze.effects.portal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.game.GameConstants;

/**
 * Manages visual effects for portal interactions.
 * <p>
 * This class controls portal state transitions, glow rendering,
 * particle animations, and optional player hiding during
 * portal enter and exit sequences.
 */

public class PortalEffectManager {
    /**
     * Internal state of the portal animation lifecycle.
     */
    private enum State { IDLE, ACTIVE, FINISHED }
    public enum PortalOwner {
        DOOR,
        PLAYER
    }
    private PortalOwner owner = PortalOwner.DOOR;


    private State currentState = State.IDLE;
    private PortalParticlePool particlePool;
    private Texture glowTexture;

    private float timer = 0f;
    private float animationDuration = 2.0f;
    private float playerVanishTime = 1.0f;

    private float targetX, targetY;
    private boolean playerHidden = false;
    /**
     * Creates a portal effect manager for a door-based portal.
     */

    public PortalEffectManager() {
        this.particlePool = new PortalParticlePool();
        createGlowTexture();
    }
    /**
     * Creates a portal effect manager with a specified owner.
     *
     * @param owner the entity that owns the portal effect
     */

    public PortalEffectManager(PortalOwner owner) {
        this.owner = owner;
        this.particlePool = new PortalParticlePool();
        createGlowTexture();
    }

    /**
     * Generates a radial glow texture used as the portal background.
     * <p>
     * The texture is created procedurally to allow smooth blending
     * and avoid external asset dependencies.
     */

    private void createGlowTexture() {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        float centerX = size / 2f;
        float centerY = size / 2f;
        float maxRadius = size / 2f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);

                if (distance <= maxRadius) {
                    float t = distance / maxRadius;

                    float alpha = 1.0f - t;
                    alpha = (float)Math.pow(alpha, 3.0);

                    pixmap.setColor(1f, 1f, 1f, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }

        this.glowTexture = new Texture(pixmap);
        pixmap.dispose();
    }
    /**
     * Updates portal state, particle effects, and animation timing.
     *
     * @param delta time elapsed since last frame (seconds)
     */

    public void update(float delta) {
        if (owner == PortalOwner.PLAYER) {
        }



        timer += delta;

        if (currentState == State.ACTIVE) {
            if (timer < animationDuration * 0.8f) {
                particlePool.spawnTornadoParticles(targetX, targetY, GameConstants.CELL_SIZE * 0.4f);
            }
            particlePool.update(delta, targetX, targetY);

            if(owner== PortalOwner.DOOR) {
                if (!playerHidden && timer >= playerVanishTime) {
                    playerHidden = true;
                }
            }

            if (timer >= animationDuration) {
                currentState = State.FINISHED;
            }
        } else {
            particlePool.update(delta, targetX, targetY);
        }
    }
    /**
     * Starts the portal exit animation at the given position.
     *
     * @param x portal center x-coordinate
     * @param y portal center y-coordinate
     */

    public void startExitAnimation(float x, float y) {
        this.targetX = x;
        this.targetY = y;
        this.currentState = State.ACTIVE;
        this.timer = 0f;
        this.playerHidden = false;
    }
    /**
     * Renders the portal background glow behind the door sprite.
     *
     * @param batch sprite batch used for rendering
     * @param doorX door x-coordinate
     * @param doorY door y-coordinate
     */

    public void renderBack(SpriteBatch batch, float doorX, float doorY) {
        if (glowTexture == null) return;

        float breath = MathUtils.sin(timer * 2.5f);
        float scale = 1.1f + breath * 0.15f;
        float alpha = 0.4f + breath * 0.15f;

        if (currentState == State.ACTIVE) {
            scale = 1.5f + MathUtils.sin(timer * 15f) * 0.1f;
            alpha = 0.8f;
        }

        int srcFunc = batch.getBlendSrcFunc();
        int dstFunc = batch.getBlendDstFunc();
        Color oldColor = batch.getColor();

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        batch.setColor(0.1f, 0.6f, 1.0f, alpha);

        float size = GameConstants.CELL_SIZE * 2.0f; // 光晕大一点，覆盖两格

        batch.draw(glowTexture,
                doorX - size/2 + GameConstants.CELL_SIZE/2,
                doorY - size/2 + GameConstants.CELL_SIZE/2,
                size/2, size/2,
                size, size,
                scale, scale,
                0,
                0, 0, glowTexture.getWidth(), glowTexture.getHeight(), false, false
        );

        batch.setColor(Color.WHITE);

        batch.setBlendFunction(srcFunc, dstFunc);
    }
    /**
     * Renders portal particle effects in the foreground layer.
     *
     * @param batch sprite batch used for rendering
     */

    public void renderFront(SpriteBatch batch) {
        particlePool.render(batch);
        batch.setColor(Color.WHITE);
    }
    /**
     * Returns a vertical offset for door floating animation.
     *
     * @return vertical offset value
     */

    public float getDoorFloatOffset() {
        if (currentState == State.ACTIVE) {
            return MathUtils.random(-2f, 2f);
        }
        return MathUtils.sin(timer * 2.0f) * 4.0f;
    }
    /**
     * Determines whether the player sprite should be hidden
     * during the portal animation.
     *
     * @return true if the player should be hidden
     */

    public boolean shouldHidePlayer() {
        if (owner == PortalOwner.PLAYER) return false;
        return currentState == State.ACTIVE && playerHidden;
    }
    /**
     * @return true if the portal animation has finished
     */

    public boolean isFinished() {
        return currentState == State.FINISHED;
    }
    /**
     * @return true if the portal animation is currently active
     */

    public boolean isActive() {
        return currentState == State.ACTIVE;
    }
    /** Releases all allocated resources. */
    public void dispose() {
        if (glowTexture != null) glowTexture.dispose();
        particlePool.dispose();
    }
    /**
     * Updates idle portal effects around the player.
     *
     * @param delta frame delta time
     * @param x player x-coordinate
     * @param y player y-coordinate
     * @param isLevelTransition whether a level transition is occurring
     */

    public void updatePlayerIdle(float delta, float x, float y, boolean isLevelTransition) {
        this.targetX = x;
        this.targetY = y;

        timer += delta;

        if (isLevelTransition) {
            currentState = State.ACTIVE;
        } else {
            currentState = State.IDLE;
        }

        if (MathUtils.randomBoolean(0.15f)) {
            particlePool.spawnTornadoParticles(
                    targetX,
                    targetY,
                    GameConstants.CELL_SIZE * 0.25f
            );
        }

        particlePool.update(delta, targetX, targetY);
    }
    /**
     * Starts a portal spawn animation for the player.
     *
     * @param x spawn x-coordinate
     * @param y spawn y-coordinate
     */

    public void startPlayerSpawnEffect(float x, float y) {
        this.owner = PortalOwner.PLAYER;

        this.targetX = x;
        this.targetY = y;

        this.currentState = State.ACTIVE;
        this.timer = 0f;

        this.playerHidden = false;

        this.animationDuration = 2.0f;
        this.playerVanishTime = 999f; // 永远不会触发
    }
    /** Resets the portal state to idle. */


    public void reset() {
        this.currentState = State.IDLE;
        this.timer = 0f;
        this.playerHidden = false;
    }
    /**
     * Sets the visual center of the portal.
     *
     * @param x center x-coordinate
     * @param y center y-coordinate
     */

    public void setCenter(float x, float y) {
        this.targetX = x;
        this.targetY = y;
    }
}