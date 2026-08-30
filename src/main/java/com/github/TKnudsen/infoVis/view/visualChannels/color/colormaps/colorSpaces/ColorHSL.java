package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces;

/**
 * @version 1.01
 * @since 2012-01-05
 */
public final class ColorHSL
{
    public double H, S, L;
 
    public ColorHSL(double h, double s, double v)
    {
        H = h;
        S = s;
        L = v;
    }
 
    public final String toString()
    {
        return "{H: " + H + ", S: " + S + ", L: " + L + "}";
    }
}