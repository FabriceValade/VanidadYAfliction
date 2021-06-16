/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Lethargie
 */
    public class VanidadSpiralShot {
        public boolean expired;
        public ShipAPI source;
        
        private float distance ;
        private SpriteAPI trailSprite = Global.getSettings().getSprite("fx", "vanidad_trail_smooth");
        private List<Float> trailIds;
        private List<Float> angleOffset = Arrays.asList(0f,180f);
        private float secondPerRevolution = 0.5f;
        private float secondPerRevolutionLocal = 3;
        //angle is for the rotation of the whole assembly
        private Float currentAngle =0f;
        //local is for rotation of the offseted circle upon itself
        private Float currentLocalAngle=0f;

        public VanidadSpiralShot(DamagingProjectileAPI proj) {

            this.source = proj.getSource();
            this.expired = false;
            Vector2f decoCenterInitial= new Vector2f();
            Vector2f decoOffsetInitial= new Vector2f();
            for(WeaponAPI w : this.source.getAllWeapons()){
                switch (w.getSlot().getId()){
                    case "DECOCENTER" :
                        decoCenterInitial = w.getSlot().getLocation();
                        break;
                    case "DECOOFFSET1" :
                        decoOffsetInitial = w.getSlot().getLocation();
                        break;
                    case "DECOOFFSET2" :
                        break;
                }
            }
            distance = MathUtils.getDistance(decoCenterInitial, decoOffsetInitial);
        }
        
        public void Advance(float amount, DamagingProjectileAPI proj ){
            if (trailIds == null) {
                trailIds = new ArrayList<Float>();
                trailIds.add(MagicTrailPlugin.getUniqueID());
                trailIds.add(MagicTrailPlugin.getUniqueID());
            }
            Vector2f center = proj.getLocation();
            for (int i =0 ; i< trailIds.size(); i++){
                float id = trailIds.get(i);
                float offset = angleOffset.get(i);
                float currentOffsetAngle = MathUtils.clampAngle(offset+currentAngle);
                Vector2f localCenter = MathUtils.getPointOnCircumference(center, distance, currentOffsetAngle);
                float localResultAngle = MathUtils.clampAngle(currentLocalAngle+currentOffsetAngle);
                MagicTrailPlugin.AddTrailMemberSimple(source, id, trailSprite,
                    localCenter,
                    1f,
                    localResultAngle,
                    30f,
                    15f,
                    Color.cyan,
                    0.5f,
                    0f,
                    0.5f,
                    0.5f,
                    true);
                
            }
            float angleChange = 360f * amount/secondPerRevolution;
            currentAngle += angleChange;
            currentAngle = MathUtils.clampAngle(currentAngle);
            
            float angleChangeLocal = 360f * amount/secondPerRevolutionLocal;
            currentLocalAngle += angleChangeLocal;
            currentLocalAngle = MathUtils.clampAngle(currentLocalAngle);
        }
        
    }
