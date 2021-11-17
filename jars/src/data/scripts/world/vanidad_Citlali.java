/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import org.lazywizard.lazylib.MathUtils;
/**
 *
 * @author Fabrice Valade
 */
public class vanidad_Citlali {
    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem("Citlali");
        //system.getLocation().set(22000,-14000);
        system.getLocation().set(22000,3000);
        PlanetAPI citlali_star = system.initStar("vanidad_Citlali",
                "star_orange",//set star type, the type IDs come from starsector-core/data/campaign/procgen/star_gen_data.csv
                900, //set radius, 900 is a typical radius size
                1000); //radius of corona terrain around star
        
        final float arida_distance = 3500;
        final float ice_ring_distance = 5500;
        final float krystos_distance = 8000;
        final float gaseoso_distance = 6500;
        final float outer_ring_distance = 9000;
        
        system.addAsteroidBelt(citlali_star, 1000, ice_ring_distance, 700, 130, 170, Terrain.ASTEROID_BELT, "dark expanse");                
        system.addRingBand(citlali_star,"misc" , "rings_ice0" , 500, 0, Color.yellow, 500, ice_ring_distance-100, 150);
        system.addRingBand(citlali_star,"misc" , "rings_ice0" , 80, 0, Color.green, 80, ice_ring_distance+310, 150);
        
        final float krystos_angle = 360 * (float) Math.random();
        PlanetAPI krystos = system.addPlanet("krystos", //unique id
                citlali_star, //orbiting target
                "Krystos", //name
                "frozen", //set planet type, the type IDs come from starsector-core/data/campaign/procgen/planet_gen_data.csv
                krystos_angle, //angle
                190f, //radius
                krystos_distance, //distance from orbiting target
                500f); //orbit days
        krystos.setCustomDescriptionId("vanidad_planet_kristos"); //reference descriptions.csv
        
        SectorEntityToken dds_station = system.addCustomEntity("vanidad_debajo_del_dol", "Debajo Del Sol", "station_debajo_del_dol", "vanidad");
        dds_station.setCircularOrbitPointingDown(system.getEntityById("krystos"), 45, 400, 50);
        dds_station.setCustomDescriptionId("vanidad_station_dds");
        
