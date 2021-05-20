/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
/**
 *
 * @author Lethargie
 */
public class vanidad_autarky_industry extends BaseIndustry{
    
    @Override
    public void apply() {
        super.apply(true);

        //If we have a Beta core, we actually get a bonus to our production of Ore, Food and Volatiles
        boolean beta = Commodities.BETA_CORE.equals(aiCoreId);
        int basicResourceBonus = 0;

        int size = market.getSize();

        demand(Commodities.HEAVY_MACHINERY, size);

        supply(Commodities.FOOD, size);
        if (!isFunctional()) {
            supply.clear();
        }
    }
}
