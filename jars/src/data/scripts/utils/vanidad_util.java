/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.utils;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import java.awt.geom.Line2D;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public final class vanidad_util {
    
    private vanidad_util(){}
    
    public static Vector2f RayIntersectionWithShield(CombatEntityAPI entity, Vector2f start, Vector2f end){
        
        if(entity.getShield() == null || !entity.getShield().isOn())
            return null;
        float angle = entity.getFacing();
        Vector2f loc = entity.getLocation();
        Vector2f loc2 = entity.getShield().getLocation();
        float width = entity.getShield().getActiveArc();
        Vector2f ShieldLocation = Vector2f.add(entity.getLocation(), entity.getShield().getLocation(), null);
        Vector2f test = Vector2f.add(loc, loc2, null);
        Vector2f realShieldLocation = VectorUtils.rotateAroundPivot(ShieldLocation, entity.getLocation(), angle);
        vanidad_PiePart piePart = new vanidad_PiePart(loc2, 
                entity.getShield().getRadius(), 
                entity.getShield().getFacing(), 
                entity.getShield().getActiveArc());
        return piePart.GetClosestIntersectionPoint(start, end);
        

    }
    
    public static Vector2f GetPointFrom(Vector2f start, Vector2f unitVector, float length){
        Vector2f displacement = VectorUtils.resize(unitVector, length);
        return Vector2f.add(start, displacement, null);
    }
   public static float GetPointSegmentDistanceSquared(Vector2f lineStart, Vector2f lineEnd, Vector2f point){
        return (float)Line2D.ptSegDistSq(lineStart.x, lineStart.y, lineEnd.x,
                lineEnd.y, point.x, point.y);
    } 
    
    
}
