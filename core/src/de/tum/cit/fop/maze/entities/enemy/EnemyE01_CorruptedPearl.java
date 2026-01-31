package de.tum.cit.fop.maze.entities.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.entities.enemy.EnemyBoba.BobaBullet;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.utils.Logger;
/**
 * Enemy type E01: Corrupted Pearl.
 * <p>
 * A ranged enemy that patrols the maze and switches to
 * attack mode when a player enters its detection range.
 * Uses boba projectiles to attack from mid-range.
 */
public class EnemyE01_CorruptedPearl extends Enemy {

    private EnemyState state = EnemyState.PATROL;

    private float shootCooldown = 0f;
    private static final float SHOOT_INTERVAL = 1.2f;
    private boolean isAttacking = false;
    private float attackTimer = 0f;
    protected Animation<TextureRegion> anim;

    private static final float ATTACK_WINDUP = 0.25f;
    private static final float ATTACK_FLASH = 0.08f;


    /**
     * Returns the attack sound associated with this enemy.
     *
     * @return attack sound type
     */
    @Override
    protected AudioType getAttackSound() {
        return AudioType.ENEMY_ATTACK_E01;
    }
    /**
     * Creates a Corrupted Pearl enemy at the given position.
     *
     * @param x initial grid x-position
     * @param y initial grid y-position
     */
    public EnemyE01_CorruptedPearl(int x, int y) {
        super(x, y);

        hp = 5;
        collisionDamage = 5;
        attack = 10;

        moveSpeed = 2.5f;
        moveInterval = 0.35f;
        changeDirInterval = 2.0f;
        detectRange = 6f;

        updateTexture();
        Logger.debug("E01 constructed, needsTextureUpdate=" + needsTextureUpdate);
    }
    /**
     * Applies damage to the enemy and plays a
     * type-specific hit sound.
     *
     * @param dmg damage amount
     */
    @Override
    public void takeDamage(int dmg) {
        super.takeDamage(dmg);
        AudioManager.getInstance().play(AudioType.ENEMY_ATTACKED_E01);


    }


    /**
     * Corrupted Pearl does not render debug shapes.
     *
     * @param shapeRenderer shape renderer
     */
    @Override
    public void drawShape(ShapeRenderer shapeRenderer) {
    }

    /**
     * @return render type used by this enemy
     */
    @Override
    public RenderType getRenderType() {
        return RenderType.SPRITE;
    }

    /**
     * Draws the enemy sprite or animation.
     *
     * @param batch sprite batch used for rendering
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
        super.drawSprite(batch);
    }




    /**
     * Loads and initializes directional animations
     * for this enemy type.
     */
    @Override
    protected void updateTexture() {
        Logger.debug("E01 updateTexture CALLED");
        TextureAtlas frontAtlas =
                textureManager.getEnemy1AtlasFront();
        Logger.debug("E01 atlas = " + frontAtlas);
        frontAnim = new Animation<>(
                0.12f,
                frontAtlas.findRegions("E01_front"),
                Animation.PlayMode.LOOP
        );

        TextureAtlas backAtlas =
                textureManager.getEnemy1AtlasBack();
        Logger.debug("E01 atlas = " + backAtlas);
        backAnim = new Animation<>(
                0.12f,
                backAtlas.findRegions("E01_back"),
                Animation.PlayMode.LOOP
        );

        TextureAtlas sideAtlas =
                textureManager.getEnemy1AtlasRL();
        Logger.debug("E01 atlas = " + sideAtlas);
        leftAnim = new Animation<>(
                0.12f,
                sideAtlas.findRegions("E01_left"),
                Animation.PlayMode.LOOP
        );
        rightAnim = new Animation<>(
                0.12f,
                sideAtlas.findRegions("E01_right"),
                Animation.PlayMode.LOOP
        );

        needsTextureUpdate = false;
    }

    /**
     * Updates enemy behavior, including patrol,
     * target tracking, and ranged attacks.
     *
     * @param delta time elapsed since last frame
     * @param gm    active game manager
     */
    @Override
    public void update(float delta, GameManager gm) {

        if (isMoving) {
            stateTime += delta;
        } else {
            stateTime = 0f;
        }

        if (!active) return;

        updateHitFlash(delta);

        Player target = gm.getNearestAlivePlayer(x, y);
        if (target == null) return;

        float dist = distanceTo(target);

        shootCooldown -= delta;

        state = (dist <= detectRange)
                ? EnemyState.ATTACK
                : EnemyState.PATROL;

        if (isAttacking) {
            attackTimer += delta;

            if (attackTimer >= ATTACK_WINDUP) {
                shootAt(target, gm);
                AudioManager.getInstance().play(AudioType.ENEMY_ATTACK_E01);
                shootCooldown = SHOOT_INTERVAL;
                attackTimer = -ATTACK_FLASH;
            }

            if (attackTimer >= 0f) {
                isAttacking = false;
                attackTimer = 0f;
            }

            return;
        }

        switch (state) {
            case PATROL -> patrol(delta, gm);
            case ATTACK -> combat(delta, gm, target, dist);
        }

        moveContinuously(delta);
    }


    private void patrol(float delta, GameManager gm) {
        tryMoveRandom(delta, gm);
    }

    private void combat(float delta, GameManager gm, Player target, float dist) {

        float idealDistance = detectRange * 0.5f;

        if (shootCooldown <= 0f && !isAttacking) {
            isAttacking = true;
            attackTimer = 0f;
        }

        if (!isMoving) {
            int dx = Integer.compare(target.getX(), x);
            int dy = Integer.compare(target.getY(), y);

            if (Math.abs(dx) > Math.abs(dy)) {
                dy = 0;
            } else {
                dx = 0;
            }

            int nx = x + dx;
            int ny = y + dy;

            if (dist > idealDistance + 0.5f && gm.isEnemyValidMove(nx, ny)) {
                startMoveTo(nx, ny);
            } else if (dist < idealDistance - 0.5f) {
                int bx = x - dx;
                int by = y - dy;
                if (gm.isEnemyValidMove(bx, by)) {
                    startMoveTo(bx, by);
                }
            }
        }

        if (shootCooldown <= 0f) {
            shootAt(target, gm);
            shootCooldown = SHOOT_INTERVAL;
        }
    }


    private void shootAt(Player target, GameManager gm) {
        float dx = target.getX() - x;
        float dy = target.getY() - y;

        BobaBullet bullet = new BobaBullet(
                x + 0.5f, y + 0.5f, dx, dy, attack
        );

        gm.spawnProjectile(bullet);
    }


    private float distanceTo(Player p) {
        float dx = p.getX() - x;
        float dy = p.getY() - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
