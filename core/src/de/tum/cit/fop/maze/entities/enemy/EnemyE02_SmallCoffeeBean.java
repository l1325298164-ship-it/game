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
import de.tum.cit.fop.maze.utils.Logger;
/**
 * Enemy type E02: Small Coffee Bean.
 * <p>
 * A fast-moving enemy that uses continuous movement instead of
 * grid-based steps. It deals damage through close-range collision
 * and exhibits rotating, wobbling visuals.
 */
public class EnemyE02_SmallCoffeeBean extends Enemy {

    private float targetWorldX;
    private float targetWorldY;
    private boolean isMovingContinuously = false;
    private float moveSpeedMultiplier = 1.0f;

    private float rotation = 0f;
    private float rotationSpeed = 180f; // 度/秒
    public int getCollisionDamage() {
        return collisionDamage;
    }



    /**
     * Returns the attack sound associated with this enemy.
     *
     * @return attack sound type
     */
    @Override
    protected AudioType getAttackSound() {
        return AudioType.ENEMY_ATTACK_E02;
    }

    private Animation<TextureRegion> anim;
    private float animTime = 0f;

    /**
     * Creates a Small Coffee Bean enemy at the given position.
     *
     * @param x initial grid x-position
     * @param y initial grid y-position
     */
    public EnemyE02_SmallCoffeeBean(int x, int y) {
        super(x, y);
        size = 0.8f;

        hp = 3;
        collisionDamage = 5;

        moveSpeed = 6.0f;
        moveInterval = 0.2f;
        changeDirInterval = 0.2f;
        this.worldX = x;
        this.worldY = y;
        this.targetWorldX = x;
        this.targetWorldY = y;
        updateTexture();
    }

    /**
     * Applies damage to the enemy and plays a
     * type-specific hit sound.
     *
     * @param dmg damage amount
     */
    @Override
    public void takeDamage(int dmg) {
        int actualDamage = dmg;
        AudioManager.getInstance().play(AudioType.ENEMY_ATTACKED_E02);

        super.takeDamage(actualDamage);
    }
    /**
     * Small Coffee Bean does not render debug shapes.
     *
     * @param shapeRenderer shape renderer
     */
    @Override
    public void drawShape(ShapeRenderer shapeRenderer) {

    }


