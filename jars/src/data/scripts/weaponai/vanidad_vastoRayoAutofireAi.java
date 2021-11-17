/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weaponai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;


import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;

public class vanidad_vastoRayoAutofireAi implements AutofireAIPlugin {

    final float maximalAngleToStartFiring = 30;
    final float maximalAngleForRandomFiring = 20;

    IntervalUtil interval = new IntervalUtil(0.05f, 0.15f);

    Float targetAngle;

    MissileData targetMissile = null;
    FighterData targetFighter = null;
    boolean isMissilesInCurrentArc= false;
    //ShipAPI targetFighter = null;
    final WeaponAPI weapon;
    final boolean ignoreFlares;

    public vanidad_vastoRayoAutofireAi(WeaponAPI weapon) {
        this.weapon = weapon;
        this.ignoreFlares = weapon.getShip().getMutableStats().getDynamic().getMod(Stats.PD_IGNORES_FLARES).getFlatBonus() >= 1;
    }

    @Override
    public void advance(float amount) {
        interval.advance(amount);
        if (interval.intervalElapsed()) {
            targetMissile=null;
            targetFighter=null;
            isMissilesInCurrentArc = false;
            PickMissile();
            if(targetMissile==null)
                PickFighter();
                
        }
        
    }

    @Override
    public boolean shouldFire() {
        if(isMissilesInCurrentArc)
            return true;
        if (targetMissile==null){
            if (targetFighter == null)
                return false;
            if(targetFighter.angle < maximalAngleToStartFiring)
                return true ;
        }else if(targetMissile.angle < maximalAngleToStartFiring)
            return true;
        return false;
            
    }

    @Override
    public void forceOff() {
    }

    @Override
    public Vector2f getTarget() {
         if (targetMissile == null)
             if(targetFighter==null)
                 return null;
             else
                return new Vector2f(targetFighter.fighter.getLocation());
         else
             return new Vector2f(targetMissile.missile.getLocation());
           
    }

    @Override
    public ShipAPI getTargetShip() {
        if (targetFighter==null)
            return null;
        return targetFighter.fighter;
    }

    @Override
    public WeaponAPI getWeapon() {
        return weapon;
    }

    @Override
    public MissileAPI getTargetMissile() {
        if (targetMissile != null) {
            return targetMissile.missile;
        }
        return null;
    }
    
    public float getAngleToCurentTarget()
    {
        if (targetMissile == null)
             if(targetFighter==null)
                 return 0;
             else
                return targetFighter.angle;
         else
             return targetMissile.angle;
    }
    
    private void PickMissile() {
        List<MissileAPI> missiles = CombatUtils.getMissilesWithinRange(
                weapon.getLocation(), weapon.getRange() + 200);
        //lets try to target the most dangerous missile we have a chance to stop
        //as long as we have a target missile/fighter, and we are close in angle, we should be firing
        for (MissileAPI missile : missiles) {
            if (ignoreFlares && (missile.isDecoyFlare() || missile.isFlare())) continue;
            if (missile.getOwner() == weapon.getShip().getOwner())             continue;
            if (missile.getCollisionClass() == CollisionClass.NONE)            continue;
            
            float angleToTarget = VectorUtils.getAngle(weapon.getLocation(),
                                                       missile.getLocation());
            float angleToCurrentFacing = Math.abs(MathUtils.getShortestRotation(
                    angleToTarget,
                    weapon.getCurrAngle()));
            if(angleToCurrentFacing<maximalAngleForRandomFiring)
                isMissilesInCurrentArc = true;
            MissileData missData = new MissileData((missile),
                                                   angleToCurrentFacing);
            if (targetMissile == null) {
                targetMissile = missData;
                continue;
            }
            if (targetMissile.score < missData.score) {
                targetMissile = missData;
            }
        }
    }
    
    private void PickFighter(){
        List<ShipAPI> ships = AIUtils.getNearbyEnemies(weapon.getShip(), weapon.getRange()+200);

        for (ShipAPI ship : ships) {
            if (!ship.isFighter())     continue;
            float angleToTarget = VectorUtils.getAngle(weapon.getLocation(),
                                                       ship.getLocation());
            float angleToCurrentFacing = Math.abs(MathUtils.getShortestRotation(
                    angleToTarget,
                    weapon.getSlot().getAngle() + weapon.getShip().getFacing()));
            FighterData fightData = new FighterData(ship, angleToCurrentFacing);
            if (targetFighter == null) {
                targetFighter = fightData;
                continue;
            }
            if (targetFighter.angle > fightData.angle) {
                targetFighter = fightData;
            }

        }
    }
    
    static final class MissileData {

        public MissileData(MissileAPI missile, float angle){
            this.missile = missile;
            this.angle = angle;
            this.score = missile.getDamageAmount() / missile.getHitpoints();
            if (missile.getDamageType() == DamageType.FRAGMENTATION) this.score *= 0.25;
        }

        MissileAPI missile;
        float angle;
        float score;

    }
    
    static final class FighterData {

        public FighterData(ShipAPI fighter, float angle){
            this.fighter = fighter;
            this.angle = angle;
        }

        ShipAPI fighter;
        float angle;

    }
}