/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import org.lwjgl.util.vector.Vector2f;
import org.lazywizard.lazylib.MathUtils;
import java.awt.Color;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_cadenaVioBeamEffect implements BeamEffectPlugin {

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }
        boolean hasTarget = beam.getDamageTarget() != null;
        boolean fire = MathUtils.getRandomNumberInRange(0,1) >=0.5f;
        if(fire){
            Vector2f end = beam.getTo();
            Vector2f start = beam.getFrom();
            Vector2f rPlace = MathUtils.getRandomPointOnLine(start,
                    end);
            engine.addHitParticle(
                            rPlace,
                            new Vector2f(),
                            20,
                            0.7f,
                            0.1f,
                            Color.YELLOW
                    );
        }
    }
}
