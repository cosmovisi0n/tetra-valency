package com.td.game.systems;

public class SaveManager {

    private static final String LEGACY_SAVE_DIR = "Documents/TetraValency/";
    private static final String LOCAL_SAVE_DIR = "saves/";

    public static final String SAVE_FILE_ELEMENTAL_CASTLE = "save_elemental_castle.json";
    public static final String SAVE_FILE_DESERT_OASIS = "save_desert_oasis.json";

    private static String getSaveFileName(com.td.game.map.GameMap.MapType mapType) {
        return mapType == com.td.game.map.GameMap.MapType.DESERT_OASIS
                ? SAVE_FILE_DESERT_OASIS
                : SAVE_FILE_ELEMENTAL_CASTLE;
    }

    private static com.badlogic.gdx.files.FileHandle getLocalSaveDir() {
        com.badlogic.gdx.files.FileHandle dir = com.badlogic.gdx.Gdx.files.local(LOCAL_SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static com.badlogic.gdx.files.FileHandle getLocalSaveFile(com.td.game.map.GameMap.MapType mapType) {
        return getLocalSaveDir().child(getSaveFileName(mapType));
    }

    private static com.badlogic.gdx.files.FileHandle getLegacySaveFile(com.td.game.map.GameMap.MapType mapType) {
        return com.badlogic.gdx.Gdx.files.external(LEGACY_SAVE_DIR).child(getSaveFileName(mapType));
    }

    private static void migrateLegacySaveIfNeeded(com.td.game.map.GameMap.MapType mapType) {
        com.badlogic.gdx.files.FileHandle localFile = getLocalSaveFile(mapType);
        if (localFile.exists()) {
            return;
        }
        com.badlogic.gdx.files.FileHandle legacyFile = getLegacySaveFile(mapType);
        if (legacyFile.exists()) {
            localFile.writeString(legacyFile.readString(), false);
            com.badlogic.gdx.Gdx.app.log("SaveManager",
                    "Migrated legacy save to " + localFile.file().getAbsolutePath());
        }
    }

    public static void save(SaveData data, com.td.game.map.GameMap.MapType mapType) {
        try {
            com.badlogic.gdx.files.FileHandle file = getLocalSaveFile(mapType);
            file.writeString(data.toJson(), false);
            com.badlogic.gdx.Gdx.app.log("SaveManager", "Game saved successfully to " + file.file().getAbsolutePath());
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.error("SaveManager", "Failed to save game", e);
        }
    }

    public static SaveData load(com.td.game.map.GameMap.MapType mapType) {
        try {
            migrateLegacySaveIfNeeded(mapType);
            com.badlogic.gdx.files.FileHandle file = getLocalSaveFile(mapType);
            if (file.exists()) {
                String jsonStr = file.readString();
                SaveData data = SaveData.fromJson(jsonStr);
                com.badlogic.gdx.Gdx.app.log("SaveManager", "Game loaded successfully from " + file.file().getAbsolutePath());
                return data;
            }
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.error("SaveManager", "Failed to load game", e);
        }
        return null;
    }

    public static boolean hasSave(com.td.game.map.GameMap.MapType mapType) {
        migrateLegacySaveIfNeeded(mapType);
        return getLocalSaveFile(mapType).exists();
    }

    public static void deleteSave(com.td.game.map.GameMap.MapType mapType) {
        com.badlogic.gdx.files.FileHandle localFile = getLocalSaveFile(mapType);
        if (localFile.exists()) {
            localFile.delete();
            com.badlogic.gdx.Gdx.app.log("SaveManager", "Deleted save file " + localFile.file().getAbsolutePath());
        }
        com.badlogic.gdx.files.FileHandle legacyFile = getLegacySaveFile(mapType);
        if (legacyFile.exists()) {
            legacyFile.delete();
            com.badlogic.gdx.Gdx.app.log("SaveManager", "Deleted legacy save file " + legacyFile.file().getAbsolutePath());
        }
    }
}
