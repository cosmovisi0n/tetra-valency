package com.td.game.projectiles;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.td.game.elements.Element;
import com.td.game.entities.Enemy;
import com.td.game.systems.WaveManager;

public class Projectile {
    private Vector3 position;
    private Enemy target;
    private Element element;
    private float speed;
    private float damage;
    private boolean active;
    private Vector3 currentDirection = new Vector3();
    private static final float SMOOTHING_FACTOR = 5.0f;

    public interface ImpactListener {
        void onImpact(Projectile projectile, Enemy target);
    }

    private ImpactListener impactListener;

    private ModelInstance modelInstance;

    private WaveManager waveManager;
    private static final float FIRE_AOE_RADIUS = 3.0f;

    // Light beam support
    private boolean isBeam = false;
    private Vector3 beamStart;
    private Vector3 beamEnd;
    private float beamTimer;
    private static final float BEAM_DURATION = 0.3f;

    public Projectile(Vector3 startPos, Enemy target, Element element, float speed, float damage,
            ModelInstance modelInstance, WaveManager waveManager) {
        this.position = new Vector3(startPos);
        this.target = target;
        this.element = element;
        this.speed = speed;
        this.damage = damage;
        this.active = true;
        this.modelInstance = modelInstance;
        this.waveManager = waveManager;

        // Light element: instant beam, no travel
        if (element == Element.LIGHT) {
            isBeam = true;
            beamStart = new Vector3(startPos);
            beamEnd = target.getPosition().cpy();
            beamEnd.y += 1.0f;
            beamTimer = BEAM_DURATION;
            onHit(); // Instant damage
        } else if (this.modelInstance != null) {
            this.modelInstance.transform.setToTranslation(position);
        }
    }

    public void update(float delta) {
        if (!active)
            return;

        // Beam just shows VFX and fades
        if (isBeam) {
            beamTimer -= delta;
            if (beamTimer <= 0) {
                active = false;
            }
            return;
        }

        if (target == null || !target.isAlive()) {
            active = false;
            return;
        }

        Vector3 targetPos = target.getPosition().cpy();
        targetPos.y += 1.0f;

        Vector3 targetDirection = targetPos.cpy().sub(position).nor();
        if (currentDirection.isZero()) {
            currentDirection.set(targetDirection);
        } else {
            currentDirection.lerp(targetDirection, delta * SMOOTHING_FACTOR).nor();
        }
        position.add(currentDirection.cpy().scl(speed * delta));

        if (modelInstance != null) {
            modelInstance.transform.setToTranslation(position);
        }

        if (position.dst(targetPos) < 0.8f) { // Adjusted threshold for smoothed movement
            onHit();
        }
    }

    public void setImpactListener(ImpactListener listener) {
        this.impactListener = listener;
    }

    private void onHit() {
        if (!isBeam) {
            active = false;
        }

        if (impactListener != null) {
            impactListener.onImpact(this, target);
        }

        switch (element) {
            case FIRE:
                // AoE damage + ramping fire stacks
                if (waveManager != null) {
                    for (Enemy e : waveManager.getActiveEnemies()) {
                        if (e.isAlive() && e.getPosition().dst(target.getPosition()) <= FIRE_AOE_RADIUS) {
                            e.takeDamage(damage, element);
                            e.addFireStack();
                        }
                    }
                } else {
                    target.takeDamage(damage, element);
                    target.addFireStack();
                }
                break;

            case WATER:
                // Slow effect
                target.takeDamage(damage, element);
                target.applySlow(2.0f, 0.5f);
                break;

            case EARTH:
                // Stun effect
                target.takeDamage(damage, element);
                target.applyStun(1.5f);
                break;

            case AIR:
                // Knockback effect
                target.takeDamage(damage, element);
                target.applyKnockback(1.5f);
                break;

            // ---- HYBRID ELEMENTS ----

            case ICE:
                // Full freeze (stops movement + animation)
                target.takeDamage(damage, element);
                target.applyFreeze(2.5f);
                break;

            case POISON:
                // Acid DoT stacks + healing block
                target.takeDamage(damage * 0.3f, element);
                target.applyPoison(4.0f, damage * 0.15f, 1);
                target.applyRegenBlock(5.0f);
                break;

            case STEAM:
                // HP%-based knockback: lower HP = more knockback
                target.takeDamage(damage, element);
                float hpPercent = target.getHealthPercent();
                float knockDist = 1.0f + (1.0f - hpPercent) * 3.0f; // 1-4 tiles
                target.applyKnockback(knockDist);
                break;

            case LIGHT:
                // Current HP% based damage (higher HP = more damage), instant beam
                float currentHpPercent = target.getHealthPercent();
                float hpBasedDamage = damage * (0.5f + currentHpPercent * 1.5f);
                target.takeDamage(hpBasedDamage, element);
                break;

            case GOLD:
                // Gold pillars don't fire projectiles (handled in Pillar.update)
                break;

            case LIFE:
                // Life pillars don't fire projectiles (handled in Pillar.update)
                break;

            default:
                target.takeDamage(damage, element);
                break;
        }
    }

    public void render(ModelBatch batch, Environment environment) {
        if (!active)
            return;

        if (isBeam) {
            // Beam rendering is handled by GameScreen's ShapeRenderer
            return;
        }

        if (modelInstance != null) {
            batch.render(modelInstance, environment);
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isBeam() {
        return isBeam;
    }

    public Vector3 getBeamStart() {
        return beamStart;
    }

    public Vector3 getBeamEnd() {
        return beamEnd;
    }

    public float getBeamTimer() {
        return beamTimer;
    }

    public Element getElement() {
        return element;
    }
}
