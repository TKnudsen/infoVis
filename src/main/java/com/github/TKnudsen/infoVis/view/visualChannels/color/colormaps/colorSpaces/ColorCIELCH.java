package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces;

/**
 * @version 1.01
 * @since 2012-01-05
 */
public final class ColorCIELCH
{
    public final double L, C, H;
 
    public ColorCIELCH(double l, double C, double H)
    {
        L = l;
        this.C = C;
        this.H = H;
    }
 
    public String toString()
    {
        return "{L: " + L + ", C: " + C + ", H: " + H + "}";
    }
}