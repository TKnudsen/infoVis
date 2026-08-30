package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces;

/**
 * @version 1.01
 * @since 2012-01-05
 */
public final class ColorHunterLab
{
    public double L, a, b;
 
    public ColorHunterLab(double l, double a, double b)
    {
        L = l;
        this.a = a;
        this.b = b;
    }
 
    public final String toString()
    {
        return "{L: " + L + ", a: " + a + ", b: " + b + "}";
    }
}