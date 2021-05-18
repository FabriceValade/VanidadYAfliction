/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

/**
 *
 * @author Lethargie
 */
public class vanidad_redirectPower extends BaseShipSystemScript  {
    
    	public static final float ROF_BONUS = 100f;
	public static final float FLUX_REDUCTION_MULT= 0.1f;
        public static float DAMAGE_BONUS = 50f;
        public static float SPEED_MULT = 0.50f;
	public static float ACCELERATION_MULT = 0.25f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
                //increase are additive
                stats.getEnergyRoFMult().modifyPercent(id, ROF_BONUS*effectLevel);
                stats.getEnergyWeaponDamageMult().modifyPercent(id, DAMAGE_BONUS*effectLevel);
                
                
                //decrease are multiplicative
                float trueFluxReductionMult = 1-((1-FLUX_REDUCTION_MULT)*effectLevel);
                float trueSpeedMult = 1-((1-SPEED_MULT)/effectLevel);
                float trueAccelerationMult = 1-((1-ACCELERATION_MULT)/effectLevel);
                
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, trueFluxReductionMult);
                stats.getMaxSpeed().modifyMult(id, trueSpeedMult);
                stats.getAcceleration().modifyMult(id, trueAccelerationMult);
                stats.getDeceleration().modifyMult(id, trueAccelerationMult);

	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyRoFMult().unmodify();
                stats.getEnergyWeaponFluxCostMod().unmodify();
                stats.getMaxSpeed().unmodify();
                stats.getAcceleration().unmodify();
                stats.getDeceleration().unmodify();
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {

		float bonusPercent = ROF_BONUS * effectLevel;
                float bonusDamage = DAMAGE_BONUS*effectLevel;
                float trueFluxReductionMult = 1-((1-FLUX_REDUCTION_MULT)*effectLevel);
                trueFluxReductionMult*=100;
                float trueSpeedMult = 1-((1-SPEED_MULT)/effectLevel);
                trueSpeedMult*=100;
                float trueAccelerationMult = 1-((1-ACCELERATION_MULT)/effectLevel);
                trueAccelerationMult*=100;
                
		if (index == 0) {
			return new StatusData("Energy rate of fire +" + (int) bonusPercent + "%", false);
		}
                if (index == 1) {
			return new StatusData("Energy damage bonus +" + (int) bonusDamage + "%", false);
		}
		if (index == 2) {
			return new StatusData("Energy flux use multiplicator-" + (int) trueFluxReductionMult + "%", false);
		}
                if (index == 3) {
			return new StatusData("Max speed multiplicator -" + (int) trueSpeedMult + "%", false);
		}
                if (index == 4) {
			return new StatusData("Acceleration multiplicator -" + (int) trueAccelerationMult + "%", false);
		}
		return null;
	}
    
    
}
