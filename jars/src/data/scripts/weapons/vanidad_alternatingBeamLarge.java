/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_alternatingBeamLarge implements EveryFrameWeaponEffectPlugin {
    
     private final       Color LowerColor = new Color(251,102,41);
     private final       Color CenterColor = new Color(250,65,41);
     private final       Color UpperColor = new Color(255,30,0);
    private final float inacuracyAngle = 15;
    //----------------This area is for setting all offsets for the barrels: note that the turret and hardpoint version of the weapon *must* have an equal amount of offsets--------------------
    //Offsets for medium weapons
    private static Map<Integer, Vector2f> LargeHardpointOffsets = new HashMap<Integer, Vector2f>();
    static {
        LargeHardpointOffsets.put(0, new Vector2f(42f, 6.5f));
        LargeHardpointOffsets.put(1, new Vector2f(42f, -6.5f));
    }
    private static Map<Integer, Vector2f> LargeTurretOffsets = new HashMap<Integer, Vector2f>();
    static {
        LargeTurretOffsets.put(0, new Vector2f(26f, 6.5f));
        LargeTurretOffsets.put(1, new Vector2f(26f, -6.5f));

    }
     private float charge =0f, recoilDuration = 0.4f;
     private float interFrameDelay =0f;
     private int frameNbr = 0;
     private float timer = 0f;
     private AnimationAPI theAnim;
    //-----------------------------------------------------------------------------END OF OFFSET SPECIFICATIONS---------------------------------------------------------------------------------

    //Instantiates variables we will use later
    private int counter = 0;
    private boolean runOnce = false;
    private boolean firingStarted = false;
    private boolean recoilFinished = false;
    private Map<Integer, BeamAPI> beamMap = new HashMap<Integer, BeamAPI>();
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //Don't run if we are paused, or our if weapon is null
        if (engine.isPaused() || weapon == null) {
            return;
        }
        if (!runOnce) {
            runOnce = true;
            theAnim = weapon.getAnimation();
            interFrameDelay = recoilDuration/7f;
        }
        //Resets the beam map and variables between firing
        if (weapon.getChargeLevel() <= 0) {
            beamMap.clear();
            firingStarted = false;
            theAnim.setFrame(0);
            recoilFinished = false;
            timer =0;
            return;
        }
        weapon.ensureClonedSpec();
        //If we are firing, start the code and change variables
        
        //we run all this stuff when the weapon fire. set offset, color, proper counter
        if (weapon.getChargeLevel() > 0f && !firingStarted) {
            firingStarted = true;
            if (!LargeHardpointOffsets.containsKey(counter)) {
                counter = 0;
            }
            if (counter == 0) {
                frameNbr=1;
            } else {
                frameNbr=8;
            }
            int counterForBeams = 0;
            for (BeamAPI beam : engine.getBeams()) {
                if (beam.getWeapon() == weapon) {
                    if (!beamMap.containsValue(beam)) {
                        beamMap.put(counterForBeams,
                                beam);
                        counterForBeams++;
                    }
                }
            }
            
            

            float randomInterpolate = 0.8f * (float) Math.random() - 0.4f;

            Color finalColor;
            if (randomInterpolate >= 0) {
                finalColor = Misc.interpolateColor(CenterColor,
                        UpperColor,
                        randomInterpolate);
            } else {
                finalColor = Misc.interpolateColor(CenterColor,
                        LowerColor,
                        randomInterpolate * -1f);
            }
            int green = finalColor.getGreen();
            int red = finalColor.getRed();
            int blue = finalColor.getBlue();
            Color fringe = new Color(red / 2,
                    green / 2,
                    blue / 2);
            beamMap.get(0).setCoreColor(finalColor);
            beamMap.get(0).setFringeColor(fringe);

            float currentAngle = (inacuracyAngle * (float) Math.random()) - inacuracyAngle / 2;
            weapon.getSpec().getHardpointFireOffsets().set(0,
                    LargeHardpointOffsets.get(counter));
            weapon.getSpec().getHiddenFireOffsets().set(0,
                    LargeTurretOffsets.get(counter));
            weapon.getSpec().getTurretFireOffsets().set(0,
                    LargeTurretOffsets.get(counter));

            weapon.getSpec().getHardpointAngleOffsets().set(0,
                    currentAngle);
            weapon.getSpec().getTurretAngleOffsets().set(0,
                    currentAngle);
            counter++;
            
        }
        
        //this is run while the gun is firing
        if(firingStarted)
        {
            timer += amount;
            if (timer >= interFrameDelay && !recoilFinished)
            {
                timer -= interFrameDelay;
                frameNbr++;
            }
            //since we always increase the counter when we start firing, we are always at counter+1

            if (frameNbr==(counter)*7)
            {
                frameNbr=(counter-1)*7;
                recoilFinished =true;
            }
            theAnim.setFrame(frameNbr);
        }
        
            

            
                
            
            
        
    }
}
