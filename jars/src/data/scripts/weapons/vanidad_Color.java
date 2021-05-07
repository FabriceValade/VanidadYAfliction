/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

/**
 *
 * @author Fabrice Valade
 */
public class vanidad_Color{

    public static Color GenRandomColor(Color startColor, Color endColor) {
        float randomInterpolate = (float) Math.random();
        Color finalColor = Misc.interpolateColor(startColor, endColor,
                randomInterpolate);
        return finalColor;
    }
    public static Color GenFringeColor(Color coreColor) {

        int green = coreColor.getGreen();
        int red = coreColor.getRed();
        int blue = coreColor.getBlue();
        MathUtils.clamp(red, 0, 255);
        MathUtils.clamp(green, 0, 255);
        MathUtils.clamp(blue, 0, 255);
        return new Color(red, green, blue);
    }
    public static Color GenCoreColor(Color color)
    {
        int green = color.getGreen();
        int red = color.getRed();
        int blue = color.getBlue();
        red = 255 -((255-red)*(255-red))/255;
        green = 255 -((255-green)*(255-green))/255;
        blue = 255 -((255-blue)*(255-blue))/255;
        MathUtils.clamp(red, 0, 255);
        MathUtils.clamp(green, 0, 255);
        MathUtils.clamp(blue, 0, 255);
        return new Color(red, green, blue);
    
    
    }
}

