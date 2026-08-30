package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps;

import java.awt.Color;

/**
 * <p>
 * Static helpers for scaling a color's hue/saturation/brightness or alpha
 * component.
 * </p>
 *
 * @version 1.0
 */
public class ColorScaler {

	// Scale H/S/B parameter, keep ALPHA value
	public static Color scaleHSB(Color color, float weight, int scale_index)
	{
        int a = color.getAlpha();
        if (weight<0) weight=0;
        if (weight>1) weight=1;
        if (scale_index>2) scale_index=2;
        if (scale_index<0) scale_index=0;
        float hsb[] = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        hsb[scale_index] *= weight;
        return new Color(Color.HSBtoRGB(hsb[0],hsb[1],hsb[2]));
	}

	public static Color scaleAlpha(Color color, float alpha)
    {
		if (alpha<0) alpha = 0;
		if (alpha>1) alpha = 1;
        float components[] = color.getComponents(null);
        return new Color(components[0], components[1], components[2], alpha);
    }
	
}
