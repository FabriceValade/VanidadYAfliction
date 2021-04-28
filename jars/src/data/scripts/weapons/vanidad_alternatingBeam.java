/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_alternatingBeam implements EveryFrameWeaponEffectPlugin {
    
    private final float inacuracyAngle = 15;
    //----------------This area is for setting all offsets for the barrels: note that the turret and hardpoint version of the weapon *must* have an equal amount of offsets--------------------
    //Offsets for medium weapons
    private static Map<Integer, Vector2f> mediumHardpointOffsets = new HashMap<Integer, Vector2f>();
    static {
        mediumHardpointOffsets.put(0, new Vector2f(27.5f, 5.5f));
        mediumHardpointOffsets.put(1, new Vector2f(27.5f, -5.5f));
    }
    private static Map<Integer, Vector2f> mediumTurretOffsets = new HashMap<Integer, Vector2f>();
    static {
        mediumTurretOffsets.put(0, new Vector2f(18f, 5.5f));
        mediumTurretOffsets.put(1, new Vector2f(18f, -5.5f));

    }

    //-----------------------------------------------------------------------------END OF OFFSET SPECIFICATIONS---------------------------------------------------------------------------------

    //Instantiates variables we will use later
    private int counter = 0;
    private boolean runOnce = true;
    private Map<Integer, BeamAPI> beamMap = new HashMap<Integer, BeamAPI>();
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //Don't run if we are paused, or our if weapon is null
        if (engine.isPaused() || weapon == null) {
            return;
        }

        //Resets the beam map and variables if we are not firing
        if (weapon.getChargeLevel() <= 0) {
            beamMap.clear();
            runOnce = true;
            return;
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

        //For converge code: hide the first beam by making it invisible, and ensure all further operations are done on the second beam
        int numOffset = 0;
        if (beamMap.get(1) != null) {
            beamMap.get(0).setCoreColor(new Color(0f, 0f, 0f));
            beamMap.get(0).setFringeColor(new Color(0f, 0f, 0f));
            numOffset = 1;
        }

        //The big if-block where the magic happens: change a weapon's fireOffset via the alternating pattern specified by small/medium/large Turret/Hardpoint Offsets
        if (weapon.getSize() == WeaponAPI.WeaponSize.MEDIUM) {
            counter++;
            if (!mediumHardpointOffsets.containsKey(counter)) {
                counter = 0;
            }
            float green = 0;
            float red;
            float blue;
            float rColor = (float)Math.random();
            if (Math.random()>0.5) {
                red=1f;
                blue=rColor;               
            }
            else {
                red=rColor;
                blue=1f;
            }
            beamMap.get(0).setCoreColor(new Color(red,green,blue));
            beamMap.get(0).setFringeColor(new Color(red*0.5f, green, blue*0.5f));
            weapon.getSpec().getHardpointFireOffsets().set(numOffset, mediumHardpointOffsets.get(counter));
            weapon.getSpec().getHiddenFireOffsets().set(numOffset, mediumTurretOffsets.get(counter));
            weapon.getSpec().getTurretFireOffsets().set(numOffset, mediumTurretOffsets.get(counter));
            weapon.getSpec().getHardpointAngleOffsets().set(numOffset, (inacuracyAngle*(float)Math.random())-inacuracyAngle/2);
            weapon.getSpec().getTurretAngleOffsets().set(numOffset, (inacuracyAngle*(float)Math.random())-inacuracyAngle/2);
        } 
    }
}
