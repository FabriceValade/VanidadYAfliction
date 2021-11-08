/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.utils;

import com.fs.starfarer.api.combat.ShieldAPI;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_PiePart {
        private Vector2f center;
        private float angleFacing;
        private float angleExtend;
        private float radius;
        public vanidad_PiePart(Vector2f center, float radius, float angleFacing, float angleExtent){
            this.center = center;
            this.angleExtend = angleExtent;
            this.angleFacing = angleFacing;
            this.radius = radius;
            
        }
        
        public boolean IsPointInsideAngles(Vector2f point){
            Vector2f centerOriginatingPoint = new Vector2f();
            Vector2f.sub(point, center, centerOriginatingPoint);
            float pointAngle = (float)Math.atan2(centerOriginatingPoint.y, centerOriginatingPoint.x);
            float degreePointAngle = ((float)Math.toDegrees(pointAngle)+360)%360;
            return IsAngleInside(degreePointAngle);
        }
                
        private boolean IsAngleInside(float angle){
            float distance = Math.abs(angle-angleFacing);
            float smallestdistance = distance > 180 ? 360-distance : distance;
            return smallestdistance<angleExtend;
        }
        private boolean IsDistanceInsideRadius(float distanceSquared)
        {
            return distanceSquared < radius*radius;
        }
        public Vector2f GetClosestIntersectionPoint(Vector2f start, Vector2f end){
            Vector2f unitVector = VectorUtils.getDirectionalVector(start, end);
            Vector2f StartToEnd = Vector2f.sub(end, start, null);
            Vector2f StartToCenter = Vector2f.sub(center, start, null);

            float projection = Vector2f.dot(StartToCenter, unitVector);
            if (projection < 0 )
                return null;
            
            float ShortestDistanceSquared =  StartToCenter.lengthSquared()-projection*projection;
           if (!IsDistanceInsideRadius(ShortestDistanceSquared))
                return null;
            
            float distanceToCircleEdgeFromPerpendicular = (float)Math.sqrt(radius*radius - ShortestDistanceSquared);
            if (distanceToCircleEdgeFromPerpendicular > projection)
                return null;
            
            float distanceFromStart = projection-distanceToCircleEdgeFromPerpendicular;
            Vector2f intersectionPoint = vanidad_util.GetPointFrom(start, unitVector, distanceFromStart);
            if (!IsPointInsideAngles(intersectionPoint))
                return null;
            return intersectionPoint;
        
            
        }
        
        
    
}
