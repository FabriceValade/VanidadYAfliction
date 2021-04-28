/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.weapons.vanidad_beamFocusMineExplosion.createStandardRiftParams;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_interestingVisual {
    	public static void spawnStandardRift(DamagingProjectileAPI explosion) {
                vanidad_negativeExplosionVisual.NEParams params = createStandardRiftParams(new Color(100,100,255,255), 25f);
		CombatEngineAPI engine = Global.getCombatEngine();
		explosion.addDamagedAlready(explosion.getSource());
		
		CombatEntityAPI prev = null;
		for (int i = 0; i < 2; i++) {
			vanidad_negativeExplosionVisual.NEParams p;
                        p = params.clone();
			p.radius *= 0.75f + 0.5f * (float) Math.random();

			p.withHitGlow = prev == null;
			
			Vector2f loc = new Vector2f(explosion.getLocation());
			//loc = Misc.getPointWithinRadius(loc, p.radius * 1f);
			loc = Misc.getPointAtRadius(loc, p.radius * 0.4f);
			
			CombatEntityAPI e = engine.addLayeredRenderingPlugin(new vanidad_negativeExplosionVisual(p));
			e.getLocation().set(loc);
			
			if (prev != null) {
				float dist = Misc.getDistance(prev.getLocation(), loc);
				Vector2f vel = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(loc, prev.getLocation()));
				vel.scale(dist / (p.fadeIn + p.fadeOut) * 0.7f);
				e.getVelocity().set(vel);
			}
			
			prev = e;
		}
		
	}
        
        
    public static vanidad_negativeExplosionVisual.NEParams createStandardRiftParams(Color borderColor, float radius) {
		vanidad_negativeExplosionVisual.NEParams p = new vanidad_negativeExplosionVisual.NEParams();
		//p.radius = 50f;
		p.hitGlowSizeMult = .75f;
		//p.hitGlowSizeMult = .67f;
		p.spawnHitGlowAt = 0f;
		p.noiseMag = 1f;
		//p.fadeIn = 0f;
		//p.fadeOut = 0.25f;
		
		//p.color = new Color(175,100,255,255);
		
		//p.hitGlowSizeMult = .75f;
		p.fadeIn = 0.1f;
		//p.noisePeriod = 0.05f;
		p.underglow = new Color(100, 0, 25, 100);
		//p.withHitGlow = i == 0;
		p.withHitGlow = true;
		
		//p.radius = 20f;
		p.radius = radius;
		//p.radius *= 0.75f + 0.5f * (float) Math.random();
		
		p.color = borderColor;
		return p;
	}
}
