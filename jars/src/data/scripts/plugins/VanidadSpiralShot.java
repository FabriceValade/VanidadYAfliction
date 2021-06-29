/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicRender;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.LazyLib;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Lethargie should hold the area it covers, wether it intersect with an
 * entity and what it does when an entity does so
 */
public class VanidadSpiralShot {

    public boolean expired;
    public ShipAPI source;
    public float damagePerHit;
    public float empPerHit;

    private float fadeTime = 1f;

    //this keeps the subpoints and allow us to act as a wave
    private static final int NUMSUBPROJ = 10;

    private class SubPoint {

        public float angle;
        public float distanceFromCenter;
        public List<String> affectedIds = new ArrayList<>();

        public SubPoint(float distance, float ngl) {
            angle = ngl;
            distanceFromCenter = distance;
        }

        public Vector2f getSubLocation(Vector2f centerLoc, float facingAngle) {
            float realAngle = MathUtils.clampAngle(facingAngle + angle);
            return MathUtils.getPointOnCircumference(
                    centerLoc,
                    distanceFromCenter, realAngle);
        }
    }
    private List<SubPoint> subPoints = new ArrayList<>();

    private float waveWidth;
    private SpriteAPI trailSprite = Global.getSettings().getSprite("fx",
                                                                   "vanidad_trail_smooth");
    private List<Float> trailIds;
    private Float areaTrailId = null;
    private Float areaTrailId2 = null;
    private List<Float> angleOffset = Arrays.asList(0f, 180f);
    private float secondPerRevolution = 2f;
    private float secondPerRevolutionLocal = 3;
    //angle is for the rotation of the whole assembly
    private Float currentAngle = 0f;
    //local is for rotation of the offseted circle upon itself
    private Float currentLocalAngle = 0f;
    private boolean runOnce = false;

    public VanidadSpiralShot(DamagingProjectileAPI proj) {

        this.source = proj.getSource();
        this.expired = false;
        this.damagePerHit = proj.getDamageAmount() / (NUMSUBPROJ + 1);
        this.empPerHit = proj.getEmpAmount() / (NUMSUBPROJ + 1);

        Vector2f decoCenterInitial = new Vector2f();
        Vector2f decoOffsetInitial = new Vector2f();
        for (WeaponAPI w : this.source.getAllWeapons()) {
            switch (w.getSlot().getId()) {
                case "DECOCENTER":
                    decoCenterInitial = w.getSlot().getLocation();
                    break;
                case "DECOOFFSET1":
                    decoOffsetInitial = w.getSlot().getLocation();
                    break;
                case "DECOOFFSET2":
                    break;
            }
        }
        //distance = MathUtils.getDistance(decoCenterInitial, decoOffsetInitial);
        waveWidth = proj.getProjectileSpec().getWidth();
        SpriteAPI shock = Global.getSettings().getSprite("fx", "vanidad_shockwave2");
        MagicRender.objectspace(shock, proj, new Vector2f(-waveWidth/8,0), new Vector2f(0, 0), new Vector2f(waveWidth,waveWidth/2), new Vector2f(0,0),
                                    180, 0, true,
                                    Color.cyan, true, 0,
                                    99f, 0.5f, true);
        //this should initialize all of our subpoints, we only need to track them to see when/where we affect something
        float interPointWidth = waveWidth / NUMSUBPROJ;
        subPoints.add(new SubPoint(0, 0));
        for (int subPointNbr = 1; subPointNbr <= NUMSUBPROJ / 2; subPointNbr++) {
            subPoints.add(new SubPoint(interPointWidth * subPointNbr, 90));
            subPoints.add(new SubPoint(interPointWidth * subPointNbr, -90));
        }

    }

