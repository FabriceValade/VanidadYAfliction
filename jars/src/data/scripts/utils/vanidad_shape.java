/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.utils;

import com.fs.starfarer.api.combat.BoundsAPI;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;
import org.lazywizard.lazylib.CollisionUtils;
import org.lwjgl.util.mapped.MappedObject;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_shape implements BoundsAPI
{
    public class Segment implements BoundsAPI.SegmentAPI {
        private Vector2f P1;
        private Vector2f P2;
        
        public Segment(Vector2f p1, Vector2f p2) {
            P1 = p1;
            P2 = p2;
        }
        public Segment(float x1, float y1, float x2, float y2){
            P1 = new Vector2f(x1,y1);
            P2 = new Vector2f(x2,y2);
        }
        @Override
        public Vector2f getP1() {
            return P1;
        }

        @Override
        public Vector2f getP2() {
            return P2;
        }
    }
    
    private List<SegmentAPI> segmentList = new ArrayList<SegmentAPI>();
    

    
    public vanidad_shape()
    {

    }
    

    @Override
    public void update(Vector2f location, float facing) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SegmentAPI> getSegments() {
        List<SegmentAPI> displaySegmentList = new ArrayList<SegmentAPI>(segmentList);
        SegmentAPI completingSegment = new Segment(this.getLastPoint(), this.getFirstPoint());
        displaySegmentList.add(completingSegment);
        return displaySegmentList;
    }

    @Override
    public void clear() {
        segmentList.clear();
    }

    @Override
    public void addSegment(float x1, float y1, float x2, float y2) {
        if (!segmentList.isEmpty()) {
            addSegment(x1,y1);
        }
        segmentList.add( new Segment(x1,y1,x2,y2));
    }

    @Override
    public void addSegment(float x2, float y2) {
        Vector2f newSegmentP2 = new Vector2f(x2, y2);
        segmentList.add(new Segment(this.getLastPoint(), newSegmentP2));
    }
    
    public void addSegment(Vector2f p1, Vector2f p2){
        this.addSegment(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    
    public void addSegment(Vector2f p1){
        this.addSegment(p1.getX(), p1.getY());
    }
    
    public Vector2f getFirstPoint(){
        if (segmentList.isEmpty())
            return null;
        return segmentList.get(0).getP1();
    }
    
    public Vector2f getLastPoint(){
        if(segmentList.isEmpty())
            return null;
        return segmentList.get(segmentList.size()-1).getP2();
    }
    
    public List<Vector2f> getPoints()
    {
        List<Vector2f> points = new ArrayList<>(segmentList.size() + 1);
        SegmentAPI seg;
        for (int x = 0; x < segmentList.size(); x++) {
            seg = segmentList.get(x);

            points.add(seg.getP1());
            // Make sure to add the final point
            if (x == (segmentList.size() - 1)) {
                points.add(seg.getP2());
            }
        }
        return points;
    }
    
    public boolean isShapeEmpty(){
        return segmentList.isEmpty();
    }
    
    public boolean isCollides(Vector2f circleCenter, float circleRadius){
        if (isShapeEmpty())
            return false;
        if (isPointWithinShape(circleCenter))
            return true;
        for (SegmentAPI segment : segmentList){
            if (CollisionUtils.getCollides(segment.getP1(), segment.getP2(),circleCenter, circleRadius))
                return true;
        }
            
        return false;
    }
    
    //this method is slow as molasse (m x n) . Not too bad if the shape are triangle/rectangle, but dont abuse it for larger
    public boolean isCollides(BoundsAPI bound){
        if (this.isShapeEmpty())
            return false;
        boolean testedPoints = false;
        List<SegmentAPI> boundSegments = bound.getSegments();
        for(SegmentAPI localSegment : segmentList){
            for(SegmentAPI boundSegment : boundSegments){
                if (!testedPoints){
                    if (isPointWithinShape(boundSegment.getP1()) || isPointWithinShape(boundSegment.getP2()))
                        return true;
                }
                Vector2f collision = CollisionUtils.getCollisionPoint(boundSegment.getP1(), boundSegment.getP2(), localSegment.getP1(), localSegment.getP2());
                if (collision != null)
                    return true;
            }
            testedPoints = true;
        }
        return false;
    }
    
    public boolean isPointWithinShape(Vector2f point)
    {
        if (isShapeEmpty())
            return false;
        List<Vector2f> points = new ArrayList<>(segmentList.size() + 1);
        SegmentAPI seg;
        for (int x = 0; x < segmentList.size(); x++) {
            seg = segmentList.get(x);

            // Use this opportunity to test if the point is exactly on the bounds
            if (CollisionUtils.isPointOnSegment(point, seg)) {
                return true;
            }

            points.add(seg.getP1());
            // Make sure to add the final point
            if (x == (segmentList.size() - 1)) {
                points.add(seg.getP2());
            }
        }
        return isPointInsidePolygon(points, point);
    }
    
    public static boolean isPointInsidePolygon (List<Vector2f> points, Vector2f point){
        //stolen from magiclib CollisionUtils
        // Check if the point is inside the bounds polygon
        // This code uses the extremely efficient PNPOLY solution taken from:
        // http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
        int i, j;
        boolean result = false;
        for (i = 0, j = points.size() - 1; i < points.size(); j = i++)
        {
            if ((points.get(i).y > point.y) != (points.get(j).y > point.y)
                    && (point.x < (points.get(j).x - points.get(i).x)
                    * (point.y - points.get(i).y)
                    / (points.get(j).y - points.get(i).y) + points.get(i).x))
            {
                result = !result;
            }
        }
        return result;
    }
}
