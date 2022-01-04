/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public abstract class vanidad_beamFlakEffect implements BeamEffectPlugin {

    abstract protected BeamFlakExplosion getFlakInstance();
    private boolean hasFired = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }
        if (beam.getBrightness() < 1f) {
            return;
        }
        if (beam.getBrightness() == 1) {
            Vector2f start = beam.getFrom();
            Vector2f end = beam.getTo();
            float prevLength = beam.getLengthPrevFrame();

            if (!hasFired) {

                float maxRange = beam.getWeapon().getRange();
                float distanceRatio = getFlakInstance().computeDistanceRatio(
                        prevLength);

                boolean hasTarget = beam.getDamageTarget() != null;
                boolean mightExplode = (prevLength >= maxRange * 0.95f) || hasTarget;
                if (mightExplode && distanceRatio > 0) {

                    hasFired = true;

                    getFlakInstance().spawnDamagingExplosion(engine,
                                                             distanceRatio, beam);
                    spawnFlakVisual(engine, end, distanceRatio, beam);
                }
            }
        }
    }

    protected abstract void spawnFlakVisual(CombatEngineAPI engine, Vector2f end, float distanceRatio, BeamAPI beam);

    public class BeamFlakExplosion {

        public float minRangeForFullExplosion = 800;
        public float minRangeForNoExplosion = 350;
        public float explosionDamage = 90f;
        public float explosionRange = 250f;
        public float explosionDuration = 1f;

        public BeamFlakExplosion() {

        }

        public BeamFlakExplosion(float minRangeForFullExplosion, float minRangeForNoExplosion, float explosionDamage, float explosionRange, float explosionDuration) {
            this.minRangeForFullExplosion = minRangeForFullExplosion;
            this.minRangeForNoExplosion = minRangeForNoExplosion;
            this.explosionDamage = explosionDamage;
            this.explosionRange = explosionRange;
            this.explosionDuration = explosionDuration;
        }

        public float computeDistanceRatio(float currentLength) {
            float distanceRatio = 0;
            if (currentLength >= minRangeForFullExplosion) {
                distanceRatio = 1;
            } else if (currentLength <= minRangeForNoExplosion) {
                distanceRatio = 0;
            } else {
                distanceRatio = computedDistanceRatio(currentLength);
            }
            return distanceRatio;
        }

        public void spawnDamagingExplosion(CombatEngineAPI engine, float distanceRatio, BeamAPI beam) {
            Vector2f end = beam.getTo();
            float damageRatio = 0.5f * (distanceRatio + 1f);

            DamagingExplosionSpec explosionSpec = new DamagingExplosionSpec(0.1f,
                                                                            explosionRange * distanceRatio, //radius
                                                                            explosionRange * distanceRatio * 0.5f, //core radius
                                                                            explosionDamage * damageRatio, //max damage
                                                                            explosionDamage * damageRatio, //min damage
                                                                            CollisionClass.PROJECTILE_FF,
                                                                            CollisionClass.PROJECTILE_FIGHTER,
                                                                            25f, //particlesize min
                                                                            50f, //particlesize range
                                                                            0.4f, //particle duration
                                                                            0, //particle count
                                                                            new Color(
                                                                                    125,
                                                                                    0,
                                                                                    0,
                                                                                    0), //particle color
                                                                            new Color(
                                                                                    0)); //exlosion color
            explosionSpec.setDamageType(DamageType.HIGH_EXPLOSIVE);
            explosionSpec.setDuration(2f);
            explosionSpec.setShowGraphic(false);

            DamagingProjectileAPI boom = engine.spawnDamagingExplosion(
                    explosionSpec,
                    beam.getSource(),
                    end);
            engine.applyDamageModifiersToSpawnedProjectileWithNullWeapon(
                    beam.getSource(), WeaponAPI.WeaponType.ENERGY, false,
                    boom.getDamage());
        }

        private float computedDistanceRatio(float currentLength) {
            return (currentLength - minRangeForNoExplosion) / (minRangeForFullExplosion - minRangeForNoExplosion);
        }
    }
}

