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
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_alternatingBeam implements EveryFrameWeaponEffectPlugin {
    
    private final       Color LowerColor = new Color(251,189,41);
    private final       Color UpperColor = new Color(250,65,41);
    private final float inacuracyAngle = 10;

    //Instantiates variables we will use later
    private vanidad_beamRecoilHandler recoilHandler= new vanidad_beamRecoilHandler();
    private boolean runOnce = false;
    private boolean firingStarted = false;
    
    //stuff for the recoil
    public float recoilDuration = 0.3f;
    public float muzzleDistMin = 1f;
    public float muzzleDistMax = 10f;
    public float muzzleAngleMin = -18f;
    public float muzzleAngleMax = 18f;
    public float muzzleSizeMin = 10f;
    public float muzzleSizeMax = 20f;
    
    private Map<Integer, BeamAPI> beamMap = new HashMap<>();
    
 @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //Don't run if we are paused, or our if weapon is null
        if (engine.isPaused() || weapon == null) {
            return;
        }
        if (!runOnce) {
            runOnce = true;
            recoilHandler.init(engine, weapon, 1);
            recoilHandler.recoilDuration = recoilDuration;
            recoilHandler.muzzleDistMin = muzzleDistMin;
            recoilHandler.muzzleDistMax = muzzleDistMax;
            recoilHandler.muzzleAngleMin = muzzleAngleMin;
            recoilHandler.muzzleAngleMax = muzzleAngleMax;
            recoilHandler.muzzleSizeMin = muzzleSizeMin;
            recoilHandler.muzzleSizeMax = muzzleSizeMax;
        }
        recoilHandler.advance(amount);
        //Resets the beam map and variables between firing
        if (weapon.getChargeLevel() <= 0) {
            beamMap.clear();
            firingStarted = false;
            return;
        }
        

        
        //we run all this stuff when the weapon fire. set offset, color, proper counter
        if (weapon.getChargeLevel() > 0f && !firingStarted) {
           
            
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
            if(counterForBeams==0)
                return;
            firingStarted = true;
            Color finalColor=vanidad_Color.GenRandomColor(LowerColor,UpperColor);
            Color fringe = vanidad_Color.GenFringeColor(finalColor);
            beamMap.get(0).setCoreColor(vanidad_Color.GenCoreColor(finalColor));
            beamMap.get(0).setFringeColor(fringe);
            
            

            float currentAngle = (inacuracyAngle * (float) Math.random())-inacuracyAngle/2f ;

            
            weapon.ensureClonedSpec();
           
           //if we want to modify offset angle instead of actual firing angle
            weapon.getSpec().getHardpointAngleOffsets().set(0,currentAngle);
            weapon.getSpec().getTurretAngleOffsets().set(0,currentAngle);
            
            //lets specify custom position/angle for recoil
            Vector2f backMuzzlePosOffset = new Vector2f(-8,0);
            Vector2f muzzleRotated = VectorUtils.rotate(backMuzzlePosOffset, weapon.getCurrAngle());
            Vector2f firePoint = new Vector2f(0,0);
            Vector2f.add(weapon.getLocation(), muzzleRotated, firePoint);
            Float fireAngle = MathUtils.clampAngle(weapon.getCurrAngle()+180);
            recoilHandler.fire(0,firePoint,fireAngle);
            
            
        }
    }
}
