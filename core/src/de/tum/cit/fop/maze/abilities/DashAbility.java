package de.tum.cit.fop.maze.abilities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.game.GameConstants;

import java.util.Map;
/**
 * Dash ability that allows the player to quickly move forward.
 * <p>
 * This ability uses a charge-based system instead of the standard cooldown
 * mechanism. Each dash consumes one charge, which regenerates over time.
 * The player becomes temporarily invincible while dashing.
 */
public class DashAbility extends Ability {
    /** Maximum number of dash charges. */
    private int maxCharges = 2;

    /** Time in seconds required to regenerate one charge. */
    private float chargeCooldown = 4.0f;
    /** Duration of a single dash in seconds. */
    private float dashDuration = 0.8f;
    /** Additional invincibility time granted after dash ends. */
    private float invincibleBonus = 0f;
    /** Current available dash charges. */
    private int charges = maxCharges;
    /** Timer used to regenerate dash charges. */
    private float chargeTimer = 0f;
    /**
     * Creates a new dash ability with default parameters.
     */
    public DashAbility() {
        super("Dash", "Quick dash forward", 0f, 0.8f);
        this.dashDuration = 0.8f;
    }


    @Override
    protected boolean shouldConsumeMana() {
        return false;
    }

    @Override
    protected boolean shouldStartCooldown() {
        return false;
    }

    @Override
    protected boolean shouldBecomeActive() {
        return true;
    }

    /**
     * Checks whether the dash ability can be activated.
     *
     * @param player the player instance
     * @return {@code true} if at least one charge is available and the player
     *         is not already dashing
     */
    @Override
    public boolean canActivate(Player player) {
        return charges > 0 && !player.isDashing();
    }




    @Override
    protected void onActivate(Player player, GameManager gameManager) {
        charges--;

        AudioManager.getInstance().play(AudioType.SKILL_DASH);

        float angle = 0f;
        switch (player.getDirection()) {
            case RIGHT -> angle = 0f;
            case UP    -> angle = 90f;
            case LEFT  -> angle = 180f;
            case DOWN  -> angle = 270f;
        }

        if (gameManager.getCombatEffectManager() != null) {
            gameManager.getCombatEffectManager().spawnDash(
                    player.getWorldX() * GameConstants.CELL_SIZE,
                    player.getWorldY() * GameConstants.CELL_SIZE,
                    angle,
                    this.level
            );
        }

        player.startDash(dashDuration, invincibleBonus);
    }




    /**
     * Updates the dash ability state.
     * <p>
     * Handles charge regeneration independently of the base ability cooldown system.
     *
     * @param delta       time elapsed since last frame (seconds)
     * @param player      the player instance
     * @param gameManager the game manager
     */
    @Override
    public void update(float delta, Player player, GameManager gameManager) {
        super.update(delta, player, gameManager);

        if (charges < maxCharges) {
            chargeTimer += delta;
            if (chargeTimer >= chargeCooldown) {
                charges++;
                chargeTimer = 0f;
            }
        }
    }

    /**
     * Draws visual effects related to the dash ability.
     * <p>
     * Dash has no persistent visual rendering handled here.
     *
     * @param batch  the sprite batch
     * @param shapeRenderer the shape renderer
     * @param player the player instance
     */
    @Override
    public void draw(SpriteBatch batch, ShapeRenderer shapeRenderer, Player player) {
    }


    @Override
    protected void onUpgrade() {
        switch (level) {
            case 2 -> {
                chargeCooldown = 1.6f;
            }
            case 3 -> {
                maxCharges = 3;
                charges = Math.min(charges + 1, maxCharges);
            }
            case 4 -> {
                dashDuration = 1.0f;
            }
            case 5 -> {
                invincibleBonus = 0.2f;
            }
        }
    }


    /**
     * Returns the current number of available dash charges.
     *
     * @return current charge count
     */
    public int getCurrentCharges() {
        return charges;
    }
    /**
     * Returns the maximum number of dash charges.
     *
     * @return maximum charge count
     */
    public int getMaxCharges() {
        return maxCharges;
    }
    /**
     * Returns the current charge regeneration progress.
     * <p>
     * The value is normalized to the range {@code 0.0f}–{@code 1.0f}.
     *
     * @return charge regeneration progress
     */
    public float getChargeProgress() {
        if (charges >= maxCharges) return 1f;
        return chargeTimer / chargeCooldown;
    }
    /**
     * Returns the unique identifier of this ability.
     *
     * @return ability ID
     */
    @Override
    public String getId() {
        return "dash";
    }

    /**
     * Saves the current runtime state of the dash ability.
     *
     * @return a map containing serialized state data
     */
    @Override
    public Map<String, Object> saveState() {
        Map<String, Object> m = super.saveState();
        m.put("charges", charges);
        m.put("chargeTimer", chargeTimer);
        return m;
    }
    /**
     * Restores the dash ability state from saved data.
     *
     * @param m the saved state map
     */
    @Override
    public void loadState(Map<String, Object> m) {
        super.loadState(m);
        charges = (int) m.getOrDefault("charges", maxCharges);
        chargeTimer = ((Number)m.getOrDefault("chargeTimer", 0f)).floatValue();
    }

}
