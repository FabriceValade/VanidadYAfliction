/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.missions;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.Random;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
/**
 *
 * @author Lethargie
 */



public class vanidad_lostObject_mission extends HubMissionWithBarEvent {
        
        public static float SCAVENGER_WEIGHTED_PROB = 10f;
        public static float AUTHORITY_WEIGHTED_PROB = 15f;
        public static float SPACESHIP_PROBABILITY = 0.75f;
	
	public static enum Stage {
		GOGET,
                GET_IT_FROM_SCAVENGER,
                GET_IT_WITH_AUTHORITY,
		RETURN,
		COMPLETED,
		FAILED,
	}
	public static enum TargetVariation {
            PlANETSIDE,
            SPACESHIP
        }
        
        public static enum Variation {
            SCAVENGER,
            AUTHORITY,
            NOTHING,
        }
        
        protected Variation variation;
        protected TargetVariation targetVariation;
	protected String targetObject;
        protected PersonAPI missionGiver;
        protected StarSystemAPI system;
	protected SectorEntityToken entity;
        protected String hullname, shipname;
        
        
        @Override
        public boolean shouldShowAtMarket(MarketAPI market) {
            return market.getFactionId().equals("vanidad");
	}
        
        
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;

		//if (!"vanidad".equals(createdAt.getFaction().getId())) return false;
		
		if (barEvent) {
			setGiverRank(Ranks.CITIZEN);
			setGiverPost(Ranks.POST_TRADER);
			setGiverImportance(PersonImportance.LOW);
			setGiverTags(Tags.CONTACT_UNDERWORLD);
                        setGiverFaction("vanidad_liberador");
			findOrCreateGiver(createdAt, true, true);//put somone as personOveride;
		}
		
		missionGiver = getPerson();//either return the personOveride or the hub.getperson
		if (missionGiver == null) return false;
		
		if (!setPersonMissionRef(missionGiver, "$vanidad_loe_ref")) {
			return false;
		}//put something in memory? its confusing

		targetObject = pickOne(	"a team operating as scout",
					"a team collecting experimental data",
					"a team establishing diplomatic contact",
					"a travelling executive"
							);

