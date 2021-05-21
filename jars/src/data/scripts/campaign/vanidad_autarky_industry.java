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
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Lethargie
 */
public class vanidad_autarky_industry extends BaseIndustry implements MarketImmigrationModifier{
    
    public static List<String> AUTARKY_CONDITIONS = new ArrayList<String>();
	static {
		AUTARKY_CONDITIONS.add(Conditions.COLD);
		AUTARKY_CONDITIONS.add(Conditions.VERY_COLD);
		AUTARKY_CONDITIONS.add(Conditions.POOR_LIGHT);
		AUTARKY_CONDITIONS.add(Conditions.DARK);
	}
    public static float DEFENSE_BONUS_BASE = 0.25f;
    public static float ACCESSABILITY_BONUS_BASE = -0.1f;

    @Override
    public void apply() {
        super.apply(true);

        boolean beta = Commodities.BETA_CORE.equals(aiCoreId);
        int basicResourceBonus = 0;

        int size = market.getSize();

        demand(Commodities.HEAVY_MACHINERY, size);
        demand(Commodities.SUPPLIES, size);

        supply(Commodities.FOOD, size - 1);
        if (!isFunctional()) {
            supply.clear();
        }
        for (String id : AUTARKY_CONDITIONS) {
            market.suppressCondition(id);
        }
        
        /*this is the section about improving defense
        dont forget to edit the post demandsection method
        */
        float mult = getDeficitMult(Commodities.HEAVY_MACHINERY, Commodities.SUPPLIES);
        String extra = "";
        if (mult != 1) {
            String com = getMaxDeficit(Commodities.HEAVY_MACHINERY,Commodities.SUPPLIES).one;
            extra = " (" + getDeficitText(com).toLowerCase() + ")";
        }
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(getModId(), 1f + DEFENSE_BONUS_BASE * mult,getNameForModifier() + extra);

        if (!isFunctional()) {
            supply.clear();
            unapply();
        }
        
         /*this is the section about changing accessibility
        dont forget to edit the post demandsection method
        */
        market.getAccessibilityMod().modifyFlat(getModId(0), ACCESSABILITY_BONUS_BASE , getNameForModifier());
    }

    @Override
    public void unapply() {
        super.unapply();
        for (String id : AUTARKY_CONDITIONS) {
            market.unsuppressCondition(id);
        }
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
        market.getAccessibilityMod().unmodifyFlat(getModId(0));
    }

    @Override
    public boolean canImprove() {
        return false;
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip,
            boolean hasDemand, IndustryTooltipMode mode) {
        //if (mode == IndustryTooltipMode.NORMAL && isFunctional()) {
        if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {

            addGroundDefensesImpactSection(tooltip, DEFENSE_BONUS_BASE,
                    Commodities.SUPPLIES, Commodities.HEAVY_MACHINERY);
        }
        float total = ACCESSABILITY_BONUS_BASE;
        String totalStr = "+" + (int) Math.round(total * 100f) + "%";
        Color h = Misc.getHighlightColor();
        if (total < 0) {
            h = Misc.getNegativeHighlightColor();
            totalStr = "" + (int) Math.round(total * 100f) + "%";
        }
        float opad = 10f;
        float pad = 3f;
        if (total >= 0) {
            tooltip.addPara("Accessibility bonus: %s", opad, h, totalStr);
        } else {
            tooltip.addPara("Accessibility penalty: %s", opad, h, totalStr);
        }

        float malus = getPopulationGrowthMalus();
        tooltip.addPara("Population growth: %s", opad, h, "-" + (int) malus + "%");

    }

    public float getPopulationGrowthMalus() {

        return market.getSize()*2f;
    }
    
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		float malus = getPopulationGrowthMalus();
		
		incoming.getWeight().modifyFlat(getModId(), malus*-1, getNameForModifier());
	}
    
    @Override
    public boolean isAvailableToBuild() {
        if (!Global.getSector().getPlayerFaction().knowsIndustry(getId())) {
            return false;
        }
        return market.getPlanetEntity() != null;
    }
    @Override
    public boolean showWhenUnavailable() {
        return Global.getSector().getPlayerFaction().knowsIndustry(getId());
    }
}
