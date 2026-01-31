package de.tum.cit.fop.maze.entities.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.fop.maze.entities.GameObject;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.utils.TextureManager;
/**
 * Base class for enemy projectiles.
 * <p>
 * Handles linear movement, range limitation, wall collision,
 * and player damage. Subclasses may extend this behavior
 * with additional states or visual effects.
 */
public class EnemyBullet extends GameObject {

    protected float realX;
    protected float realY;
    protected float vx, vy;
    protected float speed = 6f;
    protected float traveled = 0f;
    protected float maxRange = 8f;
    protected int damage;
    /**
     * Creates a new enemy bullet.
     *
     * @param x      initial x position in grid coordinates
     * @param y      initial y position in grid coordinates
     * @param dx     direction x component
     * @param dy     direction y component
     * @param damage damage dealt on hit
     */
    public EnemyBullet(float x, float y, float dx, float dy, int damage) {
        super((int) x, (int) y);
        this.realX = x;
        this.realY = y;
        this.damage = damage;

        float len = (float) Math.sqrt(dx*dx + dy*dy);
        if (len != 0) {
            vx = (dx / len) * speed;
            vy = (dy / len) * speed;
        } else {
            vx = speed;
            vy = 0;
        }
    }
    /**
     * Updates bullet movement and collision.
     * <p>
     * The bullet is deactivated when it hits a wall,
     * exceeds its maximum range, or collides with a player.
     *
     * @param delta time elapsed since last frame
     * @param gm    active game manager
     */
    public void update(float delta, GameManager gm) {
        float moveX = vx * delta;
        float moveY = vy * delta;

        realX += moveX;
        realY += moveY;
        traveled += Math.sqrt(moveX * moveX + moveY * moveY);

        this.x = (int) realX;
        this.y = (int) realY;

        if (gm.getMazeCell(x, y) == 0) {
            active = false;
            return;
        }


        if (traveled >= maxRange) {
            active = false;
            return;
        }

        for (Player p : gm.getPlayers()) {
            if (p == null || p.isDead()) continue;

            if (p.collidesWith(this)) {
                p.takeDamage(damage);
                active = false;
                return;
            }
        }
    }

    /**
     * Enemy bullets do not render debug shapes by default.
     *
     * @param shapeRenderer shape renderer
     */
    @Override
    public void drawShape(ShapeRenderer shapeRenderer) {
        // 默认不绘制形状
    }

    /**
     * Draws the bullet sprite.
     *
     * @param batch sprite batch used for rendering
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
        if (!active) return;

        batch.setColor(1f, 0.3f, 0.3f, 1f);
        batch.draw(
                TextureManager.getInstance().getColorTexture(Color.RED),
                realX * GameConstants.CELL_SIZE,
                realY * GameConstants.CELL_SIZE,
                GameConstants.CELL_SIZE * 0.3f,
                GameConstants.CELL_SIZE * 0.3f
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * Enemy bullets do not block movement.
     *
     * @return {@code true}
     */
    @Override
    public boolean isPassable() {
        return true;
    }
    /**
     * @return render type used for this bullet
     */
    @Override
    public RenderType getRenderType() {
        return RenderType.SPRITE;
    }

    /**
     * @return current real x-position
     */
    public float getRealX() { return realX; }
    /**
     * @return current real y-position
     */
    public float getRealY() { return realY; }
}