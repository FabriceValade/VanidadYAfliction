/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_beamRecoilHandler {

    public float recoilDuration = 0.4f;
    public boolean spawnMuzzleFlash = true;
    public Color muzzleColor = new Color(255,255,0,0);
    public float muzzleDistMin = 1f;
    public float muzzleDistMax = 16f;
    public float muzzleAngleMin = -25f;
    public float muzzleAngleMax = 25f;
    public float muzzleSizeMin = 12f;
    public float muzzleSizeMax = 35f;
    
    
    private int currentBarrel;
    private float interFrameDelay = 0f;
    private int frameNbr = 0;
    private float timer = 0f;
    private AnimationAPI theAnim;
    private WeaponAPI weapon;
    private int numBarrelFrame;
    private boolean firingStarted = false;

    public vanidad_beamRecoilHandler() {

    }

    public void init(WeaponAPI w, int numBarrel) {
        weapon = w;
        theAnim = weapon.getAnimation();
        int numFrame = theAnim.getNumFrames();
        numBarrelFrame = numFrame / numBarrel;
        interFrameDelay = recoilDuration / numBarrelFrame;
    }

    public void advance(float amount) {
        if (!firingStarted) {
            theAnim.setFrame(0);
            return;
        }
        timer += amount;
        if (timer >= interFrameDelay) {
            timer -= interFrameDelay;
            frameNbr++;
        }

        if (frameNbr == (currentBarrel) * numBarrelFrame) {
            frameNbr = (currentBarrel - 1) * numBarrelFrame;
            firingStarted = false;
            timer = 0;
        }
        theAnim.setFrame(frameNbr);

    }

    public void fire(CombatEngineAPI engine, int barrelIndex)
    {
        currentBarrel = barrelIndex;
        firingStarted = true;
        
        if (spawnMuzzleFlash)
        {
            Float realFiringAngle = weapon.getCurrAngle();
            Vector2f firePoint = weapon.getFirePoint(0);

            for (int i = 0; i < 12; i++) {
                float dist = MathUtils.getRandomNumberInRange(muzzleDistMin, muzzleDistMax);
                float spreadedAngle = MathUtils.getRandomNumberInRange(muzzleAngleMin, muzzleAngleMax);
                float spreadedSize = MathUtils.getRandomNumberInRange(muzzleSizeMin, muzzleSizeMax);
                float randomSpeed = MathUtils.getRandomNumberInRange(200, 400);
                
                
                Vector2f endPoint = MathUtils.getPoint(firePoint,dist,spreadedAngle+realFiringAngle);
                Vector2f endPoint2 = MathUtils.getPoint(firePoint,randomSpeed,spreadedAngle+realFiringAngle);
                Vector2f vel = Vector2f.sub(endPoint2, firePoint, null);
                
                engine.addHitParticle(endPoint,
                        vel,
                        spreadedSize,
                        0.4f,
                        0.2f,
                        muzzleColor);
            }
        }
    }

}
