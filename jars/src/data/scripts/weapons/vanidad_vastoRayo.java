/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicAnim;
import data.scripts.util.MagicRender;
import data.scripts.utils.vanidad_wideBeam;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.CollisionUtils;

public class vanidad_vastoRayo implements EveryFrameWeaponEffectPlugin {

    public final float width = 150;


    private final IntervalUtil timer = new IntervalUtil(0.25f, 0.25f);
    private final float IntervalFractionOfSecond= 0.25f;
    private final float dmgMultAdditionalShip = 0.5f;
    private int numberOfComb = 7;
    private ShipAPI ship;
    private WeaponAPI weapon;
    private float damagePerInterval;
    
    private boolean isDoneOnce = false;

    public SpriteAPI pointSprite = Global.getSettings().getSprite("fx","vanidad_dot");
    public SpriteAPI trailSprite = Global.getSettings().getSprite("fx","vanidad_vastoBeam2");

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) return;
        
        if (!isDoneOnce){
            ship = weapon.getShip();
            this.weapon = weapon;
        }
        

        vanidad_wideBeam wideBeams = new vanidad_wideBeam();

        for (BeamAPI beam : engine.getBeams()) {
            if (beam.getWeapon() == weapon) wideBeams= new vanidad_wideBeam(beam, width, numberOfComb);
        }
        timer.advance(amount);
        if (!wideBeams.IsExisting)
            return;
        
        /*engine.addSwirlyNebulaParticle(wideBeams.coreBeam.getTo(),
                                       new Vector2f(0, 0),
                                       45,
                                       2f,
                                       0.5f,
                                       0f,
                                       1f,
                                       Color.GREEN,
                                       true);*/
        /*engine.addNebulaSmokeParticle(wideBeams.coreBeam.getTo(), new Vector2f(0, 0), width, 5f, 0f, 0f,
                                      2f, Color.GREEN);*/
        Vector2f velocity = wideBeams.getVectorAlongDirection(200);
        //engine.addSmokeParticle(wideBeams.coreBeam.getTo(), velocity, width, 0.1f, 1f, Color.yellow);
        MagicRender.battlespace(trailSprite, 
                                wideBeams.coreBeam.getTo(), //loc
                                velocity, //vel
                                new Vector2f(150,75), //size
                                new Vector2f(-150,-75), //growth
                                0, //angle
                                25, //spin
                                Color.yellow, 
                                true, 
                                0.1f, //fadein
                                0.5f,//full
                                1f); //fadeout

        if (timer.intervalElapsed()) {
            if (wideBeams.IsExisting) {
                HandleShips(CombatUtils.getShipsWithinRange( weapon.getLocation(), 
                                                             weapon.getRange()),
                            wideBeams);
                HandleMissiles(CombatUtils.getMissilesWithinRange(weapon.getLocation(),
                                                                  weapon.getRange()), 
                               wideBeams);
            }
        }
    }
    public void HandleShips(List<ShipAPI> ships, vanidad_wideBeam wideBeam) {
        
        for (ShipAPI shipToCheck : ships) {
            if (shipToCheck.getOwner() == ship.getOwner()) {
                continue;
            }
            if (shipToCheck.getCollisionClass() == CollisionClass.NONE) {
                continue;
            }
            if (shipToCheck.isFighter()) {
                //fighter get hit for full damage if in range
                if (wideBeam.IsEntityIntersecting(shipToCheck)) {
                    MagicRender.singleframe(pointSprite,
                                            shipToCheck.getLocation(),
                                            new Vector2f(10, 10),
                                            0,
                                            Color.red,
                                            false);
                    Global.getCombatEngine().applyDamage(shipToCheck, shipToCheck.getLocation(),
                           GetWeaponDamagePerIntervalFighter(),
                           DamageType.ENERGY,
                           0,
                           false, false, ship);
                }

            } else {
                //each subpoint can damage an actual ship
                List<Vector2f> collisionPoints = wideBeam.getCombCollisionPoint(
                        shipToCheck);
                for (Vector2f point : collisionPoints) {
                    MagicRender.singleframe(pointSprite,
                                            point,
                                            new Vector2f(10, 10),
                                            0,
                                            Color.red,
                                            false);
                    Global.getCombatEngine().applyDamage(shipToCheck, point,
                           GetWeaponDamagePerIntervalShip(),
                           DamageType.ENERGY,
                           0,
                           false, false, ship);
                }
            }

        }
    }
    public void HandleMissiles(List<MissileAPI> missiles, vanidad_wideBeam wideBeam){
        
        for (MissileAPI missile : missiles){
            if(wideBeam.IsCollisionCircleIntersecting(missile))
                Global.getCombatEngine().applyDamage(missile, missile.getLocation(),
                           GetWeaponDamagePerIntervalMissiles(),
                           DamageType.ENERGY,
                           0,
                           false, false, ship);
        }
        
    }
    
    private float GetWeaponDamagePerIntervalFighter(){              
        float dmgMultFighter = ship.getMutableStats().getDamageToFighters().getModifiedInt();
        return GetWeaponDamagePerInterval() * dmgMultFighter;
    }
    private float GetWeaponDamagePerIntervalMissiles(){              
        float dmgMultMissiles = ship.getMutableStats().getDamageToMissiles().getModifiedValue();
        return GetWeaponDamagePerInterval() * dmgMultMissiles;
    }
    private float GetWeaponDamagePerIntervalShip(){
        return GetWeaponDamagePerInterval()*dmgMultAdditionalShip / numberOfComb;
    }
    private float GetWeaponDamagePerInterval(){
        damagePerInterval = weapon.getSpec().getDerivedStats().getDps();
        float dmgMultBeam = ship.getMutableStats().getBeamWeaponDamageMult().getModifiedValue();
        return dmgMultBeam*damagePerInterval*IntervalFractionOfSecond;        
       
    }
}
