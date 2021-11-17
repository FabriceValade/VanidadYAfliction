/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts;


import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import data.scripts.weaponai.vanidad_vastoRayoAutofireAi;


import data.scripts.world.vanidad_Gen;
/**
 *
 * @author lethargie
 */
public class vanidad_aflictionModPlugin extends BaseModPlugin {
    
    
    public static final boolean isExerelin;
    static
    {
        boolean foundExerelin;
        if (Global.getSettings().getModManager().isModEnabled("nexerelin")) {
            foundExerelin = true;
		} else {
			foundExerelin = false;
		}
        isExerelin = foundExerelin;
    }
    
    public static final String VASTORAYO = "vanidad_vastorayo";
    
    
    @Override
    public void onNewGame() {
        
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("vanidad");
	initVanidad();
    }
    
    @Override
    public PluginPick<AutofireAIPlugin> pickWeaponAutofireAI(WeaponAPI weapon) {
        switch (weapon.getId()) {
            case VASTORAYO:
                return new PluginPick<AutofireAIPlugin>(new vanidad_vastoRayoAutofireAi(weapon), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
        }
        return null;
    }
    
     private static void initVanidad() {
        new vanidad_Gen().generate(Global.getSector());
    }
}
