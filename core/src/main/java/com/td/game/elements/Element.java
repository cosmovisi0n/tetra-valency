package com.td.game.elements;

public enum Element {

    FIRE("Fire", "🔥", 1f, 0.2f, 0.1f),
    WATER("Water", "💧", 0.2f, 0.4f, 1f),
    EARTH("Earth", "🪨", 0.6f, 0.4f, 0.2f),
    AIR("Air", "💨", 1f, 1f, 0.3f),

    STEAM("Steam", "☁️", 0.7f, 0.7f, 0.7f),
    ICE("Ice", "❄️", 0.4f, 0.9f, 1f),
    POISON("Poison", "☠️", 0.2f, 0.8f, 0.2f),
    LIGHT("Light", "✨", 1f, 1f, 0.8f),
    GOLD("Gold", "💰", 1f, 0.84f, 0f),
    LIFE("Life", "🌿", 0.3f, 0.9f, 0.4f);

    private final String displayName;
    private final String emoji;
    private final float r, g, b;

    Element(String displayName, String emoji, float r, float g, float b) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public Tier getTier() {
        return isPrime() ? Tier.BASIC : Tier.HYBRID;
    }

    public float getBaseRangeMultiplier() {
        return getTier() == Tier.HYBRID ? 1.2f : 1.0f;
    }

    public float getBaseDamageMultiplier() {
        return getTier() == Tier.HYBRID ? 1.3f : 1.0f;
    }

    public boolean isPrime() {
        return this == FIRE || this == WATER || this == EARTH || this == AIR;
    }

    public static Element merge(Element a, Element b) {
        if (a == null || b == null)
            return null;
        if (!a.isPrime() || !b.isPrime())
            return null;
        if (a == b)
            return null;

        if (a.ordinal() > b.ordinal()) {
            Element temp = a;
            a = b;
            b = temp;
        }

        if (a == FIRE && b == WATER)
            return STEAM;

        if (a == WATER && b == AIR)
            return ICE;

        if (a == WATER && b == EARTH)
            return POISON;

        if (a == FIRE && b == AIR)
            return LIGHT;

        if (a == FIRE && b == EARTH)
            return GOLD;

        if (a == EARTH && b == AIR)
            return LIFE;

        return null;
    }

    public enum Tier {
        BASIC, HYBRID
    }
}
