package com.td.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.td.game.elements.Element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CombatUtils {

    private static final float MULTIPLIER_STRONG = 1.5f;
    private static final float MULTIPLIER_WEAK = 0.5f;
    private static final float MULTIPLIER_NEUTRAL = 1.0f;

    private static Map<String, Set<String>> strongAgainstMap = new HashMap<>();
    private static Map<String, Set<String>> weakAgainstMap = new HashMap<>();
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized)
            return;

        try {
            FileHandle file = Gdx.files.internal("data/element_interaction.json");
            if (!file.exists()) {
                file = Gdx.files.internal("assets/data/element_interaction.json");
            }

            if (file.exists()) {
                JsonValue root = new JsonReader().parse(file);
                JsonValue system = root.get("elemental_combat_system");
                JsonValue relationships = system.get("relationships");

                for (JsonValue rel : relationships) {
                    String elemName = rel.getString("element").toUpperCase();

                    Set<String> strong = new HashSet<>();
                    if (rel.has("strong_against")) {
                        for (String txt : rel.get("strong_against").asStringArray()) {
                            strong.add(txt.toUpperCase());
                        }
                    }
                    strongAgainstMap.put(elemName, strong);

                    Set<String> weak = new HashSet<>();
                    if (rel.has("weak_against")) {
                        for (String txt : rel.get("weak_against").asStringArray()) {
                            weak.add(txt.toUpperCase());
                        }
                    }
                    weakAgainstMap.put(elemName, weak);
                }
            } else {
                // Hardcoded fallback matrix if JSON is missing
                setupFallbackMatrix();
            }
        } catch (Exception e) {
            setupFallbackMatrix();
        }
        initialized = true;
    }

    private static void setupFallbackMatrix() {
        // Clear maps
        strongAgainstMap.clear();
        weakAgainstMap.clear();

        // Prime relationships
        addRelationship("FIRE", new String[] { "AIR", "POISON" }, new String[] { "WATER", "EARTH" });
        addRelationship("WATER", new String[] { "FIRE", "EARTH" }, new String[] { "AIR", "ICE" });
        addRelationship("EARTH", new String[] { "WATER", "GOLD" }, new String[] { "FIRE", "AIR" });
        addRelationship("AIR", new String[] { "EARTH", "ICE" }, new String[] { "FIRE", "STEAM" });

        // Hybrid relationships
        addRelationship("STEAM", new String[] { "FIRE", "AIR" }, new String[] { "WATER", "ICE" });
        addRelationship("ICE", new String[] { "WATER", "STEAM" }, new String[] { "FIRE", "LIGHT" });
        addRelationship("POISON", new String[] { "LIFE", "EARTH" }, new String[] { "FIRE", "LIGHT" });
        addRelationship("LIGHT", new String[] { "POISON", "ICE" }, new String[] { "EARTH", "GOLD" });
        addRelationship("GOLD", new String[] { "LIGHT", "EARTH" }, new String[] { "WATER", "LIFE" });
        addRelationship("LIFE", new String[] { "GOLD", "WATER" }, new String[] { "POISON", "FIRE" });
    }

    private static void addRelationship(String element, String[] strongArr, String[] weakArr) {
        Set<String> strong = new HashSet<>();
        for (String s : strongArr)
            strong.add(s);
        strongAgainstMap.put(element, strong);

        Set<String> weak = new HashSet<>();
        for (String w : weakArr)
            weak.add(w);
        weakAgainstMap.put(element, weak);
    }

    public static float getDamageMultiplier(Element attacker, Element defender) {
        if (attacker == null || defender == null)
            return MULTIPLIER_NEUTRAL;

        if (!initialized) {
            initialize();
        }

        String attackerName = attacker.getDisplayName().toUpperCase();
        String defenderName = defender.getDisplayName().toUpperCase();

        if (strongAgainstMap.containsKey(attackerName)) {
            if (strongAgainstMap.get(attackerName).contains(defenderName)) {
                return MULTIPLIER_STRONG;
            }
        }

        if (weakAgainstMap.containsKey(attackerName)) {
            if (weakAgainstMap.get(attackerName).contains(defenderName)) {
                return MULTIPLIER_WEAK;
            }
        }

        return MULTIPLIER_NEUTRAL;
    }
}
