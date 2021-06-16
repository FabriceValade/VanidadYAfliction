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
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicAnim;
import data.scripts.util.MagicRender;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_revolving_generator implements EveryFrameWeaponEffectPlugin {
    enum State {
        START,
        STEADY,
        OFF,
        STOPPING,
        PARTIAL,
    }
    
    private List<Float> LIGHNINGY = Arrays.asList(16f,-16f);
    private List<CombatEngineLayers> LIGHNINGLAYER = Arrays.asList(CombatEngineLayers.BELOW_SHIPS_LAYER,CombatEngineLayers.ABOVE_SHIPS_LAYER);
    private float currentAngleCore =0;
    private float currentAngleSmall =0;
    private float currentAngleOffset =0;
    private float RevolutionPerSecondCore = 1f/8f;
    private float RevolutionPerSecondSmall = 1f/5f;
    private float RevolutionPerSecondOffset = 1f/2f;
    private float offsetRadius;
    private WeaponAPI DECOCENTER,DECOOFFSET1,DECOOFFSET2;
    Vector2f decoCenterInitial, decoOffsetInitial;
    private boolean runOnce = false;
    private boolean refit = false;
    private State currentState = State.STEADY;
    private float currentStateTime = 0;
    private float MAXSTARTSTATETIME = 1f;
    private float MAXSTOPSTATETIME = 0.7f;
    private IntervalUtil lightningInterval = new IntervalUtil(0.4f, 0.7f);
    private IntervalUtil arcInterval = new IntervalUtil(0.4f, 0.7f);
    
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        WeaponSlotAPI theSlot = weapon.getSlot();
        
         if(!runOnce){
            runOnce=true;
            ShipAPI ship=weapon.getShip();
            for(WeaponAPI w : ship.getAllWeapons()){
                switch (w.getSlot().getId()){
                    case "DECOCENTER" :
                        DECOCENTER=w;
                        decoCenterInitial = DECOCENTER.getSlot().getLocation();
                        break;
                    case "DECOOFFSET1" :
                        DECOOFFSET1=w;
                        decoOffsetInitial = DECOOFFSET1.getSlot().getLocation();
                        break;
                    case "DECOOFFSET2" :
                        DECOOFFSET2=w;
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
        lightningInterval.advance(engine.getElapsedInLastFrame());
        offsetRadius = MathUtils.getDistance(decoCenterInitial, decoOffsetInitial);

        ShipSystemAPI system = weapon.getShip().getSystem();
        ShipSystemAPI.SystemState systemState = system.getState();
        boolean unavailable = system.isOn() || systemState == ShipSystemAPI.SystemState.OUT;
        if(unavailable && currentState==State.STEADY)
            currentState = State.STOPPING;
        if(!unavailable && currentState==State.OFF){
             currentState = State.START;
        }
        float speedRatio ;
        switch (currentState){
            case START:
                currentStateTime = MathUtils.clamp(currentStateTime+amount, 0, MAXSTARTSTATETIME);
                speedRatio = MagicAnim.smoothNormalizeRange(currentStateTime, 0, MAXSTARTSTATETIME);
                advanceAngle(amount,weapon, speedRatio*RevolutionPerSecondCore , speedRatio*RevolutionPerSecondOffset, speedRatio*RevolutionPerSecondSmall);
                if (currentStateTime>=MAXSTARTSTATETIME){
                    currentState = State.PARTIAL;
                    currentStateTime =0;
                }   
                break;
            case PARTIAL:
                
                if (systemState == ShipSystemAPI.SystemState.IDLE)
                    currentState = State.STEADY;
            case STEADY:
                advanceAngle(amount,weapon, RevolutionPerSecondCore , RevolutionPerSecondOffset, RevolutionPerSecondSmall);
                break;
            case STOPPING:
                currentStateTime = MathUtils.clamp(currentStateTime+amount, 0, MAXSTOPSTATETIME);
                speedRatio = MathUtils.clamp(1-MagicAnim.smoothNormalizeRange(currentStateTime, 0, MAXSTOPSTATETIME), 0, 1);
                advanceAngle(amount,weapon, speedRatio*RevolutionPerSecondCore , speedRatio*RevolutionPerSecondOffset, speedRatio*RevolutionPerSecondSmall);
                if (currentStateTime>=MAXSTOPSTATETIME){
                    currentState = State.OFF;
                    currentStateTime =0;
                }
            case OFF:
                arcInterval.advance(engine.getElapsedInLastFrame());
                if (arcInterval.intervalElapsed()) {
                    WeaponAPI chosen = DECOOFFSET1;
                    if(Math.random()>=0.5)
                        chosen = DECOOFFSET2;
  
                    Vector2f sparkOrigin = MathUtils.getRandomPointOnCircumference(chosen.getLocation(), 15);
                    Vector2f sparkEnd = MathUtils.getRandomPointOnCircumference(DECOCENTER.getLocation(), MathUtils.getRandomNumberInRange(25, 30));
                    engine.spawnEmpArcVisual(DECOCENTER.getLocation(), DECOCENTER.getShip(), sparkEnd, DECOCENTER.getShip(), 1f, new Color(10, 255,255,50), new Color(255,100,0));
                }
                
        }
        

    }
    
    /**
     * rotate the assembly, speed is in revolution/second
     *
     * @param amount time elapsed
     * @param speedCore   speed of the thing under.
     * @param speedOffset  speed of rotation of the weapon slot around center of core.
     * @param SpeedSmall speed of the weapon In the slot.
     *
     *
     */
    public void advanceAngle(float amount, WeaponAPI weapon, float speedCore, float speedOffset,float SpeedSmall){
        float angleChange = 360f * amount*speedCore;
        currentAngleCore += angleChange;
        currentAngleCore = MathUtils.clampAngle(currentAngleCore);
        
        float angleChangeOffset = 360f * amount*SpeedSmall;
        currentAngleSmall += angleChangeOffset;
        currentAngleSmall = MathUtils.clampAngle(currentAngleSmall);
        
        float angleChangeOffsetSlow = 360f * amount*speedOffset;
        currentAngleOffset += angleChangeOffsetSlow;
        currentAngleOffset = MathUtils.clampAngle(currentAngleOffset);
        
        float currentAngleOfTheCoreToDisplay = MathUtils.clampAngle(currentAngleCore+weapon.getShip().getFacing());
        DECOCENTER.setCurrAngle(currentAngleOfTheCoreToDisplay);
        
        float currentAngleOfTheOffsetToDisplay = MathUtils.clampAngle(currentAngleOffset);
        float currentOffset1CenterAngle = MathUtils.clampAngle(currentAngleOfTheOffsetToDisplay-90 );
        float currentOffset2CenterAngle = MathUtils.clampAngle(currentAngleOfTheOffsetToDisplay+90);
        Vector2f offsetCenter1 = MathUtils.getPointOnCircumference(decoCenterInitial, offsetRadius, currentOffset1CenterAngle);
        DECOOFFSET1.getSlot().getLocation().set(offsetCenter1);
        Vector2f offsetCenter2 = MathUtils.getPointOnCircumference(decoCenterInitial, offsetRadius, currentOffset2CenterAngle);
        DECOOFFSET2.getSlot().getLocation().set(offsetCenter2);
        
        float offset1ResultAngle = MathUtils.clampAngle(currentAngleOfTheOffsetToDisplay+currentAngleSmall+weapon.getShip().getFacing());
        DECOOFFSET1.setCurrAngle(offset1ResultAngle);
        offset1ResultAngle = MathUtils.clampAngle(currentAngleOfTheOffsetToDisplay+currentAngleSmall+180+weapon.getShip().getFacing());
        DECOOFFSET2.setCurrAngle(offset1ResultAngle);
    }
    
}
