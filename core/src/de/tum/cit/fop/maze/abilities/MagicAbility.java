package de.tum.cit.fop.maze.abilities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.entities.enemy.Enemy;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.utils.Logger; // 导入 Logger

import java.util.Map;
/**
 * A multiphase magic ability with aiming, execution, and cooldown stages.
 * <p>
 * The ability follows a state-machine–based workflow:
 * <ul>
 *     <li>{@link Phase#IDLE}: ready to be activated</li>
 *     <li>{@link Phase#AIMING}: player selects an area of effect</li>
 *      <li>{@link Phase#EXECUTED}: damage has been applied and the ability
 *     waits for an optional second activation to trigger healing,
 *     or times out into cooldown</li>
 *     <li>{@link Phase#COOLDOWN}: internal cooldown before reuse</li>
 * </ul>
 * The ability deals area damage and can optionally heal players
 * based on the number of enemies hit if a second activation is performed.
 */
public class MagicAbility extends Ability {

    /** Cached reference to the game manager during execution. */
    private GameManager gameManager;
    /**
     * Represents the current phase of the magic ability.
     */
    public enum Phase {
        /** Ability is idle and ready to start aiming. */
        IDLE,
        /** Player is selecting the target area. */
        AIMING,
        /**
         * Damage has been applied; waits for a second activation to trigger healing
         * or times out into cooldown.
         */
        EXECUTED,
        /** Ability is cooling down internally. */
        COOLDOWN
    }
    /** Current phase of the ability state machine. */
    private Phase phase = Phase.IDLE;
    /** Time spent in the current phase (seconds). */
    private float phaseTimer = 0f;
    /** Time spent aiming (seconds). */
    private float aimingTimer = 0f;

    /**
     * Timer used during the EXECUTED phase to limit the window
     * for triggering the optional healing effect.
     */
    private float effectWaitTimer = 0f;

    /** Maximum time allowed for aiming. */
    private static final float AIMING_TIMEOUT    = 5.0f;
    /** Duration of visual effects after execution. */
    private static final float EFFECT_DURATION   = 0.6f;
    /** Base cooldown duration in seconds. */
    private static final float COOLDOWN_DURATION = 5.0f;
    /** Area-of-effect radius in tile units. */
    private int aoeTileRadius = 2;
    /** Visual radius of the AOE in world units. */
    private float aoeVisualRadius = 2.5f * GameConstants.CELL_SIZE;

    /** Prevents multiple activations within the same frame. */
    private boolean inputConsumedThisFrame = false;
    private boolean healUsed = false;

    /** X coordinate of the AOE center (tile-based). */
    private int aoeCenterX;

    /** Y coordinate of the AOE center (tile-based). */
    private int aoeCenterY;
    private int hitEnemyCount = 0;
    private float baseHealPercent = 0.10f;

    /** Additional healing percentage per enemy hit. */
    private float extraPerEnemy   = 0.02f;
    private float currentCooldown = COOLDOWN_DURATION;

    /**
     * Creates a new magic ability with default parameters.
     */
    public MagicAbility() {
        super("Magic Strike", "Aim -> Pillar Damage -> Absorb Essence", 0f, 0f);
        this.manaCost = 20;
    }

    /**
     * Returns the current phase of the ability.
     *
     * @return the current phase
     */
    public Phase getPhase() { return phase; }
    /**
     * Returns the time spent in the current phase.
     *
     * @return phase time in seconds
     */
    public float getPhaseTime() { return phaseTimer; }
    /**
     * Mana consumption is handled manually when entering the AIMING phase,
     * as this ability consists of multiple activation stages.
     */
    @Override
    protected boolean shouldConsumeMana() {    return false; }

    @Override
    protected boolean shouldStartCooldown() { return phase == Phase.COOLDOWN; }

    @Override
    protected float getCooldownDuration() { return currentCooldown; }
    /**
     * Allows activation during AIMING and EXECUTED phases to support
     * multi-stage input, while only enforcing mana availability
     * when starting from the IDLE phase.
     * Checks whether the magic ability can be activated.
     *
     * @param player the player instance
     * @return {@code true} if the ability can be activated
     */
    @Override
    public boolean canActivate(Player player) {
        if (phase == Phase.COOLDOWN) return false;
        return player.getMana() >= manaCost;
    }

