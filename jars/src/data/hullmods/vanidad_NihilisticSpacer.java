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
public class vanidad_NihilisticSpacer extends BaseHullMod {
    public static final float DPS_BONUS = 5f;
    public static final float REPAIR_MALUS = 10f;
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponDamageMult().modifyPercent(id, DPS_BONUS);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, DPS_BONUS);
        stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f + REPAIR_MALUS * 0.01f);
        
    }
     @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) DPS_BONUS + "%";
                if (index == 1) return "" + (int) REPAIR_MALUS + "%";
		return null;
	}
    
    @Override //All you need is this to be honest. The framework will do everything on its own.
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
                if (ship.getVariant().hasHullMod("CHM_commission")) {
                    ship.getVariant().removeMod("CHM_commission");
                }
				// This is to remove the unnecessary dummy hull mod. Unless the player want it... but nah!
    }
}
