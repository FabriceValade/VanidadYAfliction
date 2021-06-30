/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import data.scripts.util.MagicRender;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author lethargie
 * 
 */
public class vanidadSpiralLauncher extends BaseShipSystemScript{
    
    private boolean runOnce=false;
    private DamagingProjectileAPI projectile;
    private VanidadSpiralShot shot;
    
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        CombatEngineAPI engine = Global.getCombatEngine();
        ShipAPI ship = null;
        if (stats != null && stats.getEntity() instanceof ShipAPI) {
                ship = (ShipAPI) stats.getEntity();
            } else {
                return;
            }
        if (engine.isPaused() || ship.getOriginalOwner()==-1) {
            return;
        }
        if (state == State.ACTIVE && !runOnce) {
            
            //boolean player = false;
            
            //this spawn the projection 
            Vector2f origin = MathUtils.getPointOnCircumference(ship.getLocation(), 60, ship.getFacing());
            projectile = (DamagingProjectileAPI)engine.spawnProjectile(ship, null, "vanidad_spiraldischarge", origin, ship.getFacing(), ship.getVelocity());
            shot = new VanidadSpiralShot(projectile);
            
            //with a shockwave muzzle flash
            SpriteAPI shock = Global.getSettings().getSprite("fx", "vanidad_shockwave");
            float angle = MathUtils.clampAngle(ship.getFacing()+180);
            MagicRender.objectspace(shock, ship, new Vector2f(60,0), new Vector2f(150, 0), new Vector2f(50,50), new Vector2f(1400,1400),
                                    180, 0, true,
                                    Color.cyan, true, 0,
                                    0.1f, 0.2f, false);
            runOnce = true;
        }
        
        //this is display and damage handling of the projectile on every frame
        
        
        
        
        if (runOnce==true && !projectile.isExpired()) {

            float amount = engine.getElapsedInLastFrame();
            shot.Advance(amount, projectile);
            handleMissiles(amount);
            handleShip(amount);
        }
        
        if(state==State.IDLE){
            runOnce = false;
        }

    }
    
    public void unapply(MutableShipStatsAPI stats, String id) {

        
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {

        if (state == State.IN || state == State.OUT || state == State.ACTIVE) {
            if (index == 0) {
                return new ShipSystemStatsScript.StatusData(
                        "Despair: the spiral has abandoned us", true);
            }
        } else if (state == State.COOLDOWN) {
            if (index == 0) {
                return new ShipSystemStatsScript.StatusData(
                        "Calm: the spiral is growing", false);
            }
        } else {
            if (index == 0) {
                return new ShipSystemStatsScript.StatusData(
                        "Rejoice: the spiral is", false);
            }
            if (index == 1) {
                return new ShipSystemStatsScript.StatusData(
                        "Spiral launcher available", false);
            }
        }


        return null;
    }
    

    
    private void handleShip(float amount) {
        // lets check any ship we might hit
        List<CombatEntityAPI> ships = new ArrayList<>();
        ships.addAll(CombatUtils.getShipsWithinRange(projectile.getLocation(),
                                                     projectile.getCollisionRadius()+50));
        for (CombatEntityAPI entity : ships) {
            if (entity.getCollisionClass() == CollisionClass.NONE) {
                continue;
            }

            //ShipAPI ship = (ShipAPI) entity;
            //dont want to hit ourself 
            ShipAPI ship = null;
            if (entity instanceof ShipAPI) {
                ship = (ShipAPI) entity;
                if (ship.getId().equals(projectile.getSource().getId())) {
                    continue;
                }
            } else {
                continue;
            }

            //this method wont hit the same ship twice with one of the sub projectile
            List<VanidadSpiralShot.Impact> Impacts = shot.affectShip(projectile,
                                                                     ship);
            float speed = projectile.getVelocity().length();
            for (VanidadSpiralShot.Impact i : Impacts) {

                if (i.isShieldHit) {
                    hitShield(shot, projectile, entity, speed, amount, i.loc);
                } else {
                    hitHull(shot, projectile, entity, speed, amount, i.loc);
                }
            }

        }
    }
    
    
    private void handleMissiles(float amount) {
        //lets see any missile we might hit
        List<CombatEntityAPI> missiles = new ArrayList<>();
        missiles.addAll(CombatUtils.getMissilesWithinRange(projectile.getLocation(),
                                                           projectile.getCollisionRadius()+10f));
        for (CombatEntityAPI entity : missiles) {
            if (entity.getCollisionClass() == CollisionClass.NONE) {
                continue;
            }

            //ShipAPI ship = (ShipAPI) entity;
            //dont want to hit ourself 
            MissileAPI missile = null;
            if (entity instanceof MissileAPI) {
                missile = (MissileAPI) entity;
            } else {
                continue;
            }

            //this method works for missiles
            List<VanidadSpiralShot.Impact> Impacts = shot.affectMissiles(projectile,
                                                                         missile);
            float speed = projectile.getVelocity().length();
            for (VanidadSpiralShot.Impact i : Impacts) {
                hitMissile(shot, projectile, entity, speed, amount, i.loc);
            }

        }

    }

    
        // Deals one hit to hull
    private void hitHull(VanidadSpiralShot shot,
                         DamagingProjectileAPI proj, CombatEntityAPI entity,
                         float speed, float amount, Vector2f hitLoc) {
        float damage = shot.damagePerHit;
        float empDamage = shot.empPerHit;

        Global.getCombatEngine().spawnEmpArcPierceShields(shot.source, hitLoc, entity, entity,
                                        DamageType.ENERGY, damage, empDamage,
                                        10000f, "tachyon_lance_emp_impact",
                                        10f,
                                        Color.orange,
                                        Color.red);

        // Deal damage first, then check if it counts as a hit
        Global.getCombatEngine().applyDamage(entity, entity.getLocation(),
                           damage,
                           DamageType.ENERGY,
                           empDamage,
                           false, false, proj.getSource());

        // Render the hit
        Global.getCombatEngine().spawnExplosion(hitLoc, entity.getVelocity(), Color.ORANGE,
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
            Global.getCombatEngine().spawnEmpArcPierceShields(shot.source,
                                                              hitLoc, entity,
                                                              entity,
                                                              DamageType.ENERGY,
                                                              damage, empDamage,
                                                              10000f,
                                                              "tachyon_lance_emp_impact",
                                                              10f,
                                                              Color.orange,
                                                              Color.red);
        } else {
            Global.getCombatEngine().applyDamage(entity, proj.getLocation(),
                                                 damage + shot.unPiercedShieldDamage,
                                                 DamageType.ENERGY,
                                                 empDamage,
                                                 false, false, proj.getSource());
        }

        // Render the hit
        Global.getCombatEngine().spawnExplosion(hitLoc, entity.getVelocity(), Color.BLUE,
                              speed * amount, 1f);

    }
    
    private void hitMissile(VanidadSpiralShot shot,
                            DamagingProjectileAPI proj, CombatEntityAPI entity,
                            float speed, float amount, Vector2f hitLoc) {
        float damage = shot.damagePerHit;
        float empDamage = shot.empPerHit;
        Global.getCombatEngine().spawnEmpArc(shot.source, hitLoc, entity, entity,
                           DamageType.ENERGY, damage, empDamage,
                           10000f, "tachyon_lance_emp_impact",
                           10f,
                           Color.orange,
                           Color.red);
    }
}
