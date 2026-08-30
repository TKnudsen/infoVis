package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.staticMaps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * @version 1.0
 * @since 2010
 */
public class StaticWhiteYellowColorMap
{

    private static final Color colors[] = {
    	
	new Color(255, 255, 255), new Color(255, 255, 254),
			new Color(255, 255, 253), new Color(255, 255, 252),
			new Color(255, 255, 251), new Color(255, 255, 250),
			new Color(255, 255, 249), new Color(255, 255, 248),
			new Color(255, 255, 247), new Color(255, 255, 246),
			new Color(255, 255, 245), new Color(255, 255, 244),
			new Color(255, 255, 243), new Color(255, 255, 242),
			new Color(255, 255, 241), new Color(255, 255, 240),
			new Color(255, 255, 239), new Color(255, 255, 238),
			new Color(255, 255, 237), new Color(255, 255, 236),
			new Color(255, 255, 235), new Color(255, 255, 234),
			new Color(255, 255, 233), new Color(255, 255, 232),
			new Color(255, 255, 231), new Color(255, 255, 230),
			new Color(255, 255, 229), new Color(255, 255, 228),
			new Color(255, 255, 227), new Color(255, 255, 226),
			new Color(255, 255, 225), new Color(255, 255, 224),
			new Color(255, 255, 223), new Color(255, 255, 222),
			new Color(255, 255, 221), new Color(255, 255, 220),
			new Color(255, 255, 219), new Color(255, 255, 218),
			new Color(255, 255, 217), new Color(255, 255, 216),
			new Color(255, 255, 215), new Color(255, 255, 214),
			new Color(255, 255, 213), new Color(255, 255, 212),
			new Color(255, 255, 211), new Color(255, 255, 210),
			new Color(255, 255, 209), new Color(255, 255, 208),
			new Color(255, 255, 207), new Color(255, 255, 206),
			new Color(255, 255, 205), new Color(255, 255, 204),
			new Color(255, 255, 203), new Color(255, 255, 202),
			new Color(255, 255, 201), new Color(255, 255, 200),
			new Color(255, 255, 199), new Color(255, 255, 198),
			new Color(255, 255, 197), new Color(255, 255, 196),
			new Color(255, 255, 195), new Color(255, 255, 194),
			new Color(255, 255, 193), new Color(255, 255, 192),
			new Color(255, 255, 191), new Color(255, 255, 190),
			new Color(255, 255, 189), new Color(255, 255, 188),
			new Color(255, 255, 187), new Color(255, 255, 186),
			new Color(255, 255, 185), new Color(255, 255, 184),
			new Color(255, 255, 183), new Color(255, 255, 182),
			new Color(255, 255, 181), new Color(255, 255, 180),
			new Color(255, 255, 179), new Color(255, 255, 178),
			new Color(255, 255, 177), new Color(255, 255, 176),
			new Color(255, 255, 175), new Color(255, 255, 174),
			new Color(255, 255, 173), new Color(255, 255, 172),
			new Color(255, 255, 171), new Color(255, 255, 170),
			new Color(255, 255, 169), new Color(255, 255, 168),
			new Color(255, 255, 167), new Color(255, 255, 166),
			new Color(255, 255, 165), new Color(255, 255, 164),
			new Color(255, 255, 163), new Color(255, 255, 162),
			new Color(255, 255, 161), new Color(255, 255, 160),
			new Color(255, 255, 159), new Color(255, 255, 158),
			new Color(255, 255, 157), new Color(255, 255, 156),
			new Color(255, 255, 155), new Color(255, 255, 154),
			new Color(255, 255, 153), new Color(255, 255, 152),
			new Color(255, 255, 151), new Color(255, 255, 150),
			new Color(255, 255, 149), new Color(255, 255, 148),
			new Color(255, 255, 147), new Color(255, 255, 146),
			new Color(255, 255, 145), new Color(255, 255, 144),
			new Color(255, 255, 143), new Color(255, 255, 142),
			new Color(255, 255, 141), new Color(255, 255, 140),
			new Color(255, 255, 139), new Color(255, 255, 138),
			new Color(255, 255, 137), new Color(255, 255, 136),
			new Color(255, 255, 135), new Color(255, 255, 134),
			new Color(255, 255, 133), new Color(255, 255, 132),
			new Color(255, 255, 131), new Color(255, 255, 130),
			new Color(255, 255, 129), new Color(255, 255, 128),
			new Color(255, 255, 127), new Color(255, 255, 126),
			new Color(255, 255, 125), new Color(255, 255, 124),
			new Color(255, 255, 123), new Color(255, 255, 122),
			new Color(255, 255, 121), new Color(255, 255, 120),
			new Color(255, 255, 119), new Color(255, 255, 118),
			new Color(255, 255, 117), new Color(255, 255, 116),
			new Color(255, 255, 115), new Color(255, 255, 114),
			new Color(255, 255, 113), new Color(255, 255, 112),
			new Color(255, 255, 111), new Color(255, 255, 110),
			new Color(255, 255, 109), new Color(255, 255, 108),
			new Color(255, 255, 107), new Color(255, 255, 106),
			new Color(255, 255, 105), new Color(255, 255, 104),
			new Color(255, 255, 103), new Color(255, 255, 102),
			new Color(255, 255, 101), new Color(255, 255, 100),
			new Color(255, 255, 99), new Color(255, 255, 98),
			new Color(255, 255, 97), new Color(255, 255, 96),
			new Color(255, 255, 95), new Color(255, 255, 94),
			new Color(255, 255, 93), new Color(255, 255, 92),
			new Color(255, 255, 91), new Color(255, 255, 90),
			new Color(255, 255, 89), new Color(255, 255, 88),
			new Color(255, 255, 87), new Color(255, 255, 86),
			new Color(255, 255, 85), new Color(255, 255, 84),
			new Color(255, 255, 83), new Color(255, 255, 82),
			new Color(255, 255, 81), new Color(255, 255, 80),
			new Color(255, 255, 79), new Color(255, 255, 78),
			new Color(255, 255, 77), new Color(255, 255, 76),
			new Color(255, 255, 75), new Color(255, 255, 74),
			new Color(255, 255, 73), new Color(255, 255, 72),
			new Color(255, 255, 71), new Color(255, 255, 70),
			new Color(255, 255, 69), new Color(255, 255, 68),
			new Color(255, 255, 67), new Color(255, 255, 66),
			new Color(255, 255, 65), new Color(255, 255, 64),
			new Color(255, 255, 63), new Color(255, 255, 62),
			new Color(255, 255, 61), new Color(255, 255, 60),
			new Color(255, 255, 59), new Color(255, 255, 58),
			new Color(255, 255, 57), new Color(255, 255, 56),
			new Color(255, 255, 55), new Color(255, 255, 54),
			new Color(255, 255, 53), new Color(255, 255, 52),
			new Color(255, 255, 51), new Color(255, 255, 50),
			new Color(255, 255, 49), new Color(255, 255, 48),
			new Color(255, 255, 47), new Color(255, 255, 46),
			new Color(255, 255, 45), new Color(255, 255, 44),
			new Color(255, 255, 43), new Color(255, 255, 42),
			new Color(255, 255, 41), new Color(255, 255, 40),
			new Color(255, 255, 39), new Color(255, 255, 38),
			new Color(255, 255, 37), new Color(255, 255, 36),
			new Color(255, 255, 35), new Color(255, 255, 34),
			new Color(255, 255, 33), new Color(255, 255, 32),
			new Color(255, 255, 31), new Color(255, 255, 30),
			new Color(255, 255, 29), new Color(255, 255, 28),
			new Color(255, 255, 27), new Color(255, 255, 26),
			new Color(255, 255, 25), new Color(255, 255, 24),
			new Color(255, 255, 23), new Color(255, 255, 22),
			new Color(255, 255, 21), new Color(255, 255, 20),
			new Color(255, 255, 19), new Color(255, 255, 18),
			new Color(255, 255, 17), new Color(255, 255, 16),
			new Color(255, 255, 15), new Color(255, 255, 14),
			new Color(255, 255, 13), new Color(255, 255, 12),
			new Color(255, 255, 11), new Color(255, 255, 10),
			new Color(255, 255, 9), new Color(255, 255, 8),
			new Color(255, 255, 7), new Color(255, 255, 6),
			new Color(255, 255, 5), new Color(255, 255, 4),
			new Color(255, 255, 3), new Color(255, 255, 2),
			new Color(255, 255, 1), new Color(255, 255, 0) };
    
