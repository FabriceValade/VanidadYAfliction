/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.impl.campaign.skills.WolfpackTactics;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicRender;
import java.awt.Color;
import java.util.ArrayList;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import java.util.HashMap;

/**
 *
 * @author Lethargie
 */
public class vanidad_spiralSytemAi implements ShipSystemAIScript{
    private final IntervalUtil timer = new IntervalUtil(0.5f, 1f);
    private ShipAPI ship;
    private ShipSystemAPI system;
    private float projSpeed = 500; 
    private float range = 1000;
    ShipwideAIFlags localFlags;
    String flagconcat = "";
    ArrayList<String> flagValue = new ArrayList<String>();
    
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.localFlags = flags;
    }

    //we want to fire this thing when something is in range and is vulnerable
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        timer.advance(amount);
        //just so we dotn call that stuff all the time
        if (!timer.intervalElapsed()) {
            //return;
        }
        
        if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
        float firingScore =0;
        if (target==null){
            //return;
        }
        /*
        if (timer.intervalElapsed()) {
            String flagConcatNew = "";
            ArrayList<String> flagValueNew = new ArrayList<String>();
            for (ShipwideAIFlags.AIFlags flagKey : ShipwideAIFlags.AIFlags.values()) {
                if (localFlags.hasFlag(flagKey)) {
                    flagConcatNew = flagConcatNew + flagKey.toString();
                    flagValueNew.add(flagKey.toString());
                }

            }
            if (!flagConcatNew.equals(flagconcat)){
                flagconcat = flagConcatNew;
                flagValue = flagValueNew;
                float offset = 0;
                for(String key : flagValue){
                    
                    
                    offset+=50;
                }
            }
        
        }
*/
        if(true)
            return;
        Vector2f pointToAim = AIUtils.getBestInterceptPoint(ship.getLocation(), projSpeed, target.getLocation(),
                                      target.getVelocity());
        float angleShouldAim = 0;
        boolean inRange = false;
        if (pointToAim != null) {
            Vector2f separation = Vector2f.sub(pointToAim, ship.getLocation(),
                                               null);
            inRange = MathUtils.getDistanceSquared(pointToAim,
                                                           ship.getLocation()) < (range * range);
            angleShouldAim = VectorUtils.getFacing(separation);
            SpriteAPI target_sprite = Global.getSettings().getSprite("fx",
                                                                     "vanidad_target");
            MagicRender.singleframe(target_sprite,pointToAim,new Vector2f(200,200),angleShouldAim,Color.blue,true);
            
        }
        
        if(Math.abs(angleShouldAim-ship.getFacing())< 10 && inRange){
            //firingScore+=11;
            
        }
        
        if (firingScore >= 10f){
            Global.getCombatEngine().addFloatingText(new Vector2f(ship.getLocation().x, ship.getLocation().y), "lets go", 60, Color.WHITE, ship, 0.25f, 0.25f);
            activateSystem();
        }

    }


    private void activateSystem() {
        if (!system.isOn()) {
            ship.useSystem();
        }
        
    }
}
