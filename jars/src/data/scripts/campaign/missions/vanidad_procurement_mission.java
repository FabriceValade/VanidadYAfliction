/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.missions;

import java.awt.Color;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import static com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission.getRoundNumber;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
/**
 *
 * @author Lethargie
 */



public class vanidad_procurement_mission extends HubMissionWithBarEvent {


	public static enum Variation {
		FOOD,
		WEAPON,
	}
	
	public static enum Stage {
		GOGET,
		RETURN,
		COMPLETED,
		FAILED,
		FAILED_DECIV,
	}
	
	protected Variation variation;
	protected MarketAPI market;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;

		if (!"vanidad".equals(createdAt.getFaction().getId())) return false;
		
		if (barEvent) {
			setGiverRank(Ranks.CITIZEN);
			setGiverPost(Ranks.POST_AGENT);
			setGiverImportance(pickImportance());
			setGiverTags(Tags.CONTACT_MILITARY);
			findOrCreateGiver(createdAt, true, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		if (!setPersonMissionRef(person, "$extr_ref")) {
			return false;
		}

		

		
		if (market == null) {
			resetSearch();
			requireMarketIsNot(createdAt);
			requireMarketNotHidden();
			requireMarketNotInHyperspace();
			preferMarketSizeAtLeast(3);
			preferMarketSizeAtMost(6);
			preferMarketInDirectionOfOtherMissions();
			market = pickMarket();
		}
		
		if (market == null) return false;
		if (!setMarketMissionRef(market, "$extr_ref")) {
			return false;
		}
		

		
		makeImportant(market, "$extr_target", Stage.GOGET);
		makeImportant(getPerson(), "$extr_returnHere", Stage.RETURN);
		
		setStartingStage(Stage.GOGET);
		setSuccessStage(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		connectWithMemoryFlag(Stage.GOGET, Stage.RETURN, market, "$extr_needToReturn");
		setStageOnMemoryFlag(Stage.COMPLETED, person, "$extr_completed");
		
		addNoPenaltyFailureStages(Stage.FAILED_DECIV);
		connectWithMarketDecivilized(Stage.GOGET, Stage.FAILED_DECIV, market);
		setStageOnMarketDecivilized(Stage.FAILED_DECIV, createdAt);
		
		setTimeLimit(Stage.FAILED, 10000, null);

//		int sizeModifier = market.getSize() * 10000;
//		if (variation == Variation.PIRATE_BASE || variation == Variation.LUDDIC_PATH_BASE) {
//			sizeModifier = 10 * 10000;;
//		}
//		setCreditReward(10000 + sizeModifier, 30000 + sizeModifier);

		setCreditReward(BaseHubMission.CreditReward.AVERAGE, 10);
		
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$extr_variation", variation);
		
		set("$extr_barEvent", isBarEvent());
		set("$extr_manOrWoman", getPerson().getManOrWoman());
		set("$extr_reward", Misc.getWithDGS(getCreditsReward()));
		
		set("$extr_systemName", market.getStarSystem().getNameWithLowercaseTypeShort());
		set("$extr_marketName", market.getName());
		set("$extr_marketOnOrAt", market.getOnOrAt());
		set("$extr_dist", getDistanceLY(market));

				
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GOGET) {
			info.addPara("Go get stuff located " +
					 market.getOnOrAt() + " " + market.getName() + 
					 " in the " + market.getStarSystem().getNameWithLowercaseTypeShort() + ".", opad);

				FactionAPI f = market.getFaction();
				LabelAPI label = info.addPara("The target location is a size %s " +
								"colony controlled by " + f.getDisplayNameWithArticle() + ".",
							 opad, f.getBaseUIColor(),
							 "" + market.getSize(), f.getDisplayNameWithArticleWithoutArticle());
				label.setHighlight("" + market.getSize(), f.getDisplayNameWithArticleWithoutArticle());
				label.setHighlightColors(h, f.getBaseUIColor());
				
			
		} else if (currentStage == Stage.RETURN) {
			info.addPara(getReturnText(getPerson().getMarket().getName()) + ".", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GOGET) {
				info.addPara("Go get stuff from " +
							 market.getName() + 
							 " in the " + market.getStarSystem().getNameWithLowercaseTypeShort() + ".", pad, tc,
							 market.getFaction().getBaseUIColor(), market.getName());
				return true;
		} else if (currentStage == Stage.RETURN) {
			info.addPara(getReturnTextShort(getPerson().getMarket().getName()), tc, pad);
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return "Procure material";
	}
}





