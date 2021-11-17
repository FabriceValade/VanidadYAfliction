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
 * @author Fabrice Valade
 */
public class vanidad_delayTheInevitable extends BaseShipSystemScript {
    
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {

        stats.getShieldDamageTakenMult().modifyMult(id, 0.05f);

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().unmodify();
    }

    @Override
    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {

        if (index == 0) {
            return new ShipSystemStatsScript.StatusData(
                    "Absorbed Shield flux redirected to secundary capacitor at 0.95%",
                    false);
        }

        return null;
    }

}
