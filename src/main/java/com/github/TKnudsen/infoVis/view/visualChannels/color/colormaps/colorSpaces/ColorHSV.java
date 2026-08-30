package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces;

/**
 * @version 1.01
 * @since 2012-01-05
 */
public final class ColorHSV
{
    public double H, S, V;
 
    public ColorHSV(double h, double s, double v)
    {
        H = h;
        S = s;
        V = v;
    }
 
    public final String toString()
    {
        return "{H: " + H + ", S: " + S + ", V: " + V + "}";
    }
}