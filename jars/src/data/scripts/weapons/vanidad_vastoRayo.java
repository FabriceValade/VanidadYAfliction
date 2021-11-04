/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicAnim;
import data.scripts.util.MagicRender;
import data.scripts.utils.vanidad_wideBeam;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.CollisionUtils;

public class vanidad_vastoRayo implements EveryFrameWeaponEffectPlugin {

    public final float width = 150;

    private final ArrayList<ShipAPI> targeted_ship = new ArrayList<>();

    private final IntervalUtil timer = new IntervalUtil(0.25f, 0.25f);



    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) return;

        ShipAPI ship = weapon.getShip();

        float dmgMultMissiles = ship.getMutableStats().getDamageToMissiles().getModifiedValue();
        float dmgMultFighter = ship.getMutableStats().getDamageToFighters().getModifiedInt();

        ArrayList<BeamAPI> weaponBeams = new ArrayList<>(2);
        vanidad_wideBeam wideBeams = new vanidad_wideBeam();

        for (BeamAPI beam : engine.getBeams()) {
            if (beam.getWeapon() == weapon) wideBeams= new vanidad_wideBeam(beam, width);
        }
        
        for (Iterator<ShipAPI> iter = targeted_ship.iterator(); iter.hasNext(); ) {
            ShipAPI entry = iter.next();
            if (!entry.isAlive() || !wideBeams.IsCollisionCircleIntersecting(entry)) {
                iter.remove();
            } 
        }
        
        
        //timer.advance(amount);
        //if (timer.intervalElapsed()) {
            if (wideBeams.IsExisting) {
                List<Vector2f> points = wideBeams.rectangle.getPoints();
                SpriteAPI pointSprite = Global.getSettings().getSprite("fx",
                                                                       "vanidad_dot");
                vanidad_wideBeam.Parallels comb = wideBeams.getCombThroughBeam(5);

                for (ShipAPI shipToCheck : CombatUtils.getShipsWithinRange(
                        weapon.getLocation(), weapon.getRange())) {
                    if (shipToCheck.getOwner() == ship.getOwner()) {
                        continue;
                    }
                    if (shipToCheck.getCollisionClass() == CollisionClass.NONE) {
                        continue;
                    }
                    for (int i = 0; i < comb.count(); i++) {
                        Vector2f lineStart = new Vector2f(comb.xstart.get(i),comb.ystart.get(i));
                        Vector2f lineEnd = new Vector2f(comb.xend.get(i),comb.yend.get(i));
                        Vector2f collisionPoint = CollisionUtils.getCollisionPoint(lineStart, lineEnd, shipToCheck);
                        if (collisionPoint!= null)
                            MagicRender.singleframe(pointSprite,
                                                    collisionPoint,
                                                    new Vector2f(10, 10),
                                                    0,
                                                    Color.red,
                                                    false);
                    }
                }

 
                /*
            for (ShipAPI shipToCheck : CombatUtils.getShipsWithinRange(
                    weapon.getLocation(), weapon.getRange())) {
                if (shipToCheck.getOwner() == ship.getOwner()) {
                    continue;
                }
                if (shipToCheck.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }
                if (!targeted_ship.contains(shipToCheck) && wideBeams.IsCollisionCircleIntersecting(shipToCheck))
                    targeted_ship.add(shipToCheck);
            }
            
            
            for (ShipAPI entry : targeted_ship) {
                
            }

                 */
            }
        //}
    }
}
