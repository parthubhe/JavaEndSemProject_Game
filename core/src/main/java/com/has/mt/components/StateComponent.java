package com.has.mt.components;

import com.badlogic.gdx.Gdx;
import com.has.mt.gameobjects.Character.State; // Use Character's State enum

public class StateComponent {
    private State currentState;
    private State previousState;

    public StateComponent(State initialState) {
        if(initialState == null) {
            Gdx.app.error("StateComponent", "Attempted to initialize with null state! Defaulting to IDLE.");
            initialState = State.IDLE;
        }
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
            // Gdx.app.debug("StateComponent", "State changed from " + previousState + " to " + currentState); // Log state changes for debugging
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    public State getPreviousState() {
        return previousState;
    }

    public boolean isState(State state) {
        if(state == null) return false; // Cannot be in a null state
        return this.currentState == state;
    }
}