    public void Advance(float amount, DamagingProjectileAPI proj) {
        if (trailIds == null) {
            trailIds = new ArrayList<Float>();
            trailIds.add(MagicTrailPlugin.getUniqueID());
            trailIds.add(MagicTrailPlugin.getUniqueID());
        }
        if (areaTrailId == null) {
            areaTrailId = MagicTrailPlugin.getUniqueID();
        }
        if (areaTrailId2 == null) {
            areaTrailId2 = MagicTrailPlugin.getUniqueID();
        }
        float faceAngle = proj.getFacing();
        Vector2f center = proj.getLocation();
        float b = proj.getBrightness();
        for (int i = 0; i < trailIds.size(); i++) {
            float id = trailIds.get(i);
            float offset = angleOffset.get(i);
            float currentOffsetAngle = MathUtils.clampAngle(
                    offset + currentAngle);
            Vector2f localCenter = MathUtils.getPointOnCircumference(center,
                                                                     waveWidth / 4,
                                                                     currentOffsetAngle);
            Vector2f local2 = getNearestPointOnProjLine(localCenter, center, faceAngle);
            float localResultAngle = MathUtils.clampAngle(
                    currentLocalAngle + currentOffsetAngle);
            MagicTrailPlugin.AddTrailMemberSimple(proj, id, trailSprite,
                                                  local2,
                                                  1f,
                                                  localResultAngle,
                                                  30f,
                                                  15f,
                                                  Color.orange,
                                                  0.25f*b,
                                                  0f,
                                                  0.5f,
                                                  0.5f,
                                                  true);

        }
        MagicTrailPlugin.AddTrailMemberSimple(proj, areaTrailId, trailSprite,
                                              center,
                                              1f,
                                              proj.getFacing(),
                                              waveWidth * 3f,
                                              waveWidth * 1f,
                                              Color.cyan,
                                              0.2f*b,
                                              0f,
                                              0.3f,
                                              0.5f,
                                              false);
        /*MagicTrailPlugin.AddTrailMemberSimple(source, areaTrailId2, trailSprite,
                                              center,
                                              1f,
                                              proj.getFacing(),
                                              waveWidth * 3f,
                                              waveWidth * 3f * 0.7f,
                                              Color.cyan,
                                              0.7f,
                                              0f,
                                              0.1f,
                                              0.1f,
                                              true);*/
        float angleChange = 360f * amount / secondPerRevolution;
        currentAngle += angleChange;
        currentAngle = MathUtils.clampAngle(currentAngle);

        float angleChangeLocal = 360f * amount / secondPerRevolutionLocal;
        currentLocalAngle += angleChangeLocal;
        currentLocalAngle = MathUtils.clampAngle(currentLocalAngle);
    }

    public class Impact {

        public Vector2f loc;
        public boolean isShieldHit;

        public Impact(Vector2f l, boolean b) {
            loc = l;
            isShieldHit = b;
        }
    }

    // this should return all the point where an entity is affected and store the entiry id on each point so they wont affect it again
    //the proj is the current projectile corresponding to this spirl shot
    public List<Impact> affectShip(DamagingProjectileAPI proj, ShipAPI target) {
        List<Impact> Impacts = new ArrayList<>();

        //get a string specifying exactly what we trying to hit
        String entityId = target.getId();

        for (SubPoint p : subPoints) {
            //check if this subpoint already affected the target
            if (p.affectedIds.contains(entityId)) {
                continue;
            }
            Vector2f spaceP = p.getSubLocation(proj.getLocation(),
                                               proj.getFacing());

            //check if the current location is a shield hit
            if (isShieldHit(spaceP, target)) {
                Impacts.add(new Impact(spaceP, true));
                p.affectedIds.add(entityId);
                continue;
            }
            //check if the current location is a hull hit
            if (CollisionUtils.isPointWithinBounds(spaceP, target)) {
                Impacts.add(new Impact(spaceP, false));
                p.affectedIds.add(entityId);
                continue;
            }

        }
        return Impacts;
    }

    public List<Impact> affectMissiles(DamagingProjectileAPI proj, MissileAPI target) {
        String entityId = target.toString();
        List<Impact> Impacts = new ArrayList<>();
        for (SubPoint p : subPoints) {
            if (p.affectedIds.contains(entityId)) {
                continue;
            }
            Vector2f spaceP = p.getSubLocation(proj.getLocation(),
                                               proj.getFacing());
            if (MathUtils.isWithinRange(target.getLocation(), spaceP, 50)) {
                Impacts.add(new Impact(target.getLocation(), false));
                p.affectedIds.add(entityId);
                continue;
            }
        }
        return Impacts;
    }

    private boolean isShieldHit(Vector2f loc, CombatEntityAPI entity) {
        return entity.getShield() != null && entity.getShield().isOn()
                && entity.getShield().isWithinArc(loc);
    }
    
    
    
    private  Vector2f getNearestPointOnProjLine(Vector2f source, Vector2f center, float angle)
    {
        Vector2f lineStart = subPoints.get(0).getSubLocation(center, angle);
        Vector2f lineEnd = subPoints.get(5).getSubLocation(center, angle);
        float u = (source.x - lineStart.x) * (lineEnd.x - lineStart.x)
                + (source.y - lineStart.y) * (lineEnd.y - lineStart.y);
        float denom = Vector2f.sub(lineEnd, lineStart, new Vector2f()).length();
        denom *= denom;

        u /= denom;

        

        Vector2f i = new Vector2f();
        i.x = lineStart.x + u * (lineEnd.x - lineStart.x);
        i.y = lineStart.y + u * (lineEnd.y - lineStart.y);
        return i;
    }
}