    // linear min/max scaling; assumes value \in [0.0 .. 1.0]; applies alpha value
    public static Color getColor(float value, float alpha)
    {
        Color c = getColor(value);
        float components[] = c.getComponents(null);
        return new Color(components[0], components[1], components[2], alpha);
    }

    // linear min/max scaling; assumes value \in [0.0 .. 1.0]
    public static Color getColor(float value)
    {
      int index = Math.round( value * colors.length );
      if (index >= colors.length) {
        index = colors.length-1;
      }
      if (index < 0) {
        index = 0;
      }
      return colors[index];
    }

    public static void drawColormap(Rectangle2D rect, Graphics2D g, boolean reverse)
    {
        drawColormap(rect, g, 1f, reverse);
    }

    // draw colormap legend into given Rectangle2D (horizontally)
    public static void drawColormap(Rectangle2D rect, Graphics2D g, float alpha, boolean reverse)
    {
        // loop columns
        int w=(int)rect.getWidth();
        int h=(int)rect.getHeight();
        int i;
        for (i=0;i<w;i++)
        {
            if (reverse) g.setColor(getColor((float)(w-i)/w,alpha));
            else g.setColor(getColor((float)i/w,alpha));
            g.drawLine((int)rect.getX()+i,(int)rect.getY(),(int)rect.getX()+i,(int)rect.getY()+h);
        }
        // draw frame
        g.setColor(Color.black);
        g.draw(rect);
  }
    
    public static void drawColormap(Rectangle2D rect, Graphics2D g, String label1, String label2)
    {
        drawColormap(rect, g, 1f, label1, label2);
    }

    // draw colormap legend into given Rectangle2D (horizontally)
    public static void drawColormap(Rectangle2D rect, Graphics2D g, float alpha, String label1, String label2)
    {
        // loop columns
        int w=(int)rect.getWidth();
        int h=(int)rect.getHeight();
        int i;
        for (i=0;i<w;i++)
        {
            g.setColor(getColor((float)i/w,alpha));
            g.drawLine((int)rect.getX()+i,(int)rect.getY(),(int)rect.getX()+i,(int)rect.getY()+h);
        }
        // draw frame
        g.setColor(Color.black);
        g.draw(rect);
        // draw legend
        if (label1!=null) g.drawString(label1, (int)rect.getMinX()+5, (int)rect.getMinY()+12);
        if (label2!=null) g.drawString(label2, (int)rect.getMaxX()-30, (int)rect.getMinY()+12);
    }
}
