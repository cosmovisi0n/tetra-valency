package com.td.game.pillars;

public enum PillarType {
    RAPID("Rapid Spire", 1.5f, 1.0f, 1.0f, 100),
    POWER("Power Monolith", 1.0f, 1.5f, 1.0f, 100),
    SNIPER("Sniper Pedestal", 1.0f, 1.0f, 1.5f, 100);

    private final String displayName;
    private final float attackSpeedMult;
    private final float damageMult;
    private final float rangeMult;
    private final int price;

    PillarType(String displayName, float attackSpeedMult, float damageMult, float rangeMult, int price) {
        this.displayName = displayName;
        this.attackSpeedMult = attackSpeedMult;
        this.damageMult = damageMult;
        this.rangeMult = rangeMult;
        this.price = price;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getAttackSpeedMult() {
        return attackSpeedMult;
    }

    public float getDamageMult() {
        return damageMult;
    }

    public float getRangeMult() {
        return rangeMult;
    }

    public int getPrice() {
        return price;
    }
}
