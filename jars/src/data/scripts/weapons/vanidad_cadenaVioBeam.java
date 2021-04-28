/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import data.scripts.util.MagicAnim;
import data.scripts.util.MagicRender;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_cadenaVioBeam implements EveryFrameWeaponEffectPlugin {
    

    //Instantiates variables we will use later
    private boolean hasRendered = false;
    private int frameNbr = 0;
    private int lastFrameNbr = 7;
    private SpriteAPI[] anim1 ={Global.getSettings().getSprite("misc", "vanidad_cadenaVioAnim1"),
                                Global.getSettings().getSprite("misc", "vanidad_cadenaVioAnim2"),
                                Global.getSettings().getSprite("misc", "vanidad_cadenaVioAnim3"),
                                Global.getSettings().getSprite("misc", "vanidad_cadenaVioAnim4"),
                                Global.getSettings().getSprite("misc", "vanidad_cadenaVioAnim5"),
                                Global.getSettings().getSprite("misc", "vanidad_cadenaVioAnim6"),
                                Global.getSettings().getSprite("misc", "vanidad_cadenaVioAnim7"), };
    
    private boolean runOnce=false, refit=false, doubletake=false, sound=false;
    private ShipSystemAPI system;
    private ShipAPI ship;
    private WeaponAPI ARMRIGHT, SHOULDERLEFT, SHOULDERRIGHT, TORSO, HEAD;
    private SpriteAPI HANDLE, HAND, BARREL;
    private float HANDLE_HEIGHT, HAND_WIDTH, BARREL_HEIGHT;
    private float acceleration=0;
    private final float MAX_ACCELERATION=10, PUMP_ACTION=3, RECOIL=3;
    
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            system = ship.getSystem();
            for(WeaponAPI w : ship.getAllWeapons()){
                switch (w.getSlot().getId()){
                    case "ARMRIGHT" :
                        ARMRIGHT=w;
                        HAND = w.getSprite();
                        HAND_WIDTH=HAND.getWidth()/2;
                        break;
                    case "SHOULDERLEFT" :
                        SHOULDERLEFT=w;
                        break;
                    case "SHOULDERRIGHT" :
                        SHOULDERRIGHT=w;
                        break;
                    case "TORSO" :
                        TORSO=w;
                        break;
                    case "HEAD":
                        HEAD=w;
                        break;
                }
            }

            
            if(ship.getOriginalOwner()==-1){
                refit=true;
            }
        }
                //Don't run if we are paused, or our if weapon is null
        if (engine.isPaused() || refit) {
            return;
        }
        
        float shipFacing=ship.getFacing();
        float weaponShipOffsetAngle=MathUtils.getShortestRotation(shipFacing, weapon.getCurrAngle());
        
        //arm acceleration overlap
        float targetAcceleration=0;
        if(ship.getEngineController().isAccelerating()){
            targetAcceleration=-MAX_ACCELERATION;
        } else if (ship.getEngineController().isDecelerating() || ship.getEngineController().isAcceleratingBackwards()){
            targetAcceleration=MAX_ACCELERATION;
        }
        acceleration=Math.max(
                -MAX_ACCELERATION,
                Math.min(
                        MAX_ACCELERATION,
                        acceleration+targetAcceleration*amount/10+(targetAcceleration-acceleration)/20
                )
        );
        
        //TORSO and ARM rotations
        TORSO.setCurrAngle(shipFacing+weaponShipOffsetAngle*(0.33f) );
        
        //ARMRIGHT.setCurrAngle(shipFacing -10 +weaponShipOffsetAngle*(0.2f) +acceleration );
        
        //PAULDRONS always mid way between their arm and torso, not additional computation        
        SHOULDERRIGHT.setCurrAngle(TORSO.getCurrAngle() + MathUtils.getShortestRotation(TORSO.getCurrAngle(), ARMRIGHT.getCurrAngle())/2);
        SHOULDERLEFT.setCurrAngle(TORSO.getCurrAngle() + MathUtils.getShortestRotation(TORSO.getCurrAngle(), weapon.getCurrAngle())/2);
        
        
        //this is the chainsaw effect
        Vector2f weaponLoc = weapon.getLocation();
        float facingAngle = weapon.getCurrAngle();
        Vector2f firePointOffsetRotated = VectorUtils.rotate(new Vector2f(19,14),
                facingAngle);
        
        Vector2f firePoint=new Vector2f();
        Vector2f.add(weaponLoc,
                firePointOffsetRotated,
                firePoint);
        Vector2f endPoint = MathUtils.getPointOnCircumference(firePoint,
                weapon.getRange(),
                facingAngle);
        MagicRender.singleframe(anim1[frameNbr],
                endPoint,
                new Vector2f(12,54),
                facingAngle-90,
                Color.yellow,
                false,
                CombatEngineLayers.CONTRAILS_LAYER);
        frameNbr = (frameNbr+1)%lastFrameNbr;
            
        

         
    }
}
