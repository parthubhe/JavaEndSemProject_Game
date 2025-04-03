package com.has.mt.components;

public class HealthComponent {
    private int maxHealth;
    private int currentHealth;

    public HealthComponent(int maxHealth) {
        this.maxHealth = maxHealth > 0 ? maxHealth : 1; // Ensure maxHealth is positive
        this.currentHealth = this.maxHealth;
    }

    public void decreaseHealth(int amount) {
        if(amount < 0) return; // Don't decrease by negative amount
        currentHealth -= amount;
        if (currentHealth < 0) {
            currentHealth = 0;
        }
    }

    public void increaseHealth(int amount) {
        if(amount < 0) return; // Don't increase by negative amount
        currentHealth += amount;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth > 0 ? maxHealth : 1; // Ensure positive
        if (currentHealth > this.maxHealth) {
            currentHealth = this.maxHealth;
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
        if (maxHealth <= 0) return 0f; // Avoid division by zero
        return (float) currentHealth / maxHealth;
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public void reset() {
        currentHealth = maxHealth;
    }
}
