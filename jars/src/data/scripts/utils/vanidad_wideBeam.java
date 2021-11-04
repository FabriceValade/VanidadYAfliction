/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



package data.scripts.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicAnim;
import data.scripts.util.MagicRender;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.List;
import static org.lazywizard.lazylib.CollisionUtils.getCollides;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_wideBeam {
    private BeamAPI coreBeam;
    private float width;
    private Vector2f LastIntersectWakeDirection;
    private Vector2f directionVector;
    private Vector2f perpendicularDirectionVectorAddtoLess;
    private Vector2f perpendicularAdd;
    private Vector2f perpendicularLess;
    private enum Corners //bottom is getFrom +- perpendicular
    {
        BOTTOMADD,
        BOTTOMLESS,
        TOPADD,
        TOPLESS;
        
        public static Corners getCorners(int i) {
            return Corners.values()[i];
        }
      
    }
    public vanidad_shape rectangle = new vanidad_shape();
    public boolean IsExisting = false;
    public vanidad_wideBeam(){
        
    }
    public vanidad_wideBeam(BeamAPI beam, float width)
    {
        this.width = width;
        this.coreBeam = beam;
        Vector2f source = coreBeam.getFrom();
        Vector2f end = coreBeam.getTo();
        directionVector = VectorUtils.getDirectionalVector(source, end);
        perpendicularDirectionVectorAddtoLess = MakeRotatedVector(directionVector, -90);
        perpendicularAdd = MakeRotatedScaledVector(directionVector, 90, this.width/2);
        perpendicularLess = MakeRotatedScaledVector(directionVector, -90, this.width/2);
        this.UpdateShape();
        IsExisting = true;
    }
    public Vector2f getCollisionCircleIntersectionProjectionAddMax(CombatEntityAPI entity, boolean isFromTop){
        Vector2f AddToCenter = new Vector2f();
        Vector2f.sub(entity.getLocation(), this.GetCornerValue(Corners.BOTTOMADD), AddToCenter);

        //because of the sense of the unit vector, negative are outside the real segment, virtual projection point, greater than width are virtual too, but on the other side
        float add_To_CircleCenterProjection_Distance = Vector2f.dot(AddToCenter, perpendicularDirectionVectorAddtoLess);
        float distance;
        if (add_To_CircleCenterProjection_Distance > 0)
            distance = add_To_CircleCenterProjection_Distance- entity.getCollisionRadius();
        else
            distance = add_To_CircleCenterProjection_Distance;
        if(isFromTop)
            return getPointFromProjectionDistance(Corners.TOPADD, distance);
        return getPointFromProjectionDistance(Corners.BOTTOMADD, distance);
    }
    
    public Vector2f getCollisionCircleIntersectionProjectionAddMin(CombatEntityAPI entity, boolean isFromTop){
        Vector2f AddToCenter = new Vector2f();
        Vector2f.sub(entity.getLocation(), this.GetCornerValue(Corners.BOTTOMADD), AddToCenter);

        //because of the sense of the unit vector, negative are outside the real segment, virtual projection point, greater than width are virtual too, but on the other side
        float add_To_CircleCenterProjection_Distance = Vector2f.dot(AddToCenter, perpendicularDirectionVectorAddtoLess);
        float distance;
        if (add_To_CircleCenterProjection_Distance > (width-entity.getCollisionRadius()))
            distance = width;
        else
            distance = add_To_CircleCenterProjection_Distance + entity.getCollisionRadius();
        if(isFromTop)
            return getPointFromProjectionDistance(Corners.TOPADD, distance);
        return getPointFromProjectionDistance(Corners.BOTTOMADD, distance);
    }
    
   private Vector2f getPointFromProjectionDistance(Corners corner, float distance){
        Vector2f correction = new Vector2f(perpendicularDirectionVectorAddtoLess);
        VectorUtils.resize(correction, distance);
        Vector2f intersection = new Vector2f();
        Vector2f.add(correction,
                     this.GetCornerValue(corner),
                     intersection);
        return intersection;

    }
    
    
    public boolean IsCollisionCircleIntersecting(CombatEntityAPI entity)
    {
        return rectangle.isCollides(entity.getLocation(), entity.getCollisionRadius());
    }
    
    public boolean IsEntityIntersecting(CombatEntityAPI entity)
    {
        BoundsAPI bounds = entity.getExactBounds();

        // Entities that lack bounds will use the collision circle instead
        if (bounds == null)
        {
            return this.IsCollisionCircleIntersecting(entity);
        }
        // Convert all segments to lines, do collision checks to find closest hit
        bounds.update(entity.getLocation(), entity.getFacing());
        return rectangle.isCollides(bounds);
    }
    
    public Parallels getCombThroughBeam(int numberOfComb) {
        Parallels result = new Parallels();
        float interCombFraction = 1f / (float)numberOfComb;
        Vector2f displacement = new Vector2f();
        Vector2f start= new Vector2f();
        Vector2f end= new Vector2f();
        for (int i=0; i<=numberOfComb; i++){
            float combDistance = interCombFraction*width*i;
            VectorUtils.resize(perpendicularDirectionVectorAddtoLess, combDistance, displacement);
            Vector2f.add(this.GetCornerValue(Corners.BOTTOMADD), displacement, start);
            Vector2f.add(this.GetCornerValue(Corners.TOPADD), displacement, end);
            result.addLine(start, end);
        }
        return result;
    }
    
    private static Vector2f MakeRotatedVector(Vector2f Source, float angle){
        Vector2f dest = new Vector2f(Source);
        VectorUtils.rotate(dest, angle);
        return dest;
    }
    private static Vector2f MakeRotatedScaledVector(Vector2f source, float angle, float finalLength)
    {
        Vector2f dest = new Vector2f();
        source.normalise(dest);
        VectorUtils.rotate(dest, angle);
        VectorUtils.resize(dest, finalLength);
        return dest;
    }
    
    private Vector2f GetCornerValue(Corners corner)
    {
        Vector2f source = new Vector2f();
        Vector2f toSum = new Vector2f();
        switch (corner)
        {
            case BOTTOMADD:
                toSum = perpendicularAdd;
                source = coreBeam.getFrom();
                break;
            case BOTTOMLESS:
                toSum = perpendicularLess;
                source = coreBeam.getFrom();
                break;
            case TOPADD:
                toSum = perpendicularAdd;
                source = coreBeam.getTo();
                break;
            case TOPLESS:
                toSum = perpendicularLess;
                source = coreBeam.getTo();
                break;
        }
        Vector2f result = new Vector2f();
        Vector2f.add(source, toSum, result);
        return result;
    }
    
    private void UpdateShape()
    {
        rectangle.clear();
        rectangle.addSegment(this.GetCornerValue(Corners.BOTTOMADD), this.GetCornerValue(Corners.BOTTOMLESS));
        rectangle.addSegment(this.GetCornerValue(Corners.TOPLESS));
        rectangle.addSegment(this.GetCornerValue(Corners.TOPADD));
    }
    
    
    
    public class Parallels{
        public List<Float> xstart = new ArrayList<Float>();
        public List<Float> ystart= new ArrayList<Float>();
        public List<Float> xend= new ArrayList<Float>();
        public List<Float> yend= new ArrayList<Float>();
        public int count(){
            return xstart.size();
        }
        public Parallels(){
            
        }
        public void addLine(Vector2f start, Vector2f end){
            xstart.add(start.x);
            ystart.add(start.y);
            xend.add(end.x);
            yend.add(end.y);
        }
        public void clear(){
            xstart.clear();
            ystart.clear();
            xend.clear();
            yend.clear();
        }
    }
}