        MarketAPI krystos_market = vanidad_AddMarketplace.addMarketplace("vanidad", krystos, new ArrayList<>(Arrays.asList(dds_station)),
                "Krystos",
                6,
                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_6,
                                Conditions.ORE_ULTRARICH,
                                Conditions.RARE_ORE_RICH,
                                Conditions.DARK,
                                Conditions.VERY_COLD
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_STORAGE
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.MINING,
                                Industries.BATTLESTATION_HIGH,
                                Industries.HEAVYBATTERIES,
                                Industries.MILITARYBASE,
                                Industries.REFINING,
                                Industries.WAYSTATION,
                                "vanidad_autarky"
                        )
                ),
                true,
                false);
        krystos_market.addIndustry(Industries.ORBITALWORKS,new ArrayList<String>(Arrays.asList(Items.CORRUPTED_NANOFORGE)));
        JumpPointAPI jumpPointKrystos = Global.getFactory().createJumpPoint("vanidad_citlali_jump", "Forgotten Path");
        jumpPointKrystos.setCircularOrbit(citlali_star, krystos_angle+15, krystos_distance, 500f);
        jumpPointKrystos.setRelatedPlanet(krystos);
        

        
        system.addEntity(jumpPointKrystos);
        
        float arida_angle= 360 * (float) Math.random();
        float arida_days = 140;
        PlanetAPI arida = system.addPlanet("arida", 
                citlali_star, 
                "Arida", 
                "arid",
                arida_angle ,
                90, 
                arida_distance,
                arida_days);
        arida.setCustomDescriptionId("vanidad_planet_arida"); //reference descriptions.csv
        MarketAPI arida_market = vanidad_AddMarketplace.addMarketplace("vanidad", arida, null,
                "Arida",
                5,
                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.FARMLAND_ADEQUATE,
                                Conditions.HABITABLE,
                                Conditions.RUINS_SCATTERED,
                                Conditions.HOT,
                                Conditions.ORGANICS_TRACE                             
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_STORAGE
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.FARMING,
                                Industries.LIGHTINDUSTRY,
                                Industries.GROUNDDEFENSES,
                                Industries.PATROLHQ,
                                Industries.MINING
                        )
                ),
                true,
                false);
        
       PlanetAPI metzli = system.addPlanet("metzli", //unique id
                krystos, //orbiting target
                "Metzli", //name
                "barren", //set planet type, the type IDs come from starsector-core/data/campaign/procgen/planet_gen_data.csv
                360 * (float) Math.random(), //angle
                45f, //radius
                350, //distance from orbiting target
                5f); //orbit days
        PlanetConditionGenerator.generateConditionsForPlanet(metzli, StarAge.AVERAGE);
        
        
        PlanetAPI gaseoso = system.addPlanet("gaseoso", //unique id
                citlali_star, //orbiting target
                "Gaseoso", //name
                "gas_giant", //set planet type, the type IDs come from starsector-core/data/campaign/procgen/planet_gen_data.csv
                360 * (float) Math.random(), //angle
                350f, //radius
                gaseoso_distance, //distance from orbiting target
                250f); //orbit days
        gaseoso.getMarket().addCondition(Conditions.VOLATILES_DIFFUSE);
        gaseoso.getMarket().addCondition(Conditions.HIGH_GRAVITY);
        system.addRingBand(gaseoso,"misc" , "rings_ice0" , 80, 0, Color.green, 90, 375, 10);
        //PlanetConditionGenerator.generateConditionsForPlanet(gaseoso, StarAge.AVERAGE);
        PlanetAPI zazannen = system.addPlanet("zazannen", //unique id
                gaseoso, //orbiting target
                "Zazanen", //name
                "barren", //set planet type, the type IDs come from starsector-core/data/campaign/procgen/planet_gen_data.csv
                360 * (float) Math.random(), //angle
                20f, //radius
                750f, //distance from orbiting target
                10f); //orbit days
        zazannen.getMarket().addCondition(Conditions.DARK);
        zazannen.getMarket().addCondition(Conditions.LOW_GRAVITY);
        zazannen.getMarket().addCondition(Conditions.ORE_SPARSE);
        zazannen.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        //StarSystemGenerator.addOrbitingEntities(system, gaseoso, StarAge.YOUNG, 1, 3,400, 600, false);
        // generates hyperspace destinations for in-system jump points
        system.autogenerateHyperspaceJumpPoints(true, true);
        /*
        -----------------------this is for non planet entities
        */
        StarSystemGenerator.addSystemwideNebula(system, StarAge.OLD);
                SectorEntityToken relay = system.addCustomEntity("citlali_relay", // unique id
		"Citlali Relay", // name - if null, defaultName from custom_entities.json will be used
		"comm_relay_makeshift", // type of object, defined in custom_entities.json
		"vanidad"); // faction
	relay.setCircularOrbit( citlali_star, MathUtils.clampAngle(krystos_angle - 65), krystos_distance, 500f);
        // puerta Gate - counter-orbit to Arida
        SectorEntityToken gate = system.addCustomEntity("citlali_gate", // unique id
                "Citlali Puerta Gate", // name - if null, defaultName from custom_entities.json will be used
                Entities.INACTIVE_GATE, // type of object, defined in custom_entities.json
                null); // faction
        gate.setCircularOrbit(citlali_star, MathUtils.clampAngle(arida_angle+180),arida_distance, arida_days);
        
        
        
        
        
        //set up hyperspace editor plugin
        HyperspaceTerrainPlugin hyperspaceTerrainPlugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin(); //get instance of hyperspace terrain
        NebulaEditor nebulaEditor = new NebulaEditor(hyperspaceTerrainPlugin); //object used to make changes to hyperspace nebula

        //set up radiuses in hyperspace of system
        float minHyperspaceRadius = hyperspaceTerrainPlugin.getTileSize() * 2f; //minimum radius is two 'tiles'
        float maxHyperspaceRadius = system.getMaxRadiusInHyperspace();

        //hyperstorm-b-gone (around system in hyperspace)
        nebulaEditor.clearArc(system.getLocation().x, system.getLocation().y, 0, minHyperspaceRadius + maxHyperspaceRadius, 0f, 360f, 0.25f);

    }



}
