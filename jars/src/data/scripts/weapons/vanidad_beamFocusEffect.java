/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_beamFocusEffect implements BeamEffectPlugin {
        public static Color STANDARD_RIFT_COLOR = new Color(100,60,255,255);
	public static Color EXPLOSION_UNDERCOLOR = new Color(100, 0, 25, 100);
	public static Color NEGATIVE_SOURCE_COLOR = new Color(29,245,0,50);
	
	public static String ESTARAYO_MINELAYER = "vanidad_estarayo_minelayer";
	
	public static int MAX_RIFTS = 1;
	public static float UNUSED_RANGE_PER_SPAWN = 200;
	public static float SPAWN_SPACING = 175;
	public static float SPAWN_INTERVAL = 0.1f;
	
	
	
	protected Vector2f arcFrom = null;
	protected Vector2f prevMineLoc = null;
	
	protected boolean doneSpawningMines = false;
	protected float spawned = 0;
	protected int numToSpawn = 0;
	protected float untilNextSpawn = 0;
	protected float spawnDir = 0;
	
	protected IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);
    
    	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		tracker.advance(amount);

		
		if (beam.getBrightness() < 1f) return;
                
                if (doneSpawningMines) return;
		
		if (numToSpawn <= 0 && beam.getDamageTarget() != null) {
			float range = beam.getWeapon().getRange();
			float length = beam.getLengthPrevFrame();
			//float perSpawn = range / NUM_SPAWNS;
			numToSpawn = (int) ((range - length) / UNUSED_RANGE_PER_SPAWN) + 1;
			if (numToSpawn > MAX_RIFTS) {
				numToSpawn = MAX_RIFTS;
			}
			untilNextSpawn = 0f;
		}
                
                untilNextSpawn -= amount;
		if (untilNextSpawn > 0) return;
//		if (!canSpawn || beam.getBrightness() >= 1f) return;
		
		float perSpawn = SPAWN_SPACING;
		
		ShipAPI ship = beam.getSource();
		
		boolean spawnedMine = false;
		if (beam.getLength() > beam.getWeapon().getRange() - 10f) {
			float angle = Misc.getAngleInDegrees(beam.getFrom(), beam.getRayEndPrevFrame());
			Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
			loc.scale(beam.getLength());
			Vector2f.add(loc, beam.getFrom(), loc);
			
			spawnMine(ship, loc);
			spawnedMine = true;
		}

		if (spawnedMine) {
				doneSpawningMines = true;
		}
        }
        
        public void spawnMine(ShipAPI source, Vector2f mineLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		          MissileAPI mine = (MissileAPI) engine.spawnProjectile(source,
                    null,
                    ESTARAYO_MINELAYER,
                    mineLoc,
                    (float) Math.random() * 360f,
                    null);
		
		// "spawned" does not include this mine
		float sizeMult = 1f;
		mine.setCustomData(vanidad_beamFocusMineExplosion.SIZE_MULT_KEY, sizeMult);
			
		if (source != null) {
			Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
											source, WeaponAPI.WeaponType.MISSILE, false, mine.getDamage());
		}
		
		mine.getDamage().getModifier().modifyMult("mine_sizeMult", sizeMult);
		
		
		float fadeInTime = 0.05f;
		mine.getVelocity().scale(0);
		mine.fadeOutThenIn(fadeInTime);
		
		//Global.getCombatEngine().addPlugin(createMissileJitterPlugin(mine, fadeInTime));
		
		//mine.setFlightTime((float) Math.random());
		float liveTime = 0f;
		//liveTime = 0.01f;
		mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
		mine.addDamagedAlready(source);
		mine.setNoMineFFConcerns(true);
		
		prevMineLoc = mineLoc;
	}
        

}
