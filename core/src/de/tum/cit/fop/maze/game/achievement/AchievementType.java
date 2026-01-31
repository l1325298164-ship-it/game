package de.tum.cit.fop.maze.game.achievement;

/**
 * Defines all achievement types available in the game.
 *
 * <p>Each achievement has a unique identifier, a display name,
 * and a description shown to the player. Achievement logic
 * is handled by {@link AchievementManager}, while this enum
 * serves as a centralized definition.
 */
public enum AchievementType {
    /**
     * Achievement definitions.
     */
    ACH_01_TRAINING("ACH_01", "Training: Orientation", "Watch the PV fully once"),
    ACH_02_FIRST_CUP("ACH_02", "First Order", "Clear Level 1"),
    ACH_03_BOBA_RESCUE("ACH_03", "Boba Rescue", "Pick up a Boba Pearl (Heal Item)"),

    ACH_04_PEARL_SWEEPER("ACH_04", "Pearl Sweeper", "Kill 60 E01 Corrupted Pearls"),
    ACH_05_COFFEE_GRINDER("ACH_05", "Coffee Grinder", "Kill 40 E02 Coffee Beans"),
    ACH_06_CARAMEL_MELT("ACH_06", "Caramel Melt", "Kill 50 E03 Caramel Juggernauts"),
    ACH_07_SHELL_BREAKER("ACH_07", "Shell Breaker", "Dash-kill 50 E04 Caramel Shells"),
    ACH_08_BEST_SELLER("ACH_08", "Best Seller", "Kill 500 enemies in total"),

    ACH_09_FREE_TOPPING("ACH_09", "Free Topping", "Collect 50 Hearts"),
    ACH_10_TREASURE_MASTER("ACH_10", "Treasure Master", "Collect 3 different Treasure Buffs"),
    ACH_11_SEALED_TIGHT("ACH_11", "Sealed Tight", "Finish a level with <= 3 hits taken"),
    ACH_12_MINE_EXPERT("ACH_12", "Minesweeper", "Trigger 10 Mines without damage (WIP)"),

    ACH_13_TRUE_RECIPE("ACH_13", "True Recipe", "Find all hidden recipes (WIP)"),
    ACH_14_RENAISSANCE("ACH_14", "Renaissance", "Clear game in Hard Mode"),
    ACH_15_SUCCESS("ACH_15", "Success?", "Defeat the Boss for the first time");
    /**
     * Unique string identifier used for persistence and save data.
     */
    public final String id;
    /**
     * Human-readable name displayed in the UI.
     */
    public final String displayName;
    /**
     * Description explaining how the achievement can be unlocked.
     */
    public final String description;

    AchievementType(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }
}