/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

/**
 *
 * @author Lethargie
 */
public class vanidad_Gen implements SectorGeneratorPlugin {
     @Override
    public void generate(SectorAPI sector) {
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("vanidad");
        initFactionRelationships(sector);
        
        new vanidad_Citlali().generate(sector);
    }
    
    public static void initFactionRelationships(SectorAPI sector) {
        FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
	FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
	FactionAPI pirates = sector.getFaction(Factions.PIRATES);
	FactionAPI independent = sector.getFaction(Factions.INDEPENDENT);
	FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
	FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);
	FactionAPI player = sector.getFaction(Factions.PLAYER);
	FactionAPI diktat = sector.getFaction(Factions.DIKTAT);
        FactionAPI league = sector.getFaction(Factions.PERSEAN);
        FactionAPI vanidad = sector.getFaction("vanidad");
        
        player.setRelationship(vanidad.getId(), 0);
        
        vanidad.setRelationship(hegemony.getId(), 0.6f);
        vanidad.setRelationship(pirates.getId(), -0.6f);
        vanidad.setRelationship(diktat.getId(), -0.2f);
        
        vanidad.setRelationship(tritachyon.getId(), -0.2f);
        
        vanidad.setRelationship(independent.getId(), 0.2f);
        vanidad.setRelationship(league.getId(), 0.1f);

        path.setRelationship(vanidad.getId(), -0.9f);

        
    }
}
