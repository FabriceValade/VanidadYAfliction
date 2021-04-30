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
public class vanidad_alternatingBeamSmall implements EveryFrameWeaponEffectPlugin {
    
    private final       Color LowerColor = new Color(251,219,41);
    private final       Color CenterColor = new Color(251,189,41);
    private final       Color UpperColor = new Color(251,102,41);
    private final float inacuracyAngle = 10;

    
    
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

            weapon.ensureClonedSpec();
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

            
            weapon.getSpec().getHardpointAngleOffsets().set(0,currentAngle);
            weapon.getSpec().getTurretAngleOffsets().set(0, currentAngle);

            
                
            
            
         
    }
}
