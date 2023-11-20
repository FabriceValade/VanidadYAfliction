/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.hullmods;


import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

/**
 *
 * @author lethargie
 */
public class vanidad_VAORefit extends BaseHullMod {
    public static final float SPEED_BONUS= 10f;
    public static final float DISSIPATION_BONUS = 10f;
    public static final float ARMORHULL_MALUS = 10f;
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
        stats.getAcceleration().modifyFlat(id, SPEED_BONUS);
        stats.getFluxDissipation().modifyPercent(id, DISSIPATION_BONUS);
        stats.getArmorBonus().modifyPercent(id, -ARMORHULL_MALUS);
        stats.getHullBonus().modifyPercent(id, -ARMORHULL_MALUS);
    }
     @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SPEED_BONUS + " su";
                if (index == 1) return "" + (int) DISSIPATION_BONUS + "%";
                if (index == 2) return "" + (int) ARMORHULL_MALUS + "%";
		return null;
	}

    
    

}

