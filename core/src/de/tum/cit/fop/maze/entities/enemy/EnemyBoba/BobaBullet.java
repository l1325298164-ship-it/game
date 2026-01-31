package de.tum.cit.fop.maze.entities.enemy.EnemyBoba;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.entities.enemy.EnemyBullet;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;

/**
 * Enemy bullet with bouncing and popping behavior.
 * <p>
 * Boba bullets can bounce off walls once, wobble while flying,
 * and play a popping animation when colliding or reaching range.
 */
public class BobaBullet extends EnemyBullet {
    /**
     * State machine for boba bullet behavior.
     */
    public enum BobaState {
        FLYING, BOUNCING, POPPING
    }

    private BobaState state = BobaState.FLYING;

    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float targetScaleX = 1.0f;
    private float targetScaleY = 1.0f;

    private int bounceCount = 0;
    private final int MAX_BOUNCES = 1;
    private float rotation = 0f;
    private float rotationSpeed = 300f;

    private float wobbleTime = 0f;
    private boolean managedByEffectManager = false;
    private float popTimer = 0f;
    private final float POP_DURATION = 0.15f;
    /**
     * Creates a new boba bullet.
     *
     * @param x      initial x position
     * @param y      initial y position
     * @param dx     direction x component
     * @param dy     direction y component
     * @param damage damage dealt on hit
     */
    public BobaBullet(float x, float y, float dx, float dy, int damage) {
        super(x, y, dx, dy, damage);
        this.speed = 7f;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len != 0) {
            this.vx = (dx / len) * speed;
            this.vy = (dy / len) * speed;
        }
        this.rotation = MathUtils.random(0, 360);
    }
    /**
     * Updates bullet movement, collision, and state transitions.
     *
     * @param delta time elapsed since last frame
     * @param gm    active game manager
     */
    @Override
    public void update(float delta, GameManager gm) {
        if (!active) return;

        if (state == BobaState.POPPING) {
            updatePoppingState(delta);
            return;
        }

        float moveX = vx * delta;
        float moveY = vy * delta;
        float nextX = realX + moveX;
        float nextY = realY + moveY;
        int nextCellX = (int) nextX;
        int nextCellY = (int) nextY;

        if (gm.getMazeCell(nextCellX, nextCellY) == 0) {
            handleWallCollision(gm, nextCellX, nextCellY);
        } else {
            realX = nextX;
            realY = nextY;
            traveled += Math.sqrt(moveX * moveX + moveY * moveY);
        }

        this.x = (int) realX;
        this.y = (int) realY;

        if (traveled >= maxRange && state != BobaState.POPPING) {
            triggerPop();
        }

        for (Player p : gm.getPlayers()) {
            if (p == null || p.isDead()) continue;

            if (state != BobaState.POPPING && p.collidesWith(this)) {
                p.takeDamage(damage);
                triggerPop();
                break;
            }
        }


        updateVisuals(delta);
    }

    private void handleWallCollision(GameManager gm, int wallX, int wallY) {
        if (bounceCount >= MAX_BOUNCES) {
            triggerPop();
            return;
        }
        bounceCount++;
        state = BobaState.BOUNCING;

        boolean hitX = gm.getMazeCell((int) (realX + vx * 0.05f), (int) realY) == 0;
        boolean hitY = gm.getMazeCell((int) realX, (int) (realY + vy * 0.05f)) == 0;

        if (hitX) vx = -vx * 0.9f;
        if (hitY) vy = -vy * 0.9f;

        scaleX = 1.25f;
        scaleY = 0.8f;

        rotation += 180f;
    }

    private void updateVisuals(float delta) {
        wobbleTime += delta;
        rotation += rotationSpeed * delta;
        targetScaleX = 1.0f;
        targetScaleY = 1.0f;

        float currentSpeed = (float) Math.sqrt(vx * vx + vy * vy);
        if (state == BobaState.FLYING && currentSpeed > 1f) {
            targetScaleX = 1.0f - (currentSpeed * 0.01f);
            targetScaleY = 1.0f + (currentSpeed * 0.01f);
        }

        scaleX = scaleX + (targetScaleX - scaleX) * 15f * delta;
        scaleY = scaleY + (targetScaleY - scaleY) * 15f * delta;
    }

    private void updatePoppingState(float delta) {
        popTimer += delta;
        float progress = popTimer / POP_DURATION;
        scaleX = 1.0f + progress * 0.5f;
        scaleY = 1.0f + progress * 0.5f;
        if (popTimer >= POP_DURATION) {
            active = false;
        }
    }
    /**
     * Triggers the popping state and starts the pop animation.
     */
    public void triggerPop() {
        if (state == BobaState.POPPING) return;
        state = BobaState.POPPING;
        popTimer = 0f;
    }
    /**
     * Draws the bullet sprite.
     * <p>
     * Rendering is handled externally by an effect manager.
     *
     * @param batch sprite batch used for rendering
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
    }

    /**
     * @return current horizontal scale of the bullet
     */
    public float getScaleX() { return scaleX; }
    /**
     * @return current vertical scale of the bullet
     */
    public float getScaleY() { return scaleY; }
    /**
     * @return current rotation angle in degrees
     */
    public float getRotation() { return rotation; }
    /**
     * @return horizontal wobble offset for rendering
     */
    public float getWobbleOffsetX() {
        return (float) Math.sin(wobbleTime * 15f) * 0.05f * GameConstants.CELL_SIZE * scaleX;
    }

    /**
     * @return vertical wobble offset for rendering
     */
    public float getWobbleOffsetY() {
        return (float) Math.cos(wobbleTime * 12f) * 0.05f * GameConstants.CELL_SIZE * scaleY;
    }
    /**
     * @return {@code true} if the bullet is currently popping
     */
    public boolean isPopping() { return state == BobaState.POPPING; }
    /**
     * @return whether this bullet is managed by an external effect manager
     */
    public boolean isManagedByEffectManager() { return managedByEffectManager; }
    /**
     * Sets whether this bullet is managed by an external effect manager.
     *
     * @param managed managed state
     */
    public void setManagedByEffectManager(boolean managed) { this.managedByEffectManager = managed; }
}