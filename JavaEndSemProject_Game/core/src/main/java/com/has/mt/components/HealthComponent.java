// src/com/has/mt/components/HealthComponent.java
package com.has.mt.components;

public class HealthComponent {
    private int maxHealth;
    private int currentHealth;

    public HealthComponent(int maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public void decreaseHealth(int amount) {
        currentHealth -= amount;
        if (currentHealth < 0) {
            currentHealth = 0;
        }
    }

    public void increaseHealth(int amount) {
        currentHealth += amount;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = Math.max(0, Math.min(currentHealth, maxHealth));
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public float getHealthPercentage() {
        return (float) currentHealth / maxHealth;
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public void reset() {
        currentHealth = maxHealth;
    }
}
