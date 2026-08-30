package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces;

/**
 * @version 1.01
 * @since 2012-01-05
 */
public final class ColorXYZ
{
    public double X, Y, Z;
 
    public ColorXYZ(double x, double y, double z)
    {
        X = x;
        Y = y;
        Z = z;
    }
 
    public final String toString()
    {
        return "{X: " + X + ", Y: " + Y + ", Z: " + Z + "}";
    }
}