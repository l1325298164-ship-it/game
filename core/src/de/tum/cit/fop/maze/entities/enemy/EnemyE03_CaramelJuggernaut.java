package de.tum.cit.fop.maze.entities.enemy;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.utils.Logger;
/**
 * Enemy type E03: Caramel Juggernaut.
 * <p>
 * A heavy enemy with high health and close-range area attacks.
 * Becomes aggressive when players enter its aggro range and
 * periodically deals area-of-effect damage around itself.
 */
public class EnemyE03_CaramelJuggernaut extends Enemy {

    private EnemyState state = EnemyState.IDLE;
    private EnemyState lastState = EnemyState.IDLE;

    private boolean isAggroed = false;
    private static final float AGGRO_TRIGGER_RANGE = 5.0f;


    private float aoeCooldown = 0f;
    private static final float AOE_INTERVAL = 1.5f;
    private static final int AOE_DAMAGE = 10;

    /**
     * Returns the attack sound associated with this enemy.
     *
     * @return attack sound type
     */
    @Override
    protected AudioType getAttackSound() {
        return AudioType.ENEMY_ATTACK_E03;
    }

    private Texture aoeTexture;
    private Texture redCircleTexture;
    private boolean isAoeActive = false;
    private float aoeAnimTime = 0f;
    private static final float AOE_ANIM_DURATION = 0.3f;
    /**
     * Creates a Caramel Juggernaut enemy at the given position.
     *
     * @param x initial grid x-position
     * @param y initial grid y-position
     */
    public EnemyE03_CaramelJuggernaut(int x, int y) {
        super(x, y);
        size = 1.8f;
        hp = 28;
        collisionDamage = 8;
        attack = AOE_DAMAGE;

        moveSpeed = 1.8f;
        moveInterval = 0.4f;
        changeDirInterval = 999f;
        detectRange = 7f;

        aoeTexture = textureManager.getEnemy3AOETexture();
        redCircleTexture = createRedCircleTexture();
        updateTexture();

        direction = Direction.DOWN;
    }

    private Texture createRedCircleTexture() {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        pixmap.setColor(1.0f, 0.2f, 0.2f, 1.0f);
        pixmap.fillCircle(size/2, size/2, size/2 - 2);


        pixmap.setColor(1.0f, 0.4f, 0.4f, 0.5f);
        pixmap.drawCircle(size/2, size/2, size/2 - 2);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }

    private void drawAoeEffect(SpriteBatch batch) {
        if (redCircleTexture == null) return;

        float pulse = (float) (Math.sin(aoeAnimTime * 20f) * 0.2f + 0.8f);
        float alpha = 0.7f * (1.0f - aoeAnimTime / AOE_ANIM_DURATION);


        batch.setColor(1.0f, 0.2f, 0.2f, alpha * 0.3f);
        float outerSize = size * 1.8f * pulse;
        float outerWidth = 2 * GameConstants.CELL_SIZE * outerSize;
        float outerHeight = 2 * GameConstants.CELL_SIZE * outerSize;
        float outerX = worldX * GameConstants.CELL_SIZE +
                (GameConstants.CELL_SIZE - outerWidth) / 2f;
        float outerY = worldY * GameConstants.CELL_SIZE +
                (GameConstants.CELL_SIZE - outerHeight) / 2f;
        batch.draw(redCircleTexture, outerX, outerY, outerWidth, outerHeight);

        batch.setColor(1.0f, 0.1f, 0.1f, alpha * 0.6f);
        float middleSize = size * 1.6f;
        float middleWidth = 2 * GameConstants.CELL_SIZE * middleSize;
        float middleHeight = 2 * GameConstants.CELL_SIZE * middleSize;
        float middleX = worldX * GameConstants.CELL_SIZE +
                (GameConstants.CELL_SIZE - middleWidth) / 2f;
        float middleY = worldY * GameConstants.CELL_SIZE +
                (GameConstants.CELL_SIZE - middleHeight) / 2f;
        batch.draw(redCircleTexture, middleX, middleY, middleWidth, middleHeight);

        batch.setColor(1.0f, 0.0f, 0.0f, alpha * 0.9f);
        float innerSize = size * 1.4f * (1.0f - pulse * 0.2f);
        float innerWidth = 2 * GameConstants.CELL_SIZE * innerSize;
        float innerHeight = 2 * GameConstants.CELL_SIZE * innerSize;
        float innerX = worldX * GameConstants.CELL_SIZE +
                (GameConstants.CELL_SIZE - innerWidth) / 2f;
        float innerY = worldY * GameConstants.CELL_SIZE +
                (GameConstants.CELL_SIZE - innerHeight) / 2f;
        batch.draw(redCircleTexture, innerX, innerY, innerWidth, innerHeight);

        batch.setColor(1, 1, 1, 1);
    }

