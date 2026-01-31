package de.tum.cit.fop.maze.entities.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
/**
 * EnemyE04 – Crystallized Caramel Shell.
 *
 * <p>
 * A large 2×2 enemy that behaves more like a moving obstacle than a
 * traditional grid-based enemy.
 * </p>
 *
 * <p>
 * Design characteristics:
 * <ul>
 *   <li>Occupies a 2×2 grid area at all times.</li>
 *   <li>Uses its own movement and collision logic instead of the base Enemy movement.</li>
 *   <li>Blocks player movement and cannot be passed through.</li>
 *   <li>Receives greatly reduced damage from normal attacks.</li>
 *   <li>Can only be destroyed instantly by being hit with a Dash.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Important implementation notes:
 * <ul>
 *   <li>The enemy's logical position (x, y) represents the bottom-left
 *       corner of its 2×2 footprint.</li>
 *   <li>Movement validation must ensure all occupied cells are walkable.</li>
 *   <li>This class intentionally does NOT rely on the Enemy base class
 *       pathfinding or movement timing.</li>
 * </ul>
 * </p>
 */

public class EnemyE04_CrystallizedCaramelShell extends Enemy {

    private boolean isShellBroken = false;
    private float shellShakeTimer = 0f;
    private static final float SHELL_SHAKE_DURATION = 0.5f;
    private float shellBreakTimer = 0f;
    private static final float SHELL_BREAK_DURATION = 0.8f;
    private boolean hasEnteredAttack = false;

    private static final int GRID_SIZE = 2;
    private float sizeMultiplier = 2.0f;

    private float crystalGlowTimer = 0f;
    private float crystalRotation = 0f;
    private static final float CRYSTAL_ROTATION_SPEED = 45f;
    @Override
    protected AudioType getAttackSound() {
        return AudioType.ENEMY_ATTACK_E04;
    }

    /**
     * Creates a new Crystallized Caramel Shell enemy at the given grid position.
     *
     * @param x grid X coordinate of the bottom-left corner
     * @param y grid Y coordinate of the bottom-left corner
     */
    public EnemyE04_CrystallizedCaramelShell(int x, int y) {
        super(x, y);

        size = 2.0f;

        hp = 150;
        collisionDamage = 8;
        attack = 8;

        moveSpeed = 1.0f;
        moveInterval = 0.8f;
        changeDirInterval = 1.8f;
        detectRange = 8f;

        this.worldX = x;
        this.worldY = y;

        updateTexture();

    }

