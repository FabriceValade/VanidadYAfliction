/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.plugins.MagicTrailPlugin;

import java.awt.*;

import static com.fs.starfarer.api.util.Misc.ZERO;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
/**
 *
 * @author lethargie
 */
public class vanidad_revolving_trail implements EveryFrameWeaponEffectPlugin {
    
    
    
    private IntervalUtil effectInterval = new IntervalUtil(0.02f, 0.02f);
    private IntervalUtil animInterval = new IntervalUtil(0.1f, 0.1f);
    private int currentFrame =0;
    private Float trailID = null;
    private SpriteAPI trailSprite = Global.getSettings().getSprite("fx", "vanidad_trail_smooth");
    private AnimationAPI theAnim;
    private boolean runOnce = false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(!runOnce){
            theAnim = weapon.getAnimation();
        }
            
        
        ShipAPI ship = weapon.getShip();
        Float brightness = 0.3f;
        ShipSystemAPI system = ship.getSystem();
        ShipSystemAPI.SystemState state = system.getState();
        boolean unavailable = system.isOn() || state == ShipSystemAPI.SystemState.COOLDOWN || state == ShipSystemAPI.SystemState.OUT;
        
        effectInterval.advance(engine.getElapsedInLastFrame());
        animInterval.advance(engine.getElapsedInLastFrame());
        //handler for the trail
        if (effectInterval.intervalElapsed()) {
            if (trailID == null) {
                trailID = MagicTrailPlugin.getUniqueID();
            }
            
            
            if (!unavailable) {
                MagicTrailPlugin.AddTrailMemberSimple(ship, trailID, trailSprite,
                        weapon.getLocation(),
                        1f,
                        MathUtils.clamp(weapon.getCurrAngle(),0,180f),
                        30f,
                        15f,
                        Color.orange,
                        brightness,
                        0f,
                        0.5f,
                        1f,
                        true);
            }else{
                trailID = null;
            }            
        }
        //handler for the animation
        if (unavailable) {
            //this happen only once when the system is fired
            if (currentFrame != 0) {
                engine.spawnExplosion(weapon.getLocation(), weapon.getShip().getVelocity(), Color.orange, 30, 0.5f);
            }
            currentFrame = 0;

        } else {
            if (animInterval.intervalElapsed()) {
                int numAnimated = theAnim.getNumFrames() - 1;
                currentFrame = MathUtils.getRandomNumberInRange(0,
                                                                numAnimated - 1) + 1;
            }

        }
        theAnim.setFrame(currentFrame);
        
        
        
        //Glows off in refit screen
        if (ship.getOriginalOwner() == -1) {
            brightness = 0f;
            trailID = null;
        }


    }
}