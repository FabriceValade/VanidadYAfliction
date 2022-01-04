/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.missions;

import java.awt.Color;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Lethargie
 */

public class vanidad_procurement_mission extends HubMissionWithBarEvent {

    public static float AUTHORITY_ACTION_PROB = 1f;

    public static enum Stage {
        GOGET,
        RETURN,
        COMPLETED,
        FAILED,
        FAILED_DECIV,
    }

    protected MarketAPI goGetMarket;
    protected PersonAPI goGetContact;
    protected PersonAPI missionGiver;

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
            setGiverPost(pickOne(Ranks.POST_TRADER, Ranks.POST_ARMS_DEALER,
                                 Ranks.POST_SMUGGLER));
            setGiverImportance(pickImportance());
            setGiverTags(Tags.CONTACT_UNDERWORLD);
            setGiverFaction("vanidad_liberador");
            findOrCreateGiver(createdAt, true, true);//put somone as personOveride;
        }

        missionGiver = getPerson();//either return the personOveride or the hub.getperson
        if (missionGiver == null) {
            return false;
        }

        if (!setPersonMissionRef(missionGiver, "$vanidad_rp_ref")) {
            return false;
        }//put something in memory? its confusing
        if (barEvent) {
            setGiverIsPotentialContactOnSuccess();
        }

        if (goGetMarket == null) {
            resetSearch();
            requireMarketIsNot(createdAt);
            requireMarketNotHidden();
            requireMarketNotInHyperspace();
            preferMarketSizeAtLeast(3);
            requireMarketFactionNot("vanidad");
            preferMarketInDirectionOfOtherMissions();
            goGetMarket = pickMarket();
        }//search for tarket market

        if (goGetMarket == null) {
            return false;
        }
        if (!setMarketMissionRef(goGetMarket, "$vanidad_rp_ref")) {
            return false;
        }

        goGetContact = findOrCreateCriminal(goGetMarket, true);
        ensurePersonIsInCommDirectory(goGetMarket, goGetContact);
        makeImportant(goGetMarket, "$vanidad_rp_targetMarket", Stage.GOGET);
        makeImportant(goGetContact, "$vanidad_rp_targetContact", Stage.GOGET);
        makeImportant(missionGiver, "$vanidad_rp_returnHere", Stage.RETURN);

        setStartingStage(Stage.GOGET);
        setSuccessStage(Stage.COMPLETED);
        addFailureStages(Stage.FAILED);

        connectWithMemoryFlag(Stage.GOGET, Stage.RETURN, goGetMarket,
                              "$vanidad_rp_needToReturn");
        setStageOnMemoryFlag(Stage.COMPLETED, missionGiver,
                             "$vanidad_rp_completed");

        addNoPenaltyFailureStages(Stage.FAILED_DECIV);
        connectWithMarketDecivilized(Stage.GOGET, Stage.FAILED_DECIV,
                                     goGetMarket);
        setStageOnMarketDecivilized(Stage.FAILED_DECIV, createdAt);


        setCreditReward(BaseHubMission.CreditReward.AVERAGE, 10);
        setupFleetEncounter();

        return true;
    }

    @Override
    protected void updateInteractionDataImpl() {

        set("$vanidad_rp_barEvent", isBarEvent());
        set("$vanidad_rp_manOrWoman", missionGiver.getManOrWoman());
        set("$vanidad_rp_missionGiverName", missionGiver.getNameString());
        set("$vanidad_rp_reward", Misc.getWithDGS(getCreditsReward()));

        set("$vanidad_rp_systemName",
            goGetMarket.getStarSystem().getNameWithLowercaseTypeShort());
        set("$vanidad_rp_marketName", goGetMarket.getName());
        set("$vanidad_rp_marketOnOrAt", goGetMarket.getOnOrAt());
        set("$vanidad_rp_dist", getDistanceLY(goGetMarket));

        set("$vanidad_rp_goGetContactName", goGetContact.getNameString());
        set("$vanidad_rp_goGetContactPost", goGetContact.getPost().toLowerCase());
    }

    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GOGET) {
            info.addPara("Go get stuff located "
                    + goGetMarket.getOnOrAt() + " " + goGetMarket.getName()
                    + " in the " + goGetMarket.getStarSystem().getNameWithLowercaseTypeShort() + "."
                    + " Ask for " + goGetContact.getNameString(), opad);

            FactionAPI f = goGetMarket.getFaction();
            LabelAPI label = info.addPara("The target location is a size %s "
                    + "colony controlled by " + f.getDisplayNameWithArticle() + ".",
                                          opad, f.getBaseUIColor(),
                                          "" + goGetMarket.getSize(),
                                          f.getDisplayNameWithArticleWithoutArticle());
            label.setHighlight("" + goGetMarket.getSize(),
                               f.getDisplayNameWithArticleWithoutArticle());
            label.setHighlightColors(h, f.getBaseUIColor());

        } else if (currentStage == Stage.RETURN) {
            info.addPara(getReturnText(missionGiver.getMarket().getName()) + ".",
                         opad);
        }
    }

    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GOGET) {
            info.addPara("Go get stuff from "
                    + goGetMarket.getName()
                    + " in the " + goGetMarket.getStarSystem().getNameWithLowercaseTypeShort() + ".",
                         pad, tc,
                         goGetMarket.getFaction().getBaseUIColor(),
                         goGetMarket.getName());
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
        return "Procure \"stuff\"";
    }

    @Override
    protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params,
                                 Map<String, MemoryAPI> memoryMap) {
        if (action.equals("betrayal")) {
            setStageOnCustomCondition(Stage.FAILED, new AlwaysTrueChecker());
            return true;
        }
        return false;
    }

    private void setupFleetEncounter() {
        if (rollProbability(AUTHORITY_ACTION_PROB)) {
            beginWithinHyperspaceRangeTrigger(goGetMarket, 3f, false,
                                              Stage.RETURN);
            Vector2f loc = goGetMarket.getLocationInHyperspace();

            //fleet setup
            triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT,
                               "vanidad", FleetTypes.PATROL_MEDIUM, loc);
            triggerAutoAdjustFleetStrengthModerate();
            triggerSetFleetFaction("vanidad");
            triggerMakeHostileAndAggressive();
            triggerMakeFleetIgnoredByOtherFleets();
            triggerFleetSetName("Peace keeper");
            triggerFleetMakeFaster(true, 2, true);
            triggerMakeLowRepImpact();

            triggerPickLocationTowardsPlayer(
                    goGetMarket.getStarSystem().getHyperspaceAnchor(), 90f, 0);
            triggerSpawnFleetAtPickedLocation("$vanidad_rpauthority",
                                              "$vanidad_rp_ref");
            triggerOrderFleetInterceptPlayer();
            triggerFleetMakeImportant(null, Stage.RETURN);
            triggerFleetStopPursuingPlayerUnlessInStage(Stage.RETURN);
            triggerFleetAllowLongPursuit();

            endTrigger();
        }
    }
}