    /**
     * Checks collision with a player using a circular
     * distance-based test.
     *
     * @param p player to test against
     * @return {@code true} if the player collides with this enemy
     */
    public boolean collidesWithPlayer(Player p) {
        float dx = (p.getX() + 0.5f) - worldX;
        float dy = (p.getY() + 0.5f) - worldY;

        float distSq = dx * dx + dy * dy;

        float radius = 0.6f;
        return distSq <= radius * radius;
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
        if (!active) return;

        if (hasSingleAnimation()) {
            drawSingleAnimation(batch);
            return;
        }

        if (hasFourDirectionAnimation()) {
            drawAnimated(batch);
            return;
        }

        if (texture != null) {
            float scale = size;
            float drawSize = GameConstants.CELL_SIZE * scale;

            float drawX = worldX * GameConstants.CELL_SIZE +
                    (GameConstants.CELL_SIZE - drawSize) / 2f;
            float drawY = worldY * GameConstants.CELL_SIZE +
                    (GameConstants.CELL_SIZE - drawSize) / 2f;

            batch.draw(texture, drawX, drawY, drawSize, drawSize);
        }
    }
    @Override
    protected void drawSingleAnimation(SpriteBatch batch) {
        if (singleAnim == null) {
            super.drawSingleAnimation(batch);
            return;
        }

        TextureRegion frame = singleAnim.getKeyFrame(animTime, true);

        if (frame == null) {
            super.drawSingleAnimation(batch);
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

        batch.draw(frame, drawX, drawY,
                drawW / 2f, drawH / 2f,
                drawW, drawH,
                1f, 1f,
                rotation);
        if (isHitFlash) {
            batch.setColor(1, 1, 1, 1);
        }
    }



    /**
     * Loads and initializes animation or texture
     * resources for this enemy type.
     */
    @Override
    protected void updateTexture() {

        try {
            TextureAtlas atlas = textureManager.getEnemyE02Atla();

            if (atlas == null) {
                texture = textureManager.getEnemy2Texture();
                singleAnim = null;
            } else {
                var regions = atlas.findRegions("E02_anim");

                if (regions == null || regions.size == 0) {
                    String[] possibleNames = {"E02", "coffee", "bean", "anim"};
                    for (String name : possibleNames) {
                        regions = atlas.findRegions(name);
                        if (regions != null && regions.size > 0) {
                            break;
                        }
                    }
                }

                if (regions != null && regions.size > 0) {

                    singleAnim = new Animation<>(
                            0.1f,  // 帧间隔（秒）
                            regions,
                            Animation.PlayMode.LOOP
                    );


                    for (int i = 0; i < Math.min(regions.size, 3); i++) {
                        Logger.debug("  frame " + i + ": " +
                                regions.get(i).getRegionWidth() + "x" +
                                regions.get(i).getRegionHeight());
                    }

                    texture = null;
                } else {
                    texture = textureManager.getEnemy2Texture();
                    singleAnim = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            texture = textureManager.getEnemy2Texture();
            singleAnim = null;
        }

        needsTextureUpdate = false;
    }
    /**
     * Updates continuous movement, rotation, and
     * collision behavior for this enemy.
     *
     * @param delta time elapsed since last frame
     * @param gm    active game manager
     */
    @Override
    public void update(float delta, GameManager gm) {
        if (!active) return;
        rotation += rotationSpeed * delta;
        if (rotation > 360f) rotation -= 360f;
        animTime += delta;

        updateHitFlash(delta);
        updateContinuousMovement(delta, gm);}

    private void updateContinuousMovement(float delta, GameManager gm) {
        if (isMovingContinuously) {
            updateContinuousPosition(delta);
        }

        if (!isMovingContinuously || hasReachedTarget()) {
            chooseNewDirection(gm);
        }
    }


    private void updateContinuousPosition(float delta) {
        if (!isMovingContinuously) return;

        float dx = targetWorldX - worldX;
        float dy = targetWorldY - worldY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.01f) {
            worldX = targetWorldX;
            worldY = targetWorldY;
            isMovingContinuously = false;

            x = Math.round(worldX);
            y = Math.round(worldY);
            return;
        }

        float moveStep = moveSpeed * delta * moveSpeedMultiplier;

        if (moveStep >= distance) {
            worldX = targetWorldX;
            worldY = targetWorldY;
            isMovingContinuously = false;

            x = Math.round(worldX);
            y = Math.round(worldY);
        } else {
            worldX += (dx / distance) * moveStep;
            worldY += (dy / distance) * moveStep;
        }
    }

    private boolean hasReachedTarget() {
        float dx = targetWorldX - worldX;
        float dy = targetWorldY - worldY;
        return Math.sqrt(dx * dx + dy * dy) < 0.01f;
    }

    private void chooseNewDirection(GameManager gm) {
        for (int attempt = 0; attempt < 4; attempt++) {
            int[] dir = CARDINAL_DIRS[MathUtils.random(0, CARDINAL_DIRS.length - 1)];
            float newTargetX = worldX + dir[0];
            float newTargetY = worldY + dir[1];

            int gridX = Math.round(newTargetX);
            int gridY = Math.round(newTargetY);

            if (gm.isEnemyValidMove(gridX, gridY)) {
                targetWorldX = newTargetX;
                targetWorldY = newTargetY;
                isMovingContinuously = true;

                moveSpeedMultiplier = MathUtils.random(0.8f, 1.2f);

                x = Math.round(worldX);
                y = Math.round(worldY);


                return;
            }
        }

        isMovingContinuously = false;
    }

    @Override
    protected void startMoveTo(int nx, int ny) {
    }

    @Override
    protected void moveContinuously(float delta) {
    }

    @Override
    protected void tryMoveRandom(float delta, GameManager gm) {
    }

}