    @Override
    protected void onActivate(Player player, GameManager gm) {

        if (inputConsumedThisFrame) return;
        inputConsumedThisFrame = true;
        this.gameManager = gm;

        switch (phase) {
            case IDLE -> {
                player.useMana(manaCost);
                int mx = gm.getMouseTileX();
                int my = gm.getMouseTileY();

                if (mx < 0 || my < 0) {
                    aoeCenterX = player.getX();
                    aoeCenterY = player.getY();
                } else {
                    aoeCenterX = mx;
                    aoeCenterY = my;
                }

                aimingTimer = 0f;
                setPhase(Phase.AIMING);

                AudioManager.getInstance().play(AudioType.PLAYER2_ATTACK);
                Logger.debug("MagicAbility: Aiming Started at " + aoeCenterX + "," + aoeCenterY);
            }
            case AIMING -> {
                if (phaseTimer < 0.1f) return;

                if (gm.isUIConsumingMouse()) {
                    Logger.debug("MagicAbility: Fire blocked by UI");
                    return;
                }

                castAOE(gm);
                effectWaitTimer = 0f;
                healUsed = false;
                setPhase(Phase.EXECUTED);
                AudioManager.getInstance().play(getExecuteSoundByLevel());
                Logger.debug("MagicAbility: FIRED!");
            }
            case EXECUTED -> {
                healUsed = true;
                castHeal(gm);
                startInternalCooldown(currentCooldown);
                Logger.debug("MagicAbility: HEAL triggered by second input");
            }
        }
    }

    private AudioType getExecuteSoundByLevel() {
        if (level >= 5) {
            return AudioType.MAGIC_EXECUTE_LV5;
        } else if (level >= 3) {
            return AudioType.MAGIC_EXECUTE_LV3;
        } else {
            return AudioType.MAGIC_EXECUTE_LV1;
        }
    }


    /**
     * Updates the ability state and internal phase machine.
     *
     * @param delta       time elapsed since last frame (seconds)
     * @param player      the player instance
     * @param gm          the game manager
     */
    @Override
    public void update(float delta, Player player, GameManager gm) {
        super.update(delta, player, gm);
        inputConsumedThisFrame = false;
        phaseTimer += delta;

        switch (phase) {
            case COOLDOWN -> {
                if (ready) setPhase(Phase.IDLE);
            }
            case AIMING -> {
                aimingTimer += delta;

                if (!gm.isUIConsumingMouse()) {
                    int mx = gm.getMouseTileX();
                    int my = gm.getMouseTileY();
                    if (mx >= 0 && my >= 0) {
                        aoeCenterX = mx;
                        aoeCenterY = my;
                    }
                }

                if (aimingTimer >= AIMING_TIMEOUT) {
                    setPhase(Phase.IDLE);
                }
            }
            case EXECUTED -> {
                effectWaitTimer += delta;
                if (effectWaitTimer >= 1.0f) {
                    if (!healUsed) {
                        startInternalCooldown(0.5f);
                    }
                }
            }
        }
    }

    private void setPhase(Phase newPhase) {
        phase = newPhase;
        phaseTimer = 0f;
    }

    private void startInternalCooldown(float cd) {
        currentCooldown = cd;
        ready = false;
        cooldownTimer = 0f;
        setPhase(Phase.COOLDOWN);
    }