    private void tryMoveRandomLarge(GameManager gm) {
        int[] dir = CARDINAL_DIRS[MathUtils.random(0, CARDINAL_DIRS.length - 1)];
        int nx = x + dir[0];
        int ny = y + dir[1];
        if (canMoveTo(nx, ny, gm)) {
            startMoveTo(nx, ny);
        }
    }
    /**
     * Checks whether this enemy occupies the given grid cell.
     *
     * <p>
     * Since this enemy occupies a 2×2 area, this method returns true
     * if the specified cell lies within that footprint.
     * </p>
     *
     * @param cellX grid X coordinate
     * @param cellY grid Y coordinate
     * @return true if the cell is covered by this enemy
     */
    public boolean occupiesCell(int cellX, int cellY) {
        if (!active) return false;
        return (cellX >= x && cellX < x + GRID_SIZE &&
                cellY >= y && cellY < y + GRID_SIZE);
    }
    /**
     * Checks whether the enemy can move its entire 2×2 body to the target position.
     *
     * <p>
     * All cells covered by the 2×2 area must be valid enemy tiles and must not
     * overlap with another E04 enemy.
     * </p>
     *
     * @param targetX target grid X (bottom-left)
     * @param targetY target grid Y (bottom-left)
     * @param gm      game manager for collision and map queries
     * @return true if movement is allowed
     */
    protected boolean canMoveTo(int targetX, int targetY, GameManager gm) {
        for (int dx = 0; dx < GRID_SIZE; dx++) {
            for (int dy = 0; dy < GRID_SIZE; dy++) {
                int checkX = targetX + dx;
                int checkY = targetY + dy;

                if (!gm.isEnemyValidMove(checkX, checkY)) {
                    return false;
                }
                for (Enemy other : gm.getEnemies()) {
                    if (other != this && other instanceof EnemyE04_CrystallizedCaramelShell) {
                        if (other.occupiesCell(checkX, checkY)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Applies damage to the shell.
     *
     * <p>
     * Normal attacks deal heavily reduced damage.
     * If the shell was hit by a Dash, it will enter the shell-break state
     * and be destroyed shortly after.
     * </p>
     *
     * @param dmg incoming damage value
     */
    @Override
    public void takeDamage(int dmg) {
        if (isHitByDash()) {
            dieByShellBreak();
            resetDashHit();
            return;
        }

        int reduced = Math.max(1, dmg / 8);
        super.takeDamage(reduced);

        isHitFlash = true;
        hitFlashTimer = 0f;
        shellShakeTimer = SHELL_SHAKE_DURATION;

    }
    /**
     * Triggers the shell break sequence.
     *
     * <p>
     * Once triggered, the enemy becomes non-interactive and will be
     * destroyed after the shell break animation finishes.
     * </p>
     */
    private void dieByShellBreak() {
        if (isShellBroken) return;

        isShellBroken = true;
        shellBreakTimer = 0f;

    }

    /**
     * Updates the enemy state.
     *
     * <p>
     * This update loop bypasses most base Enemy movement logic and implements
     * custom behavior suitable for a large, slow-moving obstacle-like enemy.
     * </p>
     *
     * @param delta time since last frame
     * @param gm    game manager reference
     */
    @Override
    public void update(float delta, GameManager gm) {

        animTime += delta;

        if (isShellBroken) {
            shellBreakTimer += delta;

            if (shellBreakTimer >= SHELL_BREAK_DURATION) {
                isShellBroken = false;

                super.takeDamage(this.hp + 9999);

            }
            return;
        }

        if (!active) return;

        if (shellShakeTimer > 0f) {
            shellShakeTimer -= delta;
        }

        updateHitFlash(delta);

        Player target = gm.getNearestAlivePlayer(x + GRID_SIZE / 2, y + GRID_SIZE / 2);

        if (target != null) {
            float dist = distanceTo(target);
            if (dist <= detectRange) {
                if (!hasEnteredAttack) {
                    hasEnteredAttack = true;
                    AudioManager.getInstance().play(AudioType.ENEMY_ATTACK_E04);
                }
                chaseTarget(gm, target);
            } else {
                hasEnteredAttack = false;
                tryMoveRandomLarge(gm);
            }
        } else {
            tryMoveRandomLarge(gm);
        }


        if (isMoving) {
            float dx = targetX - worldX;
            float dy = targetY - worldY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 0.01f) {
                worldX = targetX;
                worldY = targetY;
                this.x = (int) worldX;
                this.y = (int) worldY;
                isMoving = false;
            } else {
                float step = moveSpeed * delta;
                worldX += (dx / distance) * step;
                worldY += (dy / distance) * step;
            }


        }
    }

    @Override
    public boolean isPassable() {
        return false;
    }

    @Override
    public boolean isInteractable() {
        return false;
    }


    @Override
    public RenderType getRenderType() {
        return RenderType.SPRITE;
    }

    @Override
    protected void updateTexture() {
        try {
            TextureAtlas atlas = textureManager.getEnemyE04Atlas();
            if (atlas == null) {
                texture = textureManager.getEnemy4ShellTexture();
                singleAnim = null;
            } else {
                var regions = atlas.findRegions("E04");
                if (regions == null || regions.size == 0) {
                    regions = atlas.findRegions("shell");
                }

                if (regions != null && regions.size > 0) {
                    singleAnim = new Animation<>(0.3f, regions, Animation.PlayMode.LOOP);
                    texture = null;
                } else {
                    texture = textureManager.getEnemy4ShellTexture();
                    singleAnim = null;
                }
            }
        } catch (Exception e) {
            texture = textureManager.getEnemy4ShellTexture();
            singleAnim = null;
        }
        needsTextureUpdate = false;
    }

    @Override
    public void drawSprite(SpriteBatch batch) {
        if (!active && !isShellBroken) return;

        if (isShellBroken) {
            drawShellBreakEffect(batch);
            return;
        }

        super.drawSprite(batch);

        if (singleAnim != null && active) {
            drawCrystalGlowEffect(batch);
        }
    }

    private void drawCrystalGlowEffect(SpriteBatch batch) {
        if (singleAnim == null) return;
        crystalGlowTimer += 0.016f;
        crystalRotation += CRYSTAL_ROTATION_SPEED * 0.016f;

        float glowAlpha = 0.3f + 0.2f * (float)Math.sin(crystalGlowTimer * 2f);
        TextureRegion frame = singleAnim.getKeyFrame(animTime, true);
        if (frame == null) return;

        float drawW = GameConstants.CELL_SIZE * sizeMultiplier;
        float drawH = GameConstants.CELL_SIZE * sizeMultiplier;
        float drawX = worldX * GameConstants.CELL_SIZE;
        float drawY = worldY * GameConstants.CELL_SIZE;

        batch.setColor(0.6f, 0.8f, 1.0f, glowAlpha);
        batch.draw(frame, drawX, drawY, drawW / 2f, drawH / 2f, drawW, drawH, 1f, 1f, crystalRotation);
        batch.setColor(1, 1, 1, 1);
    }

    private void drawShellBreakEffect(SpriteBatch batch) {
        if (singleAnim == null) return;

        float breakProgress = shellBreakTimer / SHELL_BREAK_DURATION;
        TextureRegion frame = singleAnim.getKeyFrame(0, true);
        if (frame == null) return;

        float breakScale = 1.0f - breakProgress * 0.5f;
        float drawW = GameConstants.CELL_SIZE * sizeMultiplier * breakScale;
        float drawH = GameConstants.CELL_SIZE * sizeMultiplier * breakScale;
        float drawX = worldX * GameConstants.CELL_SIZE + (GameConstants.CELL_SIZE * sizeMultiplier - drawW) / 2f;
        float drawY = worldY * GameConstants.CELL_SIZE + (GameConstants.CELL_SIZE * sizeMultiplier - drawH) / 2f;

        float flashAlpha = 0.8f * (1.0f - breakProgress);
        batch.setColor(1.0f, 1.0f, 1.0f, flashAlpha);

        float breakRotation = breakProgress * 360f;
        batch.draw(frame, drawX, drawY, drawW / 2f, drawH / 2f, drawW, drawH, 1f, 1f, breakRotation);
        batch.setColor(1, 1, 1, 1);
    }

    @Override
    public void drawShape(ShapeRenderer shapeRenderer) {}

    private void chaseTarget(GameManager gm, Player target) {
        if (isMoving) return;
        int dx = Integer.compare(target.getX(), x);
        int dy = Integer.compare(target.getY(), y);
        if (Math.abs(dx) > Math.abs(dy)) dy = 0;
        else dx = 0;
        int nx = x + dx;
        int ny = y + dy;
        if (canMoveTo(nx, ny, gm)) startMoveTo(nx, ny);
    }
    @Override
    protected void startMoveTo(int nx, int ny) {
        targetX = nx;
        targetY = ny;
        isMoving = true;
    }

    @Override
    protected void moveContinuously(float delta) {

    }
    private float distanceTo(Player p) {
        float centerX = x + GRID_SIZE / 2f;
        float centerY = y + GRID_SIZE / 2f;
        float dx = p.getX() + 0.5f - centerX;
        float dy = p.getY() + 0.5f - centerY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    protected void drawSingleAnimation(SpriteBatch batch) {
        if (singleAnim == null) {
            if (texture != null) {
                float drawW = GameConstants.CELL_SIZE * sizeMultiplier;
                float drawH = GameConstants.CELL_SIZE * sizeMultiplier;
                float drawX = worldX * GameConstants.CELL_SIZE;
                float drawY = worldY * GameConstants.CELL_SIZE;
                if (isHitFlash) {
                    float flashAlpha = 0.5f + 0.5f * (float) Math.sin(hitFlashTimer * 20f);
                    batch.setColor(1, 1, 1, flashAlpha);
                }
                batch.draw(texture, drawX, drawY, drawW, drawH);
                if (isHitFlash) batch.setColor(1, 1, 1, 1);
            }
            return;
        }

        float shakeOffsetX = 0f;
        float shakeOffsetY = 0f;
        if (shellShakeTimer > 0f) {
            float shakeIntensity = shellShakeTimer / SHELL_SHAKE_DURATION;
            shakeOffsetX = (MathUtils.random() - 0.5f) * 6f * shakeIntensity;
            shakeOffsetY = (MathUtils.random() - 0.5f) * 6f * shakeIntensity;
        }

        TextureRegion frame = singleAnim.getKeyFrame(animTime, true);
        if (frame == null) return;
        float drawW = GameConstants.CELL_SIZE * sizeMultiplier;
        float drawH = GameConstants.CELL_SIZE * sizeMultiplier;
        float drawX = worldX * GameConstants.CELL_SIZE + shakeOffsetX;
        float drawY = worldY * GameConstants.CELL_SIZE + shakeOffsetY;

        if (isHitFlash) {
            float flashAlpha = 0.5f + 0.5f * (float) Math.sin(hitFlashTimer * 20f);
            batch.setColor(1, 1, 1, flashAlpha);
        }
        batch.draw(frame, drawX, drawY, drawW, drawH);
        if (isHitFlash) batch.setColor(1, 1, 1, 1);
    }

    /**
     * Checks whether a world-space position lies within the enemy's 2×2 body.
     *
     * <p>
     * Used primarily for Dash hit detection, where continuous world coordinates
     * are involved instead of grid-based collision.
     * </p>
     *
     * @param wx world X coordinate
     * @param wy world Y coordinate
     * @return true if the point lies inside the enemy's body
     */
    public boolean occupiesWorld(float wx, float wy) {
        float left   = worldX;
        float right  = worldX + GRID_SIZE;
        float bottom = worldY;
        float top    = worldY + GRID_SIZE;

        return wx >= left && wx <= right &&
                wy >= bottom && wy <= top;
    }

    public float getWorldX() { return worldX; }
    public float getWorldY() { return worldY; }
    public int getRightBound() { return x + GRID_SIZE - 1; }
    public int getTopBound() { return y + GRID_SIZE - 1; }
}