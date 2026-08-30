package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces;

/**
 * @version 1.01
 * @since 2012-01-05
 */
public final class ColorCMYK
{
    public double C, M, Y, K;
 
    public ColorCMYK(double C, double M, double Y, double K)
    {
        this.C = C;
        this.M = M;
        this.Y = Y;
        this.K = K;
    }
 
    public final String toString()
    {
        return "{C: " + C + ", M: " + M + ", Y: " + Y + ", K: " + K + "}";
    }
}