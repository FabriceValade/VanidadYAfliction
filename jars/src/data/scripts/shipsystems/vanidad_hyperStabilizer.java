/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

/**
 *
 * @author Lethargie
 */
public class vanidad_hyperStabilizer extends BaseShipSystemScript  {
    
    	public static final float RANGE_BONUS_FLAT = 200f;
	
	public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
		

                
                stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BONUS_FLAT*effectLevel);
                stats.getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BONUS_FLAT*effectLevel);
                
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponRangeBonus().unmodify();
                stats.getEnergyWeaponRangeBonus().unmodify();
	}
	
	public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {


                float rangeBonus = RANGE_BONUS_FLAT*effectLevel;
		if (index == 0) {
			return new ShipSystemStatsScript.StatusData("All weapon range bonus +" + (int) rangeBonus + "SU", false);
		}

		return null;
	}
    
    
}