		requireSystemTags(ReqMode.ANY,Tags.THEME_DERELICT, Tags.THEME_MISC, Tags.THEME_RUINS);
		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_REMNANT); //remove remnant systems
		preferSystemUnexplored();
		preferSystemInDirectionOfOtherMissions();
		
		system = pickSystem();
		if (system == null) return false;
		
                WeightedRandomPicker<Variation> variationPicker = new WeightedRandomPicker<Variation>(new Random());
		variationPicker.add(Variation.NOTHING, 10f);
		variationPicker.add(Variation.AUTHORITY, AUTHORITY_WEIGHTED_PROB);
		variationPicker.add(Variation.SCAVENGER, SCAVENGER_WEIGHTED_PROB);
                variation = variationPicker.pick();
                if(rollProbability(SPACESHIP_PROBABILITY))
                    targetVariation = TargetVariation.SPACESHIP;
                else
                    targetVariation = TargetVariation.PlANETSIDE;
                variation = Variation.NOTHING;            
                
                
                pickTargetShip();
		if (entity == null) return false;

		setStartingStage(Stage.GOGET);
		setSuccessStage(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
                
		makeImportant(entity, "$vanidad_loe_target", Stage.GOGET);
		makeImportant(missionGiver, "$vanidad_loe_missionGiver", Stage.RETURN);
		setEntityMissionRef(entity, "$vanidad_loe_ref");
		
		
		connectWithMemoryFlag(Stage.GOGET, Stage.RETURN, entity, "$vanidad_loe_f_needToReturn");
		setStageOnMemoryFlag(Stage.COMPLETED, missionGiver, "$vanidad_loe_f_completed");
		
                if (variation == Variation.SCAVENGER)
                {
                    
                }
                else if(variation == Variation.AUTHORITY)
                {
                    
                }
                
		
		setTimeLimit(Stage.FAILED, 120, null);
		setCreditReward(BaseHubMission.CreditReward.AVERAGE, 10);

		
		return true;
	}
        
	@Override
	protected void updateInteractionDataImpl() {
		
		set("$vanidad_loe_barEvent", isBarEvent());
		set("$vanidad_loe_manOrWoman", missionGiver.getManOrWoman());
                set("$vanidad_loe_missionGiverName", missionGiver.getNameString());
		set("$vanidad_loe_reward", Misc.getWithDGS(getCreditsReward()));
		
		set("$vanidad_loe_target_system", system.getNameWithLowercaseTypeShort());
		set("$vanidad_loe_target_text", targetObject);
		set("$vanidad_loe_dist", getDistanceLY(system));
                set("$vanidad_loe_targetNoArticle", getTargetWithoutArticle());
                set("$vanidad_loe_variation", variation);
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GOGET) {
			info.addPara("Find the " + shipname + ", a " + hullname + 
					 " ship " + getLocated(entity) + "." +
                                         "Obtain black box of the pre-collapse VAO "+ getTargetWithoutArticle(), opad);
				
			
		} else if (currentStage == Stage.RETURN) {
			info.addPara(getReturnText(missionGiver.getMarket().getName()) + ".", opad);
		}
	}

	@Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GOGET) {
            info.addPara("Reach the "
                    + hullname
                    + " in the " + getLocated(entity),
                         pad, tc
            );
            return true;
        } else if (currentStage == Stage.RETURN) {
            info.addPara(getReturnTextShort(missionGiver.getMarket().getName()),
                         tc, pad);
            return true;
        }
        return false;
    }

	@Override
	public String getBaseName() {
		return "Obtain VAO precollapse data ";
	}
        

    
    private void pickTargetShip(){
        	WeightedRandomPicker<DerelictShipEntityPlugin.DerelictType> picker = new WeightedRandomPicker<DerelictShipEntityPlugin.DerelictType>(new Random());
		
		picker.add(DerelictShipEntityPlugin.DerelictType.LARGE, 5f);
		picker.add(DerelictShipEntityPlugin.DerelictType.MEDIUM, 10f);
		picker.add(DerelictShipEntityPlugin.DerelictType.SMALL, 20f);
                DerelictShipEntityPlugin.DerelictShipData shipData = DerelictShipEntityPlugin.createRandom("vanidad", picker.pick(), genRandom, DerelictShipEntityPlugin.getDefaultSModProb());
                FactionAPI vanidadAPI = Global.getSector().getFaction("vanidad");
                shipData.ship.shipName = vanidadAPI.pickRandomShipName();
                shipname = shipData.ship.shipName;
                String variantid = shipData.ship.variantId;
                hullname = Global.getSettings().getVariant(variantid).getHullSpec().getHullNameWithDashClass();
		entity = spawnDerelict(shipData, new LocData(EntityLocationType.HIDDEN, null, system, false));
                entity.setDiscoverable(false);

    }
    
    protected String getTargetWithoutArticle() {
		if (targetObject.startsWith("a ")) {
			return targetObject.replaceFirst("a ", "");
		}
		if (targetObject.startsWith("an ")) {
			return targetObject.replaceFirst("an ", "");
		}
		return targetObject;
	}
    
    private void setupFleetEncounter() {
            beginStageTrigger(Stage.GOGET);

            //fleet setup
            triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT,
                               "vanidad", FleetTypes.PATROL_MEDIUM, entity.getLocation());
            triggerAutoAdjustFleetStrengthModerate();
            triggerSetFleetFaction("vanidad");
            triggerMakeFleetIgnoredByOtherFleets();
            triggerFleetSetName("Exploradors");
            triggerMakeLowRepImpact();

            triggerPickLocationAroundEntity(entity, 5);
            triggerSpawnFleetAtPickedLocation("$vanidad_loe_f_authority",
                                              "$vanidad_loe_ref");
            triggerOrderFleetInterceptPlayer();
            

            endTrigger();
        
    }
}



