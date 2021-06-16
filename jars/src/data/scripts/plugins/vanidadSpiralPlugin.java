/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author Lethargie
 * thank you to ms_shadowy, I kinda learned by reading his code.
 */
public class vanidadSpiralPlugin extends BaseEveryFrameCombatPlugin {
    private CombatEngineAPI engine = null;
    private static final String PROJ_ID = "vanidad_spiral";
    private static final List<VanidadSpiralShot> SHOTS = new ArrayList<>();
    
    
    
    

    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
        }

        if (engine.isPaused()) {
            return;
        }
        
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            String spec = proj.getProjectileSpecId();

            // Is this a spiral proj?
            if (spec == null || !spec.equals(PROJ_ID)) {
                continue;
            }

            VanidadSpiralShot shot = null;
            // See if it is a shot already being tracked
            for (VanidadSpiralShot s : SHOTS) {
                if (s.source == proj.getSource()) {
                    shot = s;
                    shot.expired=false;
                    break;
                }
            }
           
            // Else start tracking it
            if (shot == null) {
                shot = new VanidadSpiralShot(proj);
                SHOTS.add(shot);
            }
            
            shot.Advance(amount, proj);
        }    
        
        
        // Clean up expired shots in SHOTS list
        List<VanidadSpiralShot> toRemove = new ArrayList<>();
        for (VanidadSpiralShot shot : SHOTS) {
            if (shot.expired) {
                toRemove.add(shot);
            }

            shot.expired = true; // We assume each shot will expire next time around
        }
        SHOTS.removeAll(toRemove);


        

    
    }
}
