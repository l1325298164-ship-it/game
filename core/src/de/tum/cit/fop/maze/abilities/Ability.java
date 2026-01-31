package de.tum.cit.fop.maze.abilities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.GameManager;

import java.util.HashMap;
import java.util.Map;
/**
 * Base class for all player abilities.
 * <p>
 * An ability defines activation logic, cooldown handling, duration handling,
 * upgrade behavior, rendering, and state persistence.
 * Subclasses should implement the abstract hook methods to define
 * concrete behavior.
 */
public abstract class Ability {
    /**
     * Defines how player input is handled for an ability.
     */
    public enum AbilityInputType {
        /** Ability is triggered once per input. */
        INSTANT,
        /** Ability remains active while input is held. */
        CONTINUOUS
    }
    /**
     * Returns the unique identifier of this ability.
     *
     * @return the ability ID
     */
    public abstract String getId();
    /**
     * Returns the input type of this ability.
     * <p>
     * Defaults to {@link AbilityInputType#INSTANT}.
     *
     * @return the input type
     */
    public AbilityInputType getInputType() {
        return AbilityInputType.INSTANT;
    }
    /** Display name of the ability. */
    protected final String name;
    /** Description shown to the player. */
    protected final String description;
    /** Cooldown duration in seconds. */
    protected final float cooldown;
    /** Active duration in seconds. */
    protected final float duration;
    /** Whether the ability is currently active. */
    protected boolean active = false;
    /** Whether the ability is ready to be activated. */
    protected boolean ready = true;
    /** Current cooldown timer in seconds. */
    protected float cooldownTimer = 0f;
    /** Current active duration timer in seconds. */
    protected float durationTimer = 0f;
    /** Mana cost required to activate the ability. */
    protected int manaCost = 0;

    /** Current upgrade level of the ability. */
    protected int level = 1;
    /** Maximum upgrade level. */
    protected int maxLevel = 5;
    /**
     * Creates a new ability.
     *
     * @param name        the display name
     * @param description the description text
     * @param cooldown    the cooldown duration in seconds
     * @param duration    the active duration in seconds
     */
    protected Ability(String name, String description, float cooldown, float duration) {
        this.name = name;
        this.description = description;
        this.cooldown = cooldown;
        this.duration = duration;
    }

    /**
     * Attempts to activate this ability.
     *
     * @param player      the player using the ability
     * @param gameManager the game manager
     * @return {@code true} if activation was successful
     */
    public boolean activate(Player player, GameManager gameManager) {
        return tryActivate(player, gameManager);
    }

    /**
     * Internal activation logic.
     *
     * @param player      the player
     * @param gameManager the game manager
     * @return {@code true} if activation succeeded
     */
    protected boolean tryActivate(Player player, GameManager gameManager) {
        if (!canActivate(player)) return false;

        if (shouldConsumeMana() && manaCost > 0) {
            player.useMana(manaCost);
        }

        onActivate(player, gameManager);
        // ⭐ 关键分叉
        if (!managesOwnLifecycle()) {

            if (shouldConsumeMana() && manaCost > 0) {
                player.useMana(manaCost);
            }

            if (shouldStartCooldown()) {
                ready = false;
                cooldownTimer = 0f;
            }

            if (shouldBecomeActive()) {
                active = true;
                durationTimer = 0f;
            }
        }
        return true;
    }
    protected boolean managesOwnLifecycle() {
        return false;
    }
    /**
     * Updates the ability state.
     *
     * @param delta       time elapsed since last frame (seconds)
     * @param player      the player
     * @param gameManager the game manager
     */
    public void update(float delta, Player player, GameManager gameManager) {

        if (active) {
            durationTimer += delta;
            updateActive(delta, player, gameManager);

            if (durationTimer >= duration) {
                active = false;
                durationTimer = 0f;
                onDeactivate(player, gameManager);
            }
        }

        if (!ready) {
            cooldownTimer += delta;
            if (cooldownTimer >= getCooldownDuration()) {
                ready = true;
                cooldownTimer = getCooldownDuration();
            }
        }
    }

