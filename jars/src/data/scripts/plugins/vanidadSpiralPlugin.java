/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.input.InputEventAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.mapped.MappedObject;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Lethargie thank you to ms_shadowy, I kinda learned by reading his
 * code. *goal: draw 2 spiral around the projectile path impact a big emp hit on
 * anything it goes though apply a plugin similar to entropy amplifier to
 * everything it goes thought
 */
public class vanidadSpiralPlugin extends BaseEveryFrameCombatPlugin {

    private CombatEngineAPI engine = null;
    private static final String PROJ_ID = "vanidad_spiral";
    private static final List<VanidadSpiralShot> SHOTS = new ArrayList<>();

    //always even to spread on each side
    private static final int NUMSUBPROJ = 10;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
        }

        if (engine.isPaused()) {
            return;
        }

        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            String spec = proj.getProjectileSpecId();

            // Is this a spiral proj?
            if (spec == null || !spec.equals(PROJ_ID)) {
                continue;
            }

            VanidadSpiralShot shot = null;
            // See if it is a shot already being tracked
            for (VanidadSpiralShot s : SHOTS) {
                if (s.source == proj.getSource()) {
                    shot = s;
                    shot.expired = false;
                    break;
                }
            }

            // Else start tracking it
            //tracked shot got all info stored into the associated spiralshot object
            //the object is removed from the list once the projectile faded
            if (shot == null) {
                shot = new VanidadSpiralShot(proj);
                SHOTS.add(shot);
            }
            //this is the visible effect arount the shot, no actual game effect
            shot.Advance(amount, proj);
            
            //lets see any missile we might hit
            List<CombatEntityAPI> missiles = new ArrayList<>();
            missiles.addAll(CombatUtils.getMissilesWithinRange(proj.getLocation(),
                                                            proj.getCollisionRadius() + 150f));
            for (CombatEntityAPI entity : missiles) {
                if (entity.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }

                //ShipAPI ship = (ShipAPI) entity;
                //dont want to hit ourself 
                MissileAPI missile = null;
                if (entity instanceof MissileAPI) {
                    missile = (MissileAPI) entity;
                } else
                    continue;

                //this method works for missiles
                List<VanidadSpiralShot.Impact> Impacts = shot.affectMissiles(proj,
                                                                           missile);
                float speed = proj.getVelocity().length();
                for (VanidadSpiralShot.Impact i : Impacts) {
                        hitMissile(shot, proj, entity, speed, amount, i.loc);
                }

            }
            
            
            // lets check any ship we might hit
            List<CombatEntityAPI> ships = new ArrayList<>();
            ships.addAll(CombatUtils.getShipsWithinRange(proj.getLocation(),
                                                         proj.getCollisionRadius() + 150f));
            
            // Would be nice to sort here so stuff gets hit closest
            // to farthest instead of top-bottom left-right

            for (CombatEntityAPI entity : ships) {
                if (entity.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }

                //ShipAPI ship = (ShipAPI) entity;
                //dont want to hit ourself 
                ShipAPI ship = null;
                if (entity instanceof ShipAPI) {
                    ship = (ShipAPI) entity;
                    if (ship.getId().equals(proj.getSource().getId())) {
                        continue;
                    }
                } else
                    continue;

                //this method wont hit the same ship twice with one of the sub projectile
                List<VanidadSpiralShot.Impact> Impacts = shot.affectShip(proj,
                                                                           ship);
                float speed = proj.getVelocity().length();
                for (VanidadSpiralShot.Impact i : Impacts) {

                    if (i.isShieldHit) {
                        hitShield(shot, proj, entity, speed, amount, i.loc);
                    } else {
                        hitHull(shot, proj, entity, speed, amount, i.loc);
                    }
                }

            }
        }

        // Clean up expired shots in SHOTS list
        List<VanidadSpiralShot> toRemove = new ArrayList<>();
        for (VanidadSpiralShot shot : SHOTS) {
            if (shot.expired) {
                toRemove.add(shot);
            }

            shot.expired = true; // We assume each shot will expire next time around
        }
        SHOTS.removeAll(toRemove);

    }

    // Deals one hit to hull
    private void hitHull(VanidadSpiralShot shot,
                         DamagingProjectileAPI proj, CombatEntityAPI entity,
                         float speed, float amount, Vector2f hitLoc) {
        float damage = shot.damagePerHit;
        float empDamage = shot.empPerHit;

        engine.spawnEmpArcPierceShields(shot.source, hitLoc, entity, entity,
                                        DamageType.ENERGY, damage, empDamage,
                                        10000f, "tachyon_lance_emp_impact",
                                        10f,
                                        Color.orange,
                                        Color.red);

        // Deal damage first, then check if it counts as a hit
        engine.applyDamage(entity, entity.getLocation(),
                           damage,
                           DamageType.ENERGY,
                           empDamage,
                           false, false, proj.getSource());

        // Render the hit
        engine.spawnExplosion(hitLoc, entity.getVelocity(), Color.ORANGE,
                              speed * amount, 1f);
    }

    // Deals one hit to shield
    private void hitShield(VanidadSpiralShot shot,
                           DamagingProjectileAPI proj, CombatEntityAPI entity,
                           float speed, float amount, Vector2f hitLoc) {

        float damage = shot.damagePerHit;
        float empDamage = shot.empPerHit;
        float pierceChance = ((ShipAPI) entity).getHardFluxLevel() - 0.1f;
        pierceChance *= shot.source.getMutableStats().getDynamic().getValue(
                Stats.SHIELD_PIERCED_MULT);
        boolean piercedShield = (float) Math.random() < pierceChance;
        if (piercedShield) {
            engine.spawnEmpArcPierceShields(shot.source, hitLoc, entity, entity,
                                            DamageType.ENERGY, damage, empDamage,
                                            10000f, "tachyon_lance_emp_impact",
                                            10f,
                                            Color.orange,
                                            Color.red);
        }
        /*engine.applyDamage(entity, proj.getLocation(),
                    damage,
                    proj.getDamageType(),
                    empDamage,
                    false, false, proj.getSource());*/

        // Render the hit
        engine.spawnExplosion(hitLoc, entity.getVelocity(), Color.BLUE,
                              speed * amount, 1f);

    }
    
    private void hitMissile(VanidadSpiralShot shot,
                            DamagingProjectileAPI proj, CombatEntityAPI entity,
                            float speed, float amount, Vector2f hitLoc) {
        float damage = shot.damagePerHit;
        float empDamage = shot.empPerHit;
        engine.spawnEmpArc(shot.source, hitLoc, entity, entity,
                           DamageType.ENERGY, damage, empDamage,
                           10000f, "tachyon_lance_emp_impact",
                           10f,
                           Color.orange,
                           Color.red);
    }

}
