package de.tum.cit.fop.maze.abilities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.utils.Logger;

import java.util.*;
/**
 * Manages all abilities of a player.
 * <p>
 * The {@code AbilityManager} is responsible for:
 * <ul>
 *     <li>Registering and equipping abilities</li>
 *     <li>Handling ability activation via slots</li>
 *     <li>Updating ability lifecycles</li>
 *     <li>Tracking currently active abilities</li>
 *     <li>Delegating rendering calls</li>
 * </ul>
 */
public class AbilityManager {
    /** All registered abilities indexed by their unique ID. */
    private final Map<String, Ability> abilities = new HashMap<>();


    /** List of abilities that are currently active. */
    private final List<Ability> activeAbilities = new ArrayList<>();
    /** Fixed-size array of equipped ability slots. */
    private final Ability[] abilitySlots = new Ability[4];

    /** The player owning these abilities. */
    private final Player player;
    /** Reference to the game manager. */
    private final GameManager gameManager;

    /**
     * Creates a new ability manager for the given player.
     *
     * @param player      the player instance
     * @param gameManager the game manager
     * @throws IllegalArgumentException if any argument is {@code null}
     */
    public AbilityManager(Player player, GameManager gameManager) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (gameManager == null) {
            throw new IllegalArgumentException("GameManager cannot be null");
        }
        this.player = player;
        this.gameManager = gameManager;
        initializeAbilities();
    }
    /**
     * Initializes the default abilities based on the player type.
     */
    private void initializeAbilities() {
        if (player.getPlayerIndex() == Player.PlayerIndex.P1) {
            register(new MeleeAttackAbility(), 0);
            register(new DashAbility(), 1);
        } else {
            register(new MagicAbility(), 0);
            register(new DashAbility(), 1);
        }
    }
    /**
     * Registers an ability and optionally assigns it to a slot.
     *
     * @param ability the ability to register
     * @param slot    the slot index, or an invalid index to skip equipping
     */
    private void register(Ability ability, int slot) {
        abilities.put(ability.getId(), ability);
        if (slot >= 0 && slot < abilitySlots.length) {
            abilitySlots[slot] = ability;
        }
    }
    /**
     * Updates all equipped abilities and refreshes the active ability list.
     *
     * @param delta time elapsed since last frame (seconds)
     */
    public void update(float delta) {
        for (Ability ability : abilitySlots) {
            if (ability != null) {
                ability.update(delta, player, gameManager);
            }
        }

        activeAbilities.clear();
        for (Ability ability : abilities.values()) {
            if (ability.isActive()) {
                activeAbilities.add(ability);
            }
        }
    }
    /**
     * Activates the ability equipped in the given slot.
     *
     * @param slot the slot index
     * @return {@code true} if the ability was successfully activated
     */
    public boolean activateSlot(int slot) {
        if (slot < 0 || slot >= abilitySlots.length) return false;

        Ability ability = abilitySlots[slot];
        if (ability == null) return false;

        boolean activated = ability.activate(player, gameManager);

        if (activated && ability.isActive() && !activeAbilities.contains(ability)) {
            activeAbilities.add(ability);
        }
        return activated;
    }
    /**
     * Upgrades the specified ability if it exists.
     *
     * @param abilityId the unique ability ID
     */
    public void upgradeAbility(String abilityId) {
        Ability ability = abilities.get(abilityId);
        if (ability != null) ability.upgrade();
    }
    /**
     * Equips an ability into a specific slot.
     *
     * @param abilityId the unique ability ID
     * @param slot      the target slot index
     */
    public void equipAbility(String abilityId, int slot) {
        if (slot < 0 || slot >= abilitySlots.length) return;
        Ability ability = abilities.get(abilityId);
        if (ability == null) {
            Logger.warning("Cannot equip ability: " + abilityId + " not found");
            return;
        }
        abilitySlots[slot] = ability;
    }
    /**
     * Resets all abilities to their initial runtime state.
     */
    public void reset() {
        for (Ability ability : abilities.values()) {
            ability.forceReset();
        }
        activeAbilities.clear();
    }
    /**
     * Draws all ability-related visual effects.
     *
     * @param batch  the sprite batch
     * @param sr     the shape renderer
     * @param player the player instance
     */
    public void drawAbilities(SpriteBatch batch, ShapeRenderer sr, Player player) {
        if (batch == null || sr == null || player == null) {
            Logger.warning("drawAbilities called with null parameters");
            return;
        }
        for (Ability ability : abilities.values()) {
            ability.draw(batch, sr, player);
        }
    }
    /**
     * Returns the ability equipped in the given slot.
     *
     * @param slot the slot index
     * @return the ability in the slot, or {@code null} if empty or invalid
     */
    public Ability getAbilityInSlot(int slot) {
        if (slot < 0 || slot >= abilitySlots.length) return null;
        return abilitySlots[slot];
    }
    /**
     * @return a map of all registered abilities
     */
    public Map<String, Ability> getAbilities() { return abilities; }
    /**
     * @return a list of currently active abilities
     */
    public List<Ability> getActiveAbilities() { return activeAbilities; }
    /**
     * @return the array of equipped ability slots
     */
    public Ability[] getAbilitySlots() { return abilitySlots; }
}
