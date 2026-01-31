package de.tum.cit.fop.maze.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import java.util.HashMap;
import java.util.Map;

/**
 * Central manager for configurable key bindings.
 * <p>
 * This class maps high-level game actions to keyboard or mouse inputs,
 * supports runtime rebinding, and persists settings using {@link Preferences}.
 * <p>
 * Implemented as a singleton to ensure consistent input handling
 * across the entire game.
 */
public class KeyBindingManager {

    private static KeyBindingManager instance;
    private final Preferences prefs;
    private static final String PREFS_NAME = "maze_controls_settings";
    /**
     * Represents all abstract actions that can be triggered by player input.
     * <p>
     * Game logic should depend on these actions rather than raw key codes
     * to allow flexible rebinding and multiple input devices.
     */

    public enum GameAction {

        P1_MOVE_UP,
        P1_MOVE_DOWN,
        P1_MOVE_LEFT,
        P1_MOVE_RIGHT,
        P1_USE_ABILITY,
        P1_DASH,
        P1_INTERACT,

        P2_MOVE_UP,
        P2_MOVE_DOWN,
        P2_MOVE_LEFT,
        P2_MOVE_RIGHT,
        P2_USE_ABILITY,
        P2_DASH,
        P2_INTERACT,

        CONSOLE,
        PAUSE
    }
    private final Map<GameAction, Integer> keyBindings;

    private KeyBindingManager() {
        keyBindings = new HashMap<>();
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        load();
    }
    /**
     * Returns the singleton instance of the key binding manager.
     *
     * @return shared {@code KeyBindingManager} instance
     */

    public static KeyBindingManager getInstance() {
        if (instance == null) {
            instance = new KeyBindingManager();
        }
        return instance;
    }
    /**
     * Loads all key bindings from preferences or assigns default values.
     */

    private void load() {

        loadBinding(GameAction.P1_MOVE_UP, Input.Keys.W);
        loadBinding(GameAction.P1_MOVE_DOWN, Input.Keys.S);
        loadBinding(GameAction.P1_MOVE_LEFT, Input.Keys.A);
        loadBinding(GameAction.P1_MOVE_RIGHT, Input.Keys.D);

        loadBinding(GameAction.P1_USE_ABILITY, Input.Keys.SPACE);
        loadBinding(GameAction.P1_DASH, Input.Keys.SHIFT_LEFT);
        loadBinding(GameAction.P1_INTERACT, Input.Keys.E);

        loadBinding(GameAction.P2_MOVE_UP, Input.Keys.UP);
        loadBinding(GameAction.P2_MOVE_DOWN, Input.Keys.DOWN);
        loadBinding(GameAction.P2_MOVE_LEFT, Input.Keys.LEFT);
        loadBinding(GameAction.P2_MOVE_RIGHT, Input.Keys.RIGHT);

        loadBinding(GameAction.P2_USE_ABILITY, Input.Buttons.LEFT);
        loadBinding(GameAction.P2_DASH, Input.Buttons.RIGHT);
        loadBinding(GameAction.P2_INTERACT, Input.Keys.NUM_1);

        loadBinding(GameAction.CONSOLE, Input.Keys.GRAVE);
        loadBinding(GameAction.PAUSE, Input.Keys.ESCAPE);

    }

    /**
     * Loads a single key binding from preferences.
     *
     * @param action     game action to bind
     * @param defaultKey key used if no saved binding exists
     */

    private void loadBinding(GameAction action, int defaultKey) {
        int keyCode = prefs.getInteger(action.name(), defaultKey);
        keyBindings.put(action, keyCode);
    }
    /**
     * Updates the key binding for a given action and saves it immediately.
     *
     * @param action     game action to rebind
     * @param newKeyCode new key or mouse button code
     */

    public void setBinding(GameAction action, int newKeyCode) {
        keyBindings.put(action, newKeyCode);
        prefs.putInteger(action.name(), newKeyCode);
        prefs.flush();
    }
    /**
     * Returns the key or mouse button code bound to the given action.
     */

    public int getKey(GameAction action) {
        return keyBindings.getOrDefault(action, Input.Keys.UNKNOWN);
    }
    /**
     * Returns a human-readable name of the key bound to the given action.
     */

    public String getKeyName(GameAction action) {
        return Input.Keys.toString(getKey(action));
    }

    /**
     * Checks whether the input bound to the given action is currently pressed.
     * <p>
     * Supports both keyboard keys and mouse buttons transparently.
     *
     * @param action game action to check
     * @return true if the input is currently pressed
     */

    public boolean isPressed(GameAction action) {
        int code = getKey(action);

        if (code == Input.Buttons.LEFT || code == Input.Buttons.RIGHT) {
            return Gdx.input.isButtonPressed(code);
        }

        return Gdx.input.isKeyPressed(code);
    }
    /**
     * Checks whether the input bound to the given action was pressed this frame.
     *
     * @param action game action to check
     * @return true if the input was just pressed
     */

    public boolean isJustPressed(GameAction action) {
        int code = getKey(action);

        if (code == Input.Buttons.LEFT || code == Input.Buttons.RIGHT) {
            return Gdx.input.isButtonJustPressed(code);
        }

        return Gdx.input.isKeyJustPressed(code);
    }



    /**
     * Resets all key bindings to their default values
     * and overwrites any saved user preferences.
     */

    public void resetToDefaults() {


        setBinding(GameAction.P1_MOVE_UP,    Input.Keys.W);
        setBinding(GameAction.P1_MOVE_DOWN,  Input.Keys.S);
        setBinding(GameAction.P1_MOVE_LEFT,  Input.Keys.A);
        setBinding(GameAction.P1_MOVE_RIGHT, Input.Keys.D);

        setBinding(GameAction.P1_USE_ABILITY, Input.Keys.SPACE);
        setBinding(GameAction.P1_DASH,        Input.Keys.SHIFT_LEFT);
        setBinding(GameAction.P1_INTERACT,    Input.Keys.E);

        setBinding(GameAction.P2_MOVE_UP,    Input.Keys.UP);
        setBinding(GameAction.P2_MOVE_DOWN,  Input.Keys.DOWN);
        setBinding(GameAction.P2_MOVE_LEFT,  Input.Keys.LEFT);
        setBinding(GameAction.P2_MOVE_RIGHT, Input.Keys.RIGHT);

        setBinding(GameAction.P2_USE_ABILITY, Input.Buttons.LEFT);
        setBinding(GameAction.P2_DASH,        Input.Buttons.RIGHT);
        setBinding(GameAction.P2_INTERACT,    Input.Keys.NUM_1);


        setBinding(GameAction.CONSOLE, Input.Keys.GRAVE);
    }

}
