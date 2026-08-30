package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces;

/**
 * @version 1.01
 * @since 2012-01-05
 */
public final class ColorCIELuv
{
    public final double L, u, v;
 
    public ColorCIELuv(double l, double u, double v)
    {
        L = l;
        this.u = u;
        this.v = v;
    }
 
    public String toString()
    {
        return "{L: " + L + ", u: " + u + ", v: " + v + "}";
    }
}