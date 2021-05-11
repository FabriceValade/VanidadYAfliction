/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts;


import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;


import data.scripts.world.vanidad_Citlali;
/**
 *
 * @author lethargie
 */
public class vanidad_aflictionModPlugin extends BaseModPlugin {
    
    @Override
    public void onNewGame() {
        
        (new vanidad_Citlali()).generate(Global.getSector());
		//SharedData.getData().getPersonBountyEventData().addParticipatingFaction("vanidad");

    }
    
}
