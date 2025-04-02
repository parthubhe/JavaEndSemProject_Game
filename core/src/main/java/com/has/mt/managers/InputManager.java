package com.has.mt.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.ObjectMap;

public class InputManager {

    public enum Action {
        MOVE_LEFT, MOVE_RIGHT, JUMP, RUN,
        ATTACK_LIGHT, ATTACK_HEAVY, ATTACK_SPECIAL1, ATTACK_SPECIAL2,
        DEFEND, // Added Defend Action
        PAUSE
    }

    private static ObjectMap<Action, Integer> keyMappings = new ObjectMap<>();

    static {
        // Default mappings
        keyMappings.put(Action.MOVE_LEFT, Input.Keys.A);
        keyMappings.put(Action.MOVE_RIGHT, Input.Keys.D);
        keyMappings.put(Action.JUMP, Input.Keys.SPACE);
        keyMappings.put(Action.RUN, Input.Keys.SHIFT_LEFT);
        keyMappings.put(Action.ATTACK_LIGHT, Input.Buttons.LEFT);
        keyMappings.put(Action.ATTACK_HEAVY, Input.Keys.F);
        keyMappings.put(Action.ATTACK_SPECIAL1, Input.Keys.E); // For primary special/projectile
        keyMappings.put(Action.ATTACK_SPECIAL2, Input.Keys.V); // For secondary special/projectile
        keyMappings.put(Action.DEFEND, Input.Keys.Q); // Mapped Q to DEFEND
        keyMappings.put(Action.PAUSE, Input.Keys.ESCAPE);
    }

    public static boolean isActionPressed(Action action) {
        int code = keyMappings.get(action, -1);
        if (code == -1) return false;

        // Check if it's a Key or a Button
        if (code >= Input.Keys.UNKNOWN && code <= Input.Keys.MAX_KEYCODE) { // It's a key
            return Gdx.input.isKeyPressed(code);
        } else if (code >= 0 && code <= Input.Buttons.FORWARD) { // Check if it's a valid button code
            return Gdx.input.isButtonPressed(code);
        }
        return false;
    }

    public static boolean isActionJustPressed(Action action) {
        int code = keyMappings.get(action, -1);
        if (code == -1) return false;

        if (code >= Input.Keys.UNKNOWN && code <= Input.Keys.MAX_KEYCODE) { // Key
            return Gdx.input.isKeyJustPressed(code);
        } else if (code >= 0 && code <= Input.Buttons.FORWARD) { // Button
            return Gdx.input.isButtonJustPressed(code);
        }
        return false;
    }

    /**
     * Checks if the key/button associated with the action was just released this frame.
     * NOTE: LibGDX does not have a built-in `isKeyJustReleased` or `isButtonJustReleased`.
     * This method is a placeholder and will currently always return false.
     * Proper implementation requires tracking input state across frames (e.g., using an InputProcessor).
     * For simple cases like exiting a 'Defend' state, check !isActionPressed(Action.DEFEND) instead.
     * @param action The action to check.
     * @return Currently always false.
     */
    public static boolean isActionJustReleased(Action action) {
        // Placeholder - Requires state tracking for proper implementation
        return false;
    }
}
