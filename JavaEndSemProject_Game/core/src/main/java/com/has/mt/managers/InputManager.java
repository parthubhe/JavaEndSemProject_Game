// src/com/has/mt/managers/InputManager.java
package com.has.mt.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.ObjectMap;

// Simple static input manager (can be made instance-based)
public class InputManager {

    public enum Action {
        MOVE_LEFT, MOVE_RIGHT, JUMP, RUN,
        ATTACK_LIGHT, ATTACK_HEAVY, ATTACK_SPECIAL1, ATTACK_SPECIAL2,
        PAUSE
    }

    private static ObjectMap<Action, Integer> keyMappings = new ObjectMap<>();

    static {
        // Default mappings
        keyMappings.put(Action.MOVE_LEFT, Input.Keys.A);
        keyMappings.put(Action.MOVE_RIGHT, Input.Keys.D);
        keyMappings.put(Action.JUMP, Input.Keys.SPACE);
        keyMappings.put(Action.RUN, Input.Keys.SHIFT_LEFT);
        keyMappings.put(Action.ATTACK_LIGHT, Input.Buttons.LEFT); // Mouse button
        keyMappings.put(Action.ATTACK_HEAVY, Input.Keys.F);
        keyMappings.put(Action.ATTACK_SPECIAL1, Input.Keys.E); // Charged
        keyMappings.put(Action.ATTACK_SPECIAL2, Input.Keys.V); // Vaderstrike
        keyMappings.put(Action.PAUSE, Input.Keys.ESCAPE);
    }

    // Method to check if an action key/button is currently pressed
    public static boolean isActionPressed(Action action) {
        int code = keyMappings.get(action, -1);
        if (code == -1) return false;

        if (code >= 0 && code <= 255) { // It's a key
            return Gdx.input.isKeyPressed(code);
        } else if (code == Input.Buttons.LEFT || code == Input.Buttons.RIGHT || code == Input.Buttons.MIDDLE) { // It's a mouse button
            return Gdx.input.isButtonPressed(code);
        }
        return false;
    }

    // Method to check if an action key/button was just pressed this frame
    public static boolean isActionJustPressed(Action action) {
        int code = keyMappings.get(action, -1);
        if (code == -1) return false;

        if (code >= 0 && code <= 255) { // Key
            return Gdx.input.isKeyJustPressed(code);
        } else if (code == Input.Buttons.LEFT || code == Input.Buttons.RIGHT || code == Input.Buttons.MIDDLE) { // Mouse button
            return Gdx.input.isButtonJustPressed(code);
        }
        return false;
    }

    // TODO: Add methods for setting custom key mappings
}
