package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces;

/**
 * @version 1.01
 * @since 2012-01-05
 */
public final class ColorCMY
{
    public double C, M, Y;
 
    public ColorCMY(double C, double M, double Y)
    {
        this.C = C;
        this.M = M;
        this.Y = Y;
    }
 
    public final String toString()
    {
        return "{C: " + C + ", M: " + M + ", Y: " + Y + "}";
    }
}