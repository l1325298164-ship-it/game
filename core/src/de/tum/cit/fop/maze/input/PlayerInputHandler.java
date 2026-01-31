package de.tum.cit.fop.maze.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.utils.Logger;
/**
 * Handles player input processing and translates it into high-level game actions.
 * <p>
 * This class reads input via {@link KeyBindingManager} and delegates the resulting
 * actions to game logic through {@link InputHandlerCallback}.
 * <p>
 * It supports multiple players, movement timing, ability cooldowns,
 * and UI input blocking.
 */
public class PlayerInputHandler {

    private boolean movedUp = false;
    private boolean movedDown = false;
    private boolean movedLeft = false;
    private boolean movedRight = false;

    private float moveTimer = 0f;
    private float abilityCooldownP1 = 0f;
    private float abilityCooldownP2 = 0f;

    private static final float ABILITY_COOLDOWN = 0.1f;
    /**
     * Creates a new player input handler.
     */

    public PlayerInputHandler() {
        Logger.debug("PlayerInputHandler initialized");
    }

    /**
     * Processes all player input for the current frame.
     * <p>
     * This method handles movement, abilities, interaction, and menu input,
     * while respecting UI input blocking and per-player state.
     *
     * @param delta    time elapsed since last frame
     * @param callback callback interface for forwarding input events
     * @param index    player identifier
     */

    public void update(
            float delta,
            InputHandlerCallback callback,
            Player.PlayerIndex index
    ) {

        if (callback.isUIConsumingMouse()) {
        if (callback.isUIConsumingMouse())
            return;
        }

        handleMovementInput(delta, callback, index);

        handleAbilityInput(delta, callback, index);

        handleActionInput(callback, index);


    }

    /**
     * Handles grid-based movement input with a configurable movement delay.
     *
     * @param delta    time elapsed since last frame
     * @param callback input callback receiver
     * @param index    player identifier
     */

    private void handleMovementInput(
            float delta,
            InputHandlerCallback callback,
            Player.PlayerIndex index
    ) {
        moveTimer += delta;

        float moveDelay =
                GameConstants.MOVE_DELAY_NORMAL * callback.getMoveDelayMultiplier();

        if (moveTimer < moveDelay) return;
        moveTimer -= moveDelay;

        int dx = 0;
        int dy = 0;

        var km = KeyBindingManager.getInstance();

        if (index == Player.PlayerIndex.P1) {
            if (km.isPressed(KeyBindingManager.GameAction.P1_MOVE_UP)) {
                dy = 1;
                movedUp = true;
            } else if (km.isPressed(KeyBindingManager.GameAction.P1_MOVE_DOWN)) {
                dy = -1;
                movedDown = true;
            } else if (km.isPressed(KeyBindingManager.GameAction.P1_MOVE_LEFT)) {
                dx = -1;
                movedLeft = true;
            } else if (km.isPressed(KeyBindingManager.GameAction.P1_MOVE_RIGHT)) {
                dx = 1;
                movedRight = true;
            }

        } else {
            if (km.isPressed(KeyBindingManager.GameAction.P2_MOVE_UP)) {
                dy = 1;
            } else if (km.isPressed(KeyBindingManager.GameAction.P2_MOVE_DOWN)) {
                dy = -1;
            } else if (km.isPressed(KeyBindingManager.GameAction.P2_MOVE_LEFT)) {
                dx = -1;
            } else if (km.isPressed(KeyBindingManager.GameAction.P2_MOVE_RIGHT)) {
                dx = 1;
            }
        }

        if (dx != 0 || dy != 0) {
            callback.onMoveInput(index, dx, dy);
        }
    }

    /**
     * Handles ability and dash input with cooldown control.
     *
     * @param delta    time elapsed since last frame
     * @param callback input callback receiver
     * @param index    player identifier
     */

    private void handleAbilityInput(
            float delta,
            InputHandlerCallback callback,
            Player.PlayerIndex index
    ){
        if (callback.isUIConsumingMouse()) {
            return;
        }
        float cd = (index == Player.PlayerIndex.P1)
                ? abilityCooldownP1
                : abilityCooldownP2;

        if (cd > 0f) {
            if (index == Player.PlayerIndex.P1) {
                abilityCooldownP1 -= delta;
            } else {
                abilityCooldownP2 -= delta;
            }
            return;
        }

        boolean used = false;
        var km = KeyBindingManager.getInstance();

        if (index == Player.PlayerIndex.P1) {

            if (km.isJustPressed(KeyBindingManager.GameAction.P1_USE_ABILITY)) {
                used = callback.onAbilityInput(index, 0);
            }

            if (km.isJustPressed(KeyBindingManager.GameAction.P1_DASH)) {
                used = callback.onAbilityInput(index, 1);
            }

        } else {


            if (km.isJustPressed(KeyBindingManager.GameAction.P2_USE_ABILITY)) {
                used = callback.onAbilityInput(index, 0);
            }

            if (km.isJustPressed(KeyBindingManager.GameAction.P2_DASH)) {
                used = callback.onAbilityInput(index, 1);
            }
        }

        if (used) {
            if (index == Player.PlayerIndex.P1) {
                abilityCooldownP1 = ABILITY_COOLDOWN;
            } else {
                abilityCooldownP2 = ABILITY_COOLDOWN;
            }
        }
    }

    /**
     * Handles interaction and menu-related input.
     *
     * @param callback input callback receiver
     * @param index    player identifier
     */

    private void handleActionInput(
            InputHandlerCallback callback,
            Player.PlayerIndex index
    ) {
        var km = KeyBindingManager.getInstance();

        if (index == Player.PlayerIndex.P1) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                callback.onMenuInput();
            }
        }

        if (index == Player.PlayerIndex.P1) {
            if (km.isJustPressed(KeyBindingManager.GameAction.P1_INTERACT)) {
                callback.onInteractInput(index);
            }
        } else {
            if (km.isJustPressed(KeyBindingManager.GameAction.P2_INTERACT)) {
                callback.onInteractInput(index);
            }
        }
    }

    /**
     * Callback interface used by {@link PlayerInputHandler} to notify
     * the game logic about processed input events.
     * <p>
     * This abstraction decouples input handling from gameplay systems.
     */

    public interface InputHandlerCallback {
        /**
         * Called when a movement input is triggered.
         *
         * @param index player identifier
         * @param dx    horizontal movement direction
         * @param dy    vertical movement direction
         */

        void onMoveInput(Player.PlayerIndex index, int dx, int dy);
        float getMoveDelayMultiplier();
        /**
         * Called when a player activates an ability.
         *
         * @param index player identifier
         * @param slot  ability slot index
         * @return true if the ability was successfully used
         */

        boolean onAbilityInput(Player.PlayerIndex index, int slot);
        void onInteractInput(Player.PlayerIndex index);

        void onMenuInput();
        /**
         * Indicates whether the UI is currently consuming mouse input.
         *
         * @return true if gameplay input should be blocked
         */

        boolean isUIConsumingMouse();
    }


    public void resetTutorialMoveFlags() {
        movedUp = movedDown = movedLeft = movedRight = false;
    }
    /**
     * @return true if the player has moved in this direction (used for tutorials)
     */

    public boolean hasMovedUp() { return movedUp; }
    public boolean hasMovedDown() { return movedDown; }
    public boolean hasMovedLeft() { return movedLeft; }
    public boolean hasMovedRight() { return movedRight; }
}
