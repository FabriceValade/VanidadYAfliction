/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;


/**
 *
 * @author Fabrice Valade
 */
public class vanidad_glow implements EveryFrameWeaponEffectPlugin {
    enum State {up,on,down,off};
    private boolean runOnce = false;
    private float chargeUpDur, chargeDownDur, timer;
    private Color transparent = new Color(255,255,255,0);
    private Color opaque = new Color(1f,1f,1f,1f);
    private Color toSet = transparent;
    private State state = State.off;
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        
        
        ShipSystemAPI system = weapon.getShip().getSystem();

        if (!runOnce) {
            runOnce= true;
            chargeUpDur = system.getChargeUpDur();
            chargeDownDur = system.getChargeDownDur();
        }
        
        if (state == State.off){
            if (system.isChargeup())
                state = State.up;
            else
                toSet=transparent;
        }
        
        if (state == State.up){
            timer+=amount;
            float level = timer/chargeUpDur;
            if (level >=1)
            {
                timer = 0;
                state = State.on;
            }else
                toSet = new Color(1,1,1,level);
        }
        
        if (state == State.on){
            if (system.isChargedown())
                state = State.down;
            else{
                toSet = opaque;
            }
                
        }
        
        if (state == State.down){
            if (!system.isActive()){
                timer = 0;
                state = State.off;
                toSet=transparent;
            }else{
                timer+=amount;
                float level = 1- timer/chargeDownDur;
                level = Math.max(level, 0);
                toSet = new Color(1,1,1,level); 
            }
        }

        weapon.getSprite().setColor(toSet);
    }
    
}
