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
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_beamFlakEffectLarge implements BeamEffectPlugin {
    private final Color PARTICLE_COLOR = new Color(215, 225, 255, 255);
    private final float EXPLOSION_DAMAGE = 150f;
    private final float EXPLOSION_RANGE = 350f;
    private boolean hasFired = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }
        if (beam.getBrightness() < 1f) return;
        if (beam.getBrightness() == 1) {
            Vector2f start = beam.getFrom();
            Vector2f end = beam.getTo();
            float prevLength = beam.getLengthPrevFrame();

            
            if (!hasFired) {
                Color beamCore = beam.getCoreColor();
                float maxRange = beam.getWeapon().getRange();
                float distanceRatio = prevLength / maxRange;
                if(prevLength>=1000)
                    distanceRatio=1;
                else if(prevLength<=300)
                    distanceRatio=0;
                else {
                    distanceRatio=(prevLength-300)/700;
                }
                
                ;
                boolean hasTarget = beam.getDamageTarget() != null;
                boolean mightExplode = (prevLength >= maxRange * 0.95f) || hasTarget;
                if (mightExplode && distanceRatio>0) {

                    hasFired = true;
                    Color explosionColor = Misc.interpolateColor(beamCore, Color.white, 0.4f);
                    Color riftColor = Misc.interpolateColor(beamCore, Color.black, 0.4f);
                    riftColor = Misc.setAlpha(beamCore,50);
                    float damageRatio = 0.5f*(distanceRatio + 1f);
                    DamagingExplosionSpec explosionSpec = new DamagingExplosionSpec(0.1f,
                            EXPLOSION_RANGE*distanceRatio, //radius
                            EXPLOSION_RANGE*distanceRatio*0.5f, //core radius
                            EXPLOSION_DAMAGE*damageRatio, //max damage
                            EXPLOSION_DAMAGE*damageRatio, //min damage
                            CollisionClass.PROJECTILE_FF,
                            CollisionClass.PROJECTILE_FIGHTER,
                            25f, //particlesize min
                            50f, //particlesize range
                            0.4f, //particle duration
                            0, //particle count
                            new Color(125,0,0,0), //particle color
                            explosionColor); //exlosion color
                    explosionSpec.setDamageType(DamageType.HIGH_EXPLOSIVE);
                    explosionSpec.setDuration(2f);
                    explosionSpec.setShowGraphic(false);
                    
                    DamagingProjectileAPI boom = engine.spawnDamagingExplosion(explosionSpec, beam.getSource(), end);
                    engine.applyDamageModifiersToSpawnedProjectileWithNullWeapon(
                            beam.getSource(), WeaponAPI.WeaponType.ENERGY, false,
                            boom.getDamage());
                    engine.spawnExplosion(end,
                            new Vector2f(0,0),
                            explosionColor,
                            EXPLOSION_RANGE*distanceRatio,
                            0.5f);
                    engine.addNegativeSwirlyNebulaParticle(end,
                            new Vector2f(0,0),
                            EXPLOSION_RANGE*distanceRatio*0.5f,
                            2f,
                            0.1f,
                            0f,
                            2f,
                            Color.white);
                    engine.addSwirlyNebulaParticle(end,
                            new Vector2f(0,0),
                            EXPLOSION_RANGE*distanceRatio,
                            2f,
                            0.1f,
                            0f,
                            2f,
                            riftColor,
                            true);
                    vanidad_negativeExplosionVisual.NEParams p = new vanidad_negativeExplosionVisual.NEParams();
                    p.hitGlowSizeMult = .75f;
                    p.spawnHitGlowAt = 0f;
                    p.noiseMag = 1f;
                    p.fadeIn = 0.1f;
                    p.underglow = riftColor;
                    p.withHitGlow = true;
                    p.radius = 35f;
                    p.color = explosionColor;
                    Vector2f loc = new Vector2f(boom.getLocation());
                    CombatEntityAPI e = engine.addLayeredRenderingPlugin(new vanidad_negativeExplosionVisual(p));
                    e.getLocation().set(loc);
                }
            }

               
            //if (beam.getWeapon().getChargeLevel() < 1) {
            //    hasFired = false;
            //}
        }
    }
    
}
