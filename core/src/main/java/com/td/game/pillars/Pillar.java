package com.td.game.pillars;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.td.game.elements.Element;
import com.td.game.utils.ModelFactory;

public class Pillar implements Disposable {
    private final PillarType type;
    private final Vector3 position;
    private Element currentElement;
    private Element firstOrb;
    private boolean active;

    private ModelInstance pillarModelInstance;
    private final ModelFactory modelFactory;

    private final float baseRange = 5f * (com.td.game.utils.Constants.TILE_SIZE / 2.0f);
    private float bonusDamageMult = 1f;
    private float bonusRangeMult = 1f;
    private float bonusAttackSpeedMult = 1f;
    private float nextHitDamageMultiplier = 1f;

    private float attackTimer = 0f;
    private float goldAccumulator = 0f;

    public Pillar(PillarType type, Vector3 position, ModelFactory modelFactory) {
        this.type = type;
        this.position = position.cpy();
        this.active = false;
        this.currentElement = null;
        this.firstOrb = null;
        this.modelFactory = modelFactory;
        updateModel();
    }

    private void updateModel() {
        Color orbColor = null;
        if (currentElement != null) {
            orbColor = new Color(currentElement.getR(), currentElement.getG(), currentElement.getB(), 1f);
        }
        Model model = modelFactory.createPillarModel(type, orbColor);
        this.pillarModelInstance = new ModelInstance(model);
        this.pillarModelInstance.transform.setToTranslation(position.x, position.y, position.z);
        this.pillarModelInstance.transform.scl(com.td.game.utils.Constants.TILE_SIZE / 2.0f);
    }

    public com.badlogic.gdx.math.collision.BoundingBox getBoundingBox() {
        float scale = com.td.game.utils.Constants.TILE_SIZE / 2.0f;
        Vector3 min = position.cpy().add(-0.6f * scale, 0, -0.6f * scale);
        Vector3 max = position.cpy().add(0.6f * scale, 2.0f * scale, 0.6f * scale);
        return new com.badlogic.gdx.math.collision.BoundingBox(min, max);
    }

    public boolean placeOrb(Element element) {
        if (element == null)
            return false;

        if (!active) {
            firstOrb = element.isPrime() ? element : null;
            currentElement = element;
            active = true;
            updateModel();
            return true;
        } else if (firstOrb != null && currentElement == firstOrb && element.isPrime()) {
            Element merged = Element.merge(firstOrb, element);
            if (merged != null) {
                currentElement = merged;
                firstOrb = null;
                updateModel();
                return true;
            }
        }
        currentElement = element;
        firstOrb = element.isPrime() ? element : null;
        updateModel();
        return true;
    }

    public void update(float delta, com.td.game.screens.GameScreen screen) {
        if (!active || currentElement == null)
            return;

        float attackCooldown = 1.0f / (type.getAttackSpeedMult() * bonusAttackSpeedMult);
        attackTimer -= delta;

        // Gold: passive gold income, no projectiles
        if (currentElement == com.td.game.elements.Element.GOLD) {
            goldAccumulator += delta;
            if (goldAccumulator >= 1.0f) {
                int goldEarned = (int) (5 * bonusDamageMult * type.getDamageMult());
                screen.earnGold(goldEarned);
                goldAccumulator -= 1.0f;
            }
            return;
        }

        // Life: revive dead enemies as allies walking backward
        if (currentElement == com.td.game.elements.Element.LIFE) {
            if (attackTimer <= 0) {
                float range = getAttackRange();
                for (com.td.game.entities.Enemy enemy : screen.getWaveManager().getActiveEnemies()) {
                    if (!enemy.isAlive() && !enemy.isRevived() && enemy.getPosition().dst(position) <= range) {
                        enemy.reviveAsAlly();
                        attackTimer = attackCooldown;
                        break;
                    }
                }
            }
            return;
        }

        // Normal attack for all other elements
        if (attackTimer <= 0) {
            com.td.game.entities.Enemy target = findTarget(screen.getWaveManager());
            if (target != null) {
                float elementMult = currentElement.getBaseDamageMultiplier();
                float damage = 20.0f * bonusDamageMult * type.getDamageMult() * elementMult * nextHitDamageMultiplier;
                float projSpeed = 15.0f;

                nextHitDamageMultiplier = 1f; // Reset after use

                Vector3 spawnPos = position.cpy();
                spawnPos.y += 2.0f;

                screen.spawnProjectile(spawnPos, target, currentElement, projSpeed, damage);
                attackTimer = attackCooldown;
            }
        }
    }