    /**
     * Determines whether the ability should enter the active state.
     *
     * @return {@code true} if the ability becomes active
     */
    protected boolean shouldBecomeActive() {
        return duration > 0;
    }
    /**
     * Determines whether mana should be consumed on activation.
     *
     * @return {@code true} if mana is consumed
     */
    protected boolean shouldConsumeMana() {
        return true;
    }
    /**
     * Determines whether cooldown should start on activation.
     *
     * @return {@code true} if cooldown starts
     */
    protected boolean shouldStartCooldown() {
        return true;
    }
    /**
     * Returns the effective cooldown duration.
     *
     * @return the cooldown duration in seconds
     */
    protected float getCooldownDuration() {
        return cooldown;
    }
    /**
     * Called when the ability is activated.
     */
    protected abstract void onActivate(Player player, GameManager gameManager);
    /**
     * Called every frame while the ability is active.
     */
    protected void updateActive(float delta, Player player, GameManager gameManager) {}
    /**
     * Called when the ability deactivates.
     */
    protected void onDeactivate(Player player, GameManager gameManager) {}
    /**
     * Called when the ability is upgraded.
     */
    protected abstract void onUpgrade();
    /**
     * Renders the ability effects.
     */
    public abstract void draw(SpriteBatch batch, ShapeRenderer shapeRenderer, Player player);

    /**
     * Checks whether the ability can currently be activated.
     *
     * @param player the player
     * @return {@code true} if activation is possible
     */
    public boolean canActivate(Player player) {
        return ready && player.getMana() >= manaCost;
    }
    /**
     * @return {@code true} if the ability is active
     */
    public boolean isActive() { return active; }
    /**
     * @return {@code true} if the ability is ready to use
     */
    public boolean isReady() { return ready; }
    /**
     * Returns the cooldown progress normalized to [0, 1].
     *
     * @return cooldown progress
     */
    public float getCooldownProgress() {
        float cd = getCooldownDuration();
        return cd <= 0 ? 1f : cooldownTimer / cd;
    }
    /**
     * Returns the active duration progress normalized to [0, 1].
     *
     * @return duration progress
     */
    public float getDurationProgress() {
        return duration <= 0 ? 0f : durationTimer / duration;
    }
    /**
     * @return the ability name
     */
    public String getName() { return name; }
    /**
     * @return the current ability level
     */
    public int getLevel() { return level; }
    /**
     * Upgrades the ability by one level if possible.
     */
    public void upgrade() {
        if (level < maxLevel) {
            level++;
            onUpgrade();
            playUpgradeSound();
        }
    }
    /**
     * Plays the upgrade sound effect.
     */
    protected void playUpgradeSound() {
        AudioManager.getInstance().play(getUpgradeSoundForLevel(level));
    }
    /**
     * Returns the upgrade sound for the given level.
     *
     * @param level the new level
     * @return the audio type
     */
    protected AudioType getUpgradeSoundForLevel(int level) {
        return AudioType.ABILITY_UPGRADE_COMMON;
    }

    /**
     * @return {@code true} if the ability can still be upgraded
     */
    public boolean canUpgrade() {
        return level < maxLevel;
    }
    /**
     * Sets the ability level directly.
     *
     * @param level the new level
     */
    public void setLevel(int level) {
        this.level = level;
    }
    /**
     * Resets the runtime state of the ability.
     */
    public void forceReset() {
        active = false;
        ready = true;
        cooldownTimer = 0f;
        durationTimer = 0f;
    }
    /**
     * @return the mana cost
     */
    public int getManaCost() { return manaCost; }
    /**
     * @return the ability description
     */
    public String getDescription() { return description; }
    /**
     * @return the base cooldown duration
     */
    public float getCooldown() { return cooldown; }
    /**
     * @return the base active duration
     */
    public float getDuration() { return duration; }
    /**
     * Saves the current ability state.
     *
     * @return a map containing serialized state values
     */
    public Map<String, Object> saveState() {
        Map<String, Object> m = new HashMap<>();
        m.put("level", level);
        m.put("ready", ready);
        m.put("active", active);
        m.put("cooldownTimer", cooldownTimer);
        m.put("durationTimer", durationTimer);
        return m;
    }
    /**
     * Restores the ability state from a map.
     *
     * @param m the saved state map
     */
    public void loadState(Map<String, Object> m) {
        if (m == null) return;

        Object levelObj = m.get("level");
        if (levelObj instanceof Number) {
            level = ((Number) levelObj).intValue();
        }
        
        Object readyObj = m.get("ready");
        if (readyObj instanceof Boolean) {
            ready = (Boolean) readyObj;
        }
        
        Object activeObj = m.get("active");
        if (activeObj instanceof Boolean) {
            active = (Boolean) activeObj;
        }
        
        Object cooldownObj = m.get("cooldownTimer");
        if (cooldownObj instanceof Number) {
            cooldownTimer = ((Number) cooldownObj).floatValue();
        }
        
        Object durationObj = m.get("durationTimer");
        if (durationObj instanceof Number) {
            durationTimer = ((Number) durationObj).floatValue();
        }
    }
}