    /**
     * Applies damage to the enemy and plays a
     * type-specific hit sound.
     *
     * @param dmg damage amount
     */
    @Override
    public void takeDamage(int dmg) {
        int armor = 0;
        int actualDamage = Math.max(0, dmg - armor);
        AudioManager.getInstance().play(AudioType.ENEMY_ATTACKED_E03);

        super.takeDamage(actualDamage);
    }
    /**
     * @return render type used by this enemy
     */
    @Override
    public RenderType getRenderType() {
        return RenderType.SPRITE;
    }
    /**
     * Draws the enemy sprite and active AOE effects.
     *
     * @param batch sprite batch used for rendering
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
        if (!active) return;

        super.drawSprite(batch);

        if (isAoeActive) {
            drawAoeEffect(batch);
        }
    }




    /**
     * Caramel Juggernaut does not render debug shapes.
     *
     * @param shapeRenderer shape renderer
     */
    @Override
    public void drawShape(ShapeRenderer shapeRenderer) {

    }
    /**
     * Loads and initializes directional animations
     * or fallback textures for this enemy type.
     */
    @Override
    protected void updateTexture() {

        try {
            TextureAtlas sideAtlas = textureManager.getEnemyE03Atla();
            TextureAtlas frontAtlas = textureManager.getEnemyE03Atla();
            TextureAtlas backAtlas = textureManager.getEnemyE03Atla();

            if (sideAtlas != null) {
                var leftRegions = sideAtlas.findRegions("E03_left");
                if (leftRegions != null && leftRegions.size > 0) {
                    leftAnim = new Animation<>(0.15f, leftRegions, Animation.PlayMode.LOOP);
                }

                var rightRegions = sideAtlas.findRegions("E03_right");
                if (rightRegions != null && rightRegions.size > 0) {
                    rightAnim = new Animation<>(0.15f, rightRegions, Animation.PlayMode.LOOP);
                }
            }

            if (frontAtlas != null) {
                var frontRegions = frontAtlas.findRegions("E03_front");
                if (frontRegions != null && frontRegions.size > 0) {
                    frontAnim = new Animation<>(0.15f, frontRegions, Animation.PlayMode.LOOP);
                }
            }

            if (backAtlas != null) {
                var backRegions = backAtlas.findRegions("E03_back");
                if (backRegions != null && backRegions.size > 0) {
                    backAnim = new Animation<>(0.15f, backRegions, Animation.PlayMode.LOOP);
                }
            }

            if (!hasAnimation()) {
                texture = textureManager.getEnemy3Texture();
            } else {
                texture = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            texture = textureManager.getEnemy3Texture();
        }

        needsTextureUpdate = false;
    }



    /**
     * Updates enemy behavior, including aggro detection,
     * slow pursuit, and area-of-effect attacks.
     *
     * @param delta time elapsed since last frame
     * @param gm    active game manager
     */
    @Override
    public void update(float delta, GameManager gm) {
        if (!active) return;

        if (state == EnemyState.IDLE) {
            stateTime += delta * 0.5f;
        }

        updateHitFlash(delta);

        if (isAoeActive) {
            aoeAnimTime += delta;
            if (aoeAnimTime >= AOE_ANIM_DURATION) {
                isAoeActive = false;
                aoeAnimTime = 0f;
            }
        }

        Player target = gm.getNearestAlivePlayer(x, y);
        if (target == null) {
            state = EnemyState.IDLE;
            lastState = EnemyState.IDLE;
            moveContinuously(delta);
            return;
        }

        float dist = distanceTo(target);

        if (dist <= AGGRO_TRIGGER_RANGE) {
            if (!isAggroed) {
                isAggroed = true;

                if (gm.getCombatEffectManager() != null) {
                    float ex = (this.x + 0.5f) * GameConstants.CELL_SIZE;
                    float ey = (this.y + 0.5f) * GameConstants.CELL_SIZE;
                    gm.getCombatEffectManager().spawnAggroPulse(ex, ey);
                }
            }
        }
        else if (dist > AGGRO_TRIGGER_RANGE * 2.5f) {
            isAggroed = false;
        }

        aoeCooldown -= delta;

        boolean canSeeTarget =
                dist <= detectRange &&
                        !hasWallBetween(target, gm);

        if (canSeeTarget) {
            state = EnemyState.ATTACK;
            updateDirection(target);
        } else {
            state = EnemyState.IDLE;
        }
        if (state == EnemyState.ATTACK && lastState != EnemyState.ATTACK) {
            AudioManager.getInstance().play(getAttackSound());
        }

        lastState = state;

        if (state == EnemyState.ATTACK) {
            chasePlayer(delta, gm, target);
            tryAOEAttack(target, gm);
        }

        moveContinuously(delta);
    }


    private void updateDirection(Player player) {
        int dx = player.getX() - x;
        int dy = player.getY() - y;

        if (Math.abs(dx) > Math.abs(dy)) {
            direction = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        } else {
            direction = (dy > 0) ? Direction.UP : Direction.DOWN;
        }
    }
    private boolean hasWallBetween(Player player, GameManager gm) {

        int px = player.getX();
        int py = player.getY();

        if (x == px) {
            int minY = Math.min(y, py);
            int maxY = Math.max(y, py);
            for (int ty = minY + 1; ty < maxY; ty++) {
                if (gm.getMazeCell(x, ty) == 0) {
                    return true;
                }
            }
        } else if (y == py) {
            int minX = Math.min(x, px);
            int maxX = Math.max(x, px);
            for (int tx = minX + 1; tx < maxX; tx++) {
                if (gm.getMazeCell(tx, y) == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private void tryAOEAttack(Player target, GameManager gm) {
        if (aoeCooldown > 0f) return;

        for (Player p : gm.getPlayers()) {
            if (p == null || p.isDead()) continue;

            if (isPlayerInAOE(p) && !hasWallBetween(p, gm)) {
                p.takeDamage(AOE_DAMAGE);

                isAoeActive = true;
                aoeAnimTime = 0f;
            }
        }

        aoeCooldown = AOE_INTERVAL;
    }



    private void chasePlayer(float delta, GameManager gm, Player player) {

        if (isMoving) return;

        int dx = Integer.compare(player.getX(), x);
        int dy = Integer.compare(player.getY(), y);

        if (Math.abs(dx) > Math.abs(dy)) {
            dy = 0;
        } else {
            dx = 0;
        }

        int nx = x + dx;
        int ny = y + dy;

        if (gm.isEnemyValidMove(nx, ny)) {
            startMoveTo(nx, ny);
        }
    }

    private boolean isPlayerInAOE(Player player) {
        int px = player.getX();
        int py = player.getY();

        return Math.abs(px - x) <= 1 &&
                Math.abs(py - y) <= 1;
    }


    private float distanceTo(Player p) {
        float dx = p.getX() - x;
        float dy = p.getY() - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    protected void drawAnimated(SpriteBatch batch) {
        if (!hasAnimation()) {
            drawStatic(batch);
            return;
        }

        Animation<TextureRegion> anim = getCurrentAnimation();
        if (anim == null) {
            drawStatic(batch);
            return;
        }

        TextureRegion frame = anim.getKeyFrame(stateTime, true);
        if (frame == null) {
            drawStatic(batch);
            return;
        }

        float baseScale = (float) GameConstants.CELL_SIZE / frame.getRegionHeight();
        float scale = baseScale * size;

        float drawW = frame.getRegionWidth() * scale;
        float drawH = frame.getRegionHeight() * scale;

        float drawX = worldX * GameConstants.CELL_SIZE +
                GameConstants.CELL_SIZE / 2f - drawW / 2f;
        float drawY = worldY * GameConstants.CELL_SIZE +
                GameConstants.CELL_SIZE / 2f - drawH / 2f;

        if (isHitFlash) {
            float flashAlpha = 0.5f + 0.5f * (float) Math.sin(hitFlashTimer * 20f);
            batch.setColor(1, 1, 1, flashAlpha);
        }

        batch.draw(frame, drawX, drawY, drawW, drawH);

        if (isHitFlash) {
            batch.setColor(1, 1, 1, 1);
        }
    }

    private Animation<TextureRegion> getCurrentAnimation() {
        switch (direction) {
            case LEFT -> {
                if (leftAnim != null) return leftAnim;
                if (rightAnim != null) return rightAnim;
            }
            case RIGHT -> {
                if (rightAnim != null) return rightAnim;
                if (leftAnim != null) return leftAnim;
            }
            case UP -> {
                if (backAnim != null) return backAnim;
                if (frontAnim != null) return frontAnim;
            }
            case DOWN -> {
                if (frontAnim != null) return frontAnim;
                if (backAnim != null) return backAnim;
            }
        }

        if (frontAnim != null) return frontAnim;
        if (backAnim != null) return backAnim;
        if (leftAnim != null) return leftAnim;
        if (rightAnim != null) return rightAnim;

        return null;
    }

}