    private com.td.game.entities.Enemy findTarget(com.td.game.systems.WaveManager waveManager) {
        if (waveManager == null)
            return null;
        float range = getAttackRange();
        com.td.game.entities.Enemy bestTarget = null;
        float shortestDist = Float.MAX_VALUE;

        for (com.td.game.entities.Enemy enemy : waveManager.getActiveEnemies()) {
            if (enemy.isAlive()) {
                float dst = enemy.getPosition().dst(position);
                if (dst <= range && dst < shortestDist) {
                    shortestDist = dst;
                    bestTarget = enemy;
                }
            }
        }
        return bestTarget;
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        if (pillarModelInstance != null) {
            modelBatch.render(pillarModelInstance, environment);
        }
    }

    public boolean isActive() {
        return active;
    }

    public Element getCurrentElement() {
        return currentElement;
    }

    public PillarType getType() {
        return type;
    }

    public Vector3 getPosition() {
        return position;
    }

    public float getAttackRange() {
        float range = 8.0f * type.getRangeMult();
        if (currentElement != null) {
            range *= currentElement.getBaseRangeMultiplier();
        }
        return range * bonusRangeMult;
    }

    public boolean canAcceptOrb() {
        return !active || (firstOrb != null && currentElement == firstOrb);
    }

    public Element removeOrb() {
        if (currentElement == null) {
            return null;
        }
        Element removed = currentElement;
        currentElement = null;
        firstOrb = null;
        active = false;
        updateModel();
        return removed;
    }

    public void setExternalMultipliers(float damageMult, float rangeMult, float attackSpeedMult) {
        float d = Math.max(0.1f, damageMult);
        float r = Math.max(0.1f, rangeMult);
        float s = Math.max(0.1f, attackSpeedMult);

        if (Math.abs(bonusDamageMult - d) < 0.0001f &&
                Math.abs(bonusRangeMult - r) < 0.0001f &&
                Math.abs(bonusAttackSpeedMult - s) < 0.0001f) {
            return;
        }

        bonusDamageMult = d;
        bonusRangeMult = r;
        bonusAttackSpeedMult = s;
    }

    public void setNextHitBuff(float multiplier) {
        this.nextHitDamageMultiplier = multiplier;
    }

    public void save(com.td.game.systems.SaveData.PillarSaveData pData) {
        pData.type = this.type.name();
        pData.x = this.position.x;
        pData.y = this.position.y;
        pData.z = this.position.z;
        pData.currentElement = this.currentElement != null ? this.currentElement.name() : null;
        pData.firstOrb = this.firstOrb != null ? this.firstOrb.name() : null;
        pData.active = this.active;
        pData.bonusDamageMult = this.bonusDamageMult;
        pData.bonusRangeMult = this.bonusRangeMult;
        pData.bonusAttackSpeedMult = this.bonusAttackSpeedMult;
    }

    public void load(com.td.game.systems.SaveData.PillarSaveData pData) {
        this.position.set(pData.x, pData.y, pData.z);
        this.currentElement = pData.currentElement != null ? Element.valueOf(pData.currentElement) : null;
        this.firstOrb = pData.firstOrb != null ? Element.valueOf(pData.firstOrb) : null;
        this.active = pData.active;
        this.bonusDamageMult = pData.bonusDamageMult;
        this.bonusRangeMult = pData.bonusRangeMult;
        this.bonusAttackSpeedMult = pData.bonusAttackSpeedMult;
        updateModel();
    }

    @Override
    public void dispose() {
    }
}
