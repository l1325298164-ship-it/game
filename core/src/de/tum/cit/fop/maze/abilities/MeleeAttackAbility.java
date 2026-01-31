package de.tum.cit.fop.maze.abilities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.entities.enemy.Enemy;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * A close-range melee attack ability.
 * <p>
 * This ability performs a directional sword slash in front of the player,
 * dealing damage to enemies within a small tile-based area.
 * Damage is applied at a specific time offset during the attack animation.
 */
public class MeleeAttackAbility extends Ability {

    /** Base damage dealt by the attack. */
    private int baseDamage = 5;
    /** Additional damage gained per upgrade level. */
    private int damagePerLevel = 1;

    /** Time offset applied to the hit timing. */
    private float hitTimeOffset = 0f;
    /** Time in seconds after activation when damage is applied. */
    private static final float HIT_TIME = 0.12f;

    /** Cached reference to the game manager. */
    private GameManager gameManager;
    /** List of tile coordinates affected by the attack. */
    private final List<int[]> attackTiles = new ArrayList<>();
    /** Timer tracking time since attack activation. */
    private float attackTimer = 0f;

    /** Whether damage has already been applied during this attack. */
    private boolean damageDone = false;
    /** Debug color used to visualize attack tiles. */
    private static final Color DEBUG_COLOR = new Color(1f, 0.9f, 0f, 0.5f);
    /**
     * Creates a new melee attack ability with default parameters.
     */
    public MeleeAttackAbility() {
        super("Sword Slash", "Slash enemies in front of you", 0.8f, HIT_TIME);
        this.manaCost = 10;
    }

    @Override
    protected void onActivate(Player player, GameManager gameManager) {
        this.gameManager = gameManager;
        attackTimer = 0f;
        damageDone = false;

        calculateAttackTiles(player);
        player.startAttack();

        AudioManager.getInstance().play(AudioType.SKILL_SLASH);

        if (gameManager.getCombatEffectManager() != null) {
            float angle = 0;
            switch (player.getDirection()) {
                case RIGHT -> angle = 0;
                case UP -> angle = 90;
                case LEFT -> angle = 180;
                case DOWN -> angle = 270;
            }

            // ✅ [修复] 坐标居中 + 向前偏移
            // 之前直接用 getWorldX() 会导致特效生成在格子左下角
            float centerX = (player.getWorldX() + 0.5f) * GameConstants.CELL_SIZE;
            float centerY = (player.getWorldY() + 0.5f) * GameConstants.CELL_SIZE;

            // 向攻击方向稍微偏移 20 像素，增加打击感
            float offsetDist = 20f;
            float spawnX = centerX + MathUtils.cosDeg(angle) * offsetDist;
            float spawnY = centerY + MathUtils.sinDeg(angle) * offsetDist;

            gameManager.getCombatEffectManager().spawnSlash(
                    spawnX,
                    spawnY,
                    angle,
                    this.level
            );
        }
    }

    @Override
    protected boolean shouldStartCooldown() { return true; }

    @Override
    protected boolean shouldConsumeMana() { return manaCost > 0; }
    /**
     * Updates the melee attack ability.
     * <p>
     * Applies damage once when the hit timing threshold is reached.
     *
     * @param delta       time elapsed since last frame (seconds)
     * @param player      the player instance
     * @param gameManager the game manager
     */
    @Override
    public void update(float delta, Player player, GameManager gameManager) {
        super.update(delta, player, gameManager);
        if (gameManager == null) return;

        attackTimer += delta;

        if (!damageDone && attackTimer >= HIT_TIME - hitTimeOffset) {
            dealDamage(gameManager);
            damageDone = true;
        }
    }

    @Override
    protected boolean shouldBecomeActive() { return false; }

    private void calculateAttackTiles(Player player) {
        attackTiles.clear();

        int px = player.getX();
        int py = player.getY();

        attackTiles.add(new int[]{px, py});

        switch (player.getDirection()) {

            case UP -> {
                attackTiles.add(new int[]{px, py + 1});
                attackTiles.add(new int[]{px - 1, py + 1});
                attackTiles.add(new int[]{px + 1, py + 1});
            }

            case DOWN -> {
                attackTiles.add(new int[]{px, py - 1});
                attackTiles.add(new int[]{px - 1, py - 1});
                attackTiles.add(new int[]{px + 1, py - 1});
            }

            case LEFT -> {
                attackTiles.add(new int[]{px - 1, py});
                attackTiles.add(new int[]{px - 1, py - 1});
                attackTiles.add(new int[]{px - 1, py + 1});
            }

            case RIGHT -> {
                attackTiles.add(new int[]{px + 1, py});
                attackTiles.add(new int[]{px + 1, py - 1});
                attackTiles.add(new int[]{px + 1, py + 1});
            }
        }
    }


    private void dealDamage(GameManager gameManager) {
        if (gameManager == null) return;
        int damage = (int)((baseDamage + (level - 1) * damagePerLevel));
        Set<Enemy> hitEnemies = new HashSet<>();

        for (int[] tile : attackTiles) {
            List<Enemy> enemies = gameManager.getEnemiesAt(tile[0], tile[1]);
            if (enemies != null) {
                for (Enemy enemy : enemies) {
                    if (enemy != null && !enemy.isDead() && hitEnemies.add(enemy)) {
                        enemy.takeDamage(damage);

                        if (gameManager.getCombatEffectManager() != null) {
                            float ex = (enemy.getX() + 0.5f) * GameConstants.CELL_SIZE;
                            float ey = (enemy.getY() + 0.5f) * GameConstants.CELL_SIZE;
                            gameManager.getCombatEffectManager().spawnHitSpark(ex, ey);
                        }
                    }
                }
            }
        }

        if (!hitEnemies.isEmpty()) {
            float shakeStrength = 2.0f + (level * 0.5f);
            de.tum.cit.fop.maze.utils.CameraManager.getInstance().shake(0.15f, shakeStrength);
            gameManager.triggerHitFeedback(shakeStrength);
        }
    }
    /**
     * Draws debug visualization for the melee attack area.
     * <p>
     * Only rendered when debug mode is enabled.
     *
     * @param batch  the sprite batch
     * @param shapeRenderer the shape renderer
     * @param player the player instance
     */
    @Override
    public void draw(SpriteBatch batch, ShapeRenderer shapeRenderer, Player player) {
        if (!GameConstants.DEBUG_MODE) return;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(DEBUG_COLOR);
        for (int[] tile : attackTiles) {
            shapeRenderer.rect(tile[0]*GameConstants.CELL_SIZE, tile[1]*GameConstants.CELL_SIZE, GameConstants.CELL_SIZE, GameConstants.CELL_SIZE);
        }
        shapeRenderer.end();
    }

    @Override
    protected void onUpgrade() {
        switch (level) {
            case 2 -> baseDamage += 2;
            case 3 -> damagePerLevel += 1;
            case 4 -> hitTimeOffset += 0.03f;
            case 5 -> baseDamage += 5;
        }
    }
    /**
     * Returns the unique identifier of this ability.
     *
     * @return ability ID
     */
    @Override
    public String getId() { return "melee"; }
}