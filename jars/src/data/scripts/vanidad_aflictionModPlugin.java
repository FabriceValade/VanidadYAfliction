/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts;


import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;


import data.scripts.world.vanidad_Citlali;
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

    @Override
    public void onNewGame() {
        
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("vanidad");
        (new vanidad_Citlali()).generate(Global.getSector());
		//

    }
    
}
