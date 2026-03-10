package com.td.game.systems;

public class SaveManager {

    private static final String SAVE_DIR = "Documents/TetraValency/";
    
    public static final String SAVE_FILE_ELEMENTAL_CASTLE = SAVE_DIR + "save_elemental_castle.json";
    public static final String SAVE_FILE_DESERT_OASIS = SAVE_DIR + "save_desert_oasis.json";

    public static String getSaveFilePath(com.td.game.map.GameMap.MapType mapType) {
        if (mapType == com.td.game.map.GameMap.MapType.DESERT_OASIS) {
            return SAVE_FILE_DESERT_OASIS;
        }
        return SAVE_FILE_ELEMENTAL_CASTLE;
    }

    public static void save(SaveData data, com.td.game.map.GameMap.MapType mapType) {
        try {
            String path = getSaveFilePath(mapType);
            com.badlogic.gdx.files.FileHandle file = com.badlogic.gdx.Gdx.files.external(path);
            file.writeString(data.toJson(), false);
            com.badlogic.gdx.Gdx.app.log("SaveManager", "Game saved successfully to " + path);
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.error("SaveManager", "Failed to save game", e);
        }
    }

    public static SaveData load(com.td.game.map.GameMap.MapType mapType) {
        try {
            String path = getSaveFilePath(mapType);
            com.badlogic.gdx.files.FileHandle file = com.badlogic.gdx.Gdx.files.external(path);
            if (file.exists()) {
                String jsonStr = file.readString();
                SaveData data = SaveData.fromJson(jsonStr);
                com.badlogic.gdx.Gdx.app.log("SaveManager", "Game loaded successfully from " + path);
                return data;
            }
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.error("SaveManager", "Failed to load game", e);
        }
        return null;
    }

    public static boolean hasSave(com.td.game.map.GameMap.MapType mapType) {
        String path = getSaveFilePath(mapType);
        return com.badlogic.gdx.Gdx.files.external(path).exists();
    }

    public static void deleteSave(com.td.game.map.GameMap.MapType mapType) {
        String path = getSaveFilePath(mapType);
        com.badlogic.gdx.files.FileHandle file = com.badlogic.gdx.Gdx.files.external(path);
        if (file.exists()) {
            file.delete();
            com.badlogic.gdx.Gdx.app.log("SaveManager", "Deleted save file " + path);
        }
    }
}
