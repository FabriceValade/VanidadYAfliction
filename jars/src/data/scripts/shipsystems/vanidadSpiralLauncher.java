/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author lethargie
 * 
 */
public class vanidadSpiralLauncher extends BaseShipSystemScript{
    private boolean runOnce=false;
    
    
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (!runOnce) {
            ShipAPI ship = null;
            CombatEngineAPI engine = Global.getCombatEngine();
            //boolean player = false;
            if (stats.getEntity() instanceof ShipAPI) {
                ship = (ShipAPI) stats.getEntity();
            } else {
                return;
            }
            Vector2f origin = MathUtils.getPointOnCircumference(ship.getLocation(), 60, ship.getFacing());
            engine.spawnProjectile(ship, null, "vanidad_spiraldischarge", origin, ship.getFacing(), ship.getVelocity());
            SpriteAPI shock = Global.getSettings().getSprite("fx", "vanidad_shockwave");
            float angle = MathUtils.clampAngle(ship.getFacing()+180);
            MagicRender.objectspace(shock, ship, new Vector2f(60,0), new Vector2f(200, 0), new Vector2f(50,50), new Vector2f(800,800),
                                    180, 0, true,
                                    Color.cyan, true, 0,
                                    0.2f, 0.5f, false);
            runOnce = true;
        }
    }
    
    public void unapply(MutableShipStatsAPI stats, String id) {
        runOnce=false;
        
    }
    
    public StatusData getStatusData(int index, State state, float effectLevel) {
        
        return null;
    }
}
