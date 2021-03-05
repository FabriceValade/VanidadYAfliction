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
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_beamFlakEffect implements BeamEffectPlugin {
    private final Color PARTICLE_COLOR = new Color(215, 225, 255, 255);
    private final float EXPLOSION_DAMAGE = 100f;
    private final float EXPLOSION_RANGE = 50f;
    private boolean hasFired = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }
        
        if (beam.getBrightness() == 1) {
            Vector2f start = beam.getFrom();
            Vector2f end = beam.getTo();
            float distance = MathUtils.getDistanceSquared(start, end);
            if (distance == 0) {
                return;
            }
            
            if (!hasFired) {
                Color beamCore = beam.getCoreColor();
                float maxRange = beam.getWeapon().getRange();
                float maxRangeSquared = maxRange * maxRange;
                float distanceRatio = distance / maxRangeSquared;
                ;
                boolean hasTarget = beam.getDamageTarget() != null;
                boolean mightExplode = (distance >= maxRangeSquared * 0.95f) || hasTarget;
                if (mightExplode) {

                    hasFired = true;
                    DamagingExplosionSpec explosionSpec = new DamagingExplosionSpec(0.1f,
                            EXPLOSION_RANGE*distanceRatio,
                            EXPLOSION_RANGE*distanceRatio,
                            EXPLOSION_DAMAGE,
                            EXPLOSION_DAMAGE*0.5f,
                            CollisionClass.PROJECTILE_FF,
                            CollisionClass.PROJECTILE_FIGHTER,
                            25f,
                            50f,
                            0.4f,
                            4,
                            Color.WHITE,
                            beamCore);
                    explosionSpec.setDamageType(DamageType.HIGH_EXPLOSIVE);
                    DamagingProjectileAPI boom = engine.spawnDamagingExplosion(explosionSpec, beam.getSource(), end);
                }
                //visual effect
                /*engine.addHitParticle(
                            end,
                            new Vector2f(),
                            150,
                            1f,
                            0.1f,
                            Color.WHITE
                    );
                    engine.spawnExplosion(
                            //where
                            end,
                            //speed
                            (Vector2f) new Vector2f(0, 0),
                            //color
                            beamCore,
                            //size
                            MathUtils.getRandomNumberInRange(50f, 100f),
                            //duration
                            0.4f
                    );*/
            }

               
            if (beam.getWeapon().getChargeLevel() < 1) {
                hasFired = false;
            }
        }
    }
    
}