    private void castAOE(GameManager gm) {
        hitEnemyCount = 0;

        if (gm.getCombatEffectManager() != null) {
            float cx = (aoeCenterX + 0.5f) * GameConstants.CELL_SIZE;
            float cy = (aoeCenterY + 0.5f) * GameConstants.CELL_SIZE;

            gm.getCombatEffectManager().spawnMagicCircle(cx, cy, aoeVisualRadius, 0.5f);
            gm.getCombatEffectManager().spawnMagicPillar(cx, cy, aoeVisualRadius);

            if (de.tum.cit.fop.maze.utils.CameraManager.getInstance() != null) {
                de.tum.cit.fop.maze.utils.CameraManager.getInstance().shake(0.2f, 3.0f);
            }
        } else {
            Logger.error("MagicAbility: CombatEffectManager is NULL!");
        }

        for (Enemy enemy : gm.getEnemies()) {
            if (enemy == null || enemy.isDead()) continue;
            int dx = enemy.getX() - aoeCenterX;
            int dy = enemy.getY() - aoeCenterY;
            if (dx * dx + dy * dy <= aoeTileRadius * aoeTileRadius) {
                enemy.takeDamage(20 + (level * 5));
                hitEnemyCount++;
                if (gm.getCombatEffectManager() != null) {
                    gm.getCombatEffectManager().spawnHitSpark(
                            (enemy.getX() + 0.5f) * GameConstants.CELL_SIZE,
                            (enemy.getY() + 0.5f) * GameConstants.CELL_SIZE
                    );
                }
            }
        }
    }

    private void castHeal(GameManager gm) {
        float healPercent = baseHealPercent + hitEnemyCount * extraPerEnemy;
        healPercent = Math.min(healPercent, 0.5f);
        for (Player p : gm.getPlayers()) {
            if (p == null || p.isDead()) continue;
            if (gm.getCombatEffectManager() != null) {
                float px = (p.getX() + 0.5f) * GameConstants.CELL_SIZE;
                float py = (p.getY() + 0.5f) * GameConstants.CELL_SIZE;
                gm.getCombatEffectManager().spawnMagicEssence(px - 50, py, px, py);
                gm.getCombatEffectManager().spawnMagicEssence(px + 50, py, px, py);
                if (hitEnemyCount > 0) {
                    gm.getCombatEffectManager().spawnStatusText(px, py + 40, "ABSORB", Color.CYAN);
                }
            }
            int heal = Math.max(1, Math.round(p.getMaxLives() * healPercent));
            p.heal(heal);
        }
    }
    /**
     * Draws the aiming indicator while the ability is in the aiming phase.
     *
     * @param batch  the sprite batch
     * @param sr     the shape renderer
     * @param player the player instance
     */
    @Override
    public void draw(SpriteBatch batch, ShapeRenderer sr, Player player) {
        if (phase != Phase.AIMING) return;
        sr.begin(ShapeRenderer.ShapeType.Line);
        float alpha = 0.5f + 0.5f * (float) Math.sin(phaseTimer * 5f);
        sr.setColor(0.5f, 0f, 1f, alpha);
        // 绘制瞄准圈
        sr.circle((aoeCenterX + 0.5f) * GameConstants.CELL_SIZE, (aoeCenterY + 0.5f) * GameConstants.CELL_SIZE, aoeVisualRadius);
        sr.end();
    }

    @Override
    protected void onUpgrade() {
        switch (level) {
            case 2 -> {
                aoeTileRadius += 1;
                aoeVisualRadius = (aoeTileRadius + 0.5f) * GameConstants.CELL_SIZE;
            }
            case 3 -> baseHealPercent += 0.05f;
            case 4 -> extraPerEnemy += 0.02f;
            case 5 -> {
                aoeTileRadius += 1;
                aoeVisualRadius = (aoeTileRadius + 0.5f) * GameConstants.CELL_SIZE;
                currentCooldown = 3.0f;
            }
        }
    }

    /**
     * Returns the unique identifier of this ability.
     *
     * @return ability ID
     */
    @Override
    public String getId() { return "magic"; }
    /**
     * Returns the input type of this ability.
     *
     * @return {@link AbilityInputType#INSTANT}
     */
    @Override
    public AbilityInputType getInputType() { return AbilityInputType.INSTANT; }
    /**
     * Saves the current runtime state of the magic ability.
     *
     * @return a map containing serialized state data
     */
    @Override
    public Map<String, Object> saveState() {
        Map<String, Object> m = super.saveState();
        m.put("phase", phase.name());
        return m;
    }
    /**
     * Restores the magic ability state from saved data.
     *
     * @param m the saved state map
     */
    @Override
    public void loadState(Map<String, Object> m) {
        super.loadState(m);
        phase = Phase.valueOf((String) m.getOrDefault("phase", Phase.IDLE.name()));
    }

    @Override
    protected boolean managesOwnLifecycle() {
        return true;
    }
}