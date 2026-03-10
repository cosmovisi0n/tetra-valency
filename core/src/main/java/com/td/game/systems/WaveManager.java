package com.td.game.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.td.game.elements.Element;
import com.td.game.entities.*;
import com.td.game.utils.ModelFactory;

public class WaveManager implements Disposable {
    private static final int MAX_WAVES = 50;
    private static final int PHOTO_EMPTY_TILES_BETWEEN_ENEMIES = 0;

    private int currentWave;
    private int enemiesSpawned;
    private int enemiesInWave;
    private float spawnTimer;
    private float spawnInterval;
    private boolean waveInProgress;
    private boolean allWavesComplete;

    private Array<Vector3> pathWaypoints;
    private Array<Enemy> activeEnemies;
    private ModelFactory modelFactory;
    private final Array<Integer> photoEnemyTypes;

    private static final int TYPE_PINK_BLOB = 0;
    private static final int TYPE_GOLEM = 1;
    private static final int TYPE_BAT = 2;

    private Model pinkBlobModel;
    private Model golemModel;
    private Model batModel;
    private Model demonModel;

    public WaveManager(Array<Vector3> pathWaypoints, ModelFactory modelFactory) {
        this.pathWaypoints = pathWaypoints;
        this.modelFactory = modelFactory;
        this.currentWave = 0;
        this.waveInProgress = false;
        this.allWavesComplete = false;
        this.activeEnemies = new Array<>();
        this.photoEnemyTypes = new Array<>();

        pinkBlobModel = modelFactory.loadPinkBlobModel();
        photoEnemyTypes.add(TYPE_PINK_BLOB);

        golemModel = modelFactory.loadGolemModel();
        photoEnemyTypes.add(TYPE_GOLEM);

        batModel = modelFactory.loadBatModel();
        photoEnemyTypes.add(TYPE_BAT);

        demonModel = modelFactory.loadDemonModel();
    }

    public void startNextWave() {
        if (waveInProgress)
            return;

        currentWave++;
        waveInProgress = true;

        // Calculate enemies for this wave
        enemiesInWave = getEnemiesForWave(currentWave);
        enemiesSpawned = 0;
        spawnTimer = 0f;
        spawnInterval = getSpawnIntervalForWave(currentWave);

        Gdx.app.log("WaveManager", String.format("Starting wave %d with %d enemies, spawn interval: %.1f",
                currentWave, enemiesInWave, spawnInterval));
    }

    public int getEnemiesForWave(int wave) {
        if (wave == 50) {
            return 1;
        }
        return 10 + (wave * 2);
    }

    private float getSpawnIntervalForWave(int wave) {
        // Faster spawning as waves progress
        return Math.max(0.8f, 2.0f - (wave * 0.05f));
    }

    public void update(float deltaTime) {
        if (!waveInProgress)
            return;

        // Spawn enemies over time
        if (enemiesSpawned < enemiesInWave) {
            spawnTimer += deltaTime;
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0f;
                spawnNextEnemy();
                enemiesSpawned++;
            }
        }

        // Check wave completion
        if (enemiesSpawned >= enemiesInWave && getAliveEnemyCount() == 0) {
            waveInProgress = false;
            if (currentWave >= MAX_WAVES) {
                allWavesComplete = true;
            }
        }
    }

    private void spawnNextEnemy() {
        Enemy enemy = createEnemyForWave(currentWave, enemiesSpawned);
        enemy.setWaypoints(pathWaypoints);
        activeEnemies.add(enemy);
        Gdx.app.log("WaveManager", "Spawned enemy: " + enemy.getName() + " with element: " + enemy.getElement());
    }

    private Enemy createEnemyForWave(int wave, int index) {
        if (wave == 50) {
            DemonEnemy boss = new DemonEnemy(5000f, 0.4f, 1000);
            boss.setModel(demonModel);
            boss.setVisualScaleMultiplier(4.0f);
            return boss;
        }

        if (photoEnemyTypes.size == 0) {
            throw new IllegalStateException("No enemy models found");
        }

        int type = photoEnemyTypes.get(index % photoEnemyTypes.size);
        Enemy enemy;

        float healthMult = 1f + (wave * 0.15f);
        float speedMult = Math.min(1.5f, 1f + (wave * 0.01f));

        switch (type) {
            case TYPE_PINK_BLOB:
                enemy = new PinkBlobEnemy(
                        55f * healthMult,
                        1.2f * speedMult,
                        Math.round(10 * (1 + wave * 0.1f)));
                enemy.setModel(pinkBlobModel);
                enemy.setVisualScaleMultiplier(1.2f);
                break;

            case TYPE_GOLEM:
                enemy = new GolemEnemy(
                        220f * healthMult,
                        0.6f * speedMult,
                        Math.round(40 * (1 + wave * 0.1f)));
                enemy.setModel(golemModel);
                enemy.setVisualScaleMultiplier(3.0f);
                break;

            case TYPE_BAT:
            default:
                enemy = new BatEnemy(
                        100f * healthMult,
                        1.3f * speedMult,
                        Math.round(30 * (1 + wave * 0.1f)));
                enemy.setModel(batModel);
                enemy.setVisualScaleMultiplier(1.3f);
                break;
        }

        Element randomElement = Element.values()[MathUtils.random(Element.values().length - 1)];
        enemy.setElement(randomElement);

        Gdx.app.log("WaveManager", String.format("Created enemy: %s, Health: %.0f, Element: %s",
                enemy.getName(), enemy.getMaxHealth(), randomElement.name()));

        return enemy;
    }

    public int getAliveEnemyCount() {
        int count = 0;
        for (Enemy enemy : activeEnemies) {
            if (enemy.isAlive())
                count++;
        }
        return count;
    }

    public Array<Enemy> getActiveEnemies() {
        return activeEnemies;
    }

    public void removeDeadEnemies() {
        for (int i = activeEnemies.size - 1; i >= 0; i--) {
            Enemy enemy = activeEnemies.get(i);
            if (!enemy.isAlive()) {
                activeEnemies.removeIndex(i);
            }
        }
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public void setCurrentWave(int currentWave) {
        this.currentWave = currentWave;
    }

    public boolean isWaveInProgress() {
        return waveInProgress;
    }

    public boolean areAllWavesComplete() {
        return allWavesComplete;
    }

    public int getMaxWaves() {
        return MAX_WAVES;
    }

    public int getEnemiesRemaining() {
        return (enemiesInWave - enemiesSpawned) + getAliveEnemyCount();
    }

    @Override
    public void dispose() {
        if (pinkBlobModel != null)
            pinkBlobModel.dispose();
        if (golemModel != null)
            golemModel.dispose();
        if (batModel != null)
            batModel.dispose();
        if (demonModel != null)
            demonModel.dispose();
        activeEnemies.clear();
    }

    private boolean hasModelFile(String fileName) {
        String path = "assets/3dmodels/" + fileName;
        boolean exists = Gdx.files.internal(path).exists();
        Gdx.app.log("WaveManager", "Checking for model file: " + path + " - " + (exists ? "Found" : "Not found"));
        return exists;
    }
}
