/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.MuzzleFlashSpec;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_alternatingBeam implements EveryFrameWeaponEffectPlugin {
    
    private final       Color LowerColor = new Color(251,189,41);
     private final       Color CenterColor = new Color(251,102,41);
     private final       Color UpperColor = new Color(250,65,41);
    private final float inacuracyAngle = 20;
    //----------------This area is for setting all offsets for the barrels: note that the turret and hardpoint version of the weapon *must* have an equal amount of offsets--------------------
    //Offsets for medium weapons
    private static Map<Integer, Vector2f> mediumHardpointOffsets = new HashMap<Integer, Vector2f>();
    static {
        mediumHardpointOffsets.put(0, new Vector2f(27.5f, 5.5f));
        mediumHardpointOffsets.put(1, new Vector2f(27.5f, -5.5f));
    }
    private static Map<Integer, Vector2f> mediumTurretOffsets = new HashMap<Integer, Vector2f>();
    static {
        mediumTurretOffsets.put(0, new Vector2f(14f, 5.5f));
        mediumTurretOffsets.put(1, new Vector2f(14f, -5.5f));

    }
    
    //-----------------------------------------------------------------------------END OF OFFSET SPECIFICATIONS---------------------------------------------------------------------------------

    //Instantiates variables we will use later
    private int counter = 0;
    private boolean runOnce = true;
    private float angleMemory = 0;
    private boolean hasmemory = false;
    private float timer =0f;
    private float angleMove = 0f;
  
    private Map<Integer, BeamAPI> beamMap = new HashMap<Integer, BeamAPI>();
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //Don't run if we are paused, or our if weapon is null
        if (engine.isPaused() || weapon == null) {
            return;
        }
        weapon.ensureClonedSpec();
        //Resets the beam map and variables if we are not firing
        if (weapon.getChargeLevel() <= 0) {
            beamMap.clear();
            runOnce = true;
            hasmemory = false;
            timer = 0f;
            return;
        }
        if (hasmemory)
        {
            if (Math.abs(angleMemory)>=1)
            {
                timer+=amount;
                float ratioremaining = 1f- timer/0.5f;
                angleMemory = angleMove*ratioremaining;
                weapon.getSpec().getHardpointAngleOffsets().set(0,angleMemory);
                weapon.getSpec().getTurretAngleOffsets().set(0, angleMemory);
            }
            
            
        }
        //If we are firing, start the code and change variables
        if (weapon.getChargeLevel() > 0f && runOnce) {
            runOnce = false;
            int counterForBeams = 0;
            for (BeamAPI beam : engine.getBeams()) {
                if (beam.getWeapon() == weapon) {
                    if (!beamMap.containsValue(beam)) {
                        beamMap.put(counterForBeams, beam);
                        counterForBeams++;
                    }
                }
            }
        } else {
            return;
        }

        int numOffset = 0;



            counter++;
            if (!mediumHardpointOffsets.containsKey(counter)) {
                counter = 0;
            }
            

            float randomInterpolate = 0.8f*(float)Math.random()-0.4f;
            Color finalColor;
            if (randomInterpolate>=0)
                finalColor = Misc.interpolateColor(CenterColor,
                        UpperColor,
                        randomInterpolate);
            else
                finalColor = Misc.interpolateColor(CenterColor,
                        LowerColor,
                        randomInterpolate*-1f);
            int green = finalColor.getGreen();
            int red = finalColor.getRed();
            int blue = finalColor.getBlue();
            Color fringe = new Color(red/2, green/2, blue/2);
            beamMap.get(0).setCoreColor(finalColor);
            beamMap.get(0).setFringeColor(fringe);
            
            float currentAngle = (inacuracyAngle*(float)Math.random())-inacuracyAngle/2;
            weapon.getSpec().getHardpointFireOffsets().set(numOffset, mediumHardpointOffsets.get(counter));
            weapon.getSpec().getHiddenFireOffsets().set(numOffset, mediumTurretOffsets.get(counter));
            weapon.getSpec().getTurretFireOffsets().set(numOffset, mediumTurretOffsets.get(counter));
            
            weapon.getSpec().getHardpointAngleOffsets().set(numOffset,currentAngle);
            weapon.getSpec().getTurretAngleOffsets().set(numOffset, currentAngle);
            
            angleMove = currentAngle;
            angleMemory = currentAngle;
            hasmemory = true;
            

    }
}
