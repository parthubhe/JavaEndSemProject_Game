// src/com/has/mt/components/StateComponent.java
package com.has.mt.components;

import com.badlogic.gdx.Gdx;
import com.has.mt.gameobjects.Character.State; // Use Character's State enum

public class StateComponent {
    private State currentState;
    private State previousState;

    public StateComponent(State initialState) {
        this.currentState = initialState;
        this.previousState = initialState;
    }

    public void setState(State newState) {
        if (newState == null) {
            Gdx.app.error("StateComponent", "Attempted to set null state!");
            return; // Avoid setting null state
        }
        if (this.currentState != newState) {
            this.previousState = this.currentState;
            this.currentState = newState;
            // Optional: Log state changes for debugging
            // Gdx.app.log("StateComponent", "State changed from " + previousState + " to " + currentState);
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    public State getPreviousState() {
        return previousState;
    }

    public boolean isState(State state) {
        return this.currentState == state;
    }
}
